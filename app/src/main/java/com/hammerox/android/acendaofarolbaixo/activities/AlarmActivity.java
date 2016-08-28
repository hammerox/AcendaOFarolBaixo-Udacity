package com.hammerox.android.acendaofarolbaixo.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.hammerox.android.acendaofarolbaixo.others.DetectorService;
import com.hammerox.android.acendaofarolbaixo.R;
import com.hammerox.android.acendaofarolbaixo.others.SpeechService;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AlarmActivity extends AppCompatActivity{

    @BindView(R.id.alarm_icon) ImageView alarmIcon;
    @BindString(R.string.pref_file_name) String fileName;
    @BindString(R.string.pref_alarm_vibrate_key) String alarmVibrateKey;
    @BindString(R.string.pref_alarm_type_key) String alarmTypeKey;
    @BindString(R.string.pref_alarm_sound_key) String alarmSoundKey;

    public static final String EXIT_KEY = "EXIT";
    public static final String SHOW_CONFIG_KEY = "SHOW_CONFIG";
    public static final String ALARM_TRIGGERED = "ALARM_TRIGGERED";
    public static final String CLICK_ALARM_OK = "CLICK_ALARM_OK";
    public static final String CLICK_ALARM_CONFIG = "CLICK_ALARM_CONFIG";

    private SharedPreferences preferences;
    private boolean isTesting;
    private boolean isToVibrate;
    private boolean isToAlarm;
    private boolean isToSpeech;

    private FirebaseAnalytics mAnalytics;
    private long[] vibratePattern = {0, 500, 1000};
    private Vibrator vibrator;
    private Ringtone mRingtone;
    private int mAnimationInterval = 1000; // 1 seconds by default
    private Handler mAnimationHandler;
    private Runnable mAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                YoYo.with(Techniques.Flash).playOn(findViewById(R.id.alarm_icon));
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mAnimationHandler.postDelayed(mAnimationRunnable, mAnimationInterval);
            }
        }
    };

    private static final String DEFAULT_SOUND = "default_string";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);

        // Get intent messages
        isTesting = getIntent().getBooleanExtra(DetectorService.IS_TESTING_KEY, true);

        // Report analytics if alarm is not a test
        mAnalytics = FirebaseAnalytics.getInstance(this);
        if (!isTesting) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, ALARM_TRIGGERED);
            mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        // Get preferences
        preferences = getSharedPreferences(fileName, Context.MODE_PRIVATE);

        // Get vibration
        isToVibrate = preferences.getBoolean(alarmVibrateKey, true);
        if (isToVibrate) {
            // Get instance of Vibrator from current Context
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(vibratePattern, 0);
        }

        // Get alarm type
        int alarmTypeValue = Integer.valueOf(preferences.getString(alarmTypeKey, "1"));
        switch (alarmTypeValue) {
            case 1:
                isToAlarm = true;
                playAlarmSound();
                break;
            case 2:
                isToSpeech = true;
                Intent intent = new Intent(this, SpeechService.class);
                startService(intent);
                break;
        }

        // Set animation
        startAnimation();

        // Remember to exit MainActivity if is not testing
        if (!isTesting) {
            preferences.edit()
                    .putBoolean(EXIT_KEY, true)
                    .commit();
        }
    }


    @Override
    public void onBackPressed() {
        // Do nothing
    }


    @OnClick(R.id.alarm_okay)
    public void onOkayClick(Button button) {
        finish();
        stopEverything();

        if (!isTesting) {
            // Report click to analytics
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, CLICK_ALARM_OK);
            mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            // Tell MainActivity to finish app
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    @OnClick(R.id.alarm_config)
    public void onConfigClick(Button button) {
        finish();
        stopEverything();

        if (!isTesting) {
            // Report click to analytics
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, CLICK_ALARM_CONFIG);
            mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        // Tell MainActivity to show config screen
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(SHOW_CONFIG_KEY, true);
        startActivity(intent);
    }


    private void stopEverything() {
        // Stop sounds
        if (isToAlarm) {
            stopAlarmSound();
        } else if (isToSpeech) {
            Intent intent = new Intent(this, SpeechService.class);
            stopService(intent);
        }

        // Stop vibration
        if (isToVibrate) {
            vibrator.cancel();
        }

        // Stop animation
        stopAnimation();
    }


    public void playAlarmSound() {
        String preferenceString = preferences.getString(alarmSoundKey, DEFAULT_SOUND);

        Uri mSoundURI = (preferenceString.equals(DEFAULT_SOUND))
                ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                : Uri.parse(preferenceString);

        mRingtone = RingtoneManager.getRingtone(getApplicationContext(), mSoundURI);

        try {
            mRingtone.play();
            Log.d(DetectorService.LOG_TAG, "User notified");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void stopAlarmSound() {
        try {
            mRingtone.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    void startAnimation() {
        mAnimationHandler = new Handler();
        mAnimationRunnable.run();
    }


    void stopAnimation() {
        mAnimationHandler.removeCallbacks(mAnimationRunnable);
    }

}
