package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tablas del MOD-01 Sistema de Hidromiel. Las fechas se guardan como texto
 * ISO-8601 (LocalDate.toString / LocalDateTime.toString).
 */

@Entity(tableName = "reservas_hidromiel")
data class ReservaHidromielEntity(
    @PrimaryKey val idUsuario: String,
    val saldo: Int,
    val fechaUltimaDiaria: String?
)

@Entity(tableName = "movimientos_hidromiel", indices = [Index("idUsuario")])
data class MovimientoHidromielEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idUsuario: String,
    val tipo: String,
    val cantidad: Int,
    val fecha: String,
    val saldoResultante: Int
)

@Entity(tableName = "niveles_superados", primaryKeys = ["idUsuario", "idNivel"])
data class NivelSuperadoEntity(
    val idUsuario: String,
    val idNivel: String,
    val origen: String,
    val recompensaOtorgada: Int,
    val fecha: String
)

@Entity(tableName = "mundos_desbloqueados", primaryKeys = ["idUsuario", "idMundo"])
data class MundoDesbloqueadoEntity(
    val idUsuario: String,
    val idMundo: String,
    val fecha: String
)
