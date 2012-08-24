package com.tjeannin.apprate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

public class AppRate implements android.content.DialogInterface.OnClickListener {

	private static final String TAG = "AppRater";

	private Activity hostActivity;

	private long minLaunchesUntilPrompt = 10;
	private long minDaysUntilPrompt = 7;

	private String title;
	private String message;
	private String rate;
	private String remindLater;
	private String dismiss;

	private SharedPreferences preferences;

	public AppRate(Activity hostActivity) {

		this.hostActivity = hostActivity;
		preferences = hostActivity.getSharedPreferences(PrefsContract.SHARED_PREFS_NAME, 0);

		title = "Rate " + getApplicationName(hostActivity.getApplicationContext());
		message = "If you enjoy using " + getApplicationName(hostActivity.getApplicationContext()) + ", please take a moment to rate it. Thanks for your support!";
		rate = "Rate it !";
		remindLater = "Remind me later";
		dismiss = "No thanks";
	}

	/**
	 * @param The title of the dialog to show.<br/>
	 *            Default is build from the computed application name.
	 * @return This {@link AppRate} object to allow chaining.
	 */
	public AppRate setTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * @param The message of the dialog to show.<br/>
	 *            Default is build from the computed application name and looks like : <br/>
	 *            If you enjoy using YOUR_APP_NAME, please take a moment to rate it. Thanks for your support!
	 * @return This {@link AppRate} object to allow chaining.
	 */
	public AppRate setMessage(String message) {
		this.message = message;
		return this;
	}

	/**
	 * @param rate A custom text for the rate button.
	 * @return This {@link AppRate} object to allow chaining.
	 */
	public AppRate setRateButtonText(String rate) {
		this.rate = rate;
		return this;
	}

	/**
	 * @param remindLater A custom text for the remind later button.
	 * @return This {@link AppRate} object to allow chaining.
	 */
	public AppRate setRemindLaterButtonText(String remindLater) {
		this.remindLater = remindLater;
		return this;
	}

	/**
	 * @param dismiss A custom text for the dismiss button.
	 * @return This {@link AppRate} object to allow chaining.
	 */
	public AppRate setDismissButtonText(String dismiss) {
		this.dismiss = dismiss;
		return this;
	}

	/**
	 * @param minLaunchesUntilPrompt The minimum number of days before showing the rate dialog.<br/>
	 *            Default value is 7 days.
	 * @return This {@link AppRate} object to allow chaining.
	 */
	public AppRate setMinLaunchesUntilPrompt(long minLaunchesUntilPrompt) {
		this.minLaunchesUntilPrompt = minLaunchesUntilPrompt;
		return this;
	}

	/**
	 * @param minDaysUntilPrompt The minimum number of times the user lunches the application before showing the rate dialog.<br/>
	 *            Default value is 10 times.
	 * @return This {@link AppRate} object to allow chaining.
	 */
	public AppRate setMinDaysUntilPrompt(long minDaysUntilPrompt) {
		this.minDaysUntilPrompt = minDaysUntilPrompt;
		return this;
	}

	/**
	 * Reset all the data collected about number of launches and days until first launch.
	 * @param context A context.
	 */
	public static void reset(Context context) {
		context.getSharedPreferences(PrefsContract.SHARED_PREFS_NAME, 0).edit().clear().commit();
		Log.d(TAG, "Cleared AppRate shared preferences.");
	}

	/**
	 * Display the rate dialog if needed.
	 */
	public void init() {

		Log.d(TAG, "Init AppRate");

		if (preferences.getBoolean(PrefsContract.PREF_DONT_SHOW_AGAIN, false)) {
			return;
		}

		Editor editor = preferences.edit();

		// Get and increment launch counter.
		long launch_count = preferences.getLong(PrefsContract.PREF_LAUNCH_COUNT, 0) + 1;
		editor.putLong(PrefsContract.PREF_LAUNCH_COUNT, launch_count);

		// Get date of first launch.
		Long date_firstLaunch = preferences.getLong(PrefsContract.PREF_DATE_FIRST_LAUNCH, 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong(PrefsContract.PREF_DATE_FIRST_LAUNCH, date_firstLaunch);
		}

		// Show the rate dialog if needed.
		if (launch_count >= minLaunchesUntilPrompt) {
			if (System.currentTimeMillis() >= date_firstLaunch + (minDaysUntilPrompt * DateUtils.DAY_IN_MILLIS)) {
				new AlertDialog.Builder(hostActivity)
						.setTitle(title)
						.setMessage(message)
						.setPositiveButton(rate, this)
						.setNegativeButton(dismiss, this)
						.setNeutralButton(remindLater, this)
						.create()
						.show();
			}
		}

		editor.commit();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

		Editor editor = preferences.edit();

		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			hostActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + hostActivity.getPackageName())));
			editor.putBoolean(PrefsContract.PREF_DONT_SHOW_AGAIN, true);
			break;

		case DialogInterface.BUTTON_NEGATIVE:
			editor.putBoolean(PrefsContract.PREF_DONT_SHOW_AGAIN, true);
			break;

		case DialogInterface.BUTTON_NEUTRAL:
			editor.putLong(PrefsContract.PREF_DATE_FIRST_LAUNCH, System.currentTimeMillis());
			editor.putLong(PrefsContract.PREF_LAUNCH_COUNT, 0);
			break;

		default:
			break;
		}

		editor.commit();
		dialog.dismiss();
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