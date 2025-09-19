package com.example.fieldsmart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class HomeFragment : Fragment() {

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

    private lateinit var database: DatabaseReference
    private var sensoresListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inicializar TextViews
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

        // Conectar directamente al nodo sensores
        database = FirebaseDatabase.getInstance().getReference("sensores")

        sensoresListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d("FirebaseDebug", "No hay datos en sensores")
                    return
                }

                Log.d("FirebaseDebug", "Snapshot: ${snapshot.value}")

                val sensores = snapshot.getValue(Sensores::class.java)
                Log.d("FirebaseDebug", "Sensores recibidos: $sensores")

                // Mostrar valores en los TextView
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

        database.addValueEventListener(sensoresListener!!)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (sensoresListener != null) {
            database.removeEventListener(sensoresListener!!)
        }
    }
}
