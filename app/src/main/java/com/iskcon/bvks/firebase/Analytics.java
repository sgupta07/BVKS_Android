package com.iskcon.bvks.firebase;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.iskcon.bvks.model.Lecture;

import static com.iskcon.bvks.base.BvksApplication.DEBUG;

public class Analytics {

    private static Context sContext;

    private Analytics() {
    }

    public static void init(@NonNull final Context context) {
        sContext = context;
    }

    protected static void logEvent(@NonNull final String eventName, @NonNull final Bundle params) {
        if (DEBUG) {
            // Do not log analytics for debug builds. We do not want unnecessary costs.
            Log.d("Analytics", eventName + ": " + params);
            return;
        }
        FirebaseAnalytics.getInstance(sContext).logEvent(eventName, params);
    }

    public static final class LectureAnalytics {
        public static void lecturePlayed(@NonNull Lecture lecture) {
            final Bundle params = new Bundle();
            params.putLong(FirebaseAnalytics.Param.ITEM_ID, lecture.id);
            params.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, lecture.name);
            logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
        }
    }

    public static final class DownloadAnalytics {
        public static void lectureDownload(@NonNull Lecture lecture) {
            final Bundle params = new Bundle();
            params.putLong(FirebaseAnalytics.Param.ITEM_ID, lecture.id);
            params.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, lecture.name);
            logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
        }
    }

    public static final class FavoriteAnalytics {
        public static void lectureFavoriteMark(@NonNull Lecture lecture) {
            final Bundle params = new Bundle();
            params.putLong(FirebaseAnalytics.Param.ITEM_ID, lecture.id);
            params.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, lecture.name);
            logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
        }

        public static void lectureFavoriteUnmark(@NonNull Lecture lecture) {
            final Bundle params = new Bundle();
            params.putLong(FirebaseAnalytics.Param.ITEM_ID, lecture.id);
            params.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, lecture.name);
            logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
        }
    }

    public static class FilterAnalytics {
        public static void lectureFavorite(@NonNull Lecture lecture) {
            final Bundle params = new Bundle();
            params.putLong(FirebaseAnalytics.Param.ITEM_ID, lecture.id);
            params.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, lecture.name);
            logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
        }
    }
}
