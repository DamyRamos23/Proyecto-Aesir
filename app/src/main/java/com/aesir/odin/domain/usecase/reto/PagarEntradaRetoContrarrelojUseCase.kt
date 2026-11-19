package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.hidromiel.EstadoNivel
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.ResultadoAcceso
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.repository.RetoContrarrelojRepository
import com.aesir.odin.domain.usecase.duelo.inicioDelDia

/**
 * CU-11, pasos 2 a 4: revisa el límite de 3 retos por día real (RF-147, A2.1),
 * cobra la entrada (10 de hidromiel) al MOD-01 y cuenta el reto dentro del
 * límite del día. Si el saldo no alcanza (A4.1), no se cobra nada.
 */
class PagarEntradaRetoContrarrelojUseCase(
    private val moduloHidromiel: ModuloHidromiel,
    private val repository: RetoContrarrelojRepository? = null,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    /** Retos que todavía se pueden jugar hoy (paso 2). */
    suspend fun restantesHoy(): Int {
        val jugados = repository?.contarRetosDelDia(inicioDelDia(reloj())) ?: 0
        return (PoliticaCosto.MAX_CONTRARRELOJ_DIA - jugados).coerceAtLeast(0)
    }

    suspend operator fun invoke(idReto: String? = null): ResultadoAcceso {
        if (restantesHoy() == 0) {
            return ResultadoAcceso(
                ModuloHidromiel.ID_CONTRARRELOJ, EstadoNivel.BLOQUEADO, autorizado = false,
                costo = PoliticaCosto.ENTRADA_CONTRARRELOJ, faltante = 0,
                motivo = "Ya jugaste los ${PoliticaCosto.MAX_CONTRARRELOJ_DIA} retos contrarreloj de hoy. Podrás volver a jugar mañana."
            )
        }
        val acceso = moduloHidromiel.cobrarEntradaContrarreloj(idUsuario)
        if (acceso.autorizado && idReto != null) repository?.registrarInicio(idReto, reloj())
        return acceso
    }
}
