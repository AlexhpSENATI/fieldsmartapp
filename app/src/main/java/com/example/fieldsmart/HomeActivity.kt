package com.example.fieldsmart

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val nombre = intent.getStringExtra("NOMBRE")
        val tvBienvenida: TextView = findViewById(R.id.tvBienvenida)

        tvBienvenida.text = "¡Bienvenido, $nombre! 🎉"
    }
}
