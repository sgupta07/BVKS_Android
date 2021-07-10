package com.iskcon.bvks.manager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.iskcon.bvks.listeners.NotificationSettingsListener;
import com.iskcon.bvks.model.SettingsModel;

/**
 * @AUTHOR Amandeep Singh
 * @date 02/02/2021
 */
public class SettingsManager {

    private static final String TAG = SettingsManager.class.getSimpleName();
    private static SettingsManager sInstance;
    private Context mContext;

    private SettingsManager(@NonNull Context context) {
        mContext = context;
    }

    public static SettingsManager getInstance() {
        return sInstance;
    }

    public static void createInstance(@NonNull Context context) {
        sInstance = new SettingsManager(context);
    }

    public void saveNotificationSettings(SettingsModel settings) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Settings/userSettings")
                .set(settings, SetOptions.merge()).addOnSuccessListener(aVoid -> Log.d(TAG, "Notification settings successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing notification settings", e));
        ;
    }

    public void getNotificationSettings(@NonNull NotificationSettingsListener listener) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Settings/userSettings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            SettingsModel setting = document.toObject(SettingsModel.class);
                            listener.onLoadSettingsComplete(setting);

                        } else {
                            listener.onDocumentDoesNotExist();
                        }
                    } else {
                        listener.onLoadSettingsFailed();
                        Log.d(TAG, "Error getting documents: ", task.getException());

                    }
                });
    }


}
