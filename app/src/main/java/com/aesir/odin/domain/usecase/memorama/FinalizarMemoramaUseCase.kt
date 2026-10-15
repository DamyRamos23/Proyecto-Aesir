package com.aesir.odin.domain.usecase.memorama

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.ResultadoMemorama
import com.aesir.odin.domain.repository.MemoramaRepository
import com.aesir.odin.domain.repository.RoadmapRepository

/**
 * Cierra el memorama cuando se encontraron todos los pares:
 * 1. Calcula el tiempo empleado (RF-14) y los puntos (RF-15).
 * 2. Guarda el resultado y marca el nivel como completado (RF-17).
 * 3. Notifica el resultado al MOD-01 (postcondición del CU-04), que otorga
 *    el hidromiel y decide qué niveles se abren.
 *
 * [moduloHidromiel] es opcional solo para poder probar el caso de uso sin el módulo.
 */
class FinalizarMemoramaUseCase(
    private val memoramaRepository: MemoramaRepository,
    private val roadmapRepository: RoadmapRepository,
    private val calcularPuntosUseCase: CalcularPuntosMemoramaUseCase,
    private val moduloHidromiel: ModuloHidromiel? = null,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(
        mundoId: String,
        nivelId: String,
        tiempoInicio: Long,
        tiempoFin: Long
    ): ResultadoMemorama {
        val tiempoEmpleadoSeg = ((tiempoFin - tiempoInicio) / 1_000L).coerceAtLeast(0L)
        val resultado = ResultadoMemorama(
            mundoId = mundoId,
            nivelId = nivelId,
            tiempoEmpleadoSeg = tiempoEmpleadoSeg,
            puntos = calcularPuntosUseCase(tiempoEmpleadoSeg),
            fecha = tiempoFin
        )
        memoramaRepository.guardarResultado(resultado)
        roadmapRepository.marcarMemoramaCompletado(nivelId)
        moduloHidromiel?.alFinalizarNivel(
            NivelFinalizado(idUsuario, nivelId, resultado.puntos, ResultadoNivel.SUPERADO)
        )
        return resultado
    }
}
