package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.repository.RetoContrarrelojRepository
import java.util.UUID

/** CU-11, pasos 9 y 10 y A7.2: calcula el resultado final y lo registra. */
class FinalizarRetoContrarrelojUseCase(
    private val repository: RetoContrarrelojRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(puntaje: Int, aciertos: Int, respondidas: Int): ResultadoReto {
        val resultado = ResultadoReto(
            id = UUID.randomUUID().toString(),
            puntaje = puntaje,
            penalizacion = 0,
            aciertos = aciertos,
            respondidas = respondidas,
            abandonado = false,
            fecha = reloj()
        )
        repository.guardarResultado(resultado)
        return resultado
    }
}
