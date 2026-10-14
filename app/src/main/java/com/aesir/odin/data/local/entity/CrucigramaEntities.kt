package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Concepto del banco del crucigrama (CU-07). */
@Entity(tableName = "conceptos_crucigrama", indices = [Index("temaId")])
data class ConceptoCrucigramaEntity(
    @PrimaryKey val id: String,
    val temaId: String,
    val respuesta: String,
    val descripcion: String
)

/** Resultado de un crucigrama terminado. */
@Entity(tableName = "resultados_crucigrama", indices = [Index("temaId")])
data class ResultadoCrucigramaEntity(
    @PrimaryKey val id: String,
    val temaId: String,
    val puntajeFinal: Int,
    val aciertos: Int,
    val erroresCometidos: Int,
    val fecha: Long
)
