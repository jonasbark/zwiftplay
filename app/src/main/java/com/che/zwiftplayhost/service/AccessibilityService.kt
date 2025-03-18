package com.che.zwiftplayhost.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import com.che.zap.utils.Logger


class AccessibilityService : AccessibilityService() {

    private var currentPackageName: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isMinusPressed = false
    private var isPlusPressed = false

    private val gearDecreaseRunnable = object : Runnable {
        override fun run() {
            if (isMinusPressed) {
                performGearDecrease()
                handler.postDelayed(this, 250)
            }
        }
    }

    private val gearIncreaseRunnable = object : Runnable {
        override fun run() {
            if (isPlusPressed) {
                performGearIncrease()
                handler.postDelayed(this, 250)
            }
        }
    }

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
                "Minus=Pressed " -> {
                    if (!isMinusPressed) {
                        isMinusPressed = true
                        handler.post(gearDecreaseRunnable) // Start repeating task
                    }
                }
                "Minus=Released " -> {
                    isMinusPressed = false
                    handler.removeCallbacks(gearDecreaseRunnable) // Stop repeating task
                }
                "Plus=Pressed " -> {
                    if (!isPlusPressed) {
                        isPlusPressed = true
                        handler.post(gearIncreaseRunnable) // Start repeating task
                    }
                }
                "Plus=Released " -> {
                    isPlusPressed = false
                    handler.removeCallbacks(gearIncreaseRunnable) // Stop repeating task
                }
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

        Logger.d("ACTION: Increasing gear")

        when (currentPackageName) {
            MYWHOOSH_APP_PACKAGE -> simulateTap(windowSize.right * 0.98, windowSize.bottom * 0.94)
            TRAININGPEAKS_APP_PACKAGE -> simulateTap(windowSize.centerX() * 1.32, windowSize.bottom * 0.74)
        }
    }

    private fun performGearDecrease() {
        val windowSize = getWindowSize()

        Logger.d("ACTION: Decreasing gear")

        when (currentPackageName) {
            MYWHOOSH_APP_PACKAGE -> simulateTap(windowSize.right * 0.80, windowSize.bottom * 0.94)
            TRAININGPEAKS_APP_PACKAGE -> simulateTap(windowSize.centerX() * 1.15, windowSize.bottom * 0.74)
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
