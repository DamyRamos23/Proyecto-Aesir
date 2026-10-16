package com.aesir.odin.domain.model

/** Modelos del CU-08 Resolver reto de fin de mundo (Ragnarök). */

object ReglasReto {
    const val PREGUNTAS = 10
    const val PUNTAJE_MINIMO = 70
}

/**
 * Pregunta del reto. Viene de un ejercicio de las lecciones del mundo; las
 * opciones ya están mezcladas y [respuestasCorrectas] apunta a esa mezcla.
 */
data class PreguntaReto(
    val id: String,
    val temaId: String,
    val temaNombre: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestasCorrectas: List<Int>
) {
    val esSeleccionMultiple: Boolean get() = respuestasCorrectas.size > 1
}

data class RetoFinMundo(
    val mundoId: String,
    /** Tema de tipo Ragnarök: es el nivel que el MOD-01 cobra y marca como superado. */
    val ragnarokId: String,
    val preguntas: List<PreguntaReto>
) {
    /** Temas distintos que cubren las preguntas. */
    val temasCubiertos: Int get() = preguntas.map { it.temaId }.toSet().size
}

/** Resultado de intentar abrir el reto (pasos 1 y 2, flujo alterno A2.1). */
sealed interface DisponibilidadReto {
    data class Disponible(val reto: RetoFinMundo) : DisponibilidadReto

    /** A2.1: faltan temas por completar. */
    data class Bloqueado(val temasPendientes: List<String>) : DisponibilidadReto

    /** El mundo no tiene preguntas registradas. */
    data object SinPreguntas : DisponibilidadReto
}

/** Tema en el que hubo errores durante el reto (A10.1). */
data class TemaConErrores(val temaId: String, val nombre: String, val errores: Int)

data class ResultadoRetoFinMundo(
    val mundoId: String,
    val puntaje: Int,
    val aciertos: Int,
    val errores: Int,
    val superado: Boolean,
    val fecha: Long,
    val temasConErrores: List<TemaConErrores>,
    /** Mundo que se desbloquea al superar el reto; null si no se superó o no hay otro. */
    val mundoDesbloqueadoId: String?,
    val mundoDesbloqueadoNombre: String?,
    /** A10.2: se superó el reto del último mundo. */
    val ultimoMundo: Boolean
)

/** Intento guardado en el historial de resultados (RF-66). */
data class IntentoRetoFinMundo(
    val mundoId: String,
    val puntaje: Int,
    val aciertos: Int,
    val errores: Int,
    val superado: Boolean,
    val abandonado: Boolean,
    val fecha: Long
)
