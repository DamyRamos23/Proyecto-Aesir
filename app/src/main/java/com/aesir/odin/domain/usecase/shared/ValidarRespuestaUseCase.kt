package com.aesir.odin.domain.usecase.shared

import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.ResultadoRespuesta

/**
 * Validador reutilizable entre lecciones (CU-05) y preguntas flash (CU-06).
 * - CORRECTA: se seleccionaron exactamente las opciones correctas.
 * - INCOMPLETA: todas las seleccionadas son correctas, pero faltó alguna.
 * - INCORRECTA: se seleccionó al menos una opción incorrecta, o ninguna.
 */
class ValidarRespuestaUseCase {
    operator fun invoke(seleccionadas: List<Int>, correctas: List<Int>): ResultadoRespuesta {
        val elegidas = seleccionadas.toSet()
        val esperadas = correctas.toSet()
        val estado = when {
            elegidas == esperadas -> EstadoRespuesta.CORRECTA
            elegidas.isNotEmpty() && esperadas.containsAll(elegidas) -> EstadoRespuesta.INCOMPLETA
            else -> EstadoRespuesta.INCORRECTA
        }
        return ResultadoRespuesta(estado, correctas)
    }
}
