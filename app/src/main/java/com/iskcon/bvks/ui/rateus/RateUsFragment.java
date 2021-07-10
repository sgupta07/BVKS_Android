package com.iskcon.bvks.ui.rateus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.iskcon.bvks.BuildConfig;

public class RateUsFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openGooglePlay();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private void openGooglePlay() {
         String appPackageName;
        if (BuildConfig.DEBUG) {
            appPackageName = "com.iskcon.bvks";
        } else {
            appPackageName = getActivity().getPackageName();
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}