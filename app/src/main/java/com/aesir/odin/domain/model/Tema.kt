package com.aesir.odin.domain.model

/**
 * Representa un tema dentro de un mundo del roadmap (un nodo hijo del árbol
 * de habilidades). Corresponde al modelo `Tema` de la capa Negocio en el
 * diagrama de clases.
 */
data class Tema(
    val id: String,
    val mundoId: String,
    val nombre: String,
    val descripcion: String,
    val orden: Int,
    val desbloqueado: Boolean,
    val completado: Boolean,
    val introduccionVista: Boolean
)
