package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Racha de un alumno (MOD-05). [ultimaActividad] en formato ISO (2026-11-14) o null. */
@Entity(tableName = "rachas")
data class RachaEntity(
    @PrimaryKey val idUsuario: String,
    val actual: Int,
    val maxima: Int,
    val ultimaActividad: String?,
    val ofrendas: Int,
    val titulos: List<String>
)

/** Hito de racha ya cobrado (RN-28: una sola vez por alumno). */
@Entity(tableName = "hitos_racha", primaryKeys = ["idUsuario", "dias"])
data class HitoRachaEntity(
    val idUsuario: String,
    val dias: Int,
    val bono: Int,
    val titulo: String?,
    val fecha: Long
)
