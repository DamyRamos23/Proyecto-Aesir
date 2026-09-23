package com.aesir.odin.domain.usecase.roadmap

import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * Obtiene los temas de un mundo concreto, para poblar RoadmapScreen.
 */
class ObtenerTemasPorMundoUseCase(
    private val repository: RoadmapRepository
) {
    suspend operator fun invoke(mundoId: String): List<Tema> =
        repository.obtenerTemasPorMundo(mundoId)
}
