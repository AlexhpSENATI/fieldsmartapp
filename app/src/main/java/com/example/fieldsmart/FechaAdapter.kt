package com.example.fieldsmart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class FechaAdapter(
    private val fechas: List<Date>,
    private val fechaActual: String,
    private val onClick: (Date) -> Unit
) : RecyclerView.Adapter<FechaAdapter.FechaViewHolder>() {

    private val formatoDia = SimpleDateFormat("dd", Locale.getDefault())
    private val formatoNombreDia = SimpleDateFormat("EEE", Locale.getDefault())
    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var seleccionada: String = fechaActual

    inner class FechaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDia: TextView = view.findViewById(R.id.tvDia)
        val tvNombreDia: TextView = view.findViewById(R.id.tvNombreDia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FechaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fecha, parent, false)
        return FechaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FechaViewHolder, position: Int) {
        val fecha = fechas[position]
        val fechaTexto = formatoFecha.format(fecha)
        holder.tvDia.text = formatoDia.format(fecha)
        holder.tvNombreDia.text = formatoNombreDia.format(fecha)

        val contexto = holder.itemView.context
        val esSeleccionada = fechaTexto == seleccionada
        val esHoy = fechaTexto == fechaActual

        holder.itemView.setOnClickListener {
            seleccionada = fechaTexto
            onClick(fecha)
            notifyDataSetChanged()
        }

       // Fondo redondeado según estado
        val fondo = when {
            esSeleccionada -> ContextCompat.getDrawable(contexto, R.drawable.bg_fecha_seleccionada)
            esHoy -> ContextCompat.getDrawable(contexto, R.drawable.bg_fecha_hoy)
            else -> ContextCompat.getDrawable(contexto, R.drawable.bg_fecha_normal)
        }
        holder.itemView.background = fondo

//        holder.itemView.setOnClickListener {
//            seleccionada = fechaTexto
//            onClick(fecha)
//            notifyDataSetChanged()
//        }
//
//        val colorFondo = when {
//            esSeleccionada -> ContextCompat.getColor(contexto, R.color.teal_200)
//            esHoy -> ContextCompat.getColor(contexto, R.color.purple_200)
//            else -> ContextCompat.getColor(contexto, android.R.color.transparent)
//        }
//        holder.itemView.setBackgroundColor(colorFondo)
    }

    override fun getItemCount(): Int = fechas.size
}
