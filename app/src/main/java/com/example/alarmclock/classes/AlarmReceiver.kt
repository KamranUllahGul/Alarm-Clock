package com.example.alarmclock.classes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.alarmclock.Activities.MainActivity
import com.example.alarmclock.R

class AlarmReceiver: BroadcastReceiver() {
    private lateinit var player: MediaPlayer
    companion object {
        var ringtone: Ringtone? = null
        private const val CHANNEL_ID = "alarm_channel"
        // Function to stop the ringtone
        fun stopRingtone() {
            ringtone?.stop()
            ringtone = null
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        context?.let {
            showNotification(it)  // 🔔 Show notification when alarm rings
            playRingtone(it)      // 🎵 Play alarm sound
        }

    }

    private fun showNotification(context: Context) {

        //create a channel for android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alarm Notification", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Open MainActivity when notification is clicked
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Intent to stop the alarm when the stop button is clicked
        val stopIntent = Intent(context, StopAlarmReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(context, 1, stopIntent, PendingIntent.FLAG_MUTABLE)

        // Intent to snooze the alarm when the snooze button is clicked
        val snoozeIntent = Intent(context, SnoozeAlarmReceiver::class.java)
        val snoozePendingIntent = PendingIntent.getBroadcast(context, 2, snoozeIntent, PendingIntent.FLAG_MUTABLE)

        // Build the Notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Alarm Ringing")
            .setContentText("Tap to stop or snooze")
            .setSmallIcon(R.drawable.ic_clock_ringing_notification_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)  // dismissed when tapped
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_baseline_cancel_24, "STOP", stopPendingIntent)
            .addAction(R.drawable.ic_baseline_snooze_24, "SNOOZE", snoozePendingIntent)

        // Show Notification
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notify(1, builder.build())  // id 1 for notification
            }
        }
    }

    private fun playRingtone(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {  // API level 28 (Pie) and above
            // Get the system default alarm sound
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(context, alarmUri)
                ringtone?.isLooping = true  // Enable looping
                ringtone?.play()
            } else {
                if (!ringtone!!.isPlaying) {
                    ringtone?.isLooping = true  // Enable looping
                    ringtone?.play()
                }
            }
        } else {  // For devices below API level 28 (Pie)
            // Use MediaPlayer for devices below API 28 to play and loop the alarm sound
            if (player == null) {
                val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                player = MediaPlayer.create(context, alarmUri)
                player?.isLooping = true  // Enable looping
                player?.start()
            } else {
                if (!player!!.isPlaying) {
                    player?.start()  // Start playing if not already playing
                }
            }
        }
    }

    // Function to stop the ringtone
    fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }
    }