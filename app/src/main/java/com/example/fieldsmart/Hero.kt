package com.example.fieldsmart

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object Hero {

    /**
     * Carga nombre y avatar del usuario desde Firebase solo si aún no se han cargado en el ViewModel
     */
    fun cargarUsuario(fragment: Fragment, viewModel: UsuarioViewModel) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val usuariosDB = FirebaseDatabase.getInstance().getReference("usuarios")
        val configuracionesDB = FirebaseDatabase.getInstance().getReference("configuraciones")

        // Nombre
        usuariosDB.child(uid).get().addOnSuccessListener {
            val nombre = it.child("nombre").value?.toString() ?: "Usuario"
            viewModel.setNombre("Bienvenido, $nombre")
        }.addOnFailureListener {
            viewModel.setNombre("Bienvenido, Usuario")
        }

        // Avatar
        configuracionesDB.child(uid).child("avatar").get().addOnSuccessListener { snapshot ->
            val avatarBase64 = snapshot.value?.toString()
            if (!avatarBase64.isNullOrEmpty()) {
                val bytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                viewModel.setAvatar(bmp)
            }
        }.addOnFailureListener {
            Log.e("Hero", "Error al cargar avatar: ${it.message}")
        }
    }
}
