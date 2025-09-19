package com.example.fieldsmart

data class Sensores(
    val bomba: Boolean? = null,
    val enEspera: Boolean? = null,
    val humedadAmbiental: Long? = null,
    val humedadSuelo: Long? = null,
    val ip: String? = null,
    val modoAutomatico: Boolean? = null,
    val temperatura: Long? = null,
    val tiempoRestanteEspera: Long? = null,
    val tiempoUso: Long? = null,
    val ultimoRiego: Long? = null
)
