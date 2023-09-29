package com.amg.compressaudio;

import static com.amg.compressaudio.RealPathUtil.TEMP_FILE;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/* loaded from: classes.dex */
public class MainActivity extends AppCompatActivity {

    private static String AD_UNIT_ID = "";
    private static final String TAG = "MyActivity";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);
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

    ProgressBar progressBar;
    Operation current;

    int error;
    int success;

    private boolean adIsLoading;
    private InterstitialAd interstitialAd;

    private int adsCount = 0;
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AD_UNIT_ID = getString(R.string.banner1);

        adContainerView = findViewById(R.id.ad_view_container);

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());


        this.percentText = findViewById(R.id.percent);
        this.countText = findViewById(R.id.counter);
        this.fileText = findViewById(R.id.filename);
        this.errorText = findViewById(R.id.error);
        this.successText = findViewById(R.id.success);
        progressBar = findViewById(R.id.progress_circular);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.preferences = defaultSharedPreferences;
        adsCount = preferences.getInt("ADS",0);
        loadAdInfo();
        //((AdView) findViewById(R.id.adView)).loadAd(new AdRequest.Builder().build());
        //((AdView) findViewById(R.id.adView1)).loadAd(new AdRequest.Builder().build());
        //refreshAd();
        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_AUDIO") != 0 &&ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0 && ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_MEDIA_AUDIO","android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1234);
        } else {
            initialize();
        }
    }

    void loadAdInfo(){
        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(
                this,
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(
                                TAG,
                                String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                    }

                    if (googleMobileAdsConsentManager.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }

                    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
                        // Regenerate the options menu to include a privacy setting.
                        invalidateOptionsMenu();
                    }
                });

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
        }

        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainerView
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        () -> {
                            if (!initialLayoutComplete.getAndSet(true)
                                    && googleMobileAdsConsentManager.canRequestAds()) {
                                if (adsCount % 3 == 0)
                                    loadInter();
                                loadBanner();
                            }
                        });


        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345")).build());

    }

    private void loadBanner() {
        // Create a new ad view.
        adView = new AdView(this);
        adView.setAdUnitId(AD_UNIT_ID);
        adView.setAdSize(getAdSize());

        // Replace ad container with new ad view.
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        // Start loading the ad in the background.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public void loadInter() {
        // Request a new ad if one isn't already loaded.
        if (adIsLoading || interstitialAd != null) {
            return;
        }
        adIsLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                getString(R.string.inter),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        MainActivity.this.interstitialAd = interstitialAd;
                        adIsLoading = false;
                        Log.i(TAG, "onAdLoaded");
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        interstitialAd = null;
                        adIsLoading = false;

                        String error =
                                String.format(
                                        java.util.Locale.US,
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(),
                                        loadAdError.getCode(),
                                        loadAdError.getMessage());

                    }
                });
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {}
                });

        // Load an ad.
        if (initialLayoutComplete.get()) {
            //loadBanner();
        }
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        deleteTempFile();
        super.onDestroy();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        if (SETTINGS_ACT) {
            initialize();
            SETTINGS_ACT = false;
        }
    }

    private void initialize() {

        this.format = preferences.getString("format", "mp3");
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
        findViewById(R.id.file_layout).setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                current = Operation.SelectOne;
                selectAudio();

            }
        });
        findViewById(R.id.folder_layout).setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                current = Operation.SelectFolder;
                selectFolder();
                //MainActivity.this.startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT_TREE"), PICK_AUDIO_FOLDER);
            }
        });
        findViewById(R.id.output_layout).setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                OutputsActivity.launch(MainActivity.this, new File(MainActivity.this.outputFolder));
            }
        });
    }

    private ActivityResultLauncher<Intent> selectAudioAct = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            List<File> files = new ArrayList<>();
                            String command = "";
                            String fileName = "";
                            String output = "";
                            String fileAudioPath = "";
                            String input = "";
                            error = 0;
                            success  = 0;

                            percentText.setText(0 + "%");
                            successText.setText(MainActivity.this.getString(R.string.success)+success);
                            errorText.setText(MainActivity.this.getString(R.string.error) + error);
                            if (data.getClipData() != null && current != Operation.SelectFolder)
                                current = Operation.SelectSome;
                            progressBar.setVisibility(View.VISIBLE);
                            switch (current){

                                case SelectOne:
                                    fileAudioPath = RealPathUtil.getRealPath(MainActivity.this,data.getData());
                                    input = fileAudioPath.replace(" ","\' \'");
                                    fileName = RealPathUtil.getFileName(input);
                                    output = RealPathUtil.newFilePath("Cmpsd_aud",fileName,outputFolder);
                                    command = "-y -i "+ input+" -b:a " + bitrate + " " + output;
                                    executeCommand(command,data.getData(),fileName,1);
                                    break;
                                case SelectSome:
                                    int itemCount = data.getClipData().getItemCount();
                                    for (int i  = 0; i < itemCount; i++) {
                                        Uri uri = data.getClipData().getItemAt(i).getUri();
                                        fileAudioPath = RealPathUtil.getRealPath(MainActivity.this, uri);
                                        input = fileAudioPath.replace(" ", "\' \'");
                                        fileName = RealPathUtil.getFileName(input);
                                        output = RealPathUtil.newFilePath("Cmpsd_aud", fileName, outputFolder);
                                        command = "-y -i " + input + " -b:a " + bitrate + " " + output;
                                        executeCommand(command, uri, fileName, itemCount);
                                    }
                                    break;
                                case SelectFolder:
                                    DocumentFile fromTreeUri = DocumentFile.fromTreeUri(MainActivity.this, data.getData());
                                    List<Uri> fileUris = new ArrayList();
                                    for (DocumentFile documentFile : fromTreeUri.listFiles()) {
                                        if (documentFile.isFile() && documentFile.getType().contains("audio")) {
                                            fileUris.add(documentFile.getUri());
                                        }
                                    }

                                    for (int i = 0; i < fileUris.size(); i++) {
                                        Uri uri = fileUris.get(i);
                                        fileAudioPath = RealPathUtil.getRealPath(MainActivity.this,uri);
                                        input = fileAudioPath.replace(" ","\' \'");
                                        fileName = RealPathUtil.getFileName(input);
                                        output = RealPathUtil.newFilePath("Cmpsd_aud",fileName,outputFolder);
                                        command = "-y -i "+ input+" -b:a " + bitrate + " " + output;
                                        executeCommand(command,uri,fileName,fileUris.size());
                                    }

                                    break;
                            }

                        }

                    }
                }
            }
    );
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

    private void executeCommand(String command,Uri audioUri,String fileName,int total ) {

        //fileText.setText(fileName);
        //countText.setText((success+error+1)+"/"+total);


        final int duration = MediaPlayer.create(MainActivity.this, audioUri).getDuration();
        if (current == Operation.SelectOne) {
            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {
                    float parseFloat = (Float.parseFloat(String.valueOf(newStatistics.getTime())) / duration) * 100.0f;
                    float f = parseFloat < 99.0f ? parseFloat : 100.0f;
                    percentText.setText(((int) f) + "%");
                }
            });
        }
        else {
            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {

                }
            });
        }

        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {

            @Override
            public void apply(final long executionId, final int returnCode) {
                //progressDialog.dismiss();
                if (returnCode == RETURN_CODE_SUCCESS) {
                    success++;
                    successText.setText(MainActivity.this.getString(R.string.success)+success);
                    errorText.setText(MainActivity.this.getString(R.string.error) + error);
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    error++;
                    successText.setText(MainActivity.this.getString(R.string.success)+success);
                    errorText.setText(MainActivity.this.getString(R.string.error) + error);
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
                float f = (success+error);
                f /= total; f*=100;
                percentText.setText(((int) f) + "%");
                if ((success+error) == total) {
                    progressBar.setVisibility(View.GONE);
                    showFinishDialog();
                }
            }
        });

    }

    void showFinishDialog(){
        AlertDialog.Builder title = new AlertDialog.Builder(MainActivity.this).setTitle(MainActivity.this.getString(R.string.completed));
        title.setMessage(MainActivity.this.getString(R.string.folder) + "\n" + MainActivity.this.outputFolder).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.CompressAudios.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (adsCount % 3 == 0){
                    showInterstitial();
                }
            }
        }).show();
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(this);
            adsCount++;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("ADS",adsCount);
            editor.apply();
        } else {

            if (googleMobileAdsConsentManager.canRequestAds()) {
                loadInter();
            }
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

    void selectAudio(){


        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        selectAudioAct.launch(Intent.createChooser(intent,"Select Audio"));
        //selectAudioAct.launch(Intent.createChooser(intent,"Select Video"));
    }

    void selectFolder(){
        Intent intent =  new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
        selectAudioAct.launch(Intent.createChooser(intent,"Select Audio Folder"));
    }

    private void deleteTempFile() {
        final File[] files = getCacheDir().listFiles();
        if (files != null) {
            for (final File file : files) {
                //if (file.getName().contains(TEMP_FILE)) {
                    file.delete();
                    Log.d("DELETE",file.getPath());
                //}
            }
        }
    }
}
