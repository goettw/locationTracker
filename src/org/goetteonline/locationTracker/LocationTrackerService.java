package org.goetteonline.locationTracker;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rabbitmq.client.AMQP.BasicProperties;

public class LocationTrackerService extends Service {
	private static final String EXCHANGE_NAME = "logs";
	private static final String EXCHANGE_TYPE = "fanout";
	IConnectToRabbitMQ rabbitMQ = null;
	String rabbitMQHostPref;
	String rabbitMQUsernamePref;
	String rabbitMQPasswordPref;
	String lastMessage=null;
	PrefListener prefListener;

	/*
	 * Run RabbitMQ Tasks in their in threads
	 */
	private class RabbitMQLocationSenderTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String hostName = params[0];
			String username = params[1];
			String password = params[2];
			String message = params[3];
			
			IConnectToRabbitMQ newRabbitMQ = new IConnectToRabbitMQ(hostName, username, password, EXCHANGE_NAME, EXCHANGE_TYPE);
			boolean mustConnect = (rabbitMQ == null || !rabbitMQ.equals(newRabbitMQ));
			Log.i(RabbitMQLocationSenderTask.class.getName(), "mustConnect = " + mustConnect);
			Log.i(RabbitMQLocationSenderTask.class.getName(), "rabbitMQ = " + rabbitMQ);
		//	Log.i(RabbitMQLocationSenderTask.class.getName(), "rabbitMQ.equals(newRabbitMQ) = " + rabbitMQ.equals(newRabbitMQ));
			
			if (rabbitMQ == null) {
				rabbitMQ = newRabbitMQ;
			}
			else if (mustConnect) {
				rabbitMQ.dispose();				
				rabbitMQ = newRabbitMQ;
			}

			if (mustConnect || rabbitMQ.getmModel() == null) {
				
				Log.i(RabbitMQLocationSenderTask.class.getName(), "connect to " + hostName);
				broadcastUIUpdate(R.id.textRabbitMQStatus,"connect to " + hostName);
				
				if (rabbitMQ.connectToRabbitMQ() == false) {
					Log.e(RabbitMQLocationSenderTask.class.getName(), "... failed to connect to " + hostName);
					broadcastUIUpdate(R.id.textRabbitMQStatus,"connect to " + hostName + " failed");
					return null;
				}
				else
					Log.i(RabbitMQLocationSenderTask.class.getName(), "connected to " + hostName);					
			}

			broadcastUIUpdate(R.id.textRabbitMQStatus,"connected to " + hostName);

			if (rabbitMQ.getmModel() == null)
				return null;
			try {
				BasicProperties bp = new BasicProperties.Builder().contentType("application/json").build();
				rabbitMQ.getmModel().basicPublish(EXCHANGE_NAME, "", bp, message.getBytes());
				return null;
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	}

	@Override
	public void onCreate() {

		Log.i(LocationTrackerService.class.toString(), "start");
	
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefListener = new PrefListener();
		sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);
		rabbitMQHostPref = sharedPrefs.getString(SettingsActivity.PREF_KEY_RABBIT_HOST, "");
		rabbitMQUsernamePref = sharedPrefs.getString(SettingsActivity.PREF_KEY_RABBIT_USERNAME, "");
		rabbitMQPasswordPref = sharedPrefs.getString(SettingsActivity.PREF_KEY_RABBIT_PASSWORD, "");
		
		long minTime = Long.parseLong(sharedPrefs.getString(SettingsActivity.PREF_KEY_MIN_TIME, "10000"));
		float minDistance = Float.parseFloat(sharedPrefs.getString(SettingsActivity.PREF_KEY_MIN_DISTANCE, "100"));
		
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new myLocationListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);

	}
	public void broadcastUIUpdate(int field, String text)
	
	    {
	
	       Intent intent = new Intent("MyCustomIntent");
	       // add data to the Intent
	       intent.putExtra("text", text);
	       intent.putExtra("field", field);
	       intent.setAction("org.goetteonline.locationTracker.UPDATEVIEW");
	       sendBroadcast(intent);
	    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	class myLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			Log.i(myLocationListener.class.toString(), "locationChanged");
			try {

				JSONObject m = new JSONObject();

				m.put("longitude", +location.getLongitude());
				m.put("latitude", location.getLatitude());

				broadcastUIUpdate(R.id.textLong,Double.toString(location.getLongitude()));
				broadcastUIUpdate(R.id.textLat,Double.toString(location.getLatitude()));
				
				lastMessage = m.toString();
			
				new RabbitMQLocationSenderTask().execute(rabbitMQHostPref, rabbitMQUsernamePref, rabbitMQPasswordPref, lastMessage);
	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}
	}

	class PrefListener implements OnSharedPreferenceChangeListener {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

			Log.i("PREFERENCE_LISTENER", "key=" + key + ", value=" + sharedPreferences.getString(key, ""));

			if (SettingsActivity.PREF_KEY_RABBIT_HOST.equals(key)) {
				rabbitMQHostPref = sharedPreferences.getString(SettingsActivity.PREF_KEY_RABBIT_HOST, "");
			}
			else if (SettingsActivity.PREF_KEY_RABBIT_USERNAME.equals(key)) {
				rabbitMQUsernamePref = sharedPreferences.getString(SettingsActivity.PREF_KEY_RABBIT_USERNAME, "");
			}
			else if (SettingsActivity.PREF_KEY_RABBIT_PASSWORD.equals(key)) {
				rabbitMQPasswordPref = sharedPreferences.getString(SettingsActivity.PREF_KEY_RABBIT_PASSWORD, "");
			}
			
			// send last message to rabbitMQ based on the assumption that any change on preferences should be enough 
			// motivation to do it
			if (lastMessage != null)
				new RabbitMQLocationSenderTask().execute(rabbitMQHostPref, rabbitMQUsernamePref, rabbitMQPasswordPref, lastMessage);

		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

}
