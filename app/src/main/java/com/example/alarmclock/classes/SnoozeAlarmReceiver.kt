package com.example.alarmclock.classes

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.util.Calendar

class SnoozeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        Log.d("SnoozeAlarmReceiver", "Snooze button clicked")  // ✅ Log for debugging

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Snooze button clicked!", Toast.LENGTH_SHORT).show()
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Exact alarm permission not granted!", Toast.LENGTH_LONG).show()
                }
                return
            }
        }

        // Stop current alarm and delay to prevent it from immediately triggering again
        Log.d("SnoozeAlarmReceiver", "Stopping current alarm")
        AlarmReceiver.stopRingtone()

        // Add a small delay (e.g., 1 second) before rescheduling the alarm
        Handler(Looper.getMainLooper()).postDelayed({
            val snoozeTimeInMinutes = 1
            val calendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, snoozeTimeInMinutes)
            }

            Log.d("SnoozeAlarmReceiver", "Alarm snoozed until: ${calendar.time}")

            // Schedule the alarm again after snooze time
            val alarmIntent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            // Dismiss notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1)

        }, 1000)  // 1-second delay before rescheduling the alarm
    }
}
