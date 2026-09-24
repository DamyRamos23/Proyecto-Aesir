package com.aesir.odin.domain.model

/**
 * Representa un "mundo" del roadmap (un nodo raíz del árbol de habilidades,
 * p. ej. "Fundamentos" o "Requerimientos"). Corresponde al modelo `Mundo`
 * de la capa Negocio en el diagrama de clases.
 */
data class Mundo(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val orden: Int,
    val desbloqueado: Boolean
)
