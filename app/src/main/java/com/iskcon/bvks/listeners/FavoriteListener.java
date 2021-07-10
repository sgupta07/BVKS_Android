package com.iskcon.bvks.listeners;

import androidx.annotation.NonNull;

import com.iskcon.bvks.model.Lecture;

import java.util.List;

public interface FavoriteListener {
    void favoritesUpdated(@NonNull List<Lecture> lectureList);
}
