package com.example.fieldsmart

data class Historial(
    var humedadSuelo: Double? = null,
    var humedadAmbiental: Double? = null,
    var temperatura: Double? = null,
    var timestamp: Long? = null,
    var fecha_texto: String? = null
)
