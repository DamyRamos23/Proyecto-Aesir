package com.aesir.odin.domain.model

data class HistorialError(
    val id: String,
    val tipoDesafio: String,
    val actividadId: String,
    val descripcion: String,
    val fecha: Long
)
