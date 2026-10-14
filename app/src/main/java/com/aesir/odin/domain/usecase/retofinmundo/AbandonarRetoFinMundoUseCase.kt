package com.aesir.odin.domain.usecase.retofinmundo

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.IntentoRetoFinMundo
import com.aesir.odin.domain.model.RetoFinMundo
import com.aesir.odin.domain.repository.RetoFinMundoRepository

/**
 * A6.1: el alumno abandona el reto. Se registra como no superado, sin
 * puntaje, y el MOD-01 descuenta la entrada pagada.
 */
class AbandonarRetoFinMundoUseCase(
    private val repository: RetoFinMundoRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(reto: RetoFinMundo, aciertosHastaAhora: Int) {
        repository.guardarIntento(
            IntentoRetoFinMundo(
                mundoId = reto.mundoId,
                puntaje = 0,
                aciertos = aciertosHastaAhora,
                errores = reto.preguntas.size - aciertosHastaAhora,
                superado = false,
                abandonado = true,
                fecha = reloj()
            )
        )
        moduloHidromiel.alFinalizarNivel(NivelFinalizado(idUsuario, reto.ragnarokId, 0, ResultadoNivel.ABANDONADO))
    }
}
