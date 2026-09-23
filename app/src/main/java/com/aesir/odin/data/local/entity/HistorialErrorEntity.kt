package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_errores")
data class HistorialErrorEntity(
    @PrimaryKey val id: String,
    val tipoDesafio: String,
    val actividadId: String,
    val descripcion: String,
    val fecha: Long
)
