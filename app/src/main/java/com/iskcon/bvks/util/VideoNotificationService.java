/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iskcon.bvks.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.TaskStackBuilder;

import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.util.Log;
import com.iskcon.bvks.R;
import com.iskcon.bvks.manager.VideoPlayerManagerV3;
import com.iskcon.bvks.manager.VideoPlayerManagerV4;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.ui.main.MainActivity;

import static com.iskcon.bvks.base.BvksApplication.CHANNEL_ID_VIDEO;
import static com.iskcon.bvks.base.BvksApplication.DEBUG;

public class VideoNotificationService extends Service implements PlayerNotificationManager.NotificationListener, PlaybackPreparer {

    private static final String TAG = VideoNotificationService.class.getSimpleName();
    private static final int NOTIFICATION_ID_VIDEO = 200;

    private PlayerNotificationManager mPlayerNotificationManager;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnect;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayerNotificationManager = new PlayerNotificationManager(this, CHANNEL_ID_VIDEO,
                NOTIFICATION_ID_VIDEO, new DescriptionAdapter(), this);
        mPlayerNotificationManager.setPlayer(VideoPlayerManagerV4.getSharedInstance(this).getPlayer());
        mPlayerNotificationManager.setPlaybackPreparer(this);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "MEDIA_SESSION_TAG_VIDEO");
        mediaSession.setActive(true);
        mPlayerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
        mediaSessionConnect = new MediaSessionConnector(mediaSession);
        mediaSessionConnect.setPlayer(VideoPlayerManagerV4.getSharedInstance(this).getPlayer(), null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
        if (ongoing) {
            if (DEBUG) {
                Log.d(TAG, "Start foreground notification");
            }
            startForeground(NOTIFICATION_ID_VIDEO, notification);
        } else {
            if (DEBUG) {
                Log.d(TAG, "Stop foreground notification");
            }
            stopForeground(false);
        }
    }

    @Override
    public void preparePlayback() {
        SimpleExoPlayer player = VideoPlayerManagerV4.getSharedInstance(this).getPlayer();
        if (player != null) {
            player.retry();
        }
    }

    private class DescriptionAdapter implements
            PlayerNotificationManager.MediaDescriptionAdapter {

        @Override
        public String getCurrentContentTitle(Player player) {
            Lecture lecture = VideoPlayerManagerV4.getSharedInstance(VideoNotificationService.this).getCurrentLecture();
            return lecture != null ? lecture.name : "";
        }

        @Nullable
        @Override
        public String getCurrentContentText(Player player) {
            Lecture lecture = VideoPlayerManagerV4.getSharedInstance(VideoNotificationService.this).getCurrentLecture();
            return lecture != null ? lecture.verse : "";
        }

        @Nullable
        @Override
        public String getCurrentSubText(Player player) {
            Lecture lecture = VideoPlayerManagerV4.getSharedInstance(VideoNotificationService.this).getCurrentLecture();
            return lecture != null ? lecture.place : "";
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player,
                                          PlayerNotificationManager.BitmapCallback callback) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                return Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            } else {
                return Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.maharaj_nav_icon));
            }
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            Intent intent = new Intent(VideoNotificationService.this, MainActivity.class);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(VideoNotificationService.this);
            stackBuilder.addNextIntentWithParentStack(intent);
            // Get the PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            return resultPendingIntent;
        }
    }
}
