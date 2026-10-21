package com.aesir.odin.domain.usecase.shared

import com.aesir.odin.domain.sonido.eventoDeRespuesta
import com.aesir.odin.domain.sonido.ModuloSonido
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.ResultadoRespuesta

/**
 * Validador reutilizable entre lecciones (CU-05) y preguntas flash (CU-06).
 * - CORRECTA: se seleccionaron exactamente las opciones correctas.
 * - INCOMPLETA: todas las seleccionadas son correctas, pero faltó alguna.
 * - INCORRECTA: se seleccionó al menos una opción incorrecta, o ninguna.
 */
class ValidarRespuestaUseCase {
    /**
     * [avisarSonido]: MOD-02, disparador 2 (suena el acierto, el error o la
     * respuesta incompleta). Se apaga cuando no es el alumno respondiendo en ese
     * momento (p. ej. al calificar todo el diagnóstico de una vez) o cuando el
     * caso de uso avisa el resultado junto con otros efectos.
     */
    operator fun invoke(seleccionadas: List<Int>, correctas: List<Int>, avisarSonido: Boolean = true): ResultadoRespuesta {
        val elegidas = seleccionadas.toSet()
        val esperadas = correctas.toSet()
        val estado = when {
            elegidas == esperadas -> EstadoRespuesta.CORRECTA
            elegidas.isNotEmpty() && esperadas.containsAll(elegidas) -> EstadoRespuesta.INCOMPLETA
            else -> EstadoRespuesta.INCORRECTA
        }
        if (avisarSonido) ModuloSonido.avisar(eventoDeRespuesta(estado))
        return ResultadoRespuesta(estado, correctas)
    }
}
