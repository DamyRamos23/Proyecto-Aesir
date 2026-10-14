package com.aesir.odin.data.repository

import androidx.room.withTransaction
import com.aesir.odin.data.local.OdinDatabase
import com.aesir.odin.data.local.entity.MovimientoHidromielEntity
import com.aesir.odin.data.local.entity.MundoDesbloqueadoEntity
import com.aesir.odin.data.local.entity.NivelSuperadoEntity
import com.aesir.odin.data.local.entity.ReservaHidromielEntity
import com.aesir.odin.domain.hidromiel.HidromielRepository
import com.aesir.odin.domain.hidromiel.MundoDesbloqueado
import com.aesir.odin.domain.hidromiel.NivelSuperado
import com.aesir.odin.domain.hidromiel.OrigenSuperado
import com.aesir.odin.domain.hidromiel.ReservaHidromiel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Persistencia del MOD-01 en Room.
 *
 * Al registrar un nivel superado también marca el tema como completado, y al
 * registrar un mundo desbloqueado marca el mundo como desbloqueado, para que
 * las pantallas que leen esas columnas sigan mostrando el progreso real.
 */
class HidromielRepositoryImpl(private val db: OdinDatabase) : HidromielRepository {

    private val dao = db.hidromielDao()
    private val temaDao = db.temaDao()
    private val mundoDao = db.mundoDao()

    override suspend fun obtenerReserva(idUsuario: String): ReservaHidromiel? =
        dao.getReserva(idUsuario)?.let {
            ReservaHidromiel(
                idUsuario = it.idUsuario,
                saldo = it.saldo,
                fechaUltimaDiaria = it.fechaUltimaDiaria?.let(LocalDate::parse)
            )
        }

    override suspend fun guardarReserva(reserva: ReservaHidromiel) {
        dao.guardarReserva(
            ReservaHidromielEntity(
                idUsuario = reserva.idUsuario,
                saldo = reserva.saldo,
                fechaUltimaDiaria = reserva.fechaUltimaDiaria?.toString()
            )
        )
        if (reserva.movimientosNuevos.isNotEmpty()) {
            dao.insertarMovimientos(
                reserva.movimientosNuevos.map {
                    MovimientoHidromielEntity(
                        idUsuario = reserva.idUsuario,
                        tipo = it.tipo.name,
                        cantidad = it.cantidad,
                        fecha = it.fecha.toString(),
                        saldoResultante = it.saldoResultante
                    )
                }
            )
        }
        reserva.marcarMovimientosGuardados()
    }

    override fun observarSaldo(idUsuario: String): Flow<Int?> = dao.observarSaldo(idUsuario)

    override suspend fun obtenerNivelSuperado(idUsuario: String, idNivel: String): NivelSuperado? =
        dao.getNivelSuperado(idUsuario, idNivel)?.let {
            NivelSuperado(
                idUsuario = it.idUsuario,
                idNivel = it.idNivel,
                origen = OrigenSuperado.valueOf(it.origen),
                recompensaOtorgada = it.recompensaOtorgada,
                fecha = LocalDate.parse(it.fecha)
            )
        }

    override suspend fun registrarNivelSuperado(nivel: NivelSuperado) {
        dao.insertarNivelSuperado(
            NivelSuperadoEntity(
                idUsuario = nivel.idUsuario,
                idNivel = nivel.idNivel,
                origen = nivel.origen.name,
                recompensaOtorgada = nivel.recompensaOtorgada,
                fecha = nivel.fecha.toString()
            )
        )
        temaDao.actualizarCompletado(nivel.idNivel, completado = true)
        temaDao.actualizarDesbloqueado(nivel.idNivel, desbloqueado = true)
    }

    override suspend fun registrarMundoDesbloqueado(mundo: MundoDesbloqueado) {
        dao.insertarMundoDesbloqueado(
            MundoDesbloqueadoEntity(mundo.idUsuario, mundo.idMundo, mundo.fecha.toString())
        )
        mundoDao.actualizarDesbloqueado(mundo.idMundo, desbloqueado = true)
    }

    override suspend fun <T> enTransaccion(bloque: suspend () -> T): T =
        db.withTransaction { bloque() }
}
