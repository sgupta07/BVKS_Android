package com.iskcon.bvks.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.iskcon.bvks.listeners.StatsListener;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.model.ListenRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

public class StatsManager {

    private static final String TAG = StatsManager.class.getSimpleName();
    private static StatsManager sInstance;
    private Context mContext;
    private Map<String, ListenRecord> mStatsMap;

    private StatsManager(@NonNull Context context) {
        mContext = context;
    }

    public static StatsManager getInstance() {
        return sInstance;
    }

    public static void createInstance(@NonNull Context context) {
        sInstance = new StatsManager(context);
    }

    public void updateUserListenDetail(int progressValue, Lecture lecture,boolean isVideo) {
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
        DocumentReference doc = FirebaseFirestore.getInstance().collection("users").document(userId + "/listenInfo/" + docpath);

        doc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                //EXIST(UPDATE OLD NODE DATA)
                Log.i(TAG, "Document exist");
                ListenRecord dataModal = documentSnapshot.toObject(ListenRecord.class);
                dataModal.lastModifiedTimestamp = System.currentTimeMillis();
                if (isVideo) {
                    //VIDEO
                    dataModal.videoListen = progressValue + dataModal.videoListen;
                } else {
                    //AUDIO
                    dataModal.audioListen = progressValue + dataModal.audioListen;
                }
                if (!dataModal.playedIds.contains(lectureId)) {
                    dataModal.playedIds.add(lectureId);
                }
                if (dataModal.listenDetails != null) {
                    if (lecture.searchList != null && lecture.searchList.size() > 0 && lecture.searchList.get(0).contains("Bg")) {
                        dataModal.listenDetails.BG = progressValue + dataModal.listenDetails.BG;
                    } else if (lecture.searchList != null && lecture.searchList.size() > 0 && lecture.searchList.get(0).contains("SB")) {
                        dataModal.listenDetails.SB = progressValue + dataModal.listenDetails.SB;
                    } else if (lecture.searchList != null && lecture.searchList.size() > 0 && lecture.searchList.get(0).contains("VSN")) {
                        dataModal.listenDetails.VSN = progressValue + dataModal.listenDetails.VSN;
                    } else if (lecture.searchList != null && lecture.searchList.size() > 0 && lecture.searchList.get(0).contains("cc")) {
                        dataModal.listenDetails.CC = progressValue + dataModal.listenDetails.CC;
                    } else if (lecture.searchList != null && lecture.searchList.size() > 0 && lecture.searchList.get(0).contains("Seminars")) {
                        dataModal.listenDetails.Seminars = progressValue + dataModal.listenDetails.Seminars;
                    } else {
                        dataModal.listenDetails.others = progressValue + dataModal.listenDetails.others;
                    }
                }
                doc.set(dataModal, SetOptions.merge());
            } else {
                //NOT EXIST(CREATE NEW NODE)
                Log.i(TAG, "Document does not exist");
                GregorianCalendar calendarNew = new GregorianCalendar();
                //create date model
                ListenRecord.DateRecord dateRecord = new ListenRecord.DateRecord();
                dateRecord.day = calendarNew.get(Calendar.DATE);
                dateRecord.month = calendarNew.get(Calendar.MONTH) + 1;
                dateRecord.year = calendarNew.get(Calendar.YEAR);
                //create listen model
                ListenRecord.Listened listened = new ListenRecord.Listened();
                listened.BG = 0;
                listened.SB = 0;
                listened.VSN = 0;
                listened.CC = 0;
                listened.Seminars = 0;
                listened.others = 0;
                //create list of played ids
                List<Long> playedIds = new ArrayList<>();
                playedIds.add(lectureId);
                //create listen record model
                ListenRecord listenRecord = new ListenRecord();
                listenRecord.documentId = docpath;
                listenRecord.documentPath = doc.getPath();
                listenRecord.creationTimestamp = System.currentTimeMillis();
                listenRecord.lastModifiedTimestamp = System.currentTimeMillis();
                listenRecord.audioListen = 0;
                listenRecord.videoListen = 0;
                listenRecord.playedIds = playedIds;
                listenRecord.dateOfRecord = dateRecord;
                listenRecord.listenDetails = listened;
                //set data to firestore
                doc.set(listenRecord, SetOptions.merge());

            }

        }).addOnFailureListener(e -> {

        });
    }


    public void getUserListenDetails(StatsType statsType, @Nullable Long startDate, @Nullable Long endDate, StatsListener statsListener) {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null || userId.isEmpty()) {
            return;
        }

        CollectionReference colRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                .collection("listenInfo");

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
            case custom: {
                /*query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING)
                        .whereGreaterThanOrEqualTo("creationTimestamp", startDate)
                        .whereLessThanOrEqualTo("creationTimestamp", endDate);*/
                query = colRef.orderBy("creationTimestamp", Query.Direction.DESCENDING);
                break;

            }

            default:
                throw new IllegalStateException("Unexpected value: " + statsType);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<ListenRecord> statsModelList = new ArrayList<>();
            if (queryDocumentSnapshots != null || !queryDocumentSnapshots.getDocumentChanges().isEmpty()) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    ListenRecord listenRecord = document.toObject(ListenRecord.class);
                    statsModelList.add(listenRecord);
                }
            }
            statsListener.statsUpdated(statsModelList, statsType);
        }).addOnFailureListener(e -> {
            statsListener.onError(e.getMessage());
        });
    }

    public enum StatsType {
        day, week, month, all, year, custom, thisWeek, lastWeek, lastMonth
    }


}
