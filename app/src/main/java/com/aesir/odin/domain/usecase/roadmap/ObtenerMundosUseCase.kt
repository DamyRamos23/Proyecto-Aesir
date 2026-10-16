package com.aesir.odin.domain.usecase.roadmap

import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * Obtiene la lista de mundos del roadmap para poblar MundosScreen.
 */
class ObtenerMundosUseCase(
    private val repository: RoadmapRepository
) {
    suspend operator fun invoke(): List<Mundo> = repository.obtenerMundos().map { it.conModoAdmin() }
}

/** En modo admin todos los mundos se muestran desbloqueados. */
fun Mundo.conModoAdmin(): Mundo = if (ModoAdmin.estaActivo && !desbloqueado) copy(desbloqueado = true) else this

