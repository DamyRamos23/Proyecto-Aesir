package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Fila de la tabla "mundos" en la base de datos local (Room).
 * Corresponde a `MundoEntity` en la capa Fuentes de datos del diagrama de clases.
 */
@Entity(tableName = "mundos")
data class MundoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val orden: Int,
    val desbloqueado: Boolean
)
