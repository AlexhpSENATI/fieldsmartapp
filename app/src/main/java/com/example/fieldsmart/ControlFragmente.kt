package com.example.fieldsmart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ControlFragmente : Fragment() {
    //LLAMADO PARA LOS DATOS SE SENSORES DE FIREBASE XD
    private lateinit var database: DatabaseReference
    private lateinit var spinnerModo: Spinner
    private lateinit var editUmbralMin: EditText
    private lateinit var editUmbralMax: EditText
    private lateinit var editTiempoRiego: EditText
    private lateinit var editTiempoEspera: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEncender: Button
    private lateinit var btnApagar: Button

    //LLAMADO PARA EL ENCABEZADO DE AVATAR Y NOMBRE
    private lateinit var tvSaludo: android.widget.TextView
    private lateinit var imgAvatar: android.widget.ImageView
    private lateinit var viewModel: UsuarioViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_control, container, false)

        database = FirebaseDatabase.getInstance().reference

        spinnerModo = view.findViewById(R.id.spinnerModo)
        editUmbralMin = view.findViewById(R.id.editUmbralMin)
        editUmbralMax = view.findViewById(R.id.editUmbralMax)
        editTiempoRiego = view.findViewById(R.id.editTiempoRiego)
        editTiempoEspera = view.findViewById(R.id.editTiempoEspera)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnEncender = view.findViewById(R.id.btnEncender)
        btnApagar = view.findViewById(R.id.btnApagar)

        // VISTAS DE  NOMBRE Y AVATAR
        tvSaludo = view.findViewById(R.id.tvSaludo)
        imgAvatar = view.findViewById(R.id.imgAvatar)

        viewModel = ViewModelProvider(requireActivity())[UsuarioViewModel::class.java]

        viewModel.nombre.observe(viewLifecycleOwner) { nombre ->
            tvSaludo.text = nombre
        }
        viewModel.avatar.observe(viewLifecycleOwner) { bmp ->
            bmp?.let { Glide.with(this).load(it).circleCrop().into(imgAvatar) }
        }

        // CARGAR TADOS DEL USUARIO
        if (viewModel.nombre.value == null || viewModel.avatar.value == null) {
            Hero.cargarUsuario(this, viewModel)
        }

        //CARGAR DATOS
        database.child("config").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val modoAutomatico = snapshot.child("modoAutomatico").getValue(Boolean::class.java) ?: true
                    val umbralMin = snapshot.child("umbralMin").getValue(Int::class.java) ?: 0
                    val umbralMax = snapshot.child("umbralMax").getValue(Int::class.java) ?: 0
                    val tiempoRiego = snapshot.child("tiempoRiego").getValue(Int::class.java) ?: 0
                    val tiempoEspera = snapshot.child("tiempoEspera").getValue(Int::class.java) ?: 0

                    editUmbralMin.setText(umbralMin.toString())
                    editUmbralMax.setText(umbralMax.toString())
                    editTiempoRiego.setText(tiempoRiego.toString())
                    editTiempoEspera.setText(tiempoEspera.toString())

                    if (modoAutomatico) {
                        spinnerModo.setSelection(1)
                    } else {
                        spinnerModo.setSelection(0)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al cargar configuración", Toast.LENGTH_SHORT).show()
            }
        })

        // BOTON DE GUARDAR
        btnGuardar.setOnClickListener {
            val modoSeleccionado = spinnerModo.selectedItem.toString()
            val modoAutomatico = modoSeleccionado == "Automático"

            val umbralMin = editUmbralMin.text.toString().toIntOrNull() ?: 0
            val umbralMax = editUmbralMax.text.toString().toIntOrNull() ?: 0
            val tiempoRiego = editTiempoRiego.text.toString().toIntOrNull() ?: 0
            val tiempoEspera = editTiempoEspera.text.toString().toIntOrNull() ?: 0

            val config = mapOf(
                "modoAutomatico" to modoAutomatico,
                "umbralMin" to umbralMin,
                "umbralMax" to umbralMax,
                "tiempoRiego" to tiempoRiego,
                "tiempoEspera" to tiempoEspera
            )

            database.child("config").updateChildren(config)
            Toast.makeText(requireContext(), "Configuración guardada", Toast.LENGTH_SHORT).show()
        }

        // BOTON DE ENCENDER
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

        // BOTON DE APAGAR
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

}
