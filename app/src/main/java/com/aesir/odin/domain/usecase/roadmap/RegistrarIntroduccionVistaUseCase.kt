package com.aesir.odin.domain.usecase.roadmap


import com.aesir.odin.domain.repository.RoadmapRepository

class RegistrarIntroduccionVistaUseCase(
    private val repository: RoadmapRepository
) {
    // Paso 14: invoke(temaId) -> Paso 18: Unit
    suspend operator fun invoke(temaId: String) {
        repository.marcarIntroduccionVista(temaId)
    }
}


