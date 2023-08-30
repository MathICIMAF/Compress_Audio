package com.amg.compressaudio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class MainActivity extends AppCompatActivity {
    public static final int PICK_AUDIO_FILES = 1;
    public static final int PICK_AUDIO_FOLDER = 2;
    public static boolean SETTINGS_ACT = false;
    private String bitrate;
    String channelId = "Progress Notification";
    TextView countText;
    TextView errorText;
    TextView fileText;
    private String format;
    private NativeAd nativeAd;
    private String outputFolder;
    TextView percentText;
    SharedPreferences preferences;
    TextView successText;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, new OnInitializationCompleteListener() { // from class: com.amg.compressaudio.MainActivity.1
            @Override // com.google.android.gms.ads.initialization.OnInitializationCompleteListener
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        this.percentText = (TextView) findViewById(R.id.percent);
        this.countText = (TextView) findViewById(R.id.counter);
        this.fileText = (TextView) findViewById(R.id.filename);
        this.errorText = (TextView) findViewById(R.id.error);
        this.successText = (TextView) findViewById(R.id.success);
        ((AdView) findViewById(R.id.adView)).loadAd(new AdRequest.Builder().build());
        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0 && ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1234);
        } else {
            initialize();
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        if (SETTINGS_ACT) {
            initialize();
            SETTINGS_ACT = false;
        }
    }

    private void refreshAd() {
        AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.nativo));
        builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() { // from class: com.amg.compressaudio.MainActivity.2
            @Override // com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener
            public void onNativeAdLoaded(NativeAd nativeAd) {
                if (!(Build.VERSION.SDK_INT >= 17 ? MainActivity.this.isDestroyed() : false) && !MainActivity.this.isFinishing() && !MainActivity.this.isChangingConfigurations()) {
                    if (MainActivity.this.nativeAd != null) {
                        MainActivity.this.nativeAd.destroy();
                    }
                    MainActivity.this.nativeAd = nativeAd;
                    MainActivity.populateNativeAdView(nativeAd, (NativeAdView) MainActivity.this.getLayoutInflater().inflate(R.layout.ad_unified, (ViewGroup) null));
                    return;
                }
                nativeAd.destroy();
            }
        });
        builder.withNativeAdOptions(new NativeAdOptions.Builder().setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build()).build());
        builder.withAdListener(new AdListener() { // from class: com.amg.compressaudio.MainActivity.3
            @Override // com.google.android.gms.ads.AdListener
            public void onAdFailedToLoad(LoadAdError loadAdError) {
            }
        }).build().loadAd(new AdRequest.Builder().build());
    }

    public static void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        adView.setNativeAd(nativeAd);
        VideoController videoController = nativeAd.getMediaContent().getVideoController();
        if (videoController.hasVideoContent()) {
            videoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() { // from class: com.amg.compressaudio.MainActivity.4
                @Override // com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
                public void onVideoEnd() {
                    super.onVideoEnd();
                }
            });
        }
    }

    private void initialize() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.preferences = defaultSharedPreferences;
        this.format = defaultSharedPreferences.getString("format", "mp3");
        this.bitrate = this.preferences.getString("bitrate", "56k");
        if (Build.VERSION.SDK_INT > 29) {
            this.outputFolder = getExternalFilesDir(null).getAbsolutePath() + "/CompressAudio/";
        } else {
            this.outputFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CompressAudio/";
        }
        File file = new File(this.outputFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        ((LinearLayout) findViewById(R.id.file_layout)).setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction("android.intent.action.OPEN_DOCUMENT");
                intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
                MainActivity mainActivity = MainActivity.this;
                mainActivity.startActivityForResult(Intent.createChooser(intent, mainActivity.getString(R.string.select_file)), PICK_AUDIO_FILES);
            }
        });
        ((LinearLayout) findViewById(R.id.folder_layout)).setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT_TREE"), PICK_AUDIO_FOLDER);
            }
        });
        ((LinearLayout) findViewById(R.id.output_layout)).setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                OutputsActivity.launch(MainActivity.this, new File(MainActivity.this.outputFolder));
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DocumentFile[] listFiles;
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == -1) {
            if (requestCode == 1) {
                new CompressAudios(data).execute(new String[0]);
            } else if (requestCode == 2) {
                DocumentFile fromTreeUri = DocumentFile.fromTreeUri(this, data.getData());
                ArrayList arrayList = new ArrayList();
                for (DocumentFile documentFile : fromTreeUri.listFiles()) {
                    if (documentFile.isFile() && documentFile.getType().contains("audio")) {
                        arrayList.add(documentFile.getUri());
                    }
                }
                new CompressAudios(arrayList).execute(new String[0]);
            }
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1234) {
            try {
                if (grantResults.length == 0 || grantResults[0] != 0) {
                    return;
                }
                initialize();
            } catch (Exception unused) {
            }
        }
    }

    /* loaded from: classes.dex */
    public class CompressAudios extends AsyncTask<String, Integer, Void> {
        Intent data;
        List<Uri> fileUris;
        int type = 1;

        public CompressAudios(Intent data) {
            this.data = data;
        }

        public CompressAudios(List<Uri> uris) {
            type= 2;
            this.fileUris = uris;
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(String... strings) {
            String realPath = "";
            TextView textView;
            String str = "CHICHO";
            if (type != 1) {
                if (type == 2) {
                    int success = 0;
                    int error = 0;
                    for (int i = 0; i < this.fileUris.size(); i++) {
                        countText.setText(i+1 + "/" + this.fileUris.size());
                        Uri uri = this.fileUris.get(i);
                        if (Build.VERSION.SDK_INT >= 30) {
                            realPath = RealPathUtil.copyFileToInternal(MainActivity.this, uri);
                        } else {
                            realPath = RealPathUtil.getRealPath(MainActivity.this, uri);
                        }
                        String fileName = RealPathUtil.getFileName(realPath);
                        MainActivity.this.fileText.setText(fileName);
                        publishProgress(Integer.valueOf((int) ((i / this.fileUris.size()) * 100.0d)));
                        updateProgress( uri );
                        try {
                            AudioTool.getInstance(MainActivity.this).withAudio(realPath).compressAudio(MainActivity.this.format, MainActivity.this.bitrate).saveCurrentTo(RealPathUtil.newFilePath("", fileName, MainActivity.this.outputFolder, MainActivity.this.format)).release();
                            success++;
                            successText.setText(MainActivity.this.getString(R.string.success)+success);
                        } catch (Exception e) {
                            error++;
                            errorText.setText(MainActivity.this.getString(R.string.error) + error);
                        }
                    }
                    return null;
                }
                return null;
            } else if (this.data.getClipData() != null) {
                int itemCount = this.data.getClipData().getItemCount();
                int error = 0;
                int success  = 0;
                for (int i  = 0; i < itemCount; i++) {
                    TextView textView4 = MainActivity.this.countText;
                    textView4.setText(i+1 + "/" + itemCount);
                    Uri uri = this.data.getClipData().getItemAt(i).getUri();
                    if (Build.VERSION.SDK_INT >= 30) {
                        realPath = RealPathUtil.copyFileToInternal(MainActivity.this, uri);
                    } else {
                        realPath = RealPathUtil.getRealPath(MainActivity.this, uri);
                    }
                    String fileName = RealPathUtil.getFileName(realPath);
                    MainActivity.this.fileText.setText(fileName);
                    updateProgress( uri);
                    //publishProgress(Integer.valueOf((int) ((((double) i9) / ((double) itemCount)) * 100.0d)));
                    try {
                        AudioTool.getInstance(MainActivity.this).withAudio(realPath).compressAudio(MainActivity.this.format, MainActivity.this.bitrate).saveCurrentTo(RealPathUtil.newFilePath("", fileName, MainActivity.this.outputFolder, MainActivity.this.format)).release();
                        try {
                            success++;
                            successText.setText(MainActivity.this.getString(R.string.success) + success);
                        } catch (Exception e) {
                            error++;
                            errorText.setText(MainActivity.this.getString(R.string.error) + error);
                        }
                    } catch (Exception e) {

                    }
                }
                return null;
            } else {
                Uri data = this.data.getData();
                MainActivity.this.countText.setText("1/1");
                if (Build.VERSION.SDK_INT >= 30) {
                    realPath = RealPathUtil.copyFileToInternal(MainActivity.this, data);
                } else {
                    realPath = RealPathUtil.getRealPath(MainActivity.this, data);
                }
                String fileName = RealPathUtil.getFileName(realPath);
                MainActivity.this.fileText.setText(fileName);
                updateProgress( data);
                try {
                    AudioTool.getInstance(MainActivity.this).withAudio(realPath).compressAudio(MainActivity.this.format, MainActivity.this.bitrate).saveCurrentTo(RealPathUtil.newFilePath("", fileName, MainActivity.this.outputFolder, MainActivity.this.format)).release();
                    successText.setText(MainActivity.this.getString(R.string.success) + 1);
                    return null;
                } catch (Exception e5) {
                    errorText.setText(MainActivity.this.getString(R.string.error) + 1);
                    Log.e("CHICHO", e5.getMessage());
                    return null;
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Void unused) {
            super.onPostExecute( unused);
            AlertDialog.Builder title = new AlertDialog.Builder(MainActivity.this).setTitle(MainActivity.this.getString(R.string.completed));
            title.setMessage(MainActivity.this.getString(R.string.folder) + "\n" + MainActivity.this.outputFolder).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.CompressAudios.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        }

        void updateProgress(Uri fileUri) {
            final int duration = MediaPlayer.create(MainActivity.this, fileUri).getDuration();
            Config.enableStatisticsCallback(new StatisticsCallback() { // from class: com.amg.compressaudio.MainActivity.CompressAudios.2
                @Override // com.arthenica.mobileffmpeg.StatisticsCallback
                public void apply(Statistics newStatistics) {
                    float parseFloat = (Float.parseFloat(String.valueOf(newStatistics.getTime())) / duration) * 100.0f;
                    float f = parseFloat < 99.0f ? parseFloat : 100.0f;
                    percentText.setText(((int) f) + "%");
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... values) {
            super.onProgressUpdate( values);
        }
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return false;
    }
}
