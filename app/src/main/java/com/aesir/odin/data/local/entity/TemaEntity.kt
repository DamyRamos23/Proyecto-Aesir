package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temas")
data class TemaEntity(
    @PrimaryKey val id: String,
    val mundoId: String,
    val nombre: String,
    val descripcion: String,
    val orden: Int,
    val desbloqueado: Boolean,
    val introduccionVista: Boolean,
    val completado: Boolean
)
