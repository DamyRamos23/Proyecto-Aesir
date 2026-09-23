package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resultados_leccion")
data class ResultadoLeccionEntity(
    @PrimaryKey val id: String,
    val leccionId: String,
    val puntaje: Int,
    val erroresCometidos: Int,
    val aprobada: Boolean,
    val fecha: Long
)
