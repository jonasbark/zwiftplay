package com.che.zwiftplayhost.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Process
import kotlin.system.exitProcess

class StopAppReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("STOP_APP" == intent.action) {
            // Stop the app
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }
}
