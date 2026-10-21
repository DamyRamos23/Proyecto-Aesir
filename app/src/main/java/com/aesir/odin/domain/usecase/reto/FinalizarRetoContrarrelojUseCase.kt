package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.racha.ActividadTerminada
import com.aesir.odin.domain.racha.ModuloCarreraSol
import com.aesir.odin.domain.racha.TipoActividad
import com.aesir.odin.domain.racha.fechaLocal
import com.aesir.odin.domain.hidromiel.ContrarrelojFinalizado
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.ResultadoContrarreloj
import com.aesir.odin.domain.repository.RetoContrarrelojRepository
import java.util.UUID

/**
 * CU-11, pasos 9 y 10 y A7.2: calcula el resultado final, lo registra y avisa
 * al MOD-01, que abona el hidromiel ganado (+2 por correcta, -1 por incorrecta).
 */
class FinalizarRetoContrarrelojUseCase(
    private val repository: RetoContrarrelojRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    /** MOD-05: terminar la actividad sin abandonarla cuenta para la racha. */
    private val moduloCarreraSol: ModuloCarreraSol? = null,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(puntaje: Int, aciertos: Int, respondidas: Int): ResultadoContrarreloj {
        val resultado = ResultadoContrarreloj(
            id = UUID.randomUUID().toString(),
            puntaje = puntaje,
            penalizacion = 0,
            aciertos = aciertos,
            respondidas = respondidas,
            abandonado = false,
            fecha = reloj()
        )
        repository.guardarResultado(resultado)
        moduloHidromiel.alFinalizarContrarreloj(ContrarrelojFinalizado(idUsuario, aciertos, fallos = respondidas - aciertos))
        moduloCarreraSol?.alTerminarActividad(ActividadTerminada(idUsuario, TipoActividad.RETO_CONTRARRELOJ, abandonada = false, fechaFin = fechaLocal(resultado.fecha)))
        return resultado
    }
}
