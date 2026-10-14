package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.repository.LeccionRepository

/**
 * Avisa al MOD-01 que una lección terminó sin superarse: por llegar al
 * límite de errores (NO_SUPERADO) o porque el alumno salió (ABANDONADO).
 * En ambos casos la entrada pagada se pierde.
 */
class RegistrarNivelSinSuperarUseCase(
    private val leccionRepository: LeccionRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(leccionId: String, resultado: ResultadoNivel, puntaje: Int = 0) {
        require(resultado != ResultadoNivel.SUPERADO) { "Para un nivel superado usa FinalizarLeccionUseCase" }
        val temaId = leccionRepository.obtenerLeccion(leccionId)?.temaId ?: return
        moduloHidromiel.alFinalizarNivel(NivelFinalizado(idUsuario, temaId, puntaje, resultado))
    }
}
