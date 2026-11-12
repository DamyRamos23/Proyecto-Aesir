package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// CU-13 Duelo de enigmas. Las listas se guardan como texto con Converters.

@Entity(tableName = "conceptos_enigma")
data class ConceptoEnigmaEntity(
    @PrimaryKey val id: String,
    val temaId: String,
    val nombre: String,
    val explicacion: String
)

@Entity(tableName = "preguntas_enigma")
data class PreguntaEnigmaEntity(
    @PrimaryKey val id: String,
    val texto: String,
    val tipo: String,
    val conceptosSi: List<String>
)

/** Un duelo por fila; [resultado] null mientras está en curso (E7.2). */
@Entity(tableName = "duelos_enigmas")
data class ResultadoDueloEntity(
    @PrimaryKey val id: String,
    val conceptoId: String,
    val resultado: String?,
    val preguntasRealizadas: Int,
    val adivinanzasFallidas: Int,
    val sorbosSobrantes: Int,
    val hidromielObtenido: Int,
    val fecha: Long
)
