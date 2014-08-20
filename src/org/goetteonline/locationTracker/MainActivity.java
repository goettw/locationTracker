package org.goetteonline.locationTracker;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView longitude;
	TextView latitude;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		longitude = (TextView) findViewById(R.id.textLong);
		latitude = (TextView) findViewById(R.id.textLat);

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new myLocationListener ();
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	class myLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			longitude.setText(Double.toString(location.getLongitude()));
			latitude.setText(Double.toString(location.getLatitude()));
			
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

	
}
