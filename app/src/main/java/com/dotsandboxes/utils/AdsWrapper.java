package com.dotsandboxes.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.dotsandboxes.R;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by agile-01 on 11/11/2016.
 * <p>
 * Usage :
 * <li>1. include layout_ad_container.xml to your layout</li>
 * <li>2. add your admob id and ad unit id in strings.xml under admob_app_id & banner_ad_unit_id </li>
 * <li>3. bind views</li>
 * <pre>
 * AdView adView = (AdView)findViewById(R.id.adView);
 * </pre>
 * <li>4. get instance using builder</li>
 * <pre>
 *  AdsWrapper adsWrapper = new Builder()
 *      .with(this)
 *      .addTestDeviceIds(new String[]{AdRequest.DEVICE_ID_EMULATOR,"D2YYYYYYYYYYYYYYYYYYYYYYYY"})
 *      .build();
 * </pre>
// * <li>5. call {@link #} to load banner ads.</li>
 * <pre>
 * adsWrapper.requestBannerAd(adView);
 * </pre>
 * <li>5. call loadBannerAd(...) to load banner ads.</li>
 * <pre>
 * adsWrapper.loadBannerAd("ca-app-pub-XXXXXXXXXXXXXXXX~NNNNNNNNNN",adView);
 * </pre>
 */
public class AdsWrapper {

    private static final String TAG = "Ads";

    @Nullable
    private Context mContext;

    @Nullable
    private Location mLocation;

    private int mGender = -1;

    @Nullable
    private String[] mTestDeviceIds;

    @Nullable
    private Date mBirthdayDate;

    private AdsWrapper() {
        //no direct instances. use builder instead.
    }




    public static class Builder {

        private Context context;
        private String[] testDeviceIds;
        private Location targetedLocation;
        private Date birthdayDate;


        public Builder with(@NonNull Context context) {
            this.context = context;
            return this;
        }

        public Builder addTestDeviceIds(@NonNull String[] testDeviceIds) {
            this.testDeviceIds = testDeviceIds;
            return this;
        }


        public Builder targetLocation(@NonNull Location targetedLocation) {
            this.targetedLocation = targetedLocation;
            return this;
        }

        public Builder targetAge(@NonNull Date birthdayDate) {
            this.birthdayDate = birthdayDate;
            return this;
        }


    }
}
