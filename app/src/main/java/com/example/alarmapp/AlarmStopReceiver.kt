package com.example.alarmapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmStopReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, AlarmService::class.java)
        context.stopService(serviceIntent)
    }
}