package com.iskcon.bvks.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.iskcon.bvks.firebase.FirestoreLecture;
import com.iskcon.bvks.listeners.LectureListener;
import com.iskcon.bvks.listeners.LectureLoadListener;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.model.LectureInformation;
import com.iskcon.bvks.util.Constants;
import com.iskcon.bvks.util.PrefUtil;
import com.iskcon.bvks.util.Utils;
import com.iskcon.bvks.util.urlextractor.VideoMeta;
import com.iskcon.bvks.util.urlextractor.YouTubeExtractor;
import com.iskcon.bvks.util.urlextractor.YtFile;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class LectureManager {

    private static final String TAG = LectureManager.class.getSimpleName();
    private static LectureManager sInstance;
    private Context mContext;
    private List<LectureListener> mListeners;
    private Map<Long, Lecture> mLecturesMap;
    private HashMap<String, String> mVideoLinks;

    private LectureManager(@NonNull Context context) {
        mContext = context;
        mListeners = new ArrayList<>();
        mLecturesMap = PrefUtil.getLectureHashMapFromPrefs(mContext, PrefUtil.PREF_KEY_CACHED_LECTURES, new HashMap<>());
        mVideoLinks = (HashMap) PrefUtil.getFromPrefs(mContext, PrefUtil.VIDEO_LINKS_PREF, new HashMap<>());
        if (!mLecturesMap.isEmpty()) {
            // Lectures already loaded, so add snapshot listener to it
            addSnapShotListener();
        }
    }

    public static LectureManager getInstance() {
        return sInstance;
    }

    public static void createInstance(@NonNull Context context) {
        sInstance = new LectureManager(context);
    }

    public void addListener(@NonNull LectureListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(@NonNull LectureListener listener) {
        mListeners.remove(listener);
    }

    public Lecture getLecture(long id) {
        return mLecturesMap.get(id);
    }

    public List<Lecture> getLectures() {
        return new ArrayList<>(mLecturesMap.values());
    }

    public List<Lecture> getFavoriteLectures() {
        return new ArrayList<>(mLecturesMap.values())
                .stream()
                .filter(a -> {
                    if (a != null && a.information != null && a.information.isFavourite) {
                        return true;
                    } else {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public boolean lectureLoadComplete() {
        return !mLecturesMap.isEmpty();
    }

    public void forceLoadLectures(@Nullable LectureLoadListener listener) {
        FirebaseFirestore.getInstance().collection("lectures")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mLecturesMap.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            FirestoreLecture lecture = document.toObject(FirestoreLecture.class);
                            if (!TextUtils.isEmpty(lecture.getUrl())) {
                                mLecturesMap.put(lecture.id, lecture.getLecture());
                                if (lecture.getLecture().videoLink != null && !lecture.getLecture().videoLink.isEmpty()) {
                                   // createHLSUrl(lecture.getLecture());
                                }
                            }
                        }
                        PrefUtil.saveToPrefs(mContext, PrefUtil.PREF_KEY_CACHED_LECTURES, mLecturesMap);
                        if (listener != null) {
                            listener.onLoadLecturesComplete(new ArrayList<>(mLecturesMap.values()));
                        }
                        // Lectures already loaded, so add snapshot listener to it
                        addSnapShotListener();
                    } else {
                        if (listener != null) {
                            listener.onLoadLecturesFailed();
                        }
                    }
                });
    }

    public void forceLoadLecturesInformation() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getUid() != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("lectureInfo")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                LectureInformation lectureInformation = document.toObject(LectureInformation.class);
                                if (lectureInformation.id != 0) {
                                    Lecture lecture = mLecturesMap.get(lectureInformation.id);
                                    if (lecture != null) {
                                        lecture.information = lectureInformation;
                                        mLecturesMap.put(lectureInformation.id, lecture);

                                    }
                                }
                            }
                            if (mListeners != null) {
                                for (LectureListener listener : mListeners) {
                                    listener.lecturesUpdated(new ArrayList<>(mLecturesMap.values()),false);
                                    Log.d("LectureListFragment", "151");
                                }
                            }
                            addLectureInformationSnapShotListener();

                        } else {
                        }
                    });
        }
    }

    private void addSnapShotListener() {
        FirebaseFirestore.getInstance().collection("lectures")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }
                    mLecturesMap.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        FirestoreLecture lecture = doc.toObject(FirestoreLecture.class);
                        if (!TextUtils.isEmpty(lecture.getUrl())) {
                            mLecturesMap.put(lecture.id, lecture.getLecture());
                        }
                    }

                    PrefUtil.saveToPrefs(mContext, PrefUtil.PREF_KEY_CACHED_LECTURES, mLecturesMap);
                    if (mListeners != null) {
                        for (LectureListener listener : mListeners) {
                            listener.lecturesUpdated(new ArrayList<>(mLecturesMap.values()),false);
                            Log.d("LectureListFragment", "180");
                        }
                    }
                    forceLoadLecturesInformation();
                });
    }

    /**
     * Add lecture information snapshot listener for detect continues change
     */
    private void addLectureInformationSnapShotListener() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("lectureInfo")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }
                    for (QueryDocumentSnapshot doc : snapshots) {
                        LectureInformation lectureInformation = doc.toObject(LectureInformation.class);
                        if (lectureInformation.id != 0) {
                            Lecture lecture = mLecturesMap.get(lectureInformation.id);
                            if (lecture != null) {
                                lecture.information = lectureInformation;
                                mLecturesMap.put(lectureInformation.id, lecture);


                            }
                        }

                    }

                    if (mListeners != null) {
                        for (LectureListener listener : mListeners) {
                            listener.lecturesUpdated(new ArrayList<>(mLecturesMap.values()),false);
                            Log.d("LectureListFragment", "213");
                        }
                    }
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mListeners != null) {
                                for (LectureListener listener : mListeners) {
                                    listener.lecturesUpdated(new ArrayList<>(mLecturesMap.values()),true);
                                    Log.d("LectureListFragment", "223");
                                }
                            }
                        }
                    }, 5000);
                });
    }

    /**
     * Mark unmark lecture as favorite
     *
     * @param lecture    lecture object
     * @param isFavorite true means mark lecture as favorite false means remove lecture from favorite
     */
    public void markUnMarkFavorite(Lecture lecture, Boolean isFavorite) {
        LectureInformation lectureInformation = new LectureInformation();
        if (lecture.information != null && lecture.information.id > 0) {
            //OLD ENTRY
            lectureInformation = lecture.information;
            lectureInformation.isFavourite = isFavorite;
            setLectureInfo(lectureInformation, false);
        } else {
            //NEW ENTRY
            lectureInformation.isFavourite = isFavorite;
            lectureInformation.id = lecture.id;
            setLectureInfo(lectureInformation, true);
        }
    }

    /**
     * Mark unmark progress of lecture as completed
     *
     * @param lecture    lecture object
     * @param isComplete true means lecture progress is completed false means reset to zero
     */
    public void markUnMarkComplete(Lecture lecture, Boolean isComplete) {
        LectureInformation lectureInformation = new LectureInformation();
        if (lecture.information != null && lecture.information.id > 0) {
            //OLD ENTRY
            lectureInformation = lecture.information;
            if (isComplete) {
                //COMPLETE
                lectureInformation.lastPlayedPoint = lectureInformation.totallength;
            } else {
                // RESET
                lectureInformation.lastPlayedPoint = 0;
            }
            lectureInformation.isCompleted = isComplete;
            setLectureInfo(lectureInformation, false);
        } else {
            //NEW ENTRY
            if (isComplete) {
                //COMPLETE
                lectureInformation.lastPlayedPoint = lecture.mediaLength;
            } else {
                // RESET
                lectureInformation.lastPlayedPoint = 0;
            }
            lectureInformation.isCompleted = isComplete;
            lectureInformation.id = lecture.id;
            setLectureInfo(lectureInformation, true);
        }
    }

    public void updateCurrentPlayPoint(Lecture lecture, long position) {
        LectureInformation lectureInformation = new LectureInformation();
        if (lecture.information != null && lecture.information.id > 0) {
            //OLD ENTRY
            lectureInformation = lecture.information;
            lectureInformation.lastModifiedTimestamp = System.currentTimeMillis();
            lectureInformation.lastPlayedPoint = position;
            lectureInformation.totallength = lecture.mediaLength;
            setLectureInfo(lectureInformation, false);
        } else {
            //NEW ENTRY
            lectureInformation.id = lecture.id;
            lectureInformation.totallength = lecture.mediaLength;
            lectureInformation.lastPlayedPoint = position;
            setLectureInfo(lectureInformation, true);
        }
    }

    /**
     * Set lecture information in Firestore db
     *
     * @param lectureInformation lecture information object
     * @param isNew              set value based on if information object is new or old
     */
    private void setLectureInfo(LectureInformation lectureInformation, boolean isNew) {
        CollectionReference docRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("lectureInfo");
        if (isNew) {
            //NEW ENTRY
            lectureInformation.creationTimestamp = System.currentTimeMillis();
            lectureInformation.lastModifiedTimestamp = System.currentTimeMillis();
            lectureInformation.documentId = docRef.document().getId();
            lectureInformation.documentPath = "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/lectureInfo/" + lectureInformation.documentId;
            FirebaseFirestore.getInstance().document(lectureInformation.documentPath).set(lectureInformation, SetOptions.merge());
        } else {
            //OLD ENTRY
            lectureInformation.lastModifiedTimestamp = System.currentTimeMillis();
            FirebaseFirestore.getInstance().document(lectureInformation.documentPath).set(lectureInformation, SetOptions.merge());

        }
    }


    public int getTotalLectureCount() {
        return new ArrayList<>(mLecturesMap.values()).size();
    }

    public int getTotalHeardLectures() {
        return new ArrayList<>(mLecturesMap.values())
                .stream()
                .filter(a -> {
                    if (a != null && a.information != null && a.information.isCompleted) {
                        return true;
                    } else {
                        return false;
                    }
                })
                .collect(Collectors.toList()).size();
    }

    public void createHLSUrl(Lecture lecture) {
        mVideoLinks = (HashMap) PrefUtil.getFromPrefs(mContext, PrefUtil.VIDEO_LINKS_PREF, new HashMap<>());
        @SuppressLint("StaticFieldLeak") YouTubeExtractor mExtractor = new YouTubeExtractor(mContext) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
               if (sparseArray != null) {
                   if(sparseArray.get(22)==null){
                       return;
                   }
                    String urlHLS = sparseArray.get(22).getUrl();
//                    Log.i(TAG, "URL HLS-" + urlHLS);
//                    Log.i(TAG, "VIDEO ID-" + videoMeta.getVideoId());
                    mVideoLinks.put(videoMeta.getVideoId(), urlHLS);
                    PrefUtil.saveToPrefs(mContext, PrefUtil.VIDEO_LINKS_PREF, mVideoLinks);

                } else {
                   // Utils.getInstance().showToast(mContext, "ErrorX02-SAN");
//                    Log.i(TAG, "sparseArray Null");
                }
            }
        };
        String youTubeLink = Constants.YOU_TUBE_BASE_URL + "/watch?v=" + getYoutubeVideoId(lecture.videoLink);

//        Log.e("youtubeLink",youTubeLink);
//        Log.e("youtubeLink2",lecture.videoLink);
        mExtractor.extract(youTubeLink, true, true);
    }

    /*private String getYoutubeVideoId(@NotNull String videoLink) {
        Log.i(TAG, "videoLink---->" + videoLink);
        String lastBit;
        if (videoLink.contains("watch")) {
            lastBit = videoLink.substring(videoLink.lastIndexOf('='));
            lastBit = lastBit.replace("=", "");
        } else {
            lastBit = videoLink.substring(videoLink.lastIndexOf('/'));
            lastBit = lastBit.replace("/", "");
        }
        Log.i(TAG, "videoID---->" + lastBit);
        return lastBit;
    }*/

    private String getYoutubeVideoId(@NotNull String videoLink) {
//        Log.i(TAG, "videoLink---->" + videoLink);
        String lastBit;
        if (videoLink.contains("&")){
            String result = videoLink.split("&")[0];
            if (result.contains("watch")) {
                lastBit = result.substring(result.lastIndexOf('='));
                lastBit = lastBit.replace("=", "");
            } else {
                lastBit = result.substring(result.lastIndexOf('/'));
                lastBit = lastBit.replace("/", "");
            }
        }else {
            if (videoLink.contains("watch")) {
                lastBit = videoLink.substring(videoLink.lastIndexOf('='));
                lastBit = lastBit.replace("=", "");
            } else {
                lastBit = videoLink.substring(videoLink.lastIndexOf('/'));
                lastBit = lastBit.replace("/", "");
            }
        }
//        Log.i(TAG, "videoID---->" + lastBit);
        return lastBit;
    }
}
