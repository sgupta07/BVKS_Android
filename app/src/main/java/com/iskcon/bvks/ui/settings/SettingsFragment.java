package com.iskcon.bvks.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.firebase.messaging.FirebaseMessaging;
import com.iskcon.bvks.R;
import com.iskcon.bvks.databinding.FragmentSettingsBinding;
import com.iskcon.bvks.listeners.NotificationSettingsListener;
import com.iskcon.bvks.manager.SettingsManager;
import com.iskcon.bvks.model.SettingsModel;
import com.iskcon.bvks.util.Constants;
import com.iskcon.bvks.util.Utils;

/**
 * @AUTHOR Amandeep Singh
 * @date 30/01/2021
 */

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private FragmentSettingsBinding mBinding;
    private boolean mHindi=true, mEnglish=true, mBengali=true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SettingsManager.getInstance().getNotificationSettings(new NotificationSettingsListener() {
            @Override
            public void onLoadSettingsComplete(@NonNull SettingsModel settingsModel) {
                mBinding.setHindi(mHindi = settingsModel.notification.hindi);
                mBinding.setBengali(mBengali = settingsModel.notification.bengali);
                mBinding.setEnglish(mEnglish = settingsModel.notification.english);

                if (mHindi) {
                    subscribeToTopic(Constants.PUSH_TOPIC_HINDI);
                } else {
                    unsubscribeFromTopic(Constants.PUSH_TOPIC_HINDI);
                }
                if (mEnglish) {
                    subscribeToTopic(Constants.PUSH_TOPIC_ENGLISH);
                } else {
                    unsubscribeFromTopic(Constants.PUSH_TOPIC_ENGLISH);
                }
                if (mBengali) {
                    subscribeToTopic(Constants.PUSH_TOPIC_BENGALI);
                } else {
                    unsubscribeFromTopic(Constants.PUSH_TOPIC_BENGALI);
                }

            }

            @Override
            public void onLoadSettingsFailed() {

            }

            @Override
            public void onDocumentDoesNotExist() {

            }
        });

        mBinding.tvDefaultSettings.setOnClickListener(v -> {
            mBinding.setHindi(mHindi = true);
            mBinding.setBengali(mBengali = true);
            mBinding.setEnglish(mEnglish = true);

            subscribeToTopic(Constants.PUSH_TOPIC_HINDI);
            subscribeToTopic(Constants.PUSH_TOPIC_ENGLISH);
            subscribeToTopic(Constants.PUSH_TOPIC_BENGALI);

            SettingsModel sm = new SettingsModel(System.currentTimeMillis(), new SettingsModel.NotificationSettings(mHindi, mEnglish, mBengali));
            SettingsManager.getInstance().saveNotificationSettings(sm);
        });

        mBinding.switchHindi.setOnClickListener(v -> {
            if (mBinding.switchHindi.isChecked()) {
                mHindi = true;
                subscribeToTopic(Constants.PUSH_TOPIC_HINDI);
            } else {
                mHindi = false;
                unsubscribeFromTopic(Constants.PUSH_TOPIC_HINDI);
            }
            SettingsModel sm = new SettingsModel(System.currentTimeMillis(), new SettingsModel.NotificationSettings(mHindi, mEnglish, mBengali));
            SettingsManager.getInstance().saveNotificationSettings(sm);
        });
        mBinding.switchEnglish.setOnClickListener(v -> {
            if (mBinding.switchEnglish.isChecked()) {
                mEnglish = true;
                subscribeToTopic(Constants.PUSH_TOPIC_ENGLISH);
            } else {
                mEnglish = false;
                unsubscribeFromTopic(Constants.PUSH_TOPIC_ENGLISH);
            }
            SettingsModel sm = new SettingsModel(System.currentTimeMillis(), new SettingsModel.NotificationSettings(mHindi, mEnglish, mBengali));
            SettingsManager.getInstance().saveNotificationSettings(sm);
        });
        mBinding.switchBengali.setOnClickListener(v -> {
            if (mBinding.switchBengali.isChecked()) {
                mBengali = true;
                subscribeToTopic(Constants.PUSH_TOPIC_BENGALI);
            } else {
                mBengali = false;
                unsubscribeFromTopic(Constants.PUSH_TOPIC_BENGALI);
            }
            SettingsModel sm = new SettingsModel(System.currentTimeMillis(), new SettingsModel.NotificationSettings(mHindi, mEnglish, mBengali));
            SettingsManager.getInstance().saveNotificationSettings(sm);
        });

    }

    private void subscribeToTopic(String topicName) {
        FirebaseMessaging.getInstance().subscribeToTopic(topicName);
    }

    private void unsubscribeFromTopic(String topicName) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName);
    }
}