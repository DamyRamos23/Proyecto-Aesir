package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Par concepto-definición del banco del memorama (CU-04). */
@Entity(tableName = "pares_concepto", indices = [Index("mundoId")])
data class ParConceptoEntity(
    @PrimaryKey val id: String,
    val mundoId: String,
    val concepto: String,
    val definicion: String
)

/** Resultado de un memorama terminado (RF-17). */
@Entity(tableName = "resultados_memorama", indices = [Index("mundoId")])
data class ResultadoMemoramaEntity(
    @PrimaryKey val id: String,
    val mundoId: String,
    val nivelId: String,
    val tiempoEmpleadoSeg: Long,
    val puntos: Int,
    val fecha: Long
)
