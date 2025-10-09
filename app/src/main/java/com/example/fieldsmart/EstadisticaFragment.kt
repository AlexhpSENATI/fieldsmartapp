package com.example.fieldsmart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EstadisticaFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var database: DatabaseReference
    private var valueEventListener: ValueEventListener? = null

    // 🔹 Nuevos elementos para el saludo y avatar
    private lateinit var tvSaludo: TextView
    private lateinit var imgAvatar: ImageView
    private val auth = FirebaseAuth.getInstance()
    private val usuariosDB = FirebaseDatabase.getInstance().getReference("usuarios")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estadistica, container, false)

        // --- Inicializar vistas ---
        barChart = view.findViewById(R.id.barChart)
        tvSaludo = view.findViewById(R.id.tvSaludo)
        imgAvatar = view.findViewById(R.id.imgAvatar)

        // --- Cargar nombre y avatar ---
        cargarDatosUsuario()

        // --- Configurar base de datos para el gráfico ---
        database = FirebaseDatabase.getInstance().getReference("historial")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || !isAdded) {
                    Log.d("FirebaseDebug", "No hay datos en historial o fragment destruido")
                    return
                }

                for (data in snapshot.children) {
                    val item = data.getValue(Historial::class.java)

                    val entries = ArrayList<BarEntry>()
                    val labels = ArrayList<String>()

                    // Cada variable será una barra con un índice distinto
                    item?.humedadSuelo?.let {
                        entries.add(BarEntry(0f, it.toFloat()))
                        labels.add("Suelo")
                    }
                    item?.humedadAmbiental?.let {
                        entries.add(BarEntry(1f, it.toFloat()))
                        labels.add("Ambiental")
                    }
                    item?.temperatura?.let {
                        entries.add(BarEntry(2f, it.toFloat()))
                        labels.add("Temp")
                    }

                    val dataSet = BarDataSet(entries, "Última medición").apply {
                        setColors(
                            ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark),
                            ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark),
                            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                        )
                    }

                    val barData = BarData(dataSet)
                    barData.barWidth = 0.6f

                    barChart.data = barData
                    barChart.description = Description().apply { text = item?.fecha_texto ?: "" }

                    // Configuración del eje X
                    val xAxis = barChart.xAxis
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.valueFormatter =
                        com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)

                    barChart.axisLeft.axisMinimum = 0f
                    barChart.axisRight.isEnabled = false
                    barChart.invalidate()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error: ${error.message}")
            }
        }

        database.limitToLast(1).addValueEventListener(valueEventListener!!)
        return view
    }

    // 🔹 Método para cargar nombre y avatar desde Firebase
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

    override fun onDestroyView() {
        super.onDestroyView()
        valueEventListener?.let { database.removeEventListener(it) }
    }
}
