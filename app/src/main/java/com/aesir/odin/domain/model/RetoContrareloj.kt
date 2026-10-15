package com.aesir.odin.domain.model

import com.aesir.odin.domain.hidromiel.PoliticaCosto

/**
 * Reto armado para una partida de CU-11: las preguntas y el tiempo general.
 *
 * El puntaje del reto es el hidromiel que se gana: [PUNTOS_POR_ACIERTO] por
 * cada respuesta correcta (MOD-01). Se abona solo si el reto se termina.
 */
data class RetoContrareloj(
    val preguntas: List<PreguntaFlash>,
    val duracionSegundos: Int = DURACION_SEGUNDOS,
    val puntosPorAcierto: Int = PUNTOS_POR_ACIERTO
) {
    fun totalPreguntas(): Int = preguntas.size

    companion object {
        const val DURACION_SEGUNDOS = 120
        const val PUNTOS_POR_ACIERTO = PoliticaCosto.CONTRARRELOJ_CORRECTA
    }
}
