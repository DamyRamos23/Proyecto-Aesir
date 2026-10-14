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
    val completado: Boolean,
    /** TipoNivel del MOD-01: "NORMAL" o "RAGNAROK". */
    val tipo: String = "NORMAL",
    /** Actividad del nivel: "LECCION" o "CRUCIGRAMA" (CU-07). */
    val actividad: String = "LECCION",
    /** Anillo del árbol de habilidades: 1 = lecciones, 2 = minijuegos, 0 = Ragnarök (centro). */
    val anillo: Int = 1
)
