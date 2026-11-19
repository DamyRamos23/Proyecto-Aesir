package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.racha.ActividadTerminada
import com.aesir.odin.domain.racha.ModuloCarreraSol
import com.aesir.odin.domain.racha.TipoActividad
import com.aesir.odin.domain.racha.fechaLocal

/**
 * Cierra el repaso del mundo en el pozo de Mímir (CU-10). El repaso no guarda
 * resultados; solo avisa al MOD-05 que se terminó, porque cuenta para la racha.
 */
class FinalizarRepasoMimirUseCase(
    private val moduloCarreraSol: ModuloCarreraSol?,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke() {
        moduloCarreraSol?.alTerminarActividad(
            ActividadTerminada(idUsuario, TipoActividad.REPASO_MIMIR, abandonada = false, fechaFin = fechaLocal(reloj()))
        )
    }
}
