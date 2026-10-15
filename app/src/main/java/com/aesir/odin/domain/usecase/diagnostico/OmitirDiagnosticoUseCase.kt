package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.repository.DiagnosticoRepository

/**
 * A1.1 / RF-94: registra la evaluación como omitida. Los niveles del mundo
 * se quedan en su estado inicial definido por el MOD-01.
 */
class OmitirDiagnosticoUseCase(
    private val repository: DiagnosticoRepository
) {
    suspend operator fun invoke(mundoId: String) {
        repository.registrarOmision(mundoId)
    }
}
