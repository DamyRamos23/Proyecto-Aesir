package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** ArtefactoForja asociado a un mundo (CU-02). Contenido fijo: se siembra en cada apertura. */
@Entity(tableName = "artefactos", indices = [Index("mundoId")])
data class ArtefactoEntity(
    @PrimaryKey val id: String,
    val mundoId: String,
    val nombre: String,
    /** Poder en Hasta el Alba (columna con su nombre de v13). */
    val poderPerfecto: String,
    /** Sin uso: los artefactos ya no tienen calidad; se deja vacía para no cambiar el esquema. */
    val poderConDefecto: String = ""
)

/** ArtefactoForja que el alumno tiene en la vitrina del salón; uno por artefacto. */
@Entity(tableName = "vitrina")
data class VitrinaArtefactoEntity(
    @PrimaryKey val artefactoId: String,
    /** Sin calidades: siempre [COMPLETO] (la columna se conserva para no cambiar el esquema). */
    val calidad: String,
    val fechaObtencion: Long,
    val ultimaNocheUsada: Long?
) {
    companion object {
        const val COMPLETO = "COMPLETO"
    }
}

/** Una forja iniciada. [estado]: "EN_CURSO", "PERFECTO" o "ROTO". */
@Entity(tableName = "forjas", indices = [Index("fecha")])
data class ForjaEntity(
    @PrimaryKey val id: String,
    val artefactoId: String?,
    val apuesta: Int,
    val estado: String,
    val errores: Int,
    /** Hidromiel ganado (positivo) o perdido (negativo) al terminar; 0 mientras está en curso. */
    val hidromielNeto: Int,
    val abandonada: Boolean,
    val fecha: Long
)

/**
 * Problema de diseño de la etapa «Martillar». [piezas] son los textos de las
 * piezas posibles; [piezaCorrectaId] es el índice (desde 0) de la correcta.
 */
@Entity(tableName = "problemas_martillar", indices = [Index("mundoId")])
data class ProblemaMartillarEntity(
    @PrimaryKey val id: String,
    val mundoId: String,
    val enunciado: String,
    val piezas: List<String>,
    val piezaCorrectaId: String
)

/**
 * Definición o diagrama incompleto de la etapa «Templar». [plantilla] marca
 * los huecos con {h1}, {h2}…; [respuestas] trae la respuesta de cada hueco en
 * ese orden y [opciones] las piezas que se le ofrecen al alumno (incluye las
 * correctas).
 */
@Entity(tableName = "templados", indices = [Index("mundoId")])
data class TempladoEntity(
    @PrimaryKey val id: String,
    val mundoId: String,
    /** "DEFINICION" o "DIAGRAMA". */
    val tipo: String,
    val enunciado: String,
    val plantilla: String,
    val respuestas: List<String>,
    val opciones: List<String>
)
