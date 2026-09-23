package com.aesir.odin.domain.model

/**
 * Modelo de dominio del Tema. No conoce nada de Room ni de la base de datos.
 */
data class Tema(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val introduccionVista: Boolean
)
