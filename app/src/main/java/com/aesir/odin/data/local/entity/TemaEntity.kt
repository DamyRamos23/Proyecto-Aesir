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
    /** Actividad del nivel (ver Actividad): LECCION, CRUCIGRAMA, MEMORAMA, FLASHCARDS, ARRASTRAR o POR_DEFINIR. */
    val actividad: String = "LECCION",
    /** Anillo del árbol de habilidades: 1 = lecciones, 2 = minijuegos, 3 = retos, 0 = Ragnarök (centro). */
    val anillo: Int = 1,
    /** CU-10: el alumno demostró conocer el tema en la evaluación diagnóstica. */
    val conocido: Boolean = false
)
