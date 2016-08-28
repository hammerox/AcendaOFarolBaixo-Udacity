package com.hammerox.android.acendaofarolbaixo.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.google.firebase.analytics.FirebaseAnalytics;
import com.hammerox.android.acendaofarolbaixo.others.DetectorService;
import com.hammerox.android.acendaofarolbaixo.others.FileManager;
import com.hammerox.android.acendaofarolbaixo.R;
import com.hammerox.android.acendaofarolbaixo.others.WidgetProvider;
import com.skyfishjy.library.RippleBackground;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;


public class DetectorFragment extends Fragment {

    @BindView(R.id.detector_button) FancyButton mDetectorButton;
    @BindView(R.id.detector_ripple) RippleBackground mDetectorRipple;
    @BindView(R.id.detector_instructions_1) TextView ruleOne;
    @BindView(R.id.detector_instructions_2) TextView ruleTwo;
    @BindView(R.id.detector_instructions_3) TextView ruleThree;
    @BindString(R.string.detector_button_text_on) String buttonOn;
    @BindString(R.string.detector_button_text_off) String buttonOff;

    public static final int LOCATION_PERMISSION_ID = 1001;
    public static final String CLICK_DETECTOR = "CLICK_DETECTOR";

    private String mServiceName = DetectorService.class.getName();
    private ActivityManager mActivityManager;
    private int colorActive;
    private int colorInactive;
    private FirebaseAnalytics mAnalytics;
    private OnFragmentInteractionListener mListener;

    public DetectorFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detector, container, false);
        ButterKnife.bind(this, view);
        mAnalytics = FirebaseAnalytics.getInstance(getActivity());

        colorActive = ContextCompat.getColor(getContext(), R.color.text_secondary);
        colorInactive = ContextCompat.getColor(getContext(), R.color.divider);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        boolean isFirstTime = getActivity()
                .getSharedPreferences(FileManager.FILE_NAME, Context.MODE_PRIVATE)
                .getBoolean(FileManager.FIRST_TIME, true);

        if (isFirstTime) {
            // Enable detector if it's the first time...
            enableDetector();
            // And record that first time has passed
            getActivity()
                    .getSharedPreferences(FileManager.FILE_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(FileManager.FIRST_TIME, false)
                    .apply();
        } else {
            // If it's not the first time...
            // Look for DetectorService running on background
            // If found, toggle button to ON. If not, toggle to OFF
            if (findService(mServiceName)) {
                Log.v(DetectorService.LOG_TAG, "Found Service running");
                setButtonOn();
            } else {
                setButtonOff();
            }
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    @OnClick(R.id.detector_button)
    public void onToggleClicked(FancyButton button) {
        boolean switchOn = !findService(mServiceName);
        if (switchOn) {
//            // Location permission not granted
//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
//                return;
//            }

            enableDetector();
        } else {
            disableDetector();
        }

        // Report analytics
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, CLICK_DETECTOR);
        mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }


    private void enableDetector() {
        Intent intent = new Intent(getActivity(), DetectorService.class);
        getActivity().startService(intent);

        setButtonOn();
    }


    private void disableDetector() {
        Intent intent = new Intent(getActivity(), DetectorService.class);
        getActivity().stopService(intent);

        setButtonOff();
    }


    private void setButtonOn() {
        // Update button
        ruleOne.setTextColor(colorInactive);
        ruleTwo.setTextColor(colorActive);
        ruleThree.setTextColor(colorActive);
        mDetectorButton.setContentDescription(buttonOn);

        mDetectorRipple.startRippleAnimation();

        // Update widget
        Intent updateIntent = new Intent(WidgetProvider.ACTION_DETECTOR_UPDATED);
        updateIntent.putExtra(WidgetProvider.STATUS_KEY, true);
        getContext().sendBroadcast(updateIntent);
    }


    private void setButtonOff() {
        // Update button
        ruleOne.setTextColor(colorActive);
        ruleTwo.setTextColor(colorInactive);
        ruleThree.setTextColor(colorInactive);
        mDetectorButton.setContentDescription(buttonOff);

        mDetectorRipple.stopRippleAnimation();

        // Update widget
        Intent updateIntent = new Intent(WidgetProvider.ACTION_DETECTOR_UPDATED);
        updateIntent.putExtra(WidgetProvider.STATUS_KEY, false);
        getContext().sendBroadcast(updateIntent);
    }


    private boolean findService(String serviceName) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
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

}
