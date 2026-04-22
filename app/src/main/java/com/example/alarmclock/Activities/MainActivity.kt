package com.example.alarmclock.Activities

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alarmclock.R
import com.example.alarmclock.classes.AlarmReceiver
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var selectTime: TextView
    private lateinit var setAlarm: Button
    private lateinit var cancelAlarm: Button
    private lateinit var stopAlarm: Button
    private  var calendar: Calendar?= null
    private lateinit var alarmManager: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        // Initialize AlarmManager once
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        selectTime.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select alarm time")
                .build()
            timePicker.show(supportFragmentManager, "alarm_picker")
            timePicker.addOnPositiveButtonClickListener {
                val hour = timePicker.hour
                val minute = timePicker.minute
                val formattedTime = when {
                    hour == 0 -> "12:${String.format("%02d", minute)} AM"
                    hour == 12 -> "12:${String.format("%02d", minute)} PM"
                    hour > 12 -> "${String.format("%02d", hour - 12)}:${String.format("%02d", minute)} PM"
                    else -> "${String.format("%02d", hour)}:${String.format("%02d", minute)} AM"
                }
                selectTime.text = formattedTime

                // ✅ Assign calendar to class variable
                calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
        }
        setAlarm.setOnClickListener {
            //requestRunTimePermission()
            if (calendar == null) {
                Toast.makeText(this, "Please select a time first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12+ (API 31+)
                if (!alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Permission needed for exact alarms", Toast.LENGTH_SHORT).show()
                    requestRunTimePermission()
                    requestExactAlarmPermission()  // Ask for permission
                    return@setOnClickListener
                }else{ settingAlarm()
                }
            }else{
                settingAlarm()
            }
        }
        stopAlarm.setOnClickListener {
            AlarmReceiver.stopRingtone()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager
            notificationManager.cancel(1)
            Toast.makeText(this, "Alarm Stopped", Toast.LENGTH_SHORT).show()

        }

        cancelAlarm.setOnClickListener {
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            alarmManager.cancel(pendingIntent)
            Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        selectTime = findViewById(R.id.select_Time)
        setAlarm = findViewById(R.id.set_Alarm)
        cancelAlarm = findViewById(R.id.cancel_Alarm)
        stopAlarm = findViewById(R.id.stop_Alarm)
    }
    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13+
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
    }

    private fun requestRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied!", Toast.LENGTH_SHORT).show()
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    // If the user permanently denies permission, take them to settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
        }
    }
private fun settingAlarm(){
    // Log the alarm time to verify it is correct
    Log.d("AlarmTime", "Setting alarm for: ${calendar!!.timeInMillis} (${calendar!!.time})")

    // Create the intent to trigger AlarmReceiver
    val intent = Intent(this, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


    // Set the alarm depending on the version
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Android 7+ (API 23+)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar!!.timeInMillis,
            pendingIntent
        )
    } else {  // For older Android versions (below 6.0)
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar!!.timeInMillis,
            pendingIntent
        )
    }

    Toast.makeText(this, "Alarm Set ${selectTime.text}", Toast.LENGTH_SHORT).show()
}

}


