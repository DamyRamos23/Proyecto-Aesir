package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Banco de preguntas de Pregunta Flash (también lo usará el Reto Contrarreloj). */
@Entity(tableName = "preguntas_flash")
data class PreguntaFlashEntity(
    @PrimaryKey val id: String,
    val temaId: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestasCorrectas: List<Int>
)
