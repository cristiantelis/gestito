package com.example.gestures

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ResolveInfo
import android.gesture.Gesture
import android.gesture.GestureOverlayView
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestures.data.MappingRepository

class GestureCaptureActivity : AppCompatActivity() {

    private lateinit var overlay: GestureOverlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overlay = GestureOverlayView(this)
        overlay.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        overlay.isGestureVisible = true
        overlay.setGestureStrokeWidth(8f)
        setContentView(overlay)

        overlay.addOnGesturePerformedListener { _, gesture ->
            promptActionPickerAndSave(gesture)
        }
    }

    private fun promptActionPickerAndSave(gesture: Gesture) {
        // Build list: common actions + installed launchable apps
        val actions = mutableListOf<String>()
        actions.add("torch:toggle")
        actions.add("wifi:toggle")
        actions.add("settings:open")

        // get installed launchable apps
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val appLabels = apps.map { it.loadLabel(pm).toString() }
        val appPackages = apps.map { it.activityInfo.packageName }

        // Add apps to actions as "launch:<package>" but show label to user
        val displayList = mutableListOf<String>()
        displayList.add("Linterna (torch)")
        displayList.add("Wi‑Fi toggle")
        displayList.add("Abrir ajustes")
        for (label in appLabels) displayList.add(label)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)

        AlertDialog.Builder(this)
            .setTitle("Selecciona acción para el gesto")
            .setAdapter(adapter) { dialog, which ->
                val selectedAction: String = when (which) {
                    0 -> "torch:toggle"
                    1 -> "wifi:toggle"
                    2 -> "settings:open"
                    else -> {
                        val idx = which - 3
                        val pkg = appPackages[idx]
                        "launch:$pkg"
                    }
                }
                // Ask for gesture name
                promptNameAndSave(gesture, selectedAction)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun promptNameAndSave(gesture: Gesture, action: String) {
        // simple naming: ask with an AlertDialog input
        val input = android.widget.EditText(this)
        input.hint = "Nombre (ej: abrir_camera)"
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(50, 20, 50, 0)
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Nombre del gesto")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                val name = input.text.toString().ifBlank { "gesture_${System.currentTimeMillis()}" }
                val saved = GestureRepository.addGesture(this, name, gesture)
                if (saved) {
                    MappingRepository.saveMapping(this, name, action)
                    Toast.makeText(this, "Gesto guardado: $name → $action", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error guardando gesto", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
