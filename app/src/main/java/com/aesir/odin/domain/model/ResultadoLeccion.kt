package com.aesir.odin.domain.model

data class ResultadoLeccion(
    val leccionId: String,
    val puntaje: Int,
    val erroresCometidos: Int,
    val aprobada: Boolean,
    val fecha: Long
)
