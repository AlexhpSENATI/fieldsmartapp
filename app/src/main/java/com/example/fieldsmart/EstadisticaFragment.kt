package com.example.fieldsmart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.database.*

class EstadisticaFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var database: DatabaseReference
    private var valueEventListener: ValueEventListener? = null

    private lateinit var tvSaludo: android.widget.TextView
    private lateinit var imgAvatar: android.widget.ImageView
    private lateinit var viewModel: UsuarioViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estadistica, container, false)

        //PARTE DEL NOMBRE Y EL AVATAR
        barChart = view.findViewById(R.id.barChart)
        tvSaludo = view.findViewById(R.id.tvSaludo)
        imgAvatar = view.findViewById(R.id.imgAvatar)

        //
        viewModel = ViewModelProvider(requireActivity())[UsuarioViewModel::class.java]

        viewModel.nombre.observe(viewLifecycleOwner) { nombre ->
            tvSaludo.text = nombre
        }
        viewModel.avatar.observe(viewLifecycleOwner) { bmp ->
            bmp?.let { Glide.with(this).load(it).circleCrop().into(imgAvatar) }
        }

        // CARGAR DATOS DE USUARIOS
        if (viewModel.nombre.value == null || viewModel.avatar.value == null) {
            Hero.cargarUsuario(this, viewModel)
        }

        // GRAFICO DE BARRAS
        database = FirebaseDatabase.getInstance().getReference("historial")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || !isAdded) return

                for (data in snapshot.children) {
                    val item = data.getValue(Historial::class.java)
                    val entries = ArrayList<BarEntry>()
                    val labels = ArrayList<String>()

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

    override fun onDestroyView() {
        super.onDestroyView()
        valueEventListener?.let { database.removeEventListener(it) }
    }
}
