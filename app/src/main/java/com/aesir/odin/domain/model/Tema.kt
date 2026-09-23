package com.aesir.odin.domain.model

data class Tema(
    val id: String,
    val mundoId: String,
    val nombre: String,
    val orden: Int,
    val desbloqueado: Boolean,
    val introduccionVista: Boolean,
    val leccionCompletada: Boolean
)
