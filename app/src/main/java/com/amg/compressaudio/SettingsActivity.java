package com.amg.compressaudio;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/* loaded from: classes.dex */
public class SettingsActivity extends PreferenceActivity {
    @Override // android.preference.PreferenceActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_pref);
        MainActivity.SETTINGS_ACT = true;
    }
}
