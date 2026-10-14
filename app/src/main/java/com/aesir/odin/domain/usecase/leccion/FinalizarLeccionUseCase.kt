package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.ResultadoLeccion
import com.aesir.odin.domain.repository.LeccionRepository

/**
 * Calcula el puntaje (RF-21), guarda el resultado, registra la lección como
 * completada (RF-23) y avisa al MOD-01 Sistema de Hidromiel que el nivel
 * terminó (RF-24). El módulo marca el tema como superado, otorga la
 * recompensa y decide qué niveles se abren; ya no se desbloquea "el
 * siguiente tema" por orden.
 *
 * El puntaje va de 0 a 100 según el porcentaje de ejercicios respondidos
 * correctamente.
 */
class FinalizarLeccionUseCase(
    private val leccionRepository: LeccionRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(leccionId: String, totalEjercicios: Int, errores: Int): ResultadoLeccion {
        val aciertos = (totalEjercicios - errores).coerceAtLeast(0)
        val puntaje = if (totalEjercicios > 0) aciertos * 100 / totalEjercicios else 0
        val aprobada = errores < RegistrarErrorLeccionUseCase.LIMITE_ERRORES

        val resultado = ResultadoLeccion(
            leccionId = leccionId,
            puntaje = puntaje,
            erroresCometidos = errores,
            aprobada = aprobada,
            fecha = reloj()
        )
        leccionRepository.guardarResultado(resultado)
        if (aprobada) leccionRepository.marcarCompletada(leccionId)

        val temaId = leccionRepository.obtenerLeccion(leccionId)?.temaId
        if (temaId != null) {
            moduloHidromiel.alFinalizarNivel(
                NivelFinalizado(
                    idUsuario = idUsuario,
                    idNivel = temaId,
                    puntaje = puntaje,
                    resultado = if (aprobada) ResultadoNivel.SUPERADO else ResultadoNivel.NO_SUPERADO
                )
            )
        }
        return resultado
    }
}
