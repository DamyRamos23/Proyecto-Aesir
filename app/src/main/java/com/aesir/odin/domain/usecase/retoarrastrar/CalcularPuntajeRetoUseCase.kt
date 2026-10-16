package com.aesir.odin.domain.usecase.retoarrastrar

/** Paso 8 del CU-09 (RF-76): 100 puntos menos 20 por cada intento incorrecto. */
class CalcularPuntajeRetoUseCase {
    operator fun invoke(intentosFallidos: Int): Int =
        (PUNTAJE_INICIAL - intentosFallidos.coerceAtLeast(0) * PENALIZACION_POR_INTENTO).coerceAtLeast(0)

    companion object {
        const val PUNTAJE_INICIAL = 100
        const val PENALIZACION_POR_INTENTO = 20
    }
}
