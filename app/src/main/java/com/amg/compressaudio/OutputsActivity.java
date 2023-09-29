package com.amg.compressaudio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class OutputsActivity extends AppCompatActivity {

    private static File folder;
    SharedPreferences.Editor edit;
    List<AudioFile> files = new ArrayList();
    private RecyclerView.LayoutManager lManager;
    private RecyclerView list;
    SharedPreferences preference;
    int views;

    public static void launch(Activity activity, File folder2) {
        activity.startActivityForResult(getLaunchIntent(activity), 1);
        folder = folder2;
    }

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, OutputsActivity.class);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outputs);
        SharedPreferences preferences = getPreferences(0);
        this.preference = preferences;
        this.edit = preferences.edit();
        for (int i = 0; i < folder.listFiles().length; i++) {
            try {
                this.files.add(new AudioFile(folder.listFiles()[i]));
            } catch (Exception unused) {
            }
        }
        setTitle(getString(R.string.open_path));
        ((TextView) findViewById(R.id.output)).setText(folder.getPath());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        this.list = recyclerView;
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        this.lManager = linearLayoutManager;
        this.list.setLayoutManager(linearLayoutManager);
        TextView textView = (TextView) findViewById(R.id.empty);
        if (this.files.size() == 0) {
            textView.setVisibility(View.VISIBLE);
        }
        this.list.setAdapter(new AudiosAdapter(this.files, this));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
    }

}
