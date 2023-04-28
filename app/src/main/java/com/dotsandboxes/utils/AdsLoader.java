package com.dotsandboxes.utils;

import android.content.Context;
import android.util.Log;

import com.dotsandboxes.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdsLoader {
    public static InterstitialAd mInterstitialAd = null;
    private static final String TAG = "TAG";

    public static void loadInterstitial(Context context) {
        mInterstitialAd = null;
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, context.getString(R.string.interstitial_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.d(TAG, adError.getMessage());
                mInterstitialAd = null;
            }

            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {
                Log.d(TAG, "Ad was loaded.");
                mInterstitialAd = interstitialAd;
            }
        });
    }
}
