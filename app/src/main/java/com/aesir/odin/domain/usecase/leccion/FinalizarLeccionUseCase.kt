package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.model.ResultadoLeccion
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.usecase.roadmap.DesbloquearSiguienteTemaUseCase

/**
 * Calcula el puntaje (RF-21), guarda el resultado, registra la lección como
 * completada (RF-23) y actualiza el roadmap (RF-24).
 *
 * El puntaje va de 0 a 100 según el porcentaje de ejercicios respondidos
 * correctamente.
 */
class FinalizarLeccionUseCase(
    private val leccionRepository: LeccionRepository,
    private val registrarCompletadaUseCase: RegistrarLeccionCompletadaUseCase,
    private val desbloquearUseCase: DesbloquearSiguienteTemaUseCase,
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

        if (aprobada) {
            leccionRepository.marcarCompletada(leccionId)
            val temaId = leccionRepository.obtenerLeccion(leccionId)?.temaId
            if (temaId != null) {
                registrarCompletadaUseCase(temaId)
                desbloquearUseCase(temaId)
            }
        }
        return resultado
    }
}
