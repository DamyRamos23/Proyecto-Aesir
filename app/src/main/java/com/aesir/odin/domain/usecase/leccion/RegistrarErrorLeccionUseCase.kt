package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.repository.HistorialRepository
import java.util.UUID

/**
 * Registra un error de la lección en el historial (RF-17) y, si se alcanzó el
 * límite, registra también el intento fallido (RF-19).
 *
 * @return true si [erroresActuales] alcanzó el límite y la lección debe terminar (RF-18).
 */
class RegistrarErrorLeccionUseCase(
    private val repository: HistorialRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(leccionId: String, ejercicioId: String, erroresActuales: Int): Boolean {
        repository.registrarError(
            HistorialError(
                id = UUID.randomUUID().toString(),
                tipoDesafio = TIPO_EJERCICIO,
                actividadId = ejercicioId,
                descripcion = "Respuesta incorrecta en la lección $leccionId",
                fecha = reloj()
            )
        )
        val limiteAlcanzado = erroresActuales >= LIMITE_ERRORES
        if (limiteAlcanzado) {
            repository.registrarError(
                HistorialError(
                    id = UUID.randomUUID().toString(),
                    tipoDesafio = TIPO_LECCION_FALLIDA,
                    actividadId = leccionId,
                    descripcion = "Lección interrumpida al alcanzar $LIMITE_ERRORES errores",
                    fecha = reloj()
                )
            )
        }
        return limiteAlcanzado
    }

    companion object {
        const val LIMITE_ERRORES = 3
        const val TIPO_EJERCICIO = "EJERCICIO_LECCION"
        const val TIPO_LECCION_FALLIDA = "LECCION_FALLIDA"
    }
}
