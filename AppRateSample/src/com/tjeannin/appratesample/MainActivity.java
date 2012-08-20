package com.tjeannin.appratesample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.enrique.apprate.AppRater;

public class MainActivity extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		findViewById(R.id.restart_activity).setOnClickListener(this);

		// Init AppRater.
		new AppRater(this)
				.setMinDaysUntilPrompt(0)
				.setMinLaunchesUntilPrompt(5)
				.init();
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.restart_activity:
			finish();
			startActivity(new Intent(getApplicationContext(), MainActivity.class));
			break;

		case R.id.reset_apprate_prefs:
			AppRater.reset(this);

		default:
			break;
		}

	}
}
