package com.example.gestures

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestures.data.MappingRepository

class MappingActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var displayItems = mutableListOf<String>()
    private var gestureNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapping)

        listView = findViewById(R.id.listGestures)
        loadMappings()

        listView.setOnItemClickListener { _, _, position, _ ->
            val gestureName = gestureNames[position]
            showEditDialog(gestureName)
        }
    }

    private fun loadMappings() {
        val mappings = MappingRepository.loadAll(this)
        displayItems.clear()
        gestureNames.clear()
        mappings.forEach { m ->
            displayItems.add("${m.gestureName} → ${m.action}")
            gestureNames.add(m.gestureName)
        }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayItems)
    }

    private fun showEditDialog(gestureName: String) {
        // Build list: same options as capture (actions + apps)
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val appLabels = apps.map { it.loadLabel(pm).toString() }
        val appPackages = apps.map { it.activityInfo.packageName }

        val options = mutableListOf<String>()
        options.add("Linterna (torch)")
        options.add("Wi‑Fi toggle")
        options.add("Abrir ajustes")
        options.addAll(appLabels)
        options.add("Eliminar mapping")

        AlertDialog.Builder(this)
            .setTitle("Editar acción para $gestureName")
            .setItems(options.toTypedArray()) { _, which ->
                when {
                    which == options.size - 1 -> {
                        MappingRepository.removeMapping(this, gestureName)
                        Toast.makeText(this, "Mapping eliminado", Toast.LENGTH_SHORT).show()
                    }
                    which == 0 -> MappingRepository.saveMapping(this, gestureName, "torch:toggle")
                    which == 1 -> MappingRepository.saveMapping(this, gestureName, "wifi:toggle")
                    which == 2 -> MappingRepository.saveMapping(this, gestureName, "settings:open")
                    which >= 3 && which < 3 + appPackages.size -> {
                        val pkg = appPackages[which - 3]
                        MappingRepository.saveMapping(this, gestureName, "launch:$pkg")
                    }
                }
                loadMappings()
            }
            .show()
    }
}
