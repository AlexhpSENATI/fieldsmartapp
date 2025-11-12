package com.example.fieldsmart

data class Historial(
    var bomba: Boolean? = null,
    var humedadSuelo: Double? = null,
    var fecha_epoch: Long? = null,
    var humedadAmbiental: Double? = null,
    var temperatura: Double? = null,
    var timestamp: Long? = null,
    var fecha_texto: String? = null
)
