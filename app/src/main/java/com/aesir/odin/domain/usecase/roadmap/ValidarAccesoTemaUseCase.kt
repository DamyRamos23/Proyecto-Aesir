package com.aesir.odin.domain.usecase.roadmap

import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * Valida si un tema está desbloqueado, antes de permitir navegar a su
 * introducción.
 */
class ValidarAccesoTemaUseCase(
    private val repository: RoadmapRepository
) {
    suspend operator fun invoke(temaId: String): Boolean =
        repository.obtenerTema(temaId).desbloqueado
}
