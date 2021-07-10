package com.iskcon.bvks.listeners;

import androidx.annotation.NonNull;

import com.iskcon.bvks.manager.PopularTopLectureManager;
import com.iskcon.bvks.model.TopLectures;

import java.util.List;

public interface PopularLectureListener {
    void lectureUpdated(@NonNull List<TopLectures> topLecturesList, PopularTopLectureManager.RetrieveBy statsType);
    void onError(String msg);

}
