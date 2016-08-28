package com.hammerox.android.acendaofarolbaixo.others;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.hammerox.android.acendaofarolbaixo.R;
import com.hammerox.android.acendaofarolbaixo.activities.AlarmActivity;
import com.hammerox.android.acendaofarolbaixo.activities.MainActivity;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Mauricio on 10-Aug-16.
 */
public class DetectorService extends Service
        implements OnActivityUpdatedListener {

    public static final String IS_TESTING_KEY = "isTesting";
    public static final String LOG_TAG = "onActivityUpdated";
    public static final int NOTIFICATION_ID = 1;

    private boolean mNotifyUser;


    @Override
    public void onCreate() {
        super.onCreate();

        // Run as foreground
        runAsForeground();

        // Start variables
        mNotifyUser = true;

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();
        smartLocation.activity().start(this);
        Log.d(LOG_TAG, "DetectorService RUNNING");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
        if (detectedActivity != null) {
            int activityType = detectedActivity.getType();

            if (detectedActivity.getConfidence() == 100) {
                switch (activityType) {
                    case DetectedActivity.IN_VEHICLE:
                        if (mNotifyUser) launchAlarm();
                        mNotifyUser = false;
                        break;
                    case DetectedActivity.STILL:
                    case DetectedActivity.TILTING:
                        // Do nothing
                        break;
                    default:
                        mNotifyUser = true;
                        Log.d(LOG_TAG, "User is now prone to receive notification");
                        break;
                }
            }

            Log.d(LOG_TAG, detectedActivity.toString());
        } else {
            Log.d(LOG_TAG, "Null activity");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop detector
        SmartLocation.with(this).activity().stop();

        stopForeground(true);
        Log.d(LOG_TAG, "DetectorService STOPPED");
    }


    public void launchAlarm() {
        // Launch alarm screen
        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(IS_TESTING_KEY, false);
        startActivity(alarmIntent);

        // Save date and time
        FileManager.insertDateAndTime(this);
    }


    private void runAsForeground(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_beam)
                .setContentTitle("Acenda o Farol Baixo")
                .setContentText("Detector LIGADO. Clique para desligar")
                .setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);

    }

}
