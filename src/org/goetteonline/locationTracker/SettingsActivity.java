package org.goetteonline.locationTracker;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	final static public String PREF_KEY_RABBIT_HOST = "rabbit_host";
	final static public String PREF_KEY_RABBIT_USERNAME = "rabbit_username";
	final static public String PREF_KEY_RABBIT_PASSWORD = "rabbit_password";
	final static public String PREF_KEY_MIN_TIME = "min_time";
	final static public String PREF_KEY_MIN_DISTANCE = "min_distance";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
