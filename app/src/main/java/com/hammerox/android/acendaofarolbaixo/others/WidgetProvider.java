package com.hammerox.android.acendaofarolbaixo.others;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.hammerox.android.acendaofarolbaixo.R;

/**
 * Created by Mauricio on 23-Aug-16.
 */
public class WidgetProvider extends AppWidgetProvider {

    public static final String ACTION_DETECTOR_UPDATED =
            "com.hammerox.android.acendaofarolbaixo.ACTION_DETECTOR_UPDATED";
    public static final String ACTION_WIDGET_CLICK =
            "com.hammerox.android.acendaofarolbaixo.ACTION_WIDGET_CLICK";
    public static final String STATUS_KEY = "STATUS";
    public static final String DETECTOR_STATE = "DETECTOR_STATE";
    public static final String DETECTOR_ON = "ON";
    public static final String DETECTOR_OFF = "OFF";

    private String mServiceName = DetectorService.class.getName();
    private ActivityManager mActivityManager;
    private FirebaseAnalytics mAnalytics;
    private String textOn;
    private String textOff;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        Log.d("WidgetProvider", action);

        // Get analytics object
        if (mAnalytics == null) {
            mAnalytics = FirebaseAnalytics.getInstance(context);
        }

        switch (action) {
            case ACTION_DETECTOR_UPDATED:
                boolean statusIsOn = intent.getBooleanExtra(STATUS_KEY, false);
                changeWidgetState(context, statusIsOn);
                break;
            case ACTION_WIDGET_CLICK:
                changeServiceState(context);
                break;
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("WidgetProvider", "onUpdate");

        // Get analytics object
        if (mAnalytics == null) {
            mAnalytics = FirebaseAnalytics.getInstance(context);
        }

        // Get strings
        if (textOn == null || textOff == null) {
            textOn = context.getResources().getString(R.string.detector_button_text_on);
            textOff = context.getResources().getString(R.string.detector_button_text_off);
        }

        for (int widgetId : appWidgetIds) {
            RemoteViews widget = new RemoteViews(
                    context.getPackageName(),
                    R.layout.widget_detector);

            if (findService(context, mServiceName)) {
                widget.setImageViewResource(R.id.button, R.drawable.ic_beam_button_on);
                widget.setContentDescription(R.id.button, textOn);
            } else {
                widget.setImageViewResource(R.id.button, R.drawable.ic_beam_button_off);
                widget.setContentDescription(R.id.button, textOff);
            }

            widget.setOnClickPendingIntent(
                    R.id.button,
                    getPendingSelfIntent(context, ACTION_WIDGET_CLICK));

            appWidgetManager.updateAppWidget(widgetId, widget);
        }
    }


    public void changeServiceState(Context context) {
        Intent changeIntent = new Intent(context, DetectorService.class);
        // Look for detector's service
        if (findService(context, mServiceName)) {
            // If service is on, turn it OFF
            context.stopService(changeIntent);
            changeWidgetState(context, false);
        } else {
            // If service is off, turn it ON
            context.startService(changeIntent);
            changeWidgetState(context, true);
        }
    }


    public void changeWidgetState(Context context, boolean isToSetOn) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance
                (context);
        ComponentName thisAppWidgetComponentName =
                new ComponentName(context.getPackageName(),getClass().getName()
                );
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                thisAppWidgetComponentName);
        onUpdate(context, appWidgetManager, appWidgetIds);

        // Get strings
        if (textOn == null || textOff == null) {
            textOn = context.getResources().getString(R.string.detector_button_text_on);
            textOff = context.getResources().getString(R.string.detector_button_text_off);
        }

        for (int widgetId : appWidgetIds) {
            RemoteViews widget = new RemoteViews(
                    context.getPackageName(),
                    R.layout.widget_detector);

            if (isToSetOn) {
                widget.setImageViewResource(R.id.button, R.drawable.ic_beam_button_on);
                widget.setContentDescription(R.id.button, textOn);
            } else {
                widget.setImageViewResource(R.id.button, R.drawable.ic_beam_button_off);
                widget.setContentDescription(R.id.button, textOff);
            }

            appWidgetManager.updateAppWidget(widgetId, widget);
        }

        // Report analytics
        Bundle bundle = new Bundle();
        String param = (isToSetOn) ? DETECTOR_ON : DETECTOR_OFF;
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, DETECTOR_STATE);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, param);
        mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }


    private boolean findService(Context context, String serviceName) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }

        // Search
        for (ActivityManager.RunningServiceInfo service : mActivityManager.getRunningServices(Integer.MAX_VALUE)) {
            // Check
            if (serviceName.equals(service.service.getClassName())) {
                // If found, return true
                return true;
            }
        }

        // If not found, return false
        return false;
    }


    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
