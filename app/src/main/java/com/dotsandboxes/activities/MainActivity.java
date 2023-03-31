package com.dotsandboxes.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dotsandboxes.R;
import com.dotsandboxes.Timers;
import com.dotsandboxes.event_bus.RxBus;
import com.dotsandboxes.event_bus.events.EmitSoundEvent;
import com.dotsandboxes.fragments.ChooseTurnFragment;
import com.dotsandboxes.fragments.GameFragment;
import com.dotsandboxes.fragments.HomeFragment;
import com.dotsandboxes.fragments.PlayerNameFragment;
import com.dotsandboxes.fragments.PrivacyFragment;
import com.dotsandboxes.fragments.ResultFragment;
import com.dotsandboxes.fragments.SettingsFragment;
import com.dotsandboxes.fragments.WonLostFragment;
import com.dotsandboxes.game.controllers.Game;
import com.dotsandboxes.services.MusicPlayerService;
import com.dotsandboxes.utils.Constants;
import com.dotsandboxes.utils.PrefUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.tuyenmonkey.mkloader.MKLoader;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends MusicPlayerActivity implements PlayerNameFragment.OnFragmentInteractionListener, GameFragment.OnFragmentInteractionListener, ResultFragment.OnFragmentInteractionListener, ChooseTurnFragment.OnFragmentInteractionListener, WonLostFragment.OnFragmentInteractionListener {

    public PrefUtils prefUtils;
    @BindView(R.id.iv_toolbar_back)
    public ImageView ivToolbarBack;
    @BindView(R.id.tv_toolbar_title)
    public TextView tvToolbarTitle;
    @BindView(R.id.iv_toolbar_vibrate)
    public ImageView ivToolbarVibrate;
    @BindView(R.id.iv_toolbar_music)
    public ImageView ivToolbarMusic;
    @BindView(R.id.iv_toolbar_sound)
    public ImageView ivToolbarSound;
    @BindView(R.id.iv_toolbar_setting)
    public ImageView ivToolbarSetting;
    @BindView(R.id.iv_toolbar_facebook)
    public ImageView ivToolbarFacebook;
    @BindView(R.id.common_toolbar)
    public Toolbar commonToolbar;
    @BindView(R.id.frame_container)
    public FrameLayout frameContainer;
    public GameFragment gameFragment;
    public String player1Name;
    public String player2Name;

    @BindView(R.id.loader)
    MKLoader loader;
    @BindView(R.id.ll_loader)
    LinearLayout llLoader;
    @BindView(R.id.ll_main)
    LinearLayout llMain;
    String selectedRowItem = "4";
    String selectedColumnItem = "4";
    Animation expandIn = null;
    boolean mIsStateAlreadySaved = false;
    boolean mPendingShowDialog = false;
    String row;
    String column;
    Dialog dialogRepeat;
    private Pair<String, String> rowColumnPair = new Pair<>("4", "4");
    private String nameFromPrefrence = "";
    private String imageFromPreference = "";
    private String gameMode;
    private String playerNameMe;
    private String playerMeImage;
    private Context musicPlayerActivityContext;
    private MusicPlayerActivity musicPlayerActivity;
    private AlertDialog alert11;
    private boolean isRequestSender;
    private String myName;
    private String opponentName;
    private WonLostFragment wonLostFragment;
    private ResultFragment resultFragment;
    private ChooseTurnFragment chooseTurnFragment;
    /**
     * Class for interacting with the game interface of the service.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            mService = binder.getService();
            serviceBound = true;

            Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);

            boolean playMusic = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(getString(R.string.pref_key_music), true);

            if (playMusic) {
                intent.setAction(MusicPlayerService.ACTION_START_MUSIC);
                mService.sendCommand(intent);
            } else {
                intent.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
                mService.sendCommand(intent);
            }

            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            startService(new Intent(MusicPlayerService.ACTION_STOP_MUSIC));
            serviceBound = false;
        }
    };

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        commonToolbar.setVisibility(View.GONE);
        dialogRepeat = new Dialog(this);
        addFragment(new HomeFragment(true), false);
        loadBanners();
        Timers.timer().start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }
    private void loadBanners() {

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onStop() {
        doBindService();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    void doBindService() {
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);

        // start playing music if the user specified so in the settings screen
        boolean playMusic = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(getString(R.string.pref_key_music), true);
        if (playMusic) {
            intent.setAction(MusicPlayerService.ACTION_START_MUSIC);
        } else {
            intent.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
        }

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        serviceBound = true;
    }

    void doUnbindService() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    protected void onPause() {


        super.onPause();
    }

    @Override
    public void onBackPressed() {
        hideKeyboard(this);
        Fragment mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (mCurrentFragment != null && (mCurrentFragment instanceof PrivacyFragment)) {
            replaceFragment(new SettingsFragment(), false);
        } else if (mCurrentFragment != null && (mCurrentFragment instanceof GameFragment)) {
            showExitDailog();
        } else if (mCurrentFragment != null && !(mCurrentFragment instanceof HomeFragment)) {
            replaceFragment(new HomeFragment(false), false);
        } else {
            if (doubleBackToExitPressedOnce) {
                finish();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, Constants.BACK_BUTTON_TWICE, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 1000);
        }
    }

    /**
     * this will send the message to all connected players
     */
    public void sendMessageOnline(String message) {
    }

    /**
     * this method will start the game
     */
    @Override
    void startGame(int row, int column, String myName, String opponentName, Context context, boolean isRequestSender) {
        this.row = String.valueOf(row);
        this.column = String.valueOf(column);
        this.gameMode = Constants.ONLINE;
        this.myName = myName;
        this.opponentName = opponentName;
        if (chooseTurnFragment != null) {
            chooseTurnFragment.dismissAllowingStateLoss();
        }
        if (wonLostFragment != null) {
            wonLostFragment.dismissAllowingStateLoss();
        }
        if (resultFragment != null) {
            resultFragment.dismissAllowingStateLoss();
        }

        this.isRequestSender = isRequestSender;
        musicPlayerActivityContext = context;
        Bundle args = new Bundle();
        args.putInt(Constants.SELECTED_ROW, row);
        args.putInt(Constants.SELECTED_COLUMN, column);
        args.putString(Constants.GAME_MODE, Constants.ONLINE);
        args.putString(Constants.PLAYER1_NAME, myName);
        args.putString(Constants.PLAYER2_NAME, opponentName);
        args.putBoolean(Constants.ISREQUESTSENDER, isRequestSender);
        musicPlayerActivity = (MusicPlayerActivity) musicPlayerActivityContext;
        gameFragment = new GameFragment();
        gameFragment.setArguments(args);
        replaceFragment(gameFragment, false);
        loadGameFragment();
    }

    /**
     * this method will make the move between lines as user clicks on it
     */
    @Override
    void makeMove(int startDot, int endDot) {
        gameFragment.makeMove(startDot, endDot);
    }

    /**
     * this method shows repeat dialogue
     */
    @Override
    void showRepeatDialouge() {
        Fragment mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (mCurrentFragment != null && (mCurrentFragment instanceof GameFragment)) {

            if (wonLostFragment != null && wonLostFragment.isVisible()) {
                wonLostFragment.dismissAllowingStateLoss();
            }
            if (resultFragment != null && resultFragment.isVisible()) {
                resultFragment.dismissAllowingStateLoss();
            }
           /* AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setMessage(opponentName +" "+ getString(R.string.wants_to_play_with_you));
            builder1.setCancelable(false);

            builder1.setPositiveButton(getString(R.string.play_multiplayer),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            sendMessageOnline(getString(R.string.repeat_yes_multiplayer));
                            Bundle args = new Bundle();
                            args.putInt(Constants.SELECTED_ROW, Integer.parseInt(row));
                            args.putInt(Constants.SELECTED_COLUMN, Integer.parseInt(column));
                            args.putString(Constants.GAME_MODE, Constants.ONLINE);
                            args.putString(Constants.PLAYER1_NAME, myName);
                            args.putString(Constants.PLAYER2_NAME, opponentName);
                            args.putBoolean(Constants.ISREQUESTSENDER, isRequestSender);
                            musicPlayerActivity = (MusicPlayerActivity) musicPlayerActivityContext;
                            gameFragment = new GameFragment();
                            gameFragment.setArguments(args);
                            replaceFragment(gameFragment, false);
                            loadGameFragment();
                            dialog.cancel();
                        }
                    });

            builder1.setNegativeButton(getString(R.string.cancel_multiplayer),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            sendMessageOnline(getString(R.string.repeat_no_multiplayer));
                            if (musicPlayerActivity != null)
                                musicPlayerActivity.gameOver("");
                            dialog.cancel();
                        }
                    });

            alert11 = builder1.create();
            if (!alert11.isShowing())
                alert11.show();*/

            if (dialogRepeat.isShowing()) {
                dialogRepeat.dismiss();
            }

            dialogRepeat.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogRepeat.setContentView(R.layout.fragment_game_request);
            dialogRepeat.setCancelable(false);
            dialogRepeat.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            AppCompatTextView tvRequest = dialogRepeat.findViewById(R.id.tv_request);
            AppCompatTextView tvCancel = dialogRepeat.findViewById(R.id.tv_cancel);
            AppCompatTextView tvYes = dialogRepeat.findViewById(R.id.tv_yes);
            tvRequest.setText(opponentName + " " + getString(R.string.wants_to_play_with_you));
            tvYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageOnline(getString(R.string.repeat_yes_multiplayer));
                    Bundle args = new Bundle();
                    args.putInt(Constants.SELECTED_ROW, Integer.parseInt(row));
                    args.putInt(Constants.SELECTED_COLUMN, Integer.parseInt(column));
                    args.putString(Constants.GAME_MODE, Constants.ONLINE);
                    args.putString(Constants.PLAYER1_NAME, myName);
                    args.putString(Constants.PLAYER2_NAME, opponentName);
                    args.putBoolean(Constants.ISREQUESTSENDER, isRequestSender);
                    musicPlayerActivity = (MusicPlayerActivity) musicPlayerActivityContext;
                    gameFragment = new GameFragment();
                    gameFragment.setArguments(args);
                    replaceFragment(gameFragment, false);
                    loadGameFragment();
                    dialogRepeat.dismiss();
                }
            });
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageOnline(getString(R.string.repeat_no_multiplayer));
                    if (musicPlayerActivity != null)
                        musicPlayerActivity.gameOver("");
                    dialogRepeat.dismiss();
                }
            });
            if (!dialogRepeat.isShowing())
                dialogRepeat.show();
            Window windowView = dialogRepeat.getWindow();
            windowView.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        }
    }

    /**
     * this will start a new game once another player accepts the repeat-request
     */
    @Override
    void repeatAccepted() {
        Bundle args = new Bundle();
        args.putInt(Constants.SELECTED_ROW, Integer.parseInt(row));
        args.putInt(Constants.SELECTED_COLUMN, Integer.parseInt(column));
        args.putString(Constants.GAME_MODE, Constants.ONLINE);
        args.putString(Constants.PLAYER1_NAME, myName);
        args.putString(Constants.PLAYER2_NAME, opponentName);
        args.putBoolean(Constants.ISREQUESTSENDER, isRequestSender);
        musicPlayerActivity = (MusicPlayerActivity) musicPlayerActivityContext;
        gameFragment = new GameFragment();
        gameFragment.setArguments(args);
        replaceFragment(gameFragment, false);
        loadGameFragment();
    }

    /**
     * this will shows user that repeat-game is rejected and start home screen
     */
    @Override
    void repeatRejected() {
        if (musicPlayerActivity != null)
            musicPlayerActivity.gameOver(getString(R.string.repeat_game_rejected));
    }

    /**
     * this method call's when game is over
     * and this method get the user to the home screen
     *
     * @param showInDialogue
     */
    @Override
    void gameOver(String showInDialogue) {

        replaceFragment(new HomeFragment(false), false);
        if (wonLostFragment != null && wonLostFragment.isVisible()) {
            wonLostFragment.dismissAllowingStateLoss();
        }
        if (resultFragment != null && resultFragment.isVisible()) {
            resultFragment.dismissAllowingStateLoss();
        }
        if (showInDialogue != "") {
         /*   AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setMessage(showInDialogue);
            builder1.setCancelable(false);

            builder1.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
*/

            final Dialog dialogSort = new Dialog(this);
            dialogSort.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogSort.setContentView(R.layout.fragment_game_over);
            dialogSort.setCancelable(true);
            dialogSort.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            AppCompatTextView tvRequest = dialogSort.findViewById(R.id.tv_request);
            AppCompatTextView tvYes = dialogSort.findViewById(R.id.tv_yes);
            tvRequest.setText(showInDialogue);
            tvYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogSort.dismiss();
                }
            });
            dialogSort.show();
            Window windowView = dialogSort.getWindow();
            windowView.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * this method shows the loader and restrict the user to click outside
     */
    @Override
    void showLoader() {
        llMain.setClickable(false);
        llMain.setEnabled(false);
        llMain.setFocusable(false);
        llLoader.setClickable(false);
        llLoader.setEnabled(false);
        llLoader.setFocusable(false);
        frameContainer.setEnabled(false);
        frameContainer.setClickable(false);
        frameContainer.setFocusable(false);
        loader.setVisibility(View.VISIBLE);
        llLoader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                return;
            }
        });
        llLoader.setVisibility(View.VISIBLE);
    }

    /**
     * this method hides the loader
     */
    @Override
    void hideLoader() {
        llMain.setEnabled(true);
        llMain.setClickable(true);
        llMain.setFocusable(true);
        frameContainer.setEnabled(true);
        frameContainer.setClickable(true);
        loader.setVisibility(View.GONE);
        llLoader.setVisibility(View.GONE);
    }

    /**
     * to add fragment in container
     * tag will be same as class name of fragment
     *
     * @param addToBackStack should be added to back stack?
     */
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_container, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * to replace fragment in container
     * tag will be same as class name of fragment
     *
     * @param isAddedToBackStack should be added to back stack?
     */
    public void replaceFragment(Fragment fragment, boolean isAddedToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out);
        fragmentTransaction.replace(R.id.frame_container, fragment);
        if (isAddedToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void loadGameFragment() {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean hasMusic = preferences.getBoolean(getString(R.string.pref_key_music), true);
        final boolean hasSound = preferences.getBoolean(getString(R.string.pref_key_sound), false);
        final boolean hasVibrate = preferences.getBoolean(getString(R.string.pref_key_vibrate), false);

      /*  ivToolbarVibrate.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.icon_color_white));
        ivToolbarMusic.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.icon_color_white));
        ivToolbarSound.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.icon_color_white));*/
   /*     if (hasMusic) {
            ivToolbarSound.setImageDrawable(getResources().getDrawable(R.drawable.music_icon));
        } else {
            ivToolbarSound.setImageDrawable(getResources().getDrawable(R.drawable.musicoff));
        }

        if (hasSound) {
            ivToolbarMusic.setImageDrawable(getResources().getDrawable(R.drawable.sound_icon));
        } else {
            ivToolbarMusic.setImageDrawable(getResources().getDrawable(R.drawable.sound_off));
        }

        if (hasVibrate) {
            ivToolbarVibrate.setImageDrawable(getResources().getDrawable(R.drawable.vibrate_icon));
        } else {
            ivToolbarVibrate.setImageDrawable(getResources().getDrawable(R.drawable.vibrateoff));
        }*/

        if (hasMusic) {
            ivToolbarSound.setAlpha(1f);
        } else {
            ivToolbarSound.setAlpha(0.5f);
        }

        if (hasSound) {
            ivToolbarMusic.setAlpha(1f);
        } else {
            ivToolbarMusic.setAlpha(0.5f);
        }

        if (hasVibrate) {
            ivToolbarVibrate.setAlpha(1f);
        } else {
            ivToolbarVibrate.setAlpha(0.5f);
        }

    }

    /**
     * this method will set data when user plays with friend
     */
    public void setFriendGameData(String row, String column, String playerNameMe, String playerMeImage) {
        this.row = row;
        this.column = column;
        this.playerNameMe = playerNameMe;
        this.playerMeImage = playerMeImage;

    }

    /**
     * this method will set data when user plays with robot
     */
    public void setRobotGameData(String row, String column, String gamemode, String playerNameMe, String playerMeImage) {
        this.row = row;
        this.column = column;
        this.gameMode = gamemode;
        this.playerNameMe = playerNameMe;
        this.playerMeImage = playerMeImage;

    }

    @Override
    public void onPlayClicked(String player1Name, String player2Name) {
        Bundle args = new Bundle();
        args.putInt(Constants.SELECTED_ROW, Integer.parseInt(row));
        args.putInt(Constants.SELECTED_COLUMN, Integer.parseInt(column));
        gameMode = Constants.FRIEND;
        args.putString(Constants.GAME_MODE, Constants.FRIEND);
        this.player1Name = player1Name;
        args.putString(Constants.PLAYER1_NAME, player1Name);
        this.player2Name = player2Name;
        args.putString(Constants.PLAYER2_NAME, player2Name);

        gameFragment = new GameFragment();
        gameFragment.setArguments(args);
        replaceFragment(gameFragment, false);
        loadGameFragment();
    }

    @Override
    public void onWinFragmentLoad(int fragmentId, Bundle args) {
        if (mIsStateAlreadySaved) {
            mPendingShowDialog = true;
        } else {


            if (fragmentId == ResultFragment.FRAGMENT_ID) {
                int scorePlayer1 = args.getInt(GameFragment.ARG_PLAYER1_SCORE);
                int scorePlayer2 = args.getInt(GameFragment.ARG_PLAYER2_SCORE);
                Game.Mode mode = (Game.Mode) args.getSerializable(GameFragment.ARG_GAME_MODE);


                if (!isAppInBackground(MainActivity.this)) {
                    if (gameMode.equals(Constants.FRIEND)) {
                        resultFragment = new ResultFragment();
                        resultFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                        resultFragment.setArguments(args);
                        resultFragment.show(getSupportFragmentManager(), "dialog_fragment");
                        resultFragment.setCancelable(false);
                    } else if (gameMode.equals(Constants.ONLINE)) {
                        wonLostFragment = new WonLostFragment();
                        wonLostFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                        wonLostFragment.setArguments(args);
                        wonLostFragment.show(getSupportFragmentManager(), "dialog_fragment");
                        wonLostFragment.setCancelable(false);
                    } else {
                        ResultFragment dialog = ResultFragment.newInstance(args);
                        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                        dialog.setArguments(args);
                        dialog.show(getSupportFragmentManager(), "dialog_fragment");
                        dialog.setCancelable(false);
                    }
                }


            }
        }
    }

    @Override
    public void onSoundRequested() {
        mService.sendCommand(new Intent(MusicPlayerService.ACTION_PLAY_SOUND));
    }

    @Override
    public void onChooseTurnFragmentLoad() {
        chooseTurnFragment = new ChooseTurnFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.GAME_MODE, gameMode);
        bundle.putString(Constants.PLAYER1_NAME, player1Name);
        bundle.putString(Constants.PLAYER2_NAME, player2Name);
        bundle.putString(Constants.prefrences.NAME, playerNameMe);
        bundle.putString(Constants.prefrences.PROFILE_IMAGE, playerMeImage);
        chooseTurnFragment.setArguments(bundle);
        chooseTurnFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        chooseTurnFragment.show(getSupportFragmentManager(), "dialog_fragment");
        chooseTurnFragment.setCancelable(false);
    }

    @Override
    public void onPlayer1Selected() {
        gameFragment.onPlayer1Clicked();
    }

    @Override
    public void onPlayer2Selected() {
        gameFragment.onPlayer2Clicked();
    }

    @Override
    public void onReplayRequested(Bundle arguments) {
        if (gameMode.equals(Constants.ONLINE)) {
            sendMessageOnline("repeat?");
        } else {
            Bundle args = new Bundle();
            args.putInt(Constants.SELECTED_ROW, Integer.parseInt(row));
            args.putInt(Constants.SELECTED_COLUMN, Integer.parseInt(column));
            args.putString(Constants.GAME_MODE, gameMode);
            args.putString(Constants.PLAYER1_NAME, player1Name);
            args.putString(Constants.PLAYER2_NAME, player2Name);

            gameFragment = new GameFragment();
            gameFragment.setArguments(args);
            replaceFragment(gameFragment, false);
            loadGameFragment();
        }
    }

    @Override
    public void onMenuRequested() {
        if (gameMode.equals(Constants.ONLINE)) {
            if (musicPlayerActivity != null)
                musicPlayerActivity.gameOver("");
        } else {
            replaceFragment(new HomeFragment(false), false);
        }
    }

    @OnClick({R.id.iv_toolbar_back, R.id.iv_toolbar_vibrate, R.id.iv_toolbar_music, R.id.iv_toolbar_sound, R.id.iv_toolbar_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_toolbar_back:
                onBackPressed();
                break;
            case R.id.iv_toolbar_vibrate:
                SharedPreferences preferencesVibreate = PreferenceManager.getDefaultSharedPreferences(this);
                boolean hasVibrate = preferencesVibreate.getBoolean(getString(R.string.pref_key_vibrate), false);
                if (!hasVibrate) {
                    preferencesVibreate.edit().putBoolean(getString(R.string.pref_key_vibrate), true).apply();
                    ivToolbarVibrate.setAlpha(1f);
                } else {

                    preferencesVibreate.edit().putBoolean(getString(R.string.pref_key_vibrate), false).apply();
                    ivToolbarVibrate.setAlpha(0.5f);
                }
                gameFragment.onVibrateRequested();
                break;
            case R.id.iv_toolbar_music:
                SharedPreferences preferencesSound = PreferenceManager.getDefaultSharedPreferences(this);
                boolean hasSound = preferencesSound.getBoolean(getString(R.string.pref_key_sound), false);
                if (!hasSound) {
                    preferencesSound.edit().putBoolean(getString(R.string.pref_key_sound), true).apply();
                    ivToolbarMusic.setAlpha(1f);
                } else {
                    RxBus.getInstance().send(new EmitSoundEvent());

                    preferencesSound.edit().putBoolean(getString(R.string.pref_key_sound), false).apply();
                    ivToolbarMusic.setAlpha(0.5f);
                }
                gameFragment.onSoundRequestClick();
                break;
            case R.id.iv_toolbar_sound:
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                final boolean hasMusic = preferences.getBoolean(getString(R.string.pref_key_music), false);
                if (!hasMusic) {
                    Intent intent = new Intent(this, MusicPlayerService.class);
                    intent.setAction(MusicPlayerService.ACTION_START_MUSIC);
                    mService.sendCommand(intent);

                    preferences.edit().putBoolean(getString(R.string.pref_key_music), true).apply();
                    ivToolbarSound.setAlpha(1f);
                } else {
                    Intent intent = new Intent(this, MusicPlayerService.class);
                    intent.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
                    mService.sendCommand(intent);

                    preferences.edit().putBoolean(getString(R.string.pref_key_music), false).apply();
                    ivToolbarSound.setAlpha(0.5f);
                }
                break;
            case R.id.iv_toolbar_setting:
                break;
        }
    }

    @Override
    public void onResumeFragments() {
        super.onResumeFragments();
        mIsStateAlreadySaved = false;
        if (mPendingShowDialog) {
            mPendingShowDialog = false;
            Bundle args = gameFragment.loadGameResultFragment();
            onWinFragmentLoad(ResultFragment.FRAGMENT_ID, args);
        }
    }

    public void showExitDailog() {
     /*   AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage(getString(R.string.game_exit));
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (gameMode.equals(Constants.ONLINE)) {
                            if (musicPlayerActivity != null)
                                musicPlayerActivity.gameOver("");
                        } else {
                            replaceFragment(new HomeFragment(false), false);
                        }
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();*/

        final Dialog dialogSort = new Dialog(this);
        dialogSort.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogSort.setContentView(R.layout.fragment_exit_game);
        dialogSort.setCancelable(true);
        dialogSort.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        AppCompatTextView tvNo = dialogSort.findViewById(R.id.tv_cancel);
        AppCompatTextView tvYes = dialogSort.findViewById(R.id.tv_yes);
        tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialogSort.dismiss();
            }
        });
        tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gameMode.equals(Constants.ONLINE)) {
                    if (musicPlayerActivity != null)
                        musicPlayerActivity.gameOver("");
                } else {
                    replaceFragment(new HomeFragment(false), false);
                }
                dialogSort.dismiss();
            }
        });
        dialogSort.show();
        Window windowView = dialogSort.getWindow();
        windowView.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}
