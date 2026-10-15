package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.hidromiel.DiagnosticoCompletado
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.model.RespuestaDiagnostica
import com.aesir.odin.domain.model.ResultadoDiagnostico
import com.aesir.odin.domain.repository.DiagnosticoRepository
import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * Cierra la evaluación diagnóstica cuando se respondieron todas las preguntas:
 * 1. Evalúa las respuestas (EvaluarDiagnosticoUseCase).
 * 2. Registra la evaluación como completada con su resultado (RF-90).
 * 3. Marca como conocidos los temas demostrados (RF-87).
 * 4. Avisa al MOD-01 (RF-88): el módulo registra esos niveles como superados
 *    por diagnóstico, sin dar hidromiel (RF-99), y a partir de ellos se abren
 *    los niveles conectados del árbol. Sin temas conocidos no se toca nada y
 *    el mundo conserva su progreso inicial (A7.1, RF-98).
 *
 * [moduloHidromiel] es opcional solo para poder probar el caso de uso sin el módulo.
 */
class FinalizarDiagnosticoUseCase(
    private val evaluarDiagnosticoUseCase: EvaluarDiagnosticoUseCase,
    private val diagnosticoRepository: DiagnosticoRepository,
    private val roadmapRepository: RoadmapRepository,
    private val moduloHidromiel: ModuloHidromiel? = null,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(
        mundoId: String,
        preguntas: List<PreguntaDiagnostica>,
        respuestas: List<RespuestaDiagnostica>
    ): ResultadoDiagnostico {
        val resultado = evaluarDiagnosticoUseCase(mundoId, preguntas, respuestas)
        diagnosticoRepository.guardarResultado(resultado)

        if (resultado.temasConocidos.isNotEmpty()) {
            roadmapRepository.marcarTemasConocidos(mundoId, resultado.temasConocidos)
            moduloHidromiel?.alCompletarDiagnostico(
                DiagnosticoCompletado(idUsuario, mundoId, resultado.temasConocidos)
            )
        }
        return resultado
    }
}
