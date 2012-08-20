package com.enrique.apprate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

	public static void init(Context context, long minLaunchesUntilPrompt, int minDaysUntilPrompt) {

		SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
		if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
			return;
		}

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
		editor.putLong(PREF_LAUNCH_COUNT, launch_count);

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong(PREF_DATE_FIRST_LAUNCH, 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong(PREF_DATE_FIRST_LAUNCH, date_firstLaunch);
		}

		// Wait at least n days before opening
		if (launch_count >= minLaunchesUntilPrompt) {
			if (System.currentTimeMillis() >= date_firstLaunch + (minDaysUntilPrompt * DateUtils.DAY_IN_MILLIS)) {
				showRateDialog(context, editor);
			}
		}

		editor.commit();
	}

	private static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {

		final Dialog dialog = new Dialog(context);
		dialog.setTitle("Rate " + getApplicationName(context));

		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		TextView textView = new TextView(context);
		textView.setText("If you enjoy using " + getApplicationName(context)
				+ ", please take a moment to rate it. Thanks for your support!");
		textView.setWidth(240);
		textView.setPadding(4, 0, 4, 10);
		linearLayout.addView(textView);

		Button rateButton = new Button(context);
		rateButton.setText("Rate it!");
		rateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id=" + context.getPackageName())));
				dialog.dismiss();
			}
		});
		linearLayout.addView(rateButton);

		Button remindButton = new Button(context);
		remindButton.setText("Remind me later");
		remindButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		linearLayout.addView(remindButton);

		Button neverButton = new Button(context);
		neverButton.setText("No, thanks");
		neverButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
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