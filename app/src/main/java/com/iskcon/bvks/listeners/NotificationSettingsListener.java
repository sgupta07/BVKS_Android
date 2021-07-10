package com.iskcon.bvks.listeners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iskcon.bvks.model.SettingsModel;

public interface NotificationSettingsListener {

    void onLoadSettingsComplete(@Nullable SettingsModel settingsModel);

    void onLoadSettingsFailed();

    void onDocumentDoesNotExist();
}
