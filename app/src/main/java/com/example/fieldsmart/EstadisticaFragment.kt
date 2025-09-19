package com.example.fieldsmart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estadistica, container, false)
        barChart = view.findViewById(R.id.barChart)

        database = FirebaseDatabase.getInstance().getReference("historial")

        // 🔹 Escuchar SOLO el último registro
        database.limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d("FirebaseDebug", "No hay datos en historial")
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
                            resources.getColor(android.R.color.holo_blue_dark, null),
                            resources.getColor(android.R.color.holo_green_dark, null),
                            resources.getColor(android.R.color.holo_red_dark, null)
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
        })

        return view
    }
}
