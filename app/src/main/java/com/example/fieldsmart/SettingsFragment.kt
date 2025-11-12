package com.example.fieldsmart

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class SettingsFragment : Fragment() {

    private lateinit var txtNombre: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnCambiarAvatar: Button
    private lateinit var btnCambiarContrasena: Button
    private lateinit var viewModel: UsuarioViewModel
    private val auth = FirebaseAuth.getInstance()
    private val dbConfig = FirebaseDatabase.getInstance().reference.child("configuraciones")
    private val dbUsuarios = FirebaseDatabase.getInstance().reference.child("usuarios")

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let { guardarAvatarEnConfiguraciones(it) }
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

        viewModel = ViewModelProvider(requireActivity())[UsuarioViewModel::class.java]

        val uid = auth.currentUser?.uid

        if (uid != null) {

            dbUsuarios.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val nombre = it.child("nombre").value.toString()
                    txtNombre.text = nombre
                    viewModel.setNombre("HOLA, $nombre")
                }
            }

            dbConfig.child(uid).child("avatar").get().addOnSuccessListener {
                val avatarBase64 = it.value?.toString()
                if (!avatarBase64.isNullOrEmpty()) {
                    val bytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)


                    if (isAdded && view != null) {
                        Glide.with(requireContext())
                            .load(bmp)
                            .circleCrop()
                            .into(imgAvatar)

                        viewModel.setAvatar(bmp)
                    }
                }
            }

        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        btnCambiarContrasena.setOnClickListener {
            val email = auth.currentUser?.email
            email?.let {
                auth.sendPasswordResetEmail(it)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Revisa tu correo para cambiar la contraseña",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }
            }
        }
        btnCambiarAvatar.setOnClickListener {
            abrirGaleria()
        }

        return view
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun guardarAvatarEnConfiguraciones(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()

        //  Comprimir imagen 70%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val imageBytes = outputStream.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        dbConfig.child(uid).child("avatar").setValue(base64Image)
            .addOnSuccessListener {

                Glide.with(this).load(bitmap).circleCrop().into(imgAvatar)

                viewModel.setAvatar(bitmap)

                Toast.makeText(
                    requireContext(),
                    "Avatar guardado correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Error al guardar: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
