package com.iskcon.bvks.model;

import com.google.firebase.firestore.IgnoreExtraProperties;


@IgnoreExtraProperties
public class SettingsModel {

    public long lastModificationTime;
    public NotificationSettings notification;

    public SettingsModel(long lastModificationTime, NotificationSettings notification) {
        this.lastModificationTime = lastModificationTime;
        this.notification = notification;
    }

    public SettingsModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public static class NotificationSettings {
        public boolean hindi;
        public boolean english;
        public boolean bengali;

        public NotificationSettings(boolean hindi, boolean english, boolean bengali)
        {
            this.hindi = hindi;
            this.english = english;
            this.bengali = bengali;
        }



        public NotificationSettings()
        {

            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }


    }
}

