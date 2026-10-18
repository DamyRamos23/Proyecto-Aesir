package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.model.EstadoDiagnostico
import com.aesir.odin.domain.repository.DiagnosticoRepository
import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * RF-81 y precondiciones del CU-10: se presenta la evaluación si el mundo
 * está desbloqueado y su evaluación sigue PENDIENTE (no se ha completado ni
 * omitido antes).
 */
class ValidarAccesoDiagnosticoUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val diagnosticoRepository: DiagnosticoRepository
) {
    suspend operator fun invoke(mundoId: String): Boolean =
        roadmapRepository.mundoDesbloqueado(mundoId) &&
            diagnosticoRepository.estadoDiagnostico(mundoId) == EstadoDiagnostico.PENDIENTE
}
