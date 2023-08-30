package com.amg.compressaudio;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import java.util.Date;

/* loaded from: classes.dex */
public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private static final String TAG = "MyApplication";
    private AppOpenAdManager appOpenAdManager;
    String channelId = "Progress Notification";
    private Activity currentActivity;

    /* loaded from: classes.dex */
    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityDestroyed(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityPaused(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityResumed(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStopped(Activity activity) {
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        createNotificationChannels();
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());
        MobileAds.initialize(this, new OnInitializationCompleteListener() { // from class: com.amg.compressaudio.MyApplication.1
            @Override // com.google.android.gms.ads.initialization.OnInitializationCompleteListener
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        this.appOpenAdManager = new AppOpenAdManager();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(this.channelId, "Progress Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Progress Notification Channel");
            ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
        }
    }


    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStarted(Activity activity) {
        if (this.appOpenAdManager.isShowingAd) {
            return;
        }
        this.currentActivity = activity;
    }

    public void showAdIfAvailable(Activity activity, OnShowAdCompleteListener onShowAdCompleteListener) {
        this.appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener);
    }

    public class AppOpenAdManager {
        private static final String LOG_TAG = "AppOpenAdManager";
        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;
        private long loadTime = 0;

        public AppOpenAdManager() {
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void loadAd(Context context) {
            if (this.isLoadingAd || isAdAvailable()) {
                return;
            }
            String string = MyApplication.this.getString(R.string.load);
            this.isLoadingAd = true;
            AppOpenAd.load(context, string, new AdRequest.Builder().build(), 1, new AppOpenAd.AppOpenAdLoadCallback() { // from class: com.amg.compressaudio.MyApplication.AppOpenAdManager.1
                @Override // com.google.android.gms.ads.AdLoadCallback
                public void onAdLoaded(AppOpenAd ad) {
                    AppOpenAdManager.this.appOpenAd = ad;
                    AppOpenAdManager.this.isLoadingAd = false;
                    AppOpenAdManager.this.loadTime = new Date().getTime();
                    Log.d(AppOpenAdManager.LOG_TAG, "onAdLoaded.");
                }

                @Override // com.google.android.gms.ads.AdLoadCallback
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    AppOpenAdManager.this.isLoadingAd = false;
                    Log.d(AppOpenAdManager.LOG_TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                }
            });
        }

        private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
            return new Date().getTime() - this.loadTime < numHours * 3600000;
        }

        private boolean isAdAvailable() {
            return this.appOpenAd != null && wasLoadTimeLessThanNHoursAgo(3L);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showAdIfAvailable(final Activity activity) {
            showAdIfAvailable(activity, new OnShowAdCompleteListener() { // from class: com.amg.compressaudio.MyApplication.AppOpenAdManager.2
                @Override // com.amg.compressaudio.MyApplication.OnShowAdCompleteListener
                public void onShowAdComplete() {
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showAdIfAvailable(final Activity activity, final OnShowAdCompleteListener onShowAdCompleteListener) {
            if (this.isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.");
            } else if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.");
                onShowAdCompleteListener.onShowAdComplete();
                loadAd(activity);
            } else {
                Log.d(LOG_TAG, "Will show ad.");
                this.appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() { // from class: com.amg.compressaudio.MyApplication.AppOpenAdManager.3
                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdDismissedFullScreenContent() {
                        AppOpenAdManager.this.appOpenAd = null;
                        AppOpenAdManager.this.isShowingAd = false;
                        Log.d(AppOpenAdManager.LOG_TAG, "onAdDismissedFullScreenContent.");
                        onShowAdCompleteListener.onShowAdComplete();
                        AppOpenAdManager.this.loadAd(activity);
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        AppOpenAdManager.this.appOpenAd = null;
                        AppOpenAdManager.this.isShowingAd = false;
                        Log.d(AppOpenAdManager.LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                        onShowAdCompleteListener.onShowAdComplete();
                        AppOpenAdManager.this.loadAd(activity);
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdShowedFullScreenContent() {
                        Log.d(AppOpenAdManager.LOG_TAG, "onAdShowedFullScreenContent.");
                    }
                });
                this.isShowingAd = true;
                this.appOpenAd.show(activity);
            }
        }
    }
}
