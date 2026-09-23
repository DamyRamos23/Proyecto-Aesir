package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.repository.RoadmapRepository

class RegistrarLeccionCompletadaUseCase(private val repository: RoadmapRepository) {
    suspend operator fun invoke(temaId: String) {
        repository.marcarLeccionCompletada(temaId)
    }
}
