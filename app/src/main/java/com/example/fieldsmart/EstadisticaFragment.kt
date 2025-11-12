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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class EstadisticaFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var database: DatabaseReference
    private lateinit var tvSaludo: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var viewModel: UsuarioViewModel
    private var valueEventListener: ValueEventListener? = null
    private var fechaActual: String = ""
    private var fechaSeleccionada: String = ""
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estadistica, container, false)

        // Referencias
        tvSaludo = view.findViewById(R.id.tvSaludo)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        barChart = view.findViewById(R.id.barChart)
        lineChart = view.findViewById(R.id.lineChart)
        val rvFechas = view.findViewById<RecyclerView>(R.id.rvFechas)

        // ViewModel usuario
        viewModel = ViewModelProvider(requireActivity())[UsuarioViewModel::class.java]
        viewModel.nombre.observe(viewLifecycleOwner) { tvSaludo.text = it }
        viewModel.avatar.observe(viewLifecycleOwner) { bmp ->
            bmp?.let { Glide.with(this).load(it).circleCrop().into(imgAvatar) }
        }
        if (viewModel.nombre.value == null || viewModel.avatar.value == null) {
            Hero.cargarUsuario(this, viewModel)
        }

        // Firebase
        database = FirebaseDatabase.getInstance().getReference("historial")

        // Fecha actual
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaActual = formato.format(Date())
        fechaSeleccionada = fechaActual

        // Generar fechas +-3 días
        val calendario = Calendar.getInstance()
        val fechas = mutableListOf<Date>()
        for (i in -3..3) {
            val cal = calendario.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, i)
            fechas.add(cal.time)
        }

        // Adaptador con resaltado
        val fechaAdapter = FechaAdapter(fechas, fechaActual) { fecha ->
            fechaSeleccionada = formato.format(fecha)
            escucharDatosTiempoReal(fechaSeleccionada)
        }

        rvFechas.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rvFechas.adapter = fechaAdapter

        // Inicia con la fecha actual
        escucharDatosTiempoReal(fechaActual)

        return view
    }

    private fun escucharDatosTiempoReal(fecha: String) {
        valueEventListener?.let { database.removeEventListener(it) }

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || !isAdded) return

                val items = mutableListOf<Historial>()
                for (data in snapshot.children) {
                    val item = data.getValue(Historial::class.java)
                    if (item != null && item.fecha_texto?.startsWith(fecha) == true) {
                        items.add(item)
                    }
                }

                if (items.isEmpty()) {
                    barChart.clear()
                    lineChart.clear()
                    return
                }

                items.sortBy { it.fecha_texto }
                val ultimos = items.takeLast(6)

                val entriesTemp = ArrayList<Entry>()
                val entriesSuelo = ArrayList<Entry>()
                val entriesAmbiente = ArrayList<Entry>()
                val labels = ArrayList<String>()
                var index = 0f
                var ultimo: Historial? = null

                for (item in ultimos) {
                    val hora = item.fecha_texto?.split(" ")?.getOrNull(1)?.substring(0, 5) ?: ""
                    item.temperatura?.let { entriesTemp.add(Entry(index, it.toFloat())) }
                    item.humedadSuelo?.let { entriesSuelo.add(Entry(index, it.toFloat())) }
                    item.humedadAmbiental?.let { entriesAmbiente.add(Entry(index, it.toFloat())) }
                    labels.add(hora)
                    ultimo = item
                    index++
                }

                // === BARRAS ===
                ultimo?.let { item ->
                    val entriesBar = arrayListOf(
                        BarEntry(0f, item.humedadSuelo?.toFloat() ?: 0f),
                        BarEntry(1f, item.humedadAmbiental?.toFloat() ?: 0f),
                        BarEntry(2f, item.temperatura?.toFloat() ?: 0f)
                    )

                    val barDataSet = BarDataSet(entriesBar, "Última medición").apply {
                        setColors(
                            ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark),
                            ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark),
                            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                        )
                    }

                    val barData = BarData(barDataSet)
                    barData.barWidth = 0.6f
                    barChart.data = barData
                    barChart.axisLeft.axisMinimum = 0f
                    barChart.axisRight.isEnabled = false
                    barChart.xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                            listOf("Suelo", "Ambiente", "Temp")
                        )
                        granularity = 1f
                    }
                    if (isFirstLoad) {
                        barChart.animateY(600)
                        isFirstLoad = false
                    }
                    barChart.invalidate()
                }

                // === LÍNEAS ===
                val dataSets = mutableListOf<ILineDataSet>()

                fun crearSet(entries: ArrayList<Entry>, label: String, color: Int, fill: Int): LineDataSet {
                    return LineDataSet(entries, label).apply {
                        this.color = ContextCompat.getColor(requireContext(), color)
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawValues(false)
                        setDrawFilled(true)
                        fillColor = ContextCompat.getColor(requireContext(), fill)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }
                }

                dataSets.add(crearSet(entriesTemp, "Temperatura", android.R.color.holo_red_dark, android.R.color.holo_red_light))
                dataSets.add(crearSet(entriesSuelo, "H. Suelo", android.R.color.holo_blue_dark, android.R.color.holo_blue_light))
                dataSets.add(crearSet(entriesAmbiente, "H. Ambiente", android.R.color.holo_green_dark, android.R.color.holo_green_light))

                if (lineChart.data == null) {
                    lineChart.data = LineData(dataSets)
                    lineChart.axisLeft.axisMinimum = 0f
                    lineChart.axisRight.isEnabled = false
                    lineChart.xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
                        valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    }
                    lineChart.animateXY(800, 800)
                } else {
                    lineChart.data = LineData(dataSets)
                    lineChart.notifyDataSetChanged()
                }

                lineChart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        }

        database.addValueEventListener(valueEventListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        valueEventListener?.let { database.removeEventListener(it) }
    }
}
