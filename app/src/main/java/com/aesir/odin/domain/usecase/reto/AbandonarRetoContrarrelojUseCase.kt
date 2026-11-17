package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.hidromiel.ContrarrelojAbandonado
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.ResultadoContrarreloj
import com.aesir.odin.domain.repository.RetoContrarrelojRepository
import java.util.UUID

/**
 * CU-11, E7.1 y E7.2: el alumno abandona a mitad del reto (o la app se cerró
 * durante el reto). Se descartan el puntaje y el acumulado (la entrada no se
 * devuelve) y el MOD-01 resta [PENALIZACION] de hidromiel.
 * [ResultadoContrarreloj.penalizacion] guarda lo que realmente se restó, que
 * puede ser menos si el saldo no alcanzaba (RN-01: nunca baja de 0).
 */
class AbandonarRetoContrarrelojUseCase(
    private val repository: RetoContrarrelojRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    /** [puntajeAcumulado]: lo que se llevaba en el reto; se pierde. */
    suspend operator fun invoke(puntajeAcumulado: Int = 0, idReto: String = UUID.randomUUID().toString()): ResultadoContrarreloj {
        val aviso = moduloHidromiel.alAbandonarContrarreloj(ContrarrelojAbandonado(idUsuario, puntajeAcumulado))
        val resultado = ResultadoContrarreloj(
            id = idReto,
            puntaje = 0,
            penalizacion = -aviso.cantidad,
            aciertos = 0,
            respondidas = 0,
            abandonado = true,
            fecha = reloj()
        )
        repository.guardarResultado(resultado)
        return resultado
    }

    companion object {
        const val PENALIZACION = PoliticaCosto.ABANDONO_CONTRARRELOJ
    }
}

/**
 * CU-11, E7.2: al volver a abrir la aplicación, un reto que quedó en curso se
 * registra como abandono confirmado (se restan los 20 de hidromiel).
 */
class CerrarContrarrelojSinTerminarUseCase(
    private val repository: RetoContrarrelojRepository,
    private val abandonar: AbandonarRetoContrarrelojUseCase
) {
    suspend operator fun invoke() {
        repository.retosSinTerminar().forEach { id -> abandonar(idReto = id) }
    }
}
