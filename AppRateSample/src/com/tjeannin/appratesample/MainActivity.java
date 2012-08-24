package com.tjeannin.appratesample;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tjeannin.apprate.AppRater;

public class MainActivity extends Activity implements OnClickListener {

	private static final String LUNCH_COUNT = "lunch_count";
	private int lunchCount;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		lunchCount = sharedPreferences.getInt(LUNCH_COUNT, 0) + 1;
		sharedPreferences.edit().putInt(LUNCH_COUNT, lunchCount).commit();

		setContentView(R.layout.activity_main);

		((TextView) findViewById(R.id.lunch_count)).setText(String.valueOf(lunchCount));
		findViewById(R.id.restart_activity).setOnClickListener(this);
		findViewById(R.id.force_crash).setOnClickListener(this);
		findViewById(R.id.reset_apprate_prefs).setOnClickListener(this);

		// Init AppRater.
		new AppRater(this)
				.setMinDaysUntilPrompt(0)
				.setMinLaunchesUntilPrompt(5)
				.init();
	}

	@SuppressWarnings("null")
	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.restart_activity:
			finish();
			startActivity(new Intent(getApplicationContext(), MainActivity.class));
			break;

		case R.id.reset_apprate_prefs:
			AppRater.reset(this);
			break;

		case R.id.force_crash:
			String crash = null;
			crash.toString();
			break;

		default:
			break;
		}

	}
}
