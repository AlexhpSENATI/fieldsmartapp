package com.example.fieldsmart

data class Historial(
    val bomba: Boolean? = null,
    val fecha_epoch: Long? = null,
    val fecha_texto: String? = null,
    val humedadAmbiental: Int? = null,
    val humedadSuelo: Int? = null,
    val temperatura: Int? = null
)
