package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.model.EstadoPreguntaFlash
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.PreguntaFlashAttempt
import com.aesir.odin.domain.model.ResultadoPreguntaFlash
import com.aesir.odin.domain.repository.HidromielFlashRepository
import com.aesir.odin.domain.bitacora.ModuloBitacora
import com.aesir.odin.domain.bitacora.OrigenError
import com.aesir.odin.domain.bitacora.comoFallo
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase
import java.util.UUID

/**
 * CU-06 pasos 07-08 y flujos A7.1 / A7.2 (RF-86 a RF-90).
 *
 * - CORRECTA: suma 5 de hidromiel.
 * - INCORRECTA / INCOMPLETA: resta 2 de hidromiel (nunca baja de 0) y registra
 *   el error en el historial de errores.
 * La cantidad la decide MOD-01 Sistema de Hidromiel. En todos los casos guarda
 * el intento en el historial de preguntas flash.
 */
class ValidarRespuestaPreguntaFlashUseCase(
    private val repository: PreguntaFlashRepository,
    private val hidromiel: HidromielFlashRepository,
    private val moduloBitacora: ModuloBitacora,
    private val validador: ValidarRespuestaUseCase,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val generarId: () -> String = { UUID.randomUUID().toString() }
) {
    suspend operator fun invoke(
        pregunta: PreguntaFlash,
        respuestasElegidas: List<Int>,
        tiempoMs: Long
    ): ResultadoPreguntaFlash {
        val validacion = validador(respuestasElegidas, pregunta.respuestasCorrectas)
        val estado = when (validacion.estado) {
            EstadoRespuesta.CORRECTA -> EstadoPreguntaFlash.CORRECTA
            EstadoRespuesta.INCOMPLETA -> EstadoPreguntaFlash.INCOMPLETA
            EstadoRespuesta.INCORRECTA -> EstadoPreguntaFlash.INCORRECTA
        }

        val cambio = hidromiel.aplicarResultado(estado)

        val ahora = reloj()
        repository.guardarIntento(
            PreguntaFlashAttempt(
                id = generarId(),
                preguntaId = pregunta.id,
                respuestasElegidas = respuestasElegidas,
                estado = estado,
                puntos = cambio.cantidad,
                tiempoRespuestaMs = tiempoMs,
                fecha = ahora
            )
        )

        if (estado != EstadoPreguntaFlash.CORRECTA) {
            moduloBitacora.registrarFallo(pregunta.comoFallo(OrigenError.PREGUNTA_FLASH, respuestasElegidas, ahora))
        }

        return ResultadoPreguntaFlash(estado, validacion.opcionesCorrectas, cambio.cantidad, cambio.saldo)
    }
}
