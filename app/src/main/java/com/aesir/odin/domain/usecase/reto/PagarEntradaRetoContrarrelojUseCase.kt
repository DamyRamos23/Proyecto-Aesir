package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.ResultadoAcceso
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL

/**
 * Cobra la entrada del reto (10 de hidromiel) al MOD-01. Si el saldo no
 * alcanza, el resultado viene con autorizado = false y no se cobra nada.
 */
class PagarEntradaRetoContrarrelojUseCase(
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(): ResultadoAcceso =
        moduloHidromiel.cobrarEntradaContrarreloj(idUsuario)
}
