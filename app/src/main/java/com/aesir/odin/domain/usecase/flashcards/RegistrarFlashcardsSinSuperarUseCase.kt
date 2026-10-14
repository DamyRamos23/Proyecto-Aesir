package com.aesir.odin.domain.usecase.flashcards

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL

/**
 * A4.1 y A5.2: la actividad termina sin puntaje ni registro de completada.
 * Se avisa al MOD-01 para que la entrada pagada se pierda.
 */
class RegistrarFlashcardsSinSuperarUseCase(
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(temaId: String, resultado: ResultadoNivel) {
        require(resultado != ResultadoNivel.SUPERADO) { "Para un nivel superado usa FinalizarFlashcardsUseCase" }
        moduloHidromiel.alFinalizarNivel(NivelFinalizado(idUsuario, temaId, 0, resultado))
    }
}
