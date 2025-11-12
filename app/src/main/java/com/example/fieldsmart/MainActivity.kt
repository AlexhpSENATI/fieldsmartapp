package com.example.fieldsmart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 🔹 Cambiar color de la barra de estado (hora, señal, batería)
        window.statusBarColor = ContextCompat.getColor(this, R.color.moradoxd)


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Fragmento inicial
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, HomeFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            when (item.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_estadistica -> selectedFragment = EstadisticaFragment()
                R.id.nav_settings -> selectedFragment = SettingsFragment()
                R.id.nav_control -> selectedFragment = ControlFragmente()
            }

            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, selectedFragment)
                    .commit()
            }

            true
        }
    }
}
