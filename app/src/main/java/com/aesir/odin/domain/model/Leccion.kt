package com.aesir.odin.domain.model

data class Leccion(
    val id: String,
    val temaId: String,
    val titulo: String,
    val introduccion: String,
    val orden: Int,
    val ejercicios: List<Ejercicio>,
    val completada: Boolean
)
