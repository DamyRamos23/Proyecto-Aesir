package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lecciones")
data class LeccionEntity(
    @PrimaryKey val id: String,
    val temaId: String,
    val titulo: String,
    val introduccion: String,
    val orden: Int,
    val completada: Boolean
)
