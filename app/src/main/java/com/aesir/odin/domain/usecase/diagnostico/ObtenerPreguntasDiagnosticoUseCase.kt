package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.repository.DiagnosticoRepository

/** RF-82: las [NUM_PREGUNTAS] preguntas de la evaluación del mundo. */
class ObtenerPreguntasDiagnosticoUseCase(
    private val repository: DiagnosticoRepository
) {
    suspend operator fun invoke(mundoId: String): List<PreguntaDiagnostica> =
        repository.obtenerPreguntasPorMundo(mundoId, NUM_PREGUNTAS)

    companion object {
        const val NUM_PREGUNTAS = 10
    }
}
