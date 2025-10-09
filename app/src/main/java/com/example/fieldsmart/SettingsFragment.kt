package com.example.fieldsmart

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SettingsFragment : Fragment() {

    private lateinit var txtNombre: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnCambiarAvatar: Button
    private lateinit var btnCambiarContrasena: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference.child("usuarios")

    private var imageUri: Uri? = null

    // Activity Result Launcher para seleccionar imagen
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            imageUri?.let { uploadAvatar(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        txtNombre = view.findViewById(R.id.txtNombreUsuario)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)
        btnCambiarAvatar = view.findViewById(R.id.btnCambiarAvatar)
        btnCambiarContrasena = view.findViewById(R.id.btnCambiarContrasena)

        val uid = auth.currentUser?.uid

        // Mostrar nombre y avatar desde Firebase
        if (uid != null) {
            db.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val nombre = it.child("nombre").value.toString()
                    val avatarUrl = it.child("avatar").value?.toString()

                    txtNombre.text = nombre
                    if (!avatarUrl.isNullOrEmpty()) {
                        Glide.with(this).load(avatarUrl).circleCrop().into(imgAvatar)
                    }
                }
            }
        }

        // Botón cerrar sesión
        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        // Botón cambiar contraseña
        btnCambiarContrasena.setOnClickListener {
            val email = auth.currentUser?.email
            email?.let {
                auth.sendPasswordResetEmail(it).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Revisa tu correo para cambiar la contraseña", Toast.LENGTH_LONG).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Botón cambiar avatar
        btnCambiarAvatar.setOnClickListener {
            openImagePicker()
        }

        return view
    }

    // Abrir galería para elegir imagen
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    // Subir imagen a Firebase Storage y actualizar URL en Realtime Database
    private fun uploadAvatar(uri: Uri) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Referencia correcta en Firebase Storage
        val avatarRef = FirebaseStorage.getInstance().getReference("avatars/$uid.jpg")

        avatarRef.putFile(uri)
            .addOnSuccessListener {
                avatarRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    db.child(uid).child("avatar").setValue(downloadUrl.toString())
                        .addOnSuccessListener {
                            Glide.with(this).load(downloadUrl).circleCrop().into(imgAvatar)
                            Toast.makeText(requireContext(), "Avatar actualizado correctamente", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir la imagen: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
