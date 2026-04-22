package com.example.alarmclock.classes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null){
            AlarmReceiver.stopRingtone()

            //dismiss the notification
            val notificationManager =NotificationManagerCompat.from(context)
        notificationManager.cancel(1)// Ensure this ID matches the one in the AlarmReceiver
            Toast.makeText(context, "Alarm Stopped", Toast.LENGTH_SHORT).show()
        }


    }
}