package com.aesir.odin.domain.hidromiel

import kotlinx.coroutines.flow.Flow

/** Persistencia del MOD-01. */
interface HidromielRepository {
    suspend fun obtenerReserva(idUsuario: String): ReservaHidromiel?

    /**
     * Guarda saldo y fecha de la diaria, agrega al historial los movimientos
     * nuevos y llama a [ReservaHidromiel.marcarMovimientosGuardados].
     */
    suspend fun guardarReserva(reserva: ReservaHidromiel)

    fun observarSaldo(idUsuario: String): Flow<Int?>

    suspend fun obtenerNivelSuperado(idUsuario: String, idNivel: String): NivelSuperado?

    suspend fun registrarNivelSuperado(nivel: NivelSuperado)

    suspend fun registrarMundoDesbloqueado(mundo: MundoDesbloqueado)

    /** Ejecuta [bloque] como una sola transacción (todo o nada). */
    suspend fun <T> enTransaccion(bloque: suspend () -> T): T
}
