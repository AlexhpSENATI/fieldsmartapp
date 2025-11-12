package com.example.fieldsmart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.decorView.systemUiVisibility = 0 // Íconos blancos

        // Pedir permiso de notificaciones en Android 13+
        askNotificationPermission()

        val btnIniciar = findViewById<Button>(R.id.btnIniciar)

        btnIniciar.setOnClickListener {
            // Pasamos al Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun askNotificationPermission() {
        // Solo necesario en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startNotificationService()
            } else {
                // Pedir permiso
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Para Android 12 o menor, arrancamos directo
            startNotificationService()
        }
    }

    // Manejador del resultado del permiso
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permiso concedido ✅", Toast.LENGTH_SHORT).show()
                startNotificationService()
            } else {
                Toast.makeText(this, "Permiso denegado ❌", Toast.LENGTH_SHORT).show()
            }
        }

    private fun startNotificationService() {
        val serviceIntent = Intent(this, NotificationService::class.java)
        startService(serviceIntent)
    }
}
