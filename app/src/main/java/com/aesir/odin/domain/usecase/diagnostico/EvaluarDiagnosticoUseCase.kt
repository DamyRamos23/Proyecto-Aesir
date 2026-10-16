package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.model.EstadoDiagnostico
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.model.RespuestaDiagnostica
import com.aesir.odin.domain.model.ResultadoDiagnostico
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase

/**
 * RF-85, RF-86, RF-87 y RF-89: valida cada respuesta y determina qué temas
 * del mundo domina el alumno.
 *
 * - Solo cuenta como acierto una respuesta CORRECTA; INCORRECTA o INCOMPLETA
 *   se registran como incorrectas (A4.1).
 * - Un tema es conocido si sus aciertos llegan a [UMBRAL_ACIERTOS_TEMA]. Si el
 *   tema tiene menos preguntas que el umbral, hay que acertarlas todas.
 */
class EvaluarDiagnosticoUseCase(
    private val validarRespuestaUseCase: ValidarRespuestaUseCase,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    operator fun invoke(
        mundoId: String,
        preguntas: List<PreguntaDiagnostica>,
        respuestas: List<RespuestaDiagnostica>
    ): ResultadoDiagnostico {
        val seleccionPorPregunta = respuestas.associate { it.preguntaId to it.seleccionadas }

        val correctas = preguntas.filter { pregunta ->
            val seleccionadas = seleccionPorPregunta[pregunta.id].orEmpty()
            validarRespuestaUseCase(seleccionadas, pregunta.respuestasCorrectas).estado == EstadoRespuesta.CORRECTA
        }.map { it.id }.toSet()

        val temasConocidos = preguntas
            .groupBy { it.temaId }
            .filter { (_, delTema) ->
                val aciertos = delTema.count { it.id in correctas }
                aciertos >= minOf(UMBRAL_ACIERTOS_TEMA, delTema.size)
            }
            .keys
            .toList()

        return ResultadoDiagnostico(
            mundoId = mundoId,
            estado = EstadoDiagnostico.COMPLETADA,
            aciertos = correctas.size,
            totalPreguntas = preguntas.size,
            temasConocidos = temasConocidos,
            fecha = reloj()
        )
    }

    companion object {
        const val UMBRAL_ACIERTOS_TEMA = 2
    }
}
