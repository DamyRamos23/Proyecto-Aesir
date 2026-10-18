package com.aesir.odin.domain.model

/**
 * Pregunta del banco de Pregunta Flash (CU-06). Si [respuestasCorrectas] tiene
 * más de un índice, el alumno debe seleccionar todas las opciones correctas.
 */
data class PreguntaFlash(
    val id: String,
    val temaId: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestasCorrectas: List<Int>
) {
    val esSeleccionMultiple: Boolean get() = respuestasCorrectas.size > 1
}
