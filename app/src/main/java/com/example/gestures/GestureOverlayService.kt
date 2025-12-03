package com.example.gestures

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.gesture.Gesture
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.GestureOverlayView
import android.gesture.Prediction
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.example.gestures.data.MappingRepository

class GestureOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: GestureOverlayView? = null
    private var lib: GestureLibrary? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        loadGestureLibrary()
        createOverlay()
        startForegroundIfNeeded()
    }

    private fun loadGestureLibrary() {
        val file = java.io.File(filesDir, "gestures/gestures")
        if (file.exists()) {
            lib = GestureLibraries.fromFile(file.path)
            lib?.load()
        } else {
            lib = null
        }
    }

    private fun createOverlay() {
        overlayView = GestureOverlayView(this)
        overlayView?.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE)
        overlayView?.isGestureVisible = false
        overlayView?.setGestureStrokeWidth(8f)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        overlayView?.addOnGesturePerformedListener { _, gesture ->
            handleGesture(gesture)
        }

        val container = FrameLayout(this)
        container.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        container.addView(overlayView)
        try {
            windowManager.addView(container, params)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo añadir overlay: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleGesture(gesture: Gesture) {
        lib?.let {
            val predictions: ArrayList<Prediction> = ArrayList(it.recognize(gesture))
            if (predictions.isNotEmpty()) {
                val best = predictions.maxByOrNull { p -> p.score }
                if (best != null && best.score > 3.0) {
                    val name = best.name
                    val action = MappingRepository.getActionForGesture(this, name)
                    if (action != null) {
                        GestureActions.performAction(this, action)
                        Toast.makeText(this, "Gesto: $name → $action", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Gesto reconocido ($name) sin acción", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Gesto no reconocido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No hay gestos guardados", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "No hay librería de gestos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startForegroundIfNeeded() {
        val channelId = "gestures_overlay"
        val nm = getSystemService(NotificationManager::class.java) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Overlay gestures", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Gestures Overlay")
            .setContentText("Overlay activo para capturar gestos")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            (overlayView?.parent as? ViewGroup)?.let {
                windowManager.removeView(it)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
