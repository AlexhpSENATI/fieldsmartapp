package com.example.fieldsmart

data class Sensores(
    val bomba: Boolean = false,
    val enEspera: Boolean = false,
    val humedadAmbiental: Int = 0,
    val humedadSuelo: Int = 0,
    val ip: String = "",
    val modoAutomatico: Boolean = false,
    val temperatura: Int = 0,
    val tiempoRestanteEspera: Int = 0,
    val tiempoUso: Int = 0,
    val ultimoRiego: Int = 0
)
