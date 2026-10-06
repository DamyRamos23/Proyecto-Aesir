package com.aesir.odin.domain.usecase.roadmap

import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * Obtiene un solo mundo por id. Lo usan las pantallas de roadmap, introducción,
 * lección y resumen para saber a qué mundo pertenecen y aplicar su estilo
 * visual (colores, runas, paisaje).
 */
class ObtenerMundoUseCase(
    private val repository: RoadmapRepository
) {
    suspend operator fun invoke(mundoId: String): Mundo = repository.obtenerMundo(mundoId)
}
