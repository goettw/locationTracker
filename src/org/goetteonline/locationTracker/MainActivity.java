package org.goetteonline.locationTracker;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.rabbitmq.client.AMQP.BasicProperties;

public class MainActivity extends Activity {
	TextView longitude;
	TextView latitude;
	TextView rabbitMQConnectionStatus;
	boolean rabbitMQConnected = false;
	private static final String EXCHANGE_NAME = "logs";
	private static final String HOST_NAME = "192.168.1.201";

	IConnectToRabbitMQ rabbitMQ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		longitude = (TextView) findViewById(R.id.textLong);
		latitude = (TextView) findViewById(R.id.textLat);
		rabbitMQConnectionStatus = (TextView) findViewById(R.id.textRabbitMQStatus);
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new myLocationListener();

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);
		rabbitMQ = new IConnectToRabbitMQ("192.168.1.201", EXCHANGE_NAME,
				"fanout");
		new RabbitMQTask().execute("connect");
	}

	void updateRabbitStatus(Boolean status) {
		rabbitMQConnectionStatus.setText(Boolean.toString(status));
	}

	class myLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			longitude.setText(Double.toString(location.getLongitude()));
			latitude.setText(Double.toString(location.getLatitude()));
			
			try {
				JSONObject m = new JSONObject();
				m.put("longitude",+location.getLongitude());
				m.put("latitude",location.getLatitude());
				new RabbitMQTask().execute(m.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

	}

	class RabbitMQTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			updateRabbitStatus(rabbitMQConnected);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			System.out.println("DORABBIT"+params[0]);
			if (params[0].equals("connect")) {
				if (rabbitMQConnected == false) {

					if (rabbitMQ.connectToRabbitMQ())
						rabbitMQConnected = true;

				}
				System.out.println("connection status:" + rabbitMQConnected);
				return Boolean.valueOf(rabbitMQConnected);
			} else {
				if (rabbitMQ.getmModel() == null)
					return Boolean.valueOf(false);
				try {
					BasicProperties bp = new BasicProperties.Builder().contentType("application/json").build();
					rabbitMQ.getmModel().basicPublish(EXCHANGE_NAME, "", bp,
							params[0].getBytes());
					return Boolean.valueOf(true);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			return null;
		}

	}
}
