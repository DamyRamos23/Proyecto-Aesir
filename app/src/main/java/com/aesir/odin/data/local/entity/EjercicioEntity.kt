package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ejercicios")
data class EjercicioEntity(
    @PrimaryKey val id: String,
    val leccionId: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestasCorrectas: List<Int>,
    val orden: Int
)
