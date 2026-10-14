package com.aesir.odin.domain.usecase.crucigrama

import com.aesir.odin.domain.model.EstadoRespuesta

/**
 * Suma puntos por cada palabra correcta y aplica la penalización por cada
 * respuesta incompleta o incorrecta (RF-53). El puntaje nunca baja de 0.
 */
class ActualizarPuntajeUseCase {

    operator fun invoke(puntajeActual: Int, estado: EstadoRespuesta): Int = when (estado) {
        EstadoRespuesta.CORRECTA -> puntajeActual + PUNTOS_POR_ACIERTO
        EstadoRespuesta.INCORRECTA, EstadoRespuesta.INCOMPLETA -> (puntajeActual - PENALIZACION).coerceAtLeast(0)
    }

    companion object {
        const val PUNTOS_POR_ACIERTO = 10
        const val PENALIZACION = 3
    }
}
