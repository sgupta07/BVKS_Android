package com.iskcon.bvks.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.iskcon.bvks.listeners.PopularLectureListener;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.model.ListenRecord;
import com.iskcon.bvks.model.TopLectures;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class PopularTopLectureManager {

    private static final String TAG = PopularTopLectureManager.class.getSimpleName();
    private static PopularTopLectureManager sInstance;
    private Context mContext;


    private PopularTopLectureManager(@NonNull Context context) {
        mContext = context;

    }

    public static PopularTopLectureManager getInstance() {
        return sInstance;
    }

    public static void createInstance(@NonNull Context context) {
        sInstance = new PopularTopLectureManager(context);
    }

    public void updateTopLectureDetail(Lecture lecture) {
        if (lecture == null) {
            return;
        }
        long lectureId = lecture.id;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null || userId.isEmpty()) {
            return;
        }
//        Log.i(TAG,"UserID-"+userId);
        GregorianCalendar calendar = new GregorianCalendar();
        String docpath = calendar.get(Calendar.DATE) + "-" +
                (calendar.get(Calendar.MONTH) + 1) + "-" +
                calendar.get(Calendar.YEAR);
        DocumentReference doc = FirebaseFirestore.getInstance().collection("TopLectures").document(docpath);

        doc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                //EXIST(UPDATE OLD NODE DATA)
                Log.i(TAG, "Document exist");
                TopLectures dataModal = documentSnapshot.toObject(TopLectures.class);
                dataModal.lastModifiedTimestamp = System.currentTimeMillis();
                dataModal.audioPlayedTime = 0;
                dataModal.videoPlayedTime = 0;
                dataModal.playedIds.add(lectureId);
                if (!dataModal.playedBy.contains(userId)){
                    dataModal.playedBy.add(userId);
                }
                doc.set(dataModal, SetOptions.merge());
            } else {
                //NOT EXIST(CREATE NEW NODE)
                Log.i(TAG, "Document does not exist");
                GregorianCalendar calendarNew = new GregorianCalendar();
                //create day model
                TopLectures.Day day = new TopLectures.Day();
                day.day = calendarNew.get(Calendar.DATE);
                day.month = calendarNew.get(Calendar.MONTH) + 1;
                day.year = calendarNew.get(Calendar.YEAR);

                //create list of played ids
                List<Long> playedIds = new ArrayList<>();
                playedIds.add(lectureId);
                //create list of played by
                List<String> playedBy = new ArrayList<>();
                playedBy.add(userId);
                //create listen record model
                TopLectures listenRecord = new TopLectures();
                listenRecord.documentId = docpath;
                listenRecord.documentPath = doc.getPath();
                listenRecord.creationTimestamp = System.currentTimeMillis();
                listenRecord.lastModifiedTimestamp = System.currentTimeMillis();
                listenRecord.audioPlayedTime = 0;
                listenRecord.videoPlayedTime = 0;
                listenRecord.playedBy = playedBy;
                listenRecord.playedIds = playedIds;
                listenRecord.createdDay = day;
                //set data to firestore
                doc.set(listenRecord, SetOptions.merge());

            }

        }).addOnFailureListener(e -> {

        });
    }

    public void getPopularTopLecture(RetrieveBy statsType, PopularLectureListener listener) {

        CollectionReference colRef = FirebaseFirestore.getInstance().collection("TopLectures");

        Query query;
        switch (statsType) {
            case day: {
                GregorianCalendar calendar = new GregorianCalendar();
                String docPath = calendar.get(Calendar.DATE) + "-" +
                        (calendar.get(Calendar.MONTH) + 1) + "-" +
                        calendar.get(Calendar.YEAR);
                query = colRef.whereEqualTo("documentId", docPath);
                break;
            }
            case week: {
                query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING).limit(7);
                break;
            }
            case month: {
                query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING).limit(30);
                break;
            }
            case year: {
                query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING).limit(365);
                break;
            }

            case all: {
                query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING);
                break;
            }
            case thisWeek: {
                GregorianCalendar date = new GregorianCalendar();
                int daysInThisWeek = date.get(Calendar.DAY_OF_WEEK);
                query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING).limit(daysInThisWeek);
                break;
            }
            case lastWeek: {
                GregorianCalendar date = new GregorianCalendar();
                int daysInThisWeek = date.get(Calendar.DAY_OF_WEEK);
                query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING).limit(daysInThisWeek + 7);
                break;
            }
            case lastMonth: {
                Date date = new Date();
                int month = date.getDay();
                query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING).limit(month + 30);
                break;
            }
          /*  case custom: {
                query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING)
                        .whereGreaterThanOrEqualTo("creationTimestamp", startDate)
                        .whereLessThanOrEqualTo("creationTimestamp", endDate);
                break;

            }*/

            default:
                throw new IllegalStateException("Unexpected value: " + statsType);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<TopLectures> topLectures = new ArrayList<>();
            if (queryDocumentSnapshots != null || !queryDocumentSnapshots.getDocumentChanges().isEmpty()) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    TopLectures lectures = document.toObject(TopLectures.class);
                    topLectures.add(lectures);
                }
            }
            listener.lectureUpdated(topLectures, statsType);
        }).addOnFailureListener(e -> {
            listener.onError(e.getMessage());
        });
    }

    public enum RetrieveBy {
        day, week, month, all, year, custom, thisWeek, lastWeek, lastMonth,
    }
}
