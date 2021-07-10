package com.iskcon.bvks.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.offline.Download
import com.iskcon.bvks.R

/** Helper for creating download notifications.  */
class CustomDownloadNotificationHelper(context: Context, channelId: String?) {
    private val context: Context
    private val notificationBuilder: NotificationCompat.Builder

    /**
     * Returns a progress notification for the given downloads.
     *
     * @param smallIcon A small icon for the notification.
     * @param contentIntent An optional content intent to send when the notification is clicked.
     * @param message An optional message to display on the notification.
     * @param downloads The downloads.
     * @return The notification.
     */
    fun buildProgressNotification(
            @DrawableRes smallIcon: Int,
            contentIntent: PendingIntent?,
            message: String?,
            downloads: List<Download>): Notification {
        var totalPercentage = 0f
        var downloadTaskCount = 0
        var allDownloadPercentagesUnknown = true
        var haveDownloadedBytes = false
        var haveDownloadTasks = false
        var haveRemoveTasks = false
        for (i in downloads.indices) {
            val download = downloads[i]
            if (download.state == Download.STATE_REMOVING) {
                haveRemoveTasks = true
                continue
            }
            if (download.state != Download.STATE_RESTARTING
                    && download.state != Download.STATE_DOWNLOADING) {
                continue
            }
            haveDownloadTasks = true
            val downloadPercentage = download.percentDownloaded
            if (downloadPercentage != C.PERCENTAGE_UNSET.toFloat()) {
                allDownloadPercentagesUnknown = false
                totalPercentage += downloadPercentage
            }
            haveDownloadedBytes = haveDownloadedBytes or (download.bytesDownloaded > 0)
            downloadTaskCount++
        }
        val titleStringId = if (haveDownloadTasks) R.string.exo_download_downloading else if (haveRemoveTasks) R.string.exo_download_removing else NULL_STRING_ID
        var progress = 0
        var indeterminate = true
        if (haveDownloadTasks) {
            progress = (totalPercentage / downloadTaskCount).toInt()
            indeterminate = allDownloadPercentagesUnknown && haveDownloadedBytes
        }
        return buildNotification(
                smallIcon,
                contentIntent,
                message,
                titleStringId,  /* maxProgress= */
                100,
                progress,
                indeterminate,  /* ongoing= */
                true,  /* showWhen= */
                false)
    }

    /**
     * Returns a notification for a completed download.
     *
     * @param smallIcon A small icon for the notifications.
     * @param contentIntent An optional content intent to send when the notification is clicked.
     * @param message An optional message to display on the notification.
     * @return The notification.
     */
    fun buildDownloadCompletedNotification(
            @DrawableRes smallIcon: Int, contentIntent: PendingIntent?, message: String?): Notification {
        val titleStringId: Int = R.string.exo_download_completed
        return buildEndStateNotification(smallIcon, contentIntent, message, titleStringId)
    }

    /**
     * Returns a notification for a failed download.
     *
     * @param smallIcon A small icon for the notifications.
     * @param contentIntent An optional content intent to send when the notification is clicked.
     * @param message An optional message to display on the notification.
     * @return The notification.
     */
    fun buildDownloadFailedNotification(
            @DrawableRes smallIcon: Int, contentIntent: PendingIntent?, message: String?): Notification {
        @StringRes val titleStringId: Int = R.string.exo_download_failed
        return buildEndStateNotification(smallIcon, contentIntent, message, titleStringId)
    }

    private fun buildEndStateNotification(
            @DrawableRes smallIcon: Int,
            contentIntent: PendingIntent?,
            message: String?,
            @StringRes titleStringId: Int): Notification {
        return buildNotification(
                smallIcon,
                contentIntent,
                message,
                titleStringId,  /* maxProgress= */
                0,  /* currentProgress= */
                0,  /* indeterminateProgress= */
                false,  /* ongoing= */
                false,  /* showWhen= */
                true)
    }

    private fun buildNotification(
            @DrawableRes smallIcon: Int,
            contentIntent: PendingIntent?,
            message: String?,
            @StringRes titleStringId: Int,
            maxProgress: Int,
            currentProgress: Int,
            indeterminateProgress: Boolean,
            ongoing: Boolean,
            showWhen: Boolean): Notification {
        notificationBuilder.apply {
            setSmallIcon(smallIcon)
//            contentIntent?.let {
//                setContentIntent(contentIntent)
//            }
            setContentTitle(
                    if (titleStringId == NULL_STRING_ID) null else context.resources.getString(titleStringId))
            setStyle(
                    if (message == null) null else NotificationCompat.BigTextStyle().bigText(message))
            setProgress(maxProgress, currentProgress, indeterminateProgress)
            setOngoing(ongoing)
            setShowWhen(showWhen)
        }
        return notificationBuilder.build()
    }

    companion object {
        @StringRes
        private val NULL_STRING_ID = 0
    }

    /**
     * @param context A context.
     * @param channelId The id of the notification channel to use.
     */
    init {
        var context = context
        context = context.applicationContext
        this.context = context
        notificationBuilder = NotificationCompat.Builder(context, channelId!!)
    }
}