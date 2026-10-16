package com.aesir.odin.domain.usecase.retoarrastrar

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL

/**
 * A4.1 del CU-09 (RF-80): el alumno sale antes de terminar. No se guarda
 * resultado ni se marca como completado; se avisa al MOD-01 para que la
 * entrada pagada se pierda.
 */
class AbandonarRetoUseCase(
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(temaId: String) {
        moduloHidromiel.alFinalizarNivel(NivelFinalizado(idUsuario, temaId, 0, ResultadoNivel.ABANDONADO))
    }
}
