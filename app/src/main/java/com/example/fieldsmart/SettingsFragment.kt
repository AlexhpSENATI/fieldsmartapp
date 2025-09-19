package com.example.fieldsmart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val tv = view.findViewById<TextView>(R.id.text_configuracion)
        tv.text = "Bienvenido a la pantalla de Configuracion 🏠"
        return view
    }
}
