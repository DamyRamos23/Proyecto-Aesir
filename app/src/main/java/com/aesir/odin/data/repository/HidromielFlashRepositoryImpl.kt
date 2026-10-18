package com.aesir.odin.data.repository

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.PreguntaFlashResuelta
import com.aesir.odin.domain.hidromiel.ResultadoFlash
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.EstadoPreguntaFlash
import com.aesir.odin.domain.repository.CambioHidromiel
import com.aesir.odin.domain.repository.HidromielFlashRepository
import kotlinx.coroutines.flow.Flow

/** Delega en [ModuloHidromiel], que es el único que mueve el saldo (MOD-01). */
class HidromielFlashRepositoryImpl(
    private val modulo: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL
) : HidromielFlashRepository {

    override fun observarSaldo(): Flow<Int> = modulo.observarSaldo(idUsuario)

    override suspend fun aplicarResultado(estado: EstadoPreguntaFlash): CambioHidromiel {
        val resultado = when (estado) {
            EstadoPreguntaFlash.CORRECTA -> ResultadoFlash.CORRECTA
            EstadoPreguntaFlash.INCORRECTA -> ResultadoFlash.INCORRECTA
            EstadoPreguntaFlash.INCOMPLETA -> ResultadoFlash.INCOMPLETA
            EstadoPreguntaFlash.SIN_RESPUESTA -> ResultadoFlash.SIN_RESPUESTA
        }
        val aviso = modulo.alResolverPreguntaFlash(PreguntaFlashResuelta(idUsuario, resultado))
        return CambioHidromiel(cantidad = aviso.cantidad, saldo = aviso.saldoActual)
    }
}
