package com.example.gestures

import android.app.AlertDialog
import android.gesture.Gesture
import android.gesture.GestureOverlayView
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
            promptNameAndAction(gesture)
        }
    }

    private fun promptNameAndAction(gesture: Gesture) {
        val nameInput = EditText(this)
        nameInput.hint = "Nombre del gesto (ej: abrir_camera)"
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(50, 20, 50, 0)
        container.addView(nameInput)

        val actions = arrayOf("launch:com.package.name", "torch:toggle", "wifi:toggle", "settings:open")
        AlertDialog.Builder(this)
            .setTitle("Guardar gesto")
            .setMessage("Introduce nombre y elige acción a asociar (ej: launch:com.example.app)")
            .setView(container)
            .setSingleChoiceItems(actions, 0) { dialog, which ->
                // no-op; we'll read selection when OK
            }
            .setPositiveButton("Guardar") { dialog, _ ->
                val name = nameInput.text.toString().ifBlank { "gesture_${System.currentTimeMillis()}" }
                // read selected action
                val listView = (dialog as AlertDialog).listView
                val checked = listView.checkedItemPosition
                val action = if (checked >= 0) actions[checked] else actions[0]
                val saved = GestureRepository.addGesture(this, name, gesture)
                if (saved) {
                    GestureRepository.saveMapping(this, name, action)
                    Toast.makeText(this, "Gesto guardado y mapeado a $action", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error guardando gesto", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
            .setNegativeButton("Cancelar") { _, _ -> }
            .show()
    }
}
