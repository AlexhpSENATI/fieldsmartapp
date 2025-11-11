package com.example.fieldsmart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {
    //LLAMADO PARA LOS DATOS SE SENSORES DE FIREBASE XD
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

    //LLAMADO PARA EL ENCABEZADO DE AVATAR Y NOMBRE
    private lateinit var tvSaludo: android.widget.TextView
    private lateinit var imgAvatar: android.widget.ImageView
    private lateinit var viewModel: UsuarioViewModel

    private var sensoresListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        //LLAMADO PARA EL ENCABEZADO DE AVATAR Y NOMBRE
        tvSaludo = view.findViewById(R.id.tvSaludo)
        imgAvatar = view.findViewById(R.id.imgAvatar)

        //LLAMADO PARA LOS DATOS DEL SPE
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

        //BASE DE DATOS PARA LOS DATOS
        sensoresDB = FirebaseDatabase.getInstance().getReference("sensores")

        tvSaludo = view.findViewById(R.id.tvSaludo)
        imgAvatar = view.findViewById(R.id.imgAvatar)

        viewModel = ViewModelProvider(requireActivity())[UsuarioViewModel::class.java]

        viewModel.nombre.observe(viewLifecycleOwner) { nombre ->
            tvSaludo.text = nombre
        }

        viewModel.avatar.observe(viewLifecycleOwner) { bmp ->
            bmp?.let { Glide.with(this).load(it).circleCrop().into(imgAvatar) }
        }


        //CARGAR DATOS DEL USUARIO
        if (viewModel.nombre.value == null || viewModel.avatar.value == null) {
            Hero.cargarUsuario(this, viewModel)
        }

        escucharDatosSensores()
        return view

    }

    //ESCUCHAR DATOS DE LOS SENSORES
    private fun escucharDatosSensores() {
        sensoresListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d("FirebaseDebug", "No hay datos en sensores")
                    return
                }

                val sensores = snapshot.getValue(Sensores::class.java)
                Log.d("FirebaseDebug", "Sensores: $sensores")

                tvBomba.text = " ${sensores?.bomba ?: "--"}"
                tvHumedadSuelo.text = " ${sensores?.humedadSuelo?.toInt() ?: "--"}%"
                tvHumedadAmbiental.text = " ${sensores?.humedadAmbiental?.toInt() ?: "--"}%"
                tvTemperatura.text = " ${sensores?.temperatura?.toInt() ?: "--"}°C"
                tvModoAutomatico.text = " ${sensores?.modoAutomatico ?: "--"}"
                tvTiempoUso.text = "${sensores?.tiempoUso?.toInt() ?: "--"} s"
                tvUltimoRiego.text = " ${sensores?.ultimoRiego?.toInt() ?: "--"}"
                tvEnEspera.text = " ${sensores?.enEspera ?: "--"}"
                tvTiempoRestante.text = " ${sensores?.tiempoRestanteEspera?.toInt() ?: "--"} s"
                tvIP.text = " ${sensores?.ip ?: "--"}"
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
