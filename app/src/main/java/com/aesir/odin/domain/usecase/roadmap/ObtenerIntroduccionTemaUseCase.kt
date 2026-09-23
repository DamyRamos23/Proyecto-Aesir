package com.aesir.odin.domain.usecase.roadmap

import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.RoadmapRepository

class ObtenerIntroduccionTemaUseCase(
    private val repository: RoadmapRepository
) {
    // Paso 4: invoke(temaId) -> Paso 8: Tema
    suspend operator fun invoke(temaId: String): Tema? {
        return repository.obtenerTema(temaId)
    }
}
