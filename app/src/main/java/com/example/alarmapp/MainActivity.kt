package com.example.alarmapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.util.Calendar
import java.util.Locale
import java.util.jar.Manifest

class MainActivity : ComponentActivity() {

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    val NOTIFICATION_PERMISSION_REQUEST_CODE = 0
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
            requestExactAlarmPermission(this)
            setContent {
                AlarmApp()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun AlarmApp() {
        val context = LocalContext.current

        var time by remember { mutableStateOf(Calendar.getInstance()) }
        var isRepeat by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Chọn Giờ Báo Thức", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showTimePicker = true }) {
                Text("Chọn Giờ")
            }

            if (showTimePicker) {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        time.set(Calendar.HOUR_OF_DAY, hour)
                        time.set(Calendar.MINUTE, minute)
                        time.set(Calendar.SECOND, 0)
                        showTimePicker = false
                    },
                    time.get(Calendar.HOUR_OF_DAY),
                    time.get(Calendar.MINUTE),
                    true
                ).show()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Giờ đã chọn: ${String.format(Locale.getDefault(),"%02d:%02d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))}",
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isRepeat, onCheckedChange = { isRepeat = it })
                Text(text = "Lặp lại hàng ngày")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { setAlarm(context, time, isRepeat) }) {
                Text("Đặt Báo Thức")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun setAlarm(context: Context, calendar: Calendar, repeat: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (repeat) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        Toast.makeText(context, "Báo thức đã đặt!", Toast.LENGTH_SHORT).show()
    }
fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                "package:${context.packageName}".toUri())
            context.startActivity(intent)
        }
    }
}