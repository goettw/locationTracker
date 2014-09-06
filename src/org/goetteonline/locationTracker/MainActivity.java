package org.goetteonline.locationTracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
public static int COMMAND_LOCATION_LONGITUDE=1;
	PrefListener prefListener;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

//	TextView longitude;
//	TextView latitude;

	@Override
	protected void onDestroy() {
		stopService(intent);
		super.onDestroy();
	}

	TextView rabbitMQConnectionStatus;
	Intent intent;

	IConnectToRabbitMQ rabbitMQ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LocationTrackerBroadcastReceiver locationTrackerBroadcastReceiver = new LocationTrackerBroadcastReceiver();
		registerReceiver(locationTrackerBroadcastReceiver, new IntentFilter("org.goetteonline.locationTracker.UPDATEVIEW"));
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefListener = new PrefListener();
		sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);

//		longitude = (TextView) findViewById(R.id.textLong);
//		latitude = (TextView) findViewById(R.id.textLat);
//		rabbitMQConnectionStatus = (TextView) findViewById(R.id.textRabbitMQStatus);

		String hostName = sharedPrefs.getString(SettingsActivity.PREF_KEY_RABBIT_HOST, "");

		intent = new Intent(this, LocationTrackerService.class);

		startService(intent);
	}



	class PrefListener implements OnSharedPreferenceChangeListener {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Log.i("PREFERENCE_LISTENER", "key=" + key + ", value=" + sharedPreferences.getString(key, ""));

		}
	}
	
	public class LocationTrackerBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, final Intent intent) {
			MainActivity.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Log.i("LocationTrackerBroadcastReceiver" , "text=" + intent.getStringExtra("text"));
					TextView field = (TextView) findViewById(intent.getIntExtra("field", 0)	);
					field.setText(intent.getStringExtra("text"));
				}
			});
			
		}

	}

}
