package com.iskcon.bvks.listeners;

import com.iskcon.bvks.model.Lecture;

public interface LectureMenuPopupWindowListener {
    void download(Lecture lecture);

    void removeDownload(Lecture lecture);

    void favorite(Lecture lecture);

    void removeFavorite(Lecture lecture);

    void playlist(Lecture lecture);

    void complete(Lecture lecture);

    void removeComplete(Lecture lecture);

    void share(Lecture lecture);

    void removeLecture(Lecture lecture);
}