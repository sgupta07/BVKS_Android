package com.iskcon.bvks.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.iskcon.bvks.model.Lecture;

import java.util.List;

public class HomeViewModelV2 extends AndroidViewModel {

    private MutableLiveData<List<Lecture>> lectureList;
    private MutableLiveData<Boolean> isVideo;

    public HomeViewModelV2(@NonNull Application application) {
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
