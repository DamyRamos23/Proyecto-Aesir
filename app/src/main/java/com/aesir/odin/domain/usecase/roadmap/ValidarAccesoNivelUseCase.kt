package com.aesir.odin.domain.usecase.roadmap

import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * Valida si un nivel (mundo) está desbloqueado, tal como se ve en el
 * diagrama de secuencia (ValidarAccesoNivelUseCase). No aparece dibujado
 * en el diagrama de clases, que solo muestra ValidarAccesoTemaUseCase,
 * pero es necesario para reproducir el flujo del diagrama de secuencia y
 * se implementa de forma simétrica a ese caso de uso.
 */
class ValidarAccesoNivelUseCase(
    private val repository: RoadmapRepository
) {
    suspend operator fun invoke(nivelId: String): Boolean =
        repository.obtenerMundo(nivelId).desbloqueado
}
