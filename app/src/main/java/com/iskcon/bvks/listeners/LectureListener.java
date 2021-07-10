package com.iskcon.bvks.listeners;

import androidx.annotation.NonNull;

import com.iskcon.bvks.model.Lecture;

import java.util.List;

public interface LectureListener {
    void lecturesUpdated(@NonNull List<Lecture> lectureList,boolean isLoaderHide);
}
