package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Historial de preguntas flash respondidas, incorrectas o no respondidas (RF-38). */
@Entity(tableName = "preguntas_flash_intentos")
data class PreguntaFlashAttemptEntity(
    @PrimaryKey val id: String,
    val preguntaId: String,
    val respuestasElegidas: List<Int>,
    val estado: String,
    val puntos: Int,
    val tiempoRespuestaMs: Long,
    val fecha: Long
)
