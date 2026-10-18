package com.aesir.odin.domain.usecase.memorama

import com.aesir.odin.domain.repository.MemoramaRepository
import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * RF-05: el alumno puede acceder al memorama si el mundo está desbloqueado y
 * tiene un banco de pares suficiente. El cobro de la entrada y el estado del
 * nivel (precondición "MOD-01 autorizó el acceso") los resuelve después el
 * MOD-01 en RoadmapViewModel.intentarAbrirTema.
 */
class ValidarAccesoMemoramaUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val memoramaRepository: MemoramaRepository
) {
    suspend operator fun invoke(mundoId: String): Boolean =
        roadmapRepository.mundoDesbloqueado(mundoId) && memoramaRepository.mundoTieneMemorama(mundoId)
}
