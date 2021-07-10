/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iskcon.bvks.listeners;

import androidx.annotation.Nullable;

import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.model.Playlist;

public interface OnListItemInteractionListener {
    void playLecture(Lecture lecture, boolean isVideoMode);

    boolean downloadAllowed();

    boolean favoritesAllowed();

    boolean deleteFavoritesAllowed();


    boolean removeDownloadsAllowed();

    boolean rearrangeAllowed();

    boolean show3dotPopupMenu();

    boolean showPlaybackProgress();

    boolean isVideoOption();

    void shareLecture(Lecture lecture);

    boolean isRemoveLectureAllowed();

    @Nullable
    Playlist getPlaylistObject();
}