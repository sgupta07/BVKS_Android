package com.iskcon.bvks.ui.signout;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.iskcon.bvks.ui.signin.LoginActivity;
import com.iskcon.bvks.util.PrefUtil;

public class SignOutFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unsubscribeFromTopic("BVKS_HINDI");
        unsubscribeFromTopic("BVKS_ENGLISH");
        unsubscribeFromTopic("BVKS_BANGALI");

        FirebaseAuth.getInstance().signOut();

        PrefUtil.removeAllPrefs(getContext());

        getActivity().finish();

        startActivity(new Intent(getContext(), LoginActivity.class));
    }

    private void unsubscribeFromTopic(String topicName) {

        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName);
    }
}