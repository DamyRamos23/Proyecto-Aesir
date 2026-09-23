package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Fila de la tabla "temas" en la base de datos local (Room).
 * Corresponde a `TemaEntity` en la capa Fuentes de datos del diagrama de clases.
 */
@Entity(tableName = "temas")
data class TemaEntity(
    @PrimaryKey val id: String,
    val mundoId: String,
    val nombre: String,
    val descripcion: String,
    val orden: Int,
    val desbloqueado: Boolean,
    val completado: Boolean,
    val introduccionVista: Boolean
)
