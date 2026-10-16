package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resultados_contrarreloj")
data class ResultadoContrarrelojEntity(
    @PrimaryKey val id: String,
    val puntaje: Int,
    val penalizacion: Int,
    val aciertos: Int,
    val respondidas: Int,
    val abandonado: Boolean,
    val fecha: Long
)
