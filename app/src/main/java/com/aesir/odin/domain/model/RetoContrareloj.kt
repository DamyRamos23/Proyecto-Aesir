package com.aesir.odin.domain.model

import com.aesir.odin.domain.hidromiel.PoliticaCosto

/**
 * Reto armado para una partida de CU-11: las preguntas y el tiempo general.
 *
 * RF-154: cada acierto suma [PUNTOS_POR_ACIERTO] puntos y
 * PoliticaCosto.CONTRARRELOJ_CORRECTA de hidromiel; cada error resta
 * [PUNTOS_POR_ERROR] puntos y PoliticaCosto.CONTRARRELOJ_FALLO de hidromiel.
 * Ninguno de los dos acumulados baja de 0. El hidromiel se paga al terminar.
 */
data class RetoContrareloj(
    val preguntas: List<PreguntaFlash>,
    val duracionSegundos: Int = DURACION_SEGUNDOS,
    val puntosPorAcierto: Int = PUNTOS_POR_ACIERTO
) {
    fun totalPreguntas(): Int = preguntas.size

    companion object {
        const val DURACION_SEGUNDOS = 120
        const val MAX_PREGUNTAS = 20
        const val PUNTOS_POR_ACIERTO = 10
        const val PUNTOS_POR_ERROR = 5

        /** Acumulados del reto (puntos, hidromiel) después de una respuesta. */
        fun acumular(puntos: Int, hidromiel: Int, correcta: Boolean): Pair<Int, Int> =
            if (correcta) (puntos + PUNTOS_POR_ACIERTO) to (hidromiel + PoliticaCosto.CONTRARRELOJ_CORRECTA)
            else (puntos - PUNTOS_POR_ERROR).coerceAtLeast(0) to (hidromiel - PoliticaCosto.CONTRARRELOJ_FALLO).coerceAtLeast(0)
    }
}
