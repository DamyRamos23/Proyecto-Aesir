package com.aesir.odin.domain.model

/**
 * Pregunta del banco de Pregunta Flash. Es compartida entre CU-06 (Pregunta
 * Flash) y CU-11 (Reto Contrarreloj), que reutiliza ese banco.
 * Si [respuestasCorrectas] tiene más de un índice, hay que seleccionar todas.
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
