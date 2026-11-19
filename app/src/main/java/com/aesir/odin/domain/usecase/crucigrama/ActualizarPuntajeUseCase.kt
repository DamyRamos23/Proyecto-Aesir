package com.aesir.odin.domain.usecase.crucigrama

import com.aesir.odin.domain.hidromiel.TipoAyuda
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.ReglasCrucigrama

/**
 * Puntaje del crucigrama (CU-07, paso 10 y A5.1): empieza en 100 y baja 5
 * por cada respuesta incorrecta o incompleta (RF-98), 2 por cada letra
 * revelada y 10 por cada palabra revelada (RF-99). Nunca baja de 0 (RF-100).
 */
class ActualizarPuntajeUseCase {

    operator fun invoke(puntajeActual: Int, estado: EstadoRespuesta): Int = when (estado) {
        EstadoRespuesta.CORRECTA -> puntajeActual
        EstadoRespuesta.INCORRECTA, EstadoRespuesta.INCOMPLETA -> (puntajeActual - PENALIZACION).coerceAtLeast(0)
    }

    /** A5.1, paso 3: la ayuda resta 2 (letra) o 10 (palabra) puntos. */
    fun porAyuda(puntajeActual: Int, ayuda: TipoAyuda): Int {
        val resta = when (ayuda) {
            TipoAyuda.REVELAR_LETRA -> ReglasCrucigrama.PENALIZACION_LETRA
            TipoAyuda.REVELAR_PALABRA -> ReglasCrucigrama.PENALIZACION_PALABRA
        }
        return (puntajeActual - resta).coerceAtLeast(0)
    }

    companion object {
        const val PENALIZACION = ReglasCrucigrama.PENALIZACION_ERROR
    }
}
