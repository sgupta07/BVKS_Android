package com.iskcon.bvks.ui.favorites;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iskcon.bvks.manager.LectureManager;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.util.PrefUtil;

import java.util.ArrayList;
import java.util.List;

public class FavoriteViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> isVideo;
    private MutableLiveData<List<Lecture>> lectureList;

    public FavoriteViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<Lecture>> getLectureList() {
        if (lectureList == null) {
            lectureList = new MutableLiveData<>();
        }
        return lectureList;
    }
    public MutableLiveData<Boolean> getIsVideo() {
        if (isVideo == null) {
            isVideo = new MutableLiveData<>();
        }
        return isVideo;
    }

}
