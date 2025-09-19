package com.example.fieldsmart

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvBienvenida: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBienvenida = findViewById(R.id.tvBienvenida)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val usuario = auth.currentUser
        usuario?.let {
            val uid = it.uid

            // Buscar el nombre del usuario en la BD
            database.child("usuarios").child(uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val nombre = snapshot.child("nombre").value.toString()
                        tvBienvenida.text = "Hola $nombre 👋"
                    } else {
                        tvBienvenida.text = "Hola 👋 (no encontré tu nombre en BD)"
                    }
                }
                .addOnFailureListener {
                    tvBienvenida.text = "Error al obtener datos"
                }
        }
    }
}
