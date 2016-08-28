package com.hammerox.android.acendaofarolbaixo.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.hammerox.android.acendaofarolbaixo.R;
import com.hammerox.android.acendaofarolbaixo.fragments.DetectorFragment;
import com.hammerox.android.acendaofarolbaixo.fragments.PrefFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
        implements DetectorFragment.OnFragmentInteractionListener,
                    PrefFragment.OnFragmentInteractionListener {

    @BindString(R.string.pref_file_name) String fileName;
    @BindView(R.id.activity_main_toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_layout_container) SlidingUpPanelLayout layoutContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Look for message from AlarmActivity to finish app
        SharedPreferences pref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if (pref.getBoolean(AlarmActivity.EXIT_KEY, false)) {
            pref.edit()
                .putBoolean(AlarmActivity.EXIT_KEY, false)
                .commit();
            finish();
            return;
        }

        // Look for message from AlarmActivity to open configurations
        if (getIntent().getBooleanExtra(AlarmActivity.SHOW_CONFIG_KEY, false)) {
            if (layoutContainer.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                layoutContainer.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public void onBackPressed() {
        if (layoutContainer.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            layoutContainer.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}
