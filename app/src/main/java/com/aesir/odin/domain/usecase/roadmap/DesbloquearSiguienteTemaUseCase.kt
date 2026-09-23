package com.aesir.odin.domain.usecase.roadmap

import com.aesir.odin.domain.repository.RoadmapRepository

/** Desbloquea el tema que sigue (por orden, dentro del mismo mundo) al tema completado. */
class DesbloquearSiguienteTemaUseCase(private val repository: RoadmapRepository) {
    suspend operator fun invoke(temaIdCompletado: String) {
        val completado = repository.obtenerTema(temaIdCompletado) ?: return
        val siguiente = repository.obtenerTemas()
            .filter { it.mundoId == completado.mundoId && it.orden > completado.orden }
            .minByOrNull { it.orden }
            ?: return
        if (!siguiente.desbloqueado) repository.desbloquearTema(siguiente.id)
    }
}
