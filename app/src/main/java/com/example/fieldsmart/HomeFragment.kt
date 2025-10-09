package com.example.fieldsmart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var tvSaludo: TextView
    private lateinit var imgAvatar: ImageView

    private lateinit var tvBomba: TextView
    private lateinit var tvHumedadSuelo: TextView
    private lateinit var tvHumedadAmbiental: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var tvModoAutomatico: TextView
    private lateinit var tvTiempoUso: TextView
    private lateinit var tvUltimoRiego: TextView
    private lateinit var tvEnEspera: TextView
    private lateinit var tvTiempoRestante: TextView
    private lateinit var tvIP: TextView

    private lateinit var sensoresDB: DatabaseReference
    private lateinit var usuariosDB: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    private var sensoresListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // --- Elementos de UI ---
        tvSaludo = view.findViewById(R.id.tvSaludo)
        imgAvatar = view.findViewById(R.id.imgAvatar)

        tvBomba = view.findViewById(R.id.tvBomba)
        tvHumedadSuelo = view.findViewById(R.id.tvHumedadSuelo)
        tvHumedadAmbiental = view.findViewById(R.id.tvHumedadAmbiental)
        tvTemperatura = view.findViewById(R.id.tvTemperatura)
        tvModoAutomatico = view.findViewById(R.id.tvModoAutomatico)
        tvTiempoUso = view.findViewById(R.id.tvTiempoUso)
        tvUltimoRiego = view.findViewById(R.id.tvUltimoRiego)
        tvEnEspera = view.findViewById(R.id.tvEnEspera)
        tvTiempoRestante = view.findViewById(R.id.tvTiempoRestante)
        tvIP = view.findViewById(R.id.tvIP)

        // --- Firebase ---
        sensoresDB = FirebaseDatabase.getInstance().getReference("sensores")
        usuariosDB = FirebaseDatabase.getInstance().getReference("usuarios")

        cargarDatosUsuario()
        escucharDatosSensores()

        return view
    }

    // --- Cargar nombre y avatar desde Firebase ---
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

    // --- Escuchar cambios en los sensores ---
    private fun escucharDatosSensores() {
        sensoresListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d("FirebaseDebug", "No hay datos en sensores")
                    return
                }

                val sensores = snapshot.getValue(Sensores::class.java)
                Log.d("FirebaseDebug", "Sensores: $sensores")

                tvBomba.text = "Bomba: ${sensores?.bomba ?: "--"}"
                tvHumedadSuelo.text = "Humedad suelo: ${sensores?.humedadSuelo?.toInt() ?: "--"}%"
                tvHumedadAmbiental.text = "Humedad ambiental: ${sensores?.humedadAmbiental?.toInt() ?: "--"}%"
                tvTemperatura.text = "Temperatura: ${sensores?.temperatura?.toInt() ?: "--"}°C"
                tvModoAutomatico.text = "Modo automático: ${sensores?.modoAutomatico ?: "--"}"
                tvTiempoUso.text = "Tiempo de uso: ${sensores?.tiempoUso?.toInt() ?: "--"} s"
                tvUltimoRiego.text = "Último riego: ${sensores?.ultimoRiego?.toInt() ?: "--"}"
                tvEnEspera.text = "En espera: ${sensores?.enEspera ?: "--"}"
                tvTiempoRestante.text = "Tiempo restante: ${sensores?.tiempoRestanteEspera?.toInt() ?: "--"} s"
                tvIP.text = "IP: ${sensores?.ip ?: "--"}"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error: ${error.message}")
            }
        }

        sensoresDB.addValueEventListener(sensoresListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensoresListener?.let { sensoresDB.removeEventListener(it) }
    }
}
