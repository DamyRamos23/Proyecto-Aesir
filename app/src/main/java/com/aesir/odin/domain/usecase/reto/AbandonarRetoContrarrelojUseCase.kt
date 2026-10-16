package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.hidromiel.ContrarrelojAbandonado
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.ResultadoContrarreloj
import com.aesir.odin.domain.repository.RetoContrarrelojRepository
import java.util.UUID

/**
 * CU-11, E3.1: el alumno abandona a mitad del reto. Se descarta lo ganado
 * (puntaje 0, la entrada no se devuelve) y el MOD-01 resta [PENALIZACION] de
 * hidromiel. [ResultadoContrarreloj.penalizacion] guarda lo que realmente se restó,
 * que puede ser menos si el saldo no alcanzaba (RN-01: nunca baja de 0).
 */
class AbandonarRetoContrarrelojUseCase(
    private val repository: RetoContrarrelojRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(): ResultadoContrarreloj {
        val aviso = moduloHidromiel.alAbandonarContrarreloj(ContrarrelojAbandonado(idUsuario))
        val resultado = ResultadoContrarreloj(
            id = UUID.randomUUID().toString(),
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
