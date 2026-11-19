package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.bitacora.ModuloBitacora
import com.aesir.odin.domain.bitacora.OrigenError
import com.aesir.odin.domain.bitacora.comoFallo
import com.aesir.odin.domain.model.EstadoPreguntaFlash
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.PreguntaFlashAttempt
import com.aesir.odin.domain.model.ResultadoPreguntaFlash
import com.aesir.odin.domain.repository.HidromielFlashRepository
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import java.util.UUID

/**
 * CU-06 flujo E6.1 (RF-89): el contador llegó a cero sin respuesta. Resta 2 de
 * hidromiel (MOD-01 no deja el saldo por debajo de cero) y registra la pregunta
 * como no respondida. La avisa a la bitácora sin respuesta (MOD-06, disparador 2).
 */
class RegistrarTiempoAgotadoPreguntaFlashUseCase(
    private val repository: PreguntaFlashRepository,
    private val hidromiel: HidromielFlashRepository,
    private val moduloBitacora: ModuloBitacora,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val generarId: () -> String = { UUID.randomUUID().toString() }
) {
    suspend operator fun invoke(
        pregunta: PreguntaFlash,
        tiempoMs: Long = TIEMPO_LIMITE_MS
    ): ResultadoPreguntaFlash {
        val cambio = hidromiel.aplicarResultado(EstadoPreguntaFlash.SIN_RESPUESTA)

        repository.guardarIntento(
            PreguntaFlashAttempt(
                id = generarId(),
                preguntaId = pregunta.id,
                respuestasElegidas = emptyList(),
                estado = EstadoPreguntaFlash.SIN_RESPUESTA,
                puntos = cambio.cantidad,
                tiempoRespuestaMs = tiempoMs,
                fecha = reloj()
            )
        )
        moduloBitacora.registrarFallo(pregunta.comoFallo(OrigenError.PREGUNTA_FLASH, null, reloj()))
        return ResultadoPreguntaFlash(
            estado = EstadoPreguntaFlash.SIN_RESPUESTA,
            opcionesCorrectas = pregunta.respuestasCorrectas,
            puntos = cambio.cantidad,
            totalPuntos = cambio.saldo
        )
    }

    companion object {
        const val TIEMPO_LIMITE_MS = 10_000L
    }
}
