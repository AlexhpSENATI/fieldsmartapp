package com.example.fieldsmart

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class NotificationService : Service() {

    private lateinit var database: FirebaseDatabase
    private lateinit var sensoresRef: DatabaseReference
    private lateinit var configRef: DatabaseReference

    private var umbralMin: Int = 0
    private var umbralMax: Int = 100

    override fun onCreate() {
        super.onCreate()

        // Crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alerts_channel",
                "Alertas de Humedad",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        database = FirebaseDatabase.getInstance()
        sensoresRef = database.getReference("sensores/humedadSuelo")
        configRef = database.getReference("config")

        escucharConfig()
        escucharHumedad()
    }

    private fun escucharConfig() {
        configRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                umbralMin = snapshot.child("umbralMin").getValue(Int::class.java) ?: 0
                umbralMax = snapshot.child("umbralMax").getValue(Int::class.java) ?: 100
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun escucharHumedad() {
        sensoresRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val humedad = snapshot.getValue(Int::class.java) ?: return

                if (humedad < umbralMin) {
                    mostrarNotificacion(
                        "⚠️ ALERTA Humedad baja",
                        "Humedad actual: $humedad% (Min: $umbralMin%)"
                    )
                } else if (humedad > umbralMax) {
                    mostrarNotificacion(
                        "✅ Humedad óptima",
                        "Humedad actual: $humedad% (Max: $umbralMax%)"
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarNotificacion(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, "alerts_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
