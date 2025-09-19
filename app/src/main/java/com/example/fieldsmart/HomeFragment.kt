package com.example.fieldsmart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var tvSaludo: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvSaludo = view.findViewById(R.id.tvSaludo)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("usuarios")

        val userId = auth.currentUser?.uid

        if (userId != null) {
            database.child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    tvSaludo.text = "Hola ${usuario?.nombre}"
                } else {
                    tvSaludo.text = "Hola!"
                }
            }.addOnFailureListener {
                tvSaludo.text = "Error al obtener datos"
            }
        } else {
            tvSaludo.text = "Hola visitante"
        }

        return view
    }
}
