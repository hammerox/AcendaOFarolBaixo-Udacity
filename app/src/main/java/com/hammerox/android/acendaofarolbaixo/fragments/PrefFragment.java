package com.hammerox.android.acendaofarolbaixo.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.hammerox.android.acendaofarolbaixo.R;

import butterknife.BindString;
import butterknife.ButterKnife;


public class PrefFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    @BindString(R.string.pref_file_name) String fileName;
    @BindString(R.string.pref_alarm_test_key) String alarmTestKey;
    @BindString(R.string.pref_alarm_vibrate_key) String alarmVibrateKey;
    @BindString(R.string.pref_alarm_type_key) String alarmTypeKey;
    @BindString(R.string.pref_alarm_sound_key) String alarmSoundKey;
    @BindString(R.string.pref_alarm_speech_key) String alarmSpeechKey;
    @BindString(R.string.pref_boot_key) String bootKey;
    @BindString(R.string.pref_historic_key) String historicKey;

    private Preference alarmTestPref;
    private Preference alarmVibratePref;
    private ListPreference alarmTypePref;
    private Preference alarmSoundPref;
    private Preference alarmSpeechPref;
    private Preference bootPref;
    private Preference historicPref;
    private FirebaseAnalytics mAnalytics;
    private OnFragmentInteractionListener mListener;

    public static final String LOG_TAG = PrefFragment.class.getSimpleName();

    public PrefFragment() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this, getActivity());
        mAnalytics = FirebaseAnalytics.getInstance(getActivity());

        getPreferenceManager().setSharedPreferencesName(fileName);
        addPreferencesFromResource(R.xml.preferences);

        // Set variables
        alarmTestPref = findPreference(alarmTestKey);
        alarmTestPref.setOnPreferenceClickListener(this);

        alarmVibratePref = findPreference(alarmVibrateKey);
        alarmVibratePref.setOnPreferenceClickListener(this);

        alarmTypePref = (ListPreference) findPreference(alarmTypeKey);
        alarmTypePref.setOnPreferenceChangeListener(this);
        alarmTypePref.setOnPreferenceClickListener(this);

        alarmSoundPref = findPreference(alarmSoundKey);
        alarmSoundPref.setOnPreferenceChangeListener(this);
        alarmSoundPref.setOnPreferenceClickListener(this);

        alarmSpeechPref = findPreference(alarmSpeechKey);
        alarmSpeechPref.setOnPreferenceChangeListener(this);
        alarmSoundPref.setOnPreferenceClickListener(this);

        bootPref = findPreference(bootKey);
        bootPref.setOnPreferenceChangeListener(this);
        bootPref.setOnPreferenceClickListener(this);

        historicPref = findPreference(historicKey);
        historicPref.setOnPreferenceClickListener(this);


        /* If needed to read the preference value on fragment creation,
        * call onPreferenceChange here*/
        onPreferenceChange(alarmTypePref, alarmTypePref.getValue());
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String key = preference.getKey();
        String value = o.toString();
        Log.v(LOG_TAG, key + " is " + value);

        if (key.equals(alarmTypeKey)) {
            switch (Integer.valueOf(value)) {
                case 0:
                    alarmSoundPref.setEnabled(false);
                    alarmSpeechPref.setEnabled(false);
                    break;
                case 1:
                    alarmSoundPref.setEnabled(true);
                    alarmSpeechPref.setEnabled(false);
                    break;
                case 2:
                    alarmSoundPref.setEnabled(false);
                    alarmSpeechPref.setEnabled(true);
                    break;
            }
        }

        // Report analytics
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, key);
        bundle.putString(FirebaseAnalytics.Param.VALUE, value);
        mAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        return true;
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, preference.getKey());
        mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        return false;
    }
}
