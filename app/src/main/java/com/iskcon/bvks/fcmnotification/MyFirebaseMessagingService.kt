package com.iskcon.bvks.fcmnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iskcon.bvks.R
import com.iskcon.bvks.ui.main.MainActivity
import java.util.*


/**
 * @AUTHOR Amandeep Singh
 * @date 04/02/2021
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {
            sendNotification(remoteMessage)
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {

    }


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     */
    private fun sendNotification(
            remoteMessage: RemoteMessage
    ) {
        val messageBody: String? = remoteMessage.data["message"]
        val title: String? = remoteMessage.data["title"]
        val fromNotification: String? = remoteMessage.data["fromNotification"]

        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("fromNotification", fromNotification)
        val pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, intent,
//            PendingIntent.FLAG_UPDATE_CURRENT
                PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_small_v2)
                .setContentTitle(title)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* if (notificationManager != null) {
                 val channelList: List<NotificationChannel> =
                     notificationManager.notificationChannels
                 var i = 0
                 while (channelList != null && i < channelList.size) {
                     notificationManager.deleteNotificationChannel(channelList[i].id)
                     i++
                 }
             }*/
            val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            // Configure the notification channel.
            channel.description = title
            channel.lightColor = Color.GRAY
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.setSound(defaultSoundUri, attributes) // This is IMPORTANT
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(getNotificationId() /* ID of notification */, notificationBuilder.build())
    }

    private fun getNotificationId():Int{
        val rd =  Random()
        val id=rd.nextInt()
        Log.d("LectureListFragment", "notification id--$id")
        return id
    }
}