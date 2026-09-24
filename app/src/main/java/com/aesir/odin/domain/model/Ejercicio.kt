package com.aesir.odin.domain.model

/**
 * Ejercicio de opción múltiple. Si [respuestasCorrectas] tiene más de un índice,
 * el estudiante debe seleccionar todas las opciones correctas.
 */
data class Ejercicio(
    val id: String,
    val leccionId: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestasCorrectas: List<Int>,
    val orden: Int
) {
    val esSeleccionMultiple: Boolean get() = respuestasCorrectas.size > 1
}
