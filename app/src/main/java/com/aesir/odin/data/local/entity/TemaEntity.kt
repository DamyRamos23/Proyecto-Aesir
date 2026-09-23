package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temas")
data class TemaEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val introduccionVista: Boolean = false
)