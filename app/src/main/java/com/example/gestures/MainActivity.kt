package com.example.gestures

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvInfo: TextView

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            tvInfo.text = "Permisos solicitados; revisa los otorgados en la configuración si algo falla."
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStartOverlay)
        val btnStop = findViewById<Button>(R.id.btnStopOverlay)
        val btnCapture = findViewById<Button>(R.id.btnCapture)
        val btnMappings = findViewById<Button>(R.id.btnMappings)
        tvInfo = findViewById(R.id.tvInfo)

        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
                startActivity(intent)
                tvInfo.text = "Por favor concede permiso de overlay y vuelve."
                return@setOnClickListener
            }
            requestRuntimePermissions()
            val intent = Intent(this, GestureOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            tvInfo.text = "Overlay iniciado."
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, GestureOverlayService::class.java)
            stopService(intent)
            tvInfo.text = "Overlay detenido."
        }

        btnCapture.setOnClickListener {
            startActivity(Intent(this, GestureCaptureActivity::class.java))
        }

        btnMappings.setOnClickListener {
            startActivity(Intent(this, MappingActivity::class.java))
        }
    }

    private fun requestRuntimePermissions() {
        val perms = mutableListOf<String>()
        if (checkSelfPermission(Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.CAMERA)
        }
        // Wi‑Fi permissions normalmente declaradas en Manifest; pedir solo CAMERA aquí
        if (perms.isNotEmpty()) {
            requestPermissions.launch(perms.toTypedArray())
        }
    }
}
