package com.tjeannin.appratesample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.enrique.apprate.AppRater;

public class MainActivity extends Activity implements OnClickListener {

	public static final String PREF_NUMBER_OF_LAUNCHES = "pref_number_of_launches";
	private SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		findViewById(R.id.reset_lunch_number).setOnClickListener(this);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Increment the number of launches.
		int numberOfLunches = preferences.getInt(PREF_NUMBER_OF_LAUNCHES, 0) + 1;
		preferences.edit().putInt(PREF_NUMBER_OF_LAUNCHES, numberOfLunches).commit();

		// Display the current number of launches.
		updateLaunchesDisplay(numberOfLunches);

		// Init AppRater.
		AppRater.init(getApplicationContext(), 4, 4);
	}

	@Override
	public void onClick(View view) {
		preferences.edit().putInt(PREF_NUMBER_OF_LAUNCHES, 0).commit();
		updateLaunchesDisplay(0);
	}

	private void updateLaunchesDisplay(int numberOfLunches) {
		((TextView) findViewById(R.id.lunch_number)).setText("Launches : " + numberOfLunches);
	}
}
