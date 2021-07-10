package com.iskcon.bvks.listeners;

import androidx.annotation.Nullable;

import com.iskcon.bvks.model.LectureInformation;

public interface LectureInformationListener {
    void onLoadCurrentSnapShot(@Nullable LectureInformation obj);

    void onLoadFailed();

    void onError(String error);

    void onDocumentDoesNotExist();
}
