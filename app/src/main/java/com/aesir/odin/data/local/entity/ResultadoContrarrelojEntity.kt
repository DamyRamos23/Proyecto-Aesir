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
    val fecha: Long,
    /** RF-157: hidromiel ganado en el reto (+2 por acierto, −1 por error, sin bajar de 0). */
    val hidromiel: Int = 0,
    /** E7.2: false mientras el reto está en curso; si la app se cierra así, se registra como abandono. */
    val terminado: Boolean = true
)
