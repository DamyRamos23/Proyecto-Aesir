package com.aesir.odin.domain.usecase.memorama

/**
 * RF-15: puntos de 0 a 100 según el tiempo empleado.
 *
 * - Hasta [TIEMPO_MAX_PUNTAJE_SEG] segundos: 100 puntos.
 * - Desde [TIEMPO_LIMITE_SEG] segundos: 0 puntos.
 * - Entre ambos, bajan de forma lineal.
 */
class CalcularPuntosMemoramaUseCase {

    operator fun invoke(tiempoEmpleadoSeg: Long): Int = when {
        tiempoEmpleadoSeg <= TIEMPO_MAX_PUNTAJE_SEG -> PUNTAJE_MAXIMO
        tiempoEmpleadoSeg >= TIEMPO_LIMITE_SEG -> 0
        else -> {
            val excedente = (tiempoEmpleadoSeg - TIEMPO_MAX_PUNTAJE_SEG).toDouble()
            val rango = (TIEMPO_LIMITE_SEG - TIEMPO_MAX_PUNTAJE_SEG).toDouble()
            Math.round(PUNTAJE_MAXIMO * (1 - excedente / rango)).toInt().coerceIn(0, PUNTAJE_MAXIMO)
        }
    }

    companion object {
        const val TIEMPO_MAX_PUNTAJE_SEG = 45L
        const val TIEMPO_LIMITE_SEG = 240L
        const val PUNTAJE_MAXIMO = 100
    }
}
