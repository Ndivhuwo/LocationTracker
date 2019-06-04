package za.co.topcode.locationtracker.util

import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import za.co.topcode.locationtracker.Constants
import za.co.topcode.locationtracker.R

class NotificationHandler {

    companion object {
        val TAG: String = NotificationHandler::class.java.simpleName

        fun getNotification(context: Context, toActivity: Class<*>, title: String?,
                            subject: String?, message: String?, notificationSound: Boolean, pendingIntentFlag: Int,
                            @DrawableRes icon: Int): Notification? {
            Log.i(TAG, "Sending Notification")
            val intent = Intent(context, toActivity)

            val pendingIntent = PendingIntent.getActivity(context, Constants.REQUEST_CODE_NOTIFICATION_INTENT,
                intent, pendingIntentFlag)
            /*val pendingIntent = TaskStackBuilder.create(context).run {
                getPendingIntent(0, pendingIntentFlag)

            }*/

            pendingIntent?.let {
                Log.i(TAG, "Pending Intent present")
            }

            val trimmedTitle = Html.fromHtml(title).toString()
            val trimmedSubject = Html.fromHtml(subject).toString()
            val trimmedMessage = Html.fromHtml(message).toString()
            val notificationBuilder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID).apply {
                setContentText(trimmedSubject)
                setContentTitle(trimmedTitle)
                setStyle(NotificationCompat.BigTextStyle().bigText(trimmedMessage))
                setContentIntent(pendingIntent)
                //.addAction(android.R.drawable.arrow_down_float, "test", pendingIntent)
                setSmallIcon(icon)
                setAutoCancel(true)
                setChannelId(Constants.NOTIFICATION_CHANNEL_ID)
            }

            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                channel.description = message
                if (notificationSound) {
                    channel.setSound(defaultSoundUri, audioAttributes)
                    channel.enableLights(true)
                    channel.lightColor = Color.CYAN
                    //channel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                    channel.enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            } else {
                notificationBuilder.priority = NotificationCompat.PRIORITY_MAX
                if (notificationSound) {
                    notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND or Notification.FLAG_SHOW_LIGHTS)
                    notificationBuilder.setLights(context.resources.getColor(R.color.colorAccent), 300, 100)
                }
            }

            return notificationBuilder.build()
        }
    }

}