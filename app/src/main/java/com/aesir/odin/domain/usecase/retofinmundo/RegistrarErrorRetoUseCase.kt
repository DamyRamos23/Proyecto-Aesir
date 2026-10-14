package com.aesir.odin.domain.usecase.retofinmundo

import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.PreguntaReto
import com.aesir.odin.domain.repository.HistorialRepository
import java.util.UUID

/**
 * A7.1: registra en el historial de errores la respuesta incorrecta o
 * incompleta, indicando el tema de la pregunta (RF-59).
 */
class RegistrarErrorRetoUseCase(
    private val repository: HistorialRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(pregunta: PreguntaReto, estado: EstadoRespuesta) {
        val como = if (estado == EstadoRespuesta.INCOMPLETA) "incompleta" else "incorrecta"
        repository.registrarError(
            HistorialError(
                id = UUID.randomUUID().toString(),
                tipoDesafio = TIPO_RETO_FIN_MUNDO,
                actividadId = pregunta.id,
                descripcion = "Reto de fin de mundo, tema «${pregunta.temaNombre}»: respuesta $como",
                fecha = reloj()
            )
        )
    }

    companion object {
        const val TIPO_RETO_FIN_MUNDO = "RETO_FIN_MUNDO"
    }
}
