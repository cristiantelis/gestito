package com.example.gestures

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.widget.Toast

object GestureActions {

    // actionString examples:
    // "launch:com.example.app"
    // "torch:toggle"
    // "wifi:toggle"
    // "settings:open"

    fun performAction(context: Context, action: String) {
        when {
            action.startsWith("launch:") -> {
                val pkg = action.removePrefix("launch:")
                val pm = context.packageManager
                val intent = pm.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "App no encontrada: $pkg", Toast.LENGTH_SHORT).show()
                }
            }
            action == "torch:toggle" -> {
                toggleTorch(context)
            }
            action == "wifi:toggle" -> {
                toggleWifi(context)
            }
            action == "settings:open" -> {
                val i = Intent(android.provider.Settings.ACTION_SETTINGS)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
            }
            else -> {
                Toast.makeText(context, "Acción desconocida: $action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var torchState: Boolean = false

    private fun toggleTorch(context: Context) {
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val ids = cm.cameraIdList
            if (ids.isNotEmpty()) {
                val id = ids[0]
                // Alternamos estado interno (no es ideal en producción)
                torchState = !torchState
                cm.setTorchMode(id, torchState)
                Toast.makeText(context, "Linterna ${if (torchState) "encendida" else "apagada"}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No hay cámara disponible", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error linterna: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleWifi(context: Context) {
        try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val newState = !wm.isWifiEnabled
            wm.isWifiEnabled = newState
            Toast.makeText(context, "Wi‑Fi ${if (newState) "activado" else "desactivado"}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error Wi‑Fi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
