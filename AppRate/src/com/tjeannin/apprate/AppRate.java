package com.tjeannin.apprate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import java.lang.Thread.UncaughtExceptionHandler;

public class AppRate implements android.content.DialogInterface.OnClickListener, OnCancelListener {

    private static final String TAG = "AppRater";

    private Activity mHostActivity;

    private OnClickListener mClickListener;

    private SharedPreferences mPreferences;

    private AlertDialog.Builder mDialogBuilder = null;

    private long mMinLaunchesUntilPrompt = 0;

    private long mMinDaysUntilPrompt = 0;

    private boolean mShowIfHasCrashed = true;

    public AppRate(Activity hostActivity) {
        mHostActivity = hostActivity;
        mPreferences = hostActivity.getSharedPreferences(PrefsContract.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * @param minLaunchesUntilPrompt The minimum number of times the user lunches the application before showing the rate dialog.<br/>
     *                               Default value is 0 times.
     * @return This {@link AppRate} object to allow chaining.
     */
    public AppRate setMinLaunchesUntilPrompt(long minLaunchesUntilPrompt) {
        mMinLaunchesUntilPrompt = minLaunchesUntilPrompt;
        return this;
    }

    /**
     * @param minDaysUntilPrompt The minimum number of days before showing the rate dialog.<br/>
     *                           Default value is 0 days.
     * @return This {@link AppRate} object to allow chaining.
     */
    public AppRate setMinDaysUntilPrompt(long minDaysUntilPrompt) {
        mMinDaysUntilPrompt = minDaysUntilPrompt;
        return this;
    }

    /**
     * @param showIfCrash If <code>false</code> the rate dialog will not be shown if the application has crashed once.<br/>
     *                    Default value is <code>false</code>.
     * @return This {@link AppRate} object to allow chaining.
     */
    public AppRate setShowIfAppHasCrashed(boolean showIfCrash) {
        mShowIfHasCrashed = showIfCrash;
        return this;
    }

    /**
     * Use this method if you want to customize the style and content of the rate dialog.<br/>
     * When using the {@link AlertDialog.Builder} you should use:
     * <ul>
     * <li>{@link AlertDialog.Builder#setPositiveButton} for the <b>rate</b> button.</li>
     * <li>{@link AlertDialog.Builder#setNeutralButton} for the <b>rate later</b> button.</li>
     * <li>{@link AlertDialog.Builder#setNegativeButton} for the <b>never rate</b> button.</li>
     * </ul>
     *
     * @param customBuilder The custom dialog you want to use as the rate dialog.
     * @return This {@link AppRate} object to allow chaining.
     */
    public AppRate setCustomDialog(AlertDialog.Builder customBuilder) {
        mDialogBuilder = customBuilder;
        return this;
    }

    /**
     * Reset all the data collected about number of launches and days until first launch.
     *
     * @param context A context.
     */
    public static void reset(Context context) {
        context.getSharedPreferences(PrefsContract.SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
        Log.d(TAG, "Cleared AppRate shared preferences.");
    }

    /**
     * Display the rate dialog if needed.
     */
    public void init() {
        Log.d(TAG, "Init AppRate");

        if (mPreferences.getBoolean(PrefsContract.PREF_DONT_SHOW_AGAIN, false) || (
                mPreferences.getBoolean(PrefsContract.PREF_APP_HAS_CRASHED, false) && !mShowIfHasCrashed)) {
            return;
        }

        if (!mShowIfHasCrashed) {
            initExceptionHandler();
        }

        Editor editor = mPreferences.edit();

        // Get and increment launch counter.
        long launch_count = mPreferences.getLong(PrefsContract.PREF_LAUNCH_COUNT, Context.MODE_PRIVATE) + 1;
        editor.putLong(PrefsContract.PREF_LAUNCH_COUNT, launch_count);

        // Get date of first launch.
        Long date_firstLaunch = mPreferences.getLong(PrefsContract.PREF_DATE_FIRST_LAUNCH, Context.MODE_PRIVATE);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PrefsContract.PREF_DATE_FIRST_LAUNCH, date_firstLaunch);
        }

        // Show the rate dialog if needed.
        if (launch_count >= mMinLaunchesUntilPrompt) {
            if (System.currentTimeMillis() >= date_firstLaunch + (mMinDaysUntilPrompt * DateUtils.DAY_IN_MILLIS)) {

                if (mDialogBuilder != null) {
                    showDialog(mDialogBuilder);
                } else {
                    showDefaultDialog();
                }
            }
        }

        editor.commit();
    }

    /**
     * Initialize the {@link ExceptionHandler}.
     */
    private void initExceptionHandler() {
        Log.d(TAG, "Init AppRate ExceptionHandler");

        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();

        // Don't register again if already registered.
        if (!(currentHandler instanceof ExceptionHandler)) {
            // Register default exceptions handler.
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(currentHandler, mHostActivity));
        }
    }

    /**
     * Shows the default rate dialog.
     *
     * @return
     */
    private void showDefaultDialog() {
        Log.d(TAG, "Create default dialog.");

        String title = mHostActivity.getString(R.string.dialog_title, getApplicationName(mHostActivity.getApplicationContext()));
        String message = mHostActivity.getString(R.string.dialog_message, getApplicationName(mHostActivity.getApplicationContext()));
        String rate = mHostActivity.getString(R.string.dialog_positive_button);
        String remindLater = mHostActivity.getString(R.string.dialog_neutral_button);
        String dismiss = mHostActivity.getString(R.string.dialog_negative_button);

        new AlertDialog.Builder(mHostActivity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(rate, this)
                .setNegativeButton(dismiss, this)
                .setNeutralButton(remindLater, this)
                .setOnCancelListener(this)
                .create().show();
    }

    /**
     * Show the custom rate dialog.
     *
     * @return
     */
    private void showDialog(AlertDialog.Builder builder) {
        Log.d(TAG, "Create custom dialog.");

        AlertDialog dialog = builder.create();
        dialog.show();

        String rate = (String) dialog.getButton(AlertDialog.BUTTON_POSITIVE).getText();
        String remindLater = (String) dialog.getButton(AlertDialog.BUTTON_NEUTRAL).getText();
        String dismiss = (String) dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getText();

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, rate, this);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, remindLater, this);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, dismiss, this);

        dialog.setOnCancelListener(this);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Editor editor = mPreferences.edit();
        editor.putLong(PrefsContract.PREF_DATE_FIRST_LAUNCH, System.currentTimeMillis());
        editor.putLong(PrefsContract.PREF_LAUNCH_COUNT, Context.MODE_PRIVATE);
        editor.commit();
    }

    /**
     * @param onClickListener A listener to be called back on.
     * @return This {@link AppRate} object to allow chaining.
     */
    public AppRate setOnClickListener(OnClickListener onClickListener) {
        mClickListener = onClickListener;
        return this;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Editor editor = mPreferences.edit();

        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                try {
                    mHostActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mHostActivity.getPackageName())));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(mHostActivity, mHostActivity.getString(R.string.toast_play_store_missing_error), Toast.LENGTH_SHORT).show();
                }
                editor.putBoolean(PrefsContract.PREF_DONT_SHOW_AGAIN, true);
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                editor.putBoolean(PrefsContract.PREF_DONT_SHOW_AGAIN, true);
                break;

            case DialogInterface.BUTTON_NEUTRAL:
                editor.putLong(PrefsContract.PREF_DATE_FIRST_LAUNCH, System.currentTimeMillis());
                editor.putLong(PrefsContract.PREF_LAUNCH_COUNT, Context.MODE_PRIVATE);
                break;

            default:
                break;
        }

        editor.commit();
        dialog.dismiss();

        if (mClickListener != null) {
            mClickListener.onClick(dialog, which);
        }
    }

    /**
     * @param context A context of the current application.
     * @return The application name of the current application.
     */
    private static final String getApplicationName(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;

        try {
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), Context.MODE_PRIVATE);
        } catch (final NameNotFoundException e) {
            applicationInfo = null;
        }

        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : context.getString(R.string.application_name_unknown));
    }
}