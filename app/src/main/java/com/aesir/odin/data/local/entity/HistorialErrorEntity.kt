package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** MOD-06: preguntaId es único (RN-32); [opciones] se guarda con Converters. */
@Entity(tableName = "historial_errores", indices = [Index(value = ["preguntaId"], unique = true)])
data class HistorialErrorEntity(
    @PrimaryKey val id: String,
    val preguntaId: String,
    val origen: String,
    val temaId: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestaCorrecta: String,
    val respuestaAlumno: String?,
    val pista: String,
    val explicacion: String?,
    val estado: String,
    val fallos: Int,
    val fecha: Long
)
