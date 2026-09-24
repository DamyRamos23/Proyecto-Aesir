package com.aesir.odin.domain.usecase.roadmap

import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * Obtiene la lista de mundos del roadmap para poblar MundosScreen.
 */
class ObtenerMundosUseCase(
    private val repository: RoadmapRepository
) {
    suspend operator fun invoke(): List<Mundo> = repository.obtenerMundos()
}
