package com.aesir.odin.domain.model

/**
 * Reto armado para una partida de CU-11: las preguntas y el tiempo general.
 *
 * [PUNTOS_POR_ACIERTO] es un valor provisional: el CU-11 no define cuánto suma
 * cada respuesta correcta; se ajustará cuando se diseñe el modelo de gamificación.
 */
data class RetoContrareloj(
    val preguntas: List<PreguntaFlash>,
    val duracionSegundos: Int = DURACION_SEGUNDOS,
    val puntosPorAcierto: Int = PUNTOS_POR_ACIERTO
) {
    fun totalPreguntas(): Int = preguntas.size

    companion object {
        const val DURACION_SEGUNDOS = 120
        const val PUNTOS_POR_ACIERTO = 10
    }
}
