package com.enrique.apprate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppRater {

	private static final String SHARED_PREFS_NAME = "apprate_prefs";

	private static final String PREF_DATE_FIRST_LAUNCH = "date_firstlaunch";
	private static final String PREF_LAUNCH_COUNT = "launch_count";
	private static final String PREF_DONT_SHOW_AGAIN = "dont_show_again";

	private Activity hostActivity;

	private long minLaunchesUntilPrompt = 10;
	private long minDaysUntilPrompt = 7;

	private String title;
	private String message;
	private String rate;
	private String remindLater;
	private String dismiss;

	private SharedPreferences preferences;

	public AppRater(Activity hostActivity) {
		this.hostActivity = hostActivity;

		title = "Rate " + getApplicationName(hostActivity.getApplicationContext());
		message = "If you enjoy using " + getApplicationName(hostActivity.getApplicationContext()) + ", please take a moment to rate it. Thanks for your support!";
		rate = "Rate it !";
		remindLater = "Remind me later";
		dismiss = "No thanks.";
	}

	/**
	 * @param minLaunchesUntilPrompt The minimum number of days before showing the rate dialog.<br/>
	 *            Default value is 7 days.
	 */
	public AppRater setMinLaunchesUntilPrompt(long minLaunchesUntilPrompt) {
		this.minLaunchesUntilPrompt = minLaunchesUntilPrompt;
		return this;
	}

	/**
	 * @param minDaysUntilPrompt The minimum number of times the user lunches the application before showing the rate dialog.<br/>
	 *            Default value is 10 times.
	 */
	public AppRater setMinDaysUntilPrompt(long minDaysUntilPrompt) {
		this.minDaysUntilPrompt = minDaysUntilPrompt;
		return this;
	}

	public void init() {

		preferences = hostActivity.getSharedPreferences(SHARED_PREFS_NAME, 0);
		if (preferences.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
			return;
		}

		SharedPreferences.Editor editor = preferences.edit();

		// Increment launch counter
		long launch_count = preferences.getLong(PREF_LAUNCH_COUNT, 0) + 1;
		editor.putLong(PREF_LAUNCH_COUNT, launch_count);

		// Get date of first launch
		Long date_firstLaunch = preferences.getLong(PREF_DATE_FIRST_LAUNCH, 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong(PREF_DATE_FIRST_LAUNCH, date_firstLaunch);
		}

		// Show the rate dialog if needed.
		if (launch_count >= minLaunchesUntilPrompt) {
			if (System.currentTimeMillis() >= date_firstLaunch + (minDaysUntilPrompt * DateUtils.DAY_IN_MILLIS)) {
				showRateDialog();
			}
		}

		editor.commit();
	}

	private void showRateDialog() {

		final Dialog dialog = new Dialog(hostActivity);
		dialog.setTitle(title);

		LinearLayout linearLayout = new LinearLayout(hostActivity);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		TextView textView = new TextView(hostActivity);
		textView.setText(message);
		textView.setWidth(240);
		textView.setPadding(4, 0, 4, 10);
		linearLayout.addView(textView);

		Button rateButton = new Button(hostActivity);
		rateButton.setText(rate);
		rateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hostActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + hostActivity.getPackageName())));
				Editor editor = preferences.edit();
				if (editor != null) {
					editor.putLong(PREF_DATE_FIRST_LAUNCH, System.currentTimeMillis());
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		linearLayout.addView(rateButton);

		Button remindButton = new Button(hostActivity);
		remindButton.setText(remindLater);
		remindButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		linearLayout.addView(remindButton);

		Button neverButton = new Button(hostActivity);
		neverButton.setText(dismiss);
		neverButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Editor editor = preferences.edit();
				if (editor != null) {
					editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		linearLayout.addView(neverButton);

		dialog.setContentView(linearLayout);
		dialog.show();
	}

	/**
	 * @param context A context of the current application.
	 * @return The application name of the current application.
	 */
	private static final String getApplicationName(Context context) {
		final PackageManager packageManager = context.getPackageManager();
		ApplicationInfo applicationInfo;
		try {
			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
		} catch (final NameNotFoundException e) {
			applicationInfo = null;
		}
		return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "(unknown)");
	}
}