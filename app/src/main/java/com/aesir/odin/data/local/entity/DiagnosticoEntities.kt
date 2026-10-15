package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Pregunta de la evaluación diagnóstica (CU-10). [opciones] y
 * [respuestasCorrectas] se guardan como texto con los Converters.
 */
@Entity(tableName = "preguntas_diagnostico", indices = [Index("mundoId")])
data class PreguntaDiagnosticaEntity(
    @PrimaryKey val id: String,
    val mundoId: String,
    val temaId: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestasCorrectas: List<Int>
)

/**
 * Un registro por mundo: la evaluación solo se hace o se omite una vez
 * (precondición del CU-10). [estado]: "COMPLETADA" u "OMITIDA".
 * Si no hay fila para el mundo, la evaluación está PENDIENTE.
 */
@Entity(tableName = "resultados_diagnostico")
data class ResultadoDiagnosticoEntity(
    @PrimaryKey val mundoId: String,
    val estado: String,
    val aciertos: Int,
    val totalPreguntas: Int,
    val temasConocidos: List<String>,
    val fecha: Long
)
