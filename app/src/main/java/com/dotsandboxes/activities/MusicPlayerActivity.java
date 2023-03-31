package com.dotsandboxes.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dotsandboxes.R;
import com.dotsandboxes.services.MusicPlayerService;
import com.dotsandboxes.services.MusicService;
import com.dotsandboxes.utils.AdsWrapper;
import com.dotsandboxes.utils.Constants;
import com.dotsandboxes.utils.PrefUtils;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.preference.PreferenceManager;

/**
 * This activity is act as base activity for the application. it will bind the @MusicPlayerService
 */
public abstract class MusicPlayerActivity extends AppCompatActivity {
    final static int RC_LOOK_AT_MATCHES = 10001;
    private static final String TAG = "MusicPlayerActivity";
    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_SELECT_PLAYERS = 9010;
    private static final int RC_WAITING_ROOM = 9007;
    private static final int RC_INVITATION_INBOX = 9008;
    public MusicPlayerService mService;
    public boolean doubleBackToExitPressedOnce;
    public AdsWrapper adsWrapper;
    public PrefUtils prefUtils;
    boolean serviceBound = false;
    // The currently signed in account, used to check the account has changed outside of this activity when resuming.

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    private String senderInvitationId;
    private boolean mBoundMusicService = false;
    private String row;
    private String column;
    private String nameFromPrefrence;
    private String mMyParticipantId;
    private String mPlayerName;
    private boolean isRequestSender;
    private String mParticipantName;
    private boolean mPlaying;
    private AlertDialog dialogAcceptReject;
    private String myCreatedRoomId;
    private boolean isGameStarted;
    private int inviteesSize;

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            mService = binder.getService();
            serviceBound = true;

            Intent intent = new Intent(MusicPlayerActivity.this, MusicPlayerService.class);

            boolean playMusic = PreferenceManager.getDefaultSharedPreferences(MusicPlayerActivity.this).getBoolean(getString(R.string.pref_key_music), true);

            if (playMusic) {
                intent.setAction(MusicPlayerService.ACTION_START_MUSIC);
            } else {
                intent.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
            }

            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

            Toast.makeText(MusicPlayerActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            startService(new Intent(MusicPlayerService.ACTION_STOP_MUSIC));
            serviceBound = false;
        }
    };
    /**
     * this handleMessageSentCallback will get call back once message has send
     * and it will remove the token id from pending message list


    /**
     * this mRoomStatusCallbackHandler will gives each and every call from room players creation to disconnected form room
     */
    /**
     * this listener will receives all real time message send by another player
     */
    /**
     * this will give all callbacks of room
     */
    /**
     * this will gives all the callback of invitation receive and removed
     */


    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }

    @Override
    protected void onStop() {
        doUnbindService();
        super.onStop();
    }

    void doBindService() {
        Intent intent = new Intent(this, MusicService.class);

        // start playing music if the user specified so in the settings screen
        if (!serviceBound) {
            boolean playMusic = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_key_music), true);
            if (playMusic) {
                intent.setAction(MusicPlayerService.ACTION_START_MUSIC);
            } else {
                intent.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
            }

            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            serviceBound = true;
        }
    }

    void doUnbindService() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      prefUtils = new PrefUtils(MusicPlayerActivity.this);
        // this will check user is already signed in or not
        //if yes then it will login silently
        //else it will show sign-in manually

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        mPlaying = false;

        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            mService.stopSelf();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public boolean isAppInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }

        return isInBackground;
    }

    /**
     * This method is used to load image to image view specified
     *
     * @param imageUrl  it is the url of the image
     * @param imageView it is the imageview, in which image should load
     */
    public void setImageToImageView(String imageUrl, ImageView imageView) {
        Glide.with(this)
                .load(imageUrl).apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    /**
     * This method is used to load image to image view specified
     *
     * @param imageUrl  it is the url of the image from resouce folder
     * @param imageView it is the imageview, in which image should load
     */
    public void setImageToImageViewFromResource(int imageUrl, ImageView imageView) {
        Glide.with(this)
                .load(imageUrl)
                .into(imageView);
    }

    /**
     * this will check is user signed in or not before and return true or false
     */


    /**
     * this method sign-in silently in google play games
     */


    /**
     * this method will start the select player intent
     *
     * @param row    it will come as selected by user
     * @param column it will come as selected by user
     */


    /**
     * this method is abstract and have declaration in MainActivity.java
     */
    abstract void startGame(int row, int column, String myName, String opponentName, Context context, boolean isRequestSender);

    /**
     * this method is abstract and have declaration in MainActivity.java
     */
    abstract void makeMove(int startDot, int endDot);

    /**
     * this method is abstract and have declaration in MainActivity.java
     */
    abstract void showRepeatDialouge();

    /**
     * this method is abstract and have declaration in MainActivity.java
     */
    abstract void repeatAccepted();

    /**
     * this method is abstract and have declaration in MainActivity.java
     */
    abstract void repeatRejected();

    /**
     * this method is abstract and have declaration in MainActivity.java
     */
    abstract void gameOver(String showInDialogue);

    /**
     * this method is abstract and have declaration in MainActivity.java
     */
    abstract void showLoader();

    /**
     * this method is abstract and have declaration in MainActivity.java
     */
    abstract void hideLoader();

    /**
     * this method will save the token id in pending message list
     */

}
