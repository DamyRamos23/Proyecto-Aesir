package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Resultado de unas flashcards terminadas (CU-03). */
@Entity(tableName = "resultados_flashcards", indices = [Index("temaId")])
data class ResultadoFlashcardsEntity(
    @PrimaryKey val id: String,
    val temaId: String,
    val puntaje: Int,
    val aciertosPrimerIntento: Int,
    val errores: Int,
    val totalTarjetas: Int,
    val fecha: Long
)

/** Intento del reto de fin de mundo (CU-08, RF-66). */
@Entity(tableName = "intentos_reto_fin_mundo", indices = [Index("mundoId")])
data class ResultadoRetoFinMundoEntity(
    @PrimaryKey val id: String,
    val mundoId: String,
    val puntaje: Int,
    val aciertos: Int,
    val errores: Int,
    val superado: Boolean,
    val abandonado: Boolean,
    val fecha: Long
)
