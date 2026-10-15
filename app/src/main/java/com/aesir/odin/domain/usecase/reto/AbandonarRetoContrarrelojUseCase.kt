package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.repository.RetoContrarrelojRepository
import java.util.UUID

/**
 * CU-11, E3.1: el alumno abandona a mitad del reto. Se descarta el puntaje
 * (queda en 0) y se registra la penalización de [PENALIZACION] puntos.
 */
class AbandonarRetoContrarrelojUseCase(
    private val repository: RetoContrarrelojRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(): ResultadoReto {
        val resultado = ResultadoReto(
            id = UUID.randomUUID().toString(),
            puntaje = 0,
            penalizacion = PENALIZACION,
            aciertos = 0,
            respondidas = 0,
            abandonado = true,
            fecha = reloj()
        )
        repository.guardarResultado(resultado)
        return resultado
    }

    companion object {
        const val PENALIZACION = 20
    }
}
