package com.dotsandboxes.fragments;


import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dotsandboxes.R;
import com.dotsandboxes.Timers;
import com.dotsandboxes.utils.AdsLoader;
import com.dotsandboxes.utils.Globals;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerNameFragment extends DialogFragment {


    @BindView(R.id.edt_player_1_name)
    EditText edtPlayer1Name;
    @BindView(R.id.edt_player_2_name)
    EditText edtPlayer2Name;
    @BindView(R.id.ll_play)
    LinearLayout llPlay;

    private OnFragmentInteractionListener mListener;

    public PlayerNameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }

        // set the animations to use on showing and hiding the dialog
        getDialog().getWindow().setWindowAnimations(
                R.style.dialog_animation_fade);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_player_name, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.ll_play)
    public void onViewClicked() {
        String player1Name = edtPlayer1Name.getText().toString().trim();
        String player2Name = edtPlayer2Name.getText().toString().trim();

        if (TextUtils.isEmpty(player1Name)) {
            Toast.makeText(getActivity(), getString(R.string.enter_your_name), Toast.LENGTH_SHORT).show();
        } else if (player1Name.length() > 8) {
            Toast.makeText(getActivity(), getString(R.string.your_name_is_greater), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(player2Name)) {
            Toast.makeText(getActivity(), getString(R.string.enter_opponent_name), Toast.LENGTH_SHORT).show();
        } else if (player2Name.length() > 8) {
            Toast.makeText(getActivity(), getString(R.string.opponent_name_is_greater), Toast.LENGTH_SHORT).show();
        } else if (player1Name.equalsIgnoreCase(player2Name)) {
            Toast.makeText(getActivity(), getString(R.string.should_not_same), Toast.LENGTH_SHORT).show();
        } else {
            if (Globals.TIMER_FINISHED) {
                if (AdsLoader.mInterstitialAd != null) {
                    AdsLoader.mInterstitialAd.show(requireActivity());
                    stopAds(mListener, player1Name, player2Name);
                }else {
                    dismissDialog(mListener, player1Name, player2Name);
                }
            }else {
                dismissDialog(mListener, player1Name, player2Name);
            }

        }

    }

    private void stopAds(OnFragmentInteractionListener mListener, String player1Name, String player2Name) {
        AdsLoader.mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                Timers.timer().start();
                Globals.TIMER_FINISHED=false;
                AdsLoader.mInterstitialAd=null;
                dismissDialog(mListener, player1Name, player2Name);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.");
                AdsLoader.mInterstitialAd = null;
            }

            @Override
            public void onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.");
            }
        });

    }

    private void dismissDialog(OnFragmentInteractionListener mListener, String player1Name, String player2Name) {
        if (mListener != null) {
            mListener.onPlayClicked(player1Name, player2Name);
            dismiss();
        }
    }


    public interface OnFragmentInteractionListener {
        void onPlayClicked(String player1Name, String player2Name);
    }
}
