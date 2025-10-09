package com.example.fieldsmart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ControlFragmente : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var spinnerModo: Spinner
    private lateinit var editUmbralMin: EditText
    private lateinit var editUmbralMax: EditText
    private lateinit var editTiempoRiego: EditText
    private lateinit var editTiempoEspera: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEncender: Button
    private lateinit var btnApagar: Button

    // 🔹 Nuevos elementos de usuario
    private lateinit var tvSaludo: TextView
    private lateinit var imgAvatar: ImageView
    private val auth = FirebaseAuth.getInstance()
    private val usuariosDB = FirebaseDatabase.getInstance().getReference("usuarios")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_control, container, false)

        database = FirebaseDatabase.getInstance().reference

        // Referencias
        spinnerModo = view.findViewById(R.id.spinnerModo)
        editUmbralMin = view.findViewById(R.id.editUmbralMin)
        editUmbralMax = view.findViewById(R.id.editUmbralMax)
        editTiempoRiego = view.findViewById(R.id.editTiempoRiego)
        editTiempoEspera = view.findViewById(R.id.editTiempoEspera)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnEncender = view.findViewById(R.id.btnEncender)
        btnApagar = view.findViewById(R.id.btnApagar)

        // 🔹 Inicializar vistas del usuario
        tvSaludo = view.findViewById(R.id.tvSaludo)
        imgAvatar = view.findViewById(R.id.imgAvatar)

        // 🔹 Cargar nombre y avatar
        cargarDatosUsuario()

        // --- Cargar datos iniciales desde Firebase ---
        database.child("config").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val modoAutomatico = snapshot.child("modoAutomatico").getValue(Boolean::class.java) ?: true
                    val umbralMin = snapshot.child("umbralMin").getValue(Int::class.java) ?: 0
                    val umbralMax = snapshot.child("umbralMax").getValue(Int::class.java) ?: 0
                    val tiempoRiego = snapshot.child("tiempoRiego").getValue(Int::class.java) ?: 0
                    val tiempoEspera = snapshot.child("tiempoEspera").getValue(Int::class.java) ?: 0

                    // Llenar EditText con los valores de Firebase
                    editUmbralMin.setText(umbralMin.toString())
                    editUmbralMax.setText(umbralMax.toString())
                    editTiempoRiego.setText(tiempoRiego.toString())
                    editTiempoEspera.setText(tiempoEspera.toString())

                    // Configurar modo
                    if (modoAutomatico) {
                        spinnerModo.setSelection(1) // Automático
                    } else {
                        spinnerModo.setSelection(0) // Manual
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar configuración", Toast.LENGTH_SHORT).show()
            }
        })

        // --- Botón Guardar ---
        btnGuardar.setOnClickListener {
            val modoSeleccionado = spinnerModo.selectedItem.toString()
            val modoAutomatico = modoSeleccionado == "Automático"
            val bombaManual = !modoAutomatico

            val umbralMin = editUmbralMin.text.toString().toIntOrNull() ?: 0
            val umbralMax = editUmbralMax.text.toString().toIntOrNull() ?: 0
            val tiempoRiego = editTiempoRiego.text.toString().toIntOrNull() ?: 0
            val tiempoEspera = editTiempoEspera.text.toString().toIntOrNull() ?: 0

            val config = mapOf(
                "modoAutomatico" to modoAutomatico,
                "bombaManual" to bombaManual,
                "umbralMin" to umbralMin,
                "umbralMax" to umbralMax,
                "tiempoRiego" to tiempoRiego,
                "tiempoEspera" to tiempoEspera
            )

            database.child("config").updateChildren(config)
            Toast.makeText(requireContext(), "Configuración guardada", Toast.LENGTH_SHORT).show()
        }

        // --- Botón Encender ---
        btnEncender.setOnClickListener {
            val config = mapOf(
                "modoAutomatico" to false,
                "bombaManual" to true
            )

            database.child("config").updateChildren(config)
            database.child("sensores").child("bomba").setValue(true)

            spinnerModo.setSelection(0) // Manual
            Toast.makeText(requireContext(), "Bomba encendida (manual)", Toast.LENGTH_SHORT).show()
        }

        // --- Botón Apagar ---
        btnApagar.setOnClickListener {
            val config = mapOf(
                "modoAutomatico" to false,
                "bombaManual" to false
            )

            database.child("config").updateChildren(config)
            database.child("sensores").child("bomba").setValue(false)

            spinnerModo.setSelection(0) // Manual
            Toast.makeText(requireContext(), "Bomba apagada (manual)", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    // Método para cargar nombre y avatar desde Firebase
    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        usuariosDB.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val nombre = it.child("nombre").value.toString()
                val avatarUrl = it.child("avatar").value?.toString()

                tvSaludo.text = "HOLA, $nombre"

                if (!avatarUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(avatarUrl)
                        .circleCrop()
                        .into(imgAvatar)
                }
            } else {
                tvSaludo.text = "HOLA, Usuario"
            }
        }.addOnFailureListener {
            tvSaludo.text = "HOLA, Usuario"
        }
    }
}
