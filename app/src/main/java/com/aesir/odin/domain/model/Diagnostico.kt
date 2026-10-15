package com.aesir.odin.domain.model

/**
 * Modelos del CU-10 Realizar evaluación diagnóstico (diagrama de clases
 * CU-10, paquete Models). EstadoRespuesta y ResultadoRespuesta se reutilizan
 * de CU-05/CU-06 (ResultadoRespuesta.kt).
 */

/** Pregunta de opción múltiple asociada a un tema (nivel) del mundo. */
data class PreguntaDiagnostica(
    val id: String,
    val mundoId: String,
    val temaId: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestasCorrectas: List<Int>
) {
    val esSeleccionMultiple: Boolean get() = respuestasCorrectas.size > 1
}

/** Lo que el alumno eligió en una pregunta (RF-83, RF-84). */
data class RespuestaDiagnostica(
    val preguntaId: String,
    val seleccionadas: List<Int>
)

enum class EstadoDiagnostico { PENDIENTE, COMPLETADA, OMITIDA }

/**
 * Resultado de la evaluación (RF-89, RF-90). [temasConocidos] son los ids de
 * los temas que el alumno demostró dominar. No incluye puntos (RF-99).
 */
data class ResultadoDiagnostico(
    val mundoId: String,
    val estado: EstadoDiagnostico,
    val aciertos: Int,
    val totalPreguntas: Int,
    val temasConocidos: List<String>,
    val fecha: Long
)
