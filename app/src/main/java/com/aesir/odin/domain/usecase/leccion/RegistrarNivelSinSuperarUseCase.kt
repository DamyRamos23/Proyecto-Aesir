package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.racha.ActividadTerminada
import com.aesir.odin.domain.racha.ModuloCarreraSol
import com.aesir.odin.domain.racha.TipoActividad
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
    private val idUsuario: String = USUARIO_LOCAL,
    /** MOD-05: terminar la actividad sin abandonarla cuenta para la racha. */
    private val moduloCarreraSol: ModuloCarreraSol? = null
) {
    suspend operator fun invoke(leccionId: String, resultado: ResultadoNivel, puntaje: Int = 0) {
        require(resultado != ResultadoNivel.SUPERADO) { "Para un nivel superado usa FinalizarLeccionUseCase" }
        val temaId = leccionRepository.obtenerLeccion(leccionId)?.temaId ?: return
        moduloHidromiel.alFinalizarNivel(NivelFinalizado(idUsuario, temaId, puntaje, resultado))
        // Un nivel no superado sí se terminó; uno abandonado no cuenta (E2)
        moduloCarreraSol?.alTerminarActividad(ActividadTerminada(idUsuario, TipoActividad.NIVEL_ROADMAP, abandonada = resultado == ResultadoNivel.ABANDONADO, fechaFin = java.time.LocalDate.now()))
    }
}
