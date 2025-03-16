package com.che.zwiftplayhost.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import com.che.zap.utils.Logger


class AccessibilityService : AccessibilityService() {

    private var currentPackageName: String? = null

    override fun onCreate() {
        super.onCreate()
        Logger.registerListener(loggerListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.unregisterListener(loggerListener)
    }

    private val loggerListener = object : Logger.LogCallback {
        override fun newLogLine(line: String) {
            when (line) {
                "Minus=Pressed " -> performGearDecrease()
                "Plus=Pressed " -> performGearIncrease()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName == null) {
            return
        }
        currentPackageName = event.packageName.toString()
    }

    private fun getWindowSize(): Rect {
        val outBounds = Rect()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            rootInActiveWindow.getBoundsInWindow(outBounds)
        } else {
            rootInActiveWindow.getBoundsInScreen(outBounds)
        }
        return outBounds
    }

    private fun performGearIncrease() {
        val windowSize = getWindowSize()

        when (currentPackageName) {
            MYWHOOSH_APP_PACKAGE -> simulateTap(windowSize.right * 0.98, windowSize.bottom * 0.94)
            TRAININGPEAKS_APP_PACKAGE -> simulateTap(windowSize.right * 0.98, windowSize.bottom * 0.94)
        }
    }

    private fun performGearDecrease() {
        val windowSize = getWindowSize()

        when (currentPackageName) {
            MYWHOOSH_APP_PACKAGE -> simulateTap(windowSize.right * 0.80, windowSize.bottom * 0.94)
            TRAININGPEAKS_APP_PACKAGE -> simulateTap(windowSize.right * 0.80, windowSize.bottom * 0.94)
        }
    }


    private fun simulateTap(x: Double, y: Double) {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        path.lineTo(x.toFloat()+1, y.toFloat())

        val stroke = StrokeDescription(path, 0, ViewConfiguration.getTapTimeout().toLong())
        gestureBuilder.addStroke(stroke)

        dispatchGesture(gestureBuilder.build(), null, null)
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "Service Interrupted")
    }

    companion object {
        private const val MYWHOOSH_APP_PACKAGE = "com.mywhoosh.whooshgame"
        private const val TRAININGPEAKS_APP_PACKAGE = "com.indieVelo.client"
    }
}
