package com.example.fieldsmart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView

class ProductsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false)
        val tv = view.findViewById<TextView>(R.id.text_producto)
        tv.text = "Bienvenido a la pantalla de producto 🏠"
        return view
    }
}
