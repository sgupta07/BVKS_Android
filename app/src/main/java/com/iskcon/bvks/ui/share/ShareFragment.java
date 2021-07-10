package com.iskcon.bvks.ui.share;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.iskcon.bvks.BuildConfig;

public class ShareFragment extends Fragment {

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
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + appPackageName);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, null));
        } catch (android.content.ActivityNotFoundException anfe) {

        }
    }
}