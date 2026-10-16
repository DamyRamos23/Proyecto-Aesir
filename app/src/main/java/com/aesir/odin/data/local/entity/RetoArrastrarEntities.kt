package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Reto de arrastrar y soltar de un tema (CU-09). */
@Entity(tableName = "retos_arrastrar", indices = [Index("temaId")])
data class RetoArrastrarEntity(
    @PrimaryKey val id: String,
    val temaId: String,
    /** ModalidadReto: "ORDENAR_PROCESO" o "COMPLETAR_DIAGRAMA". */
    val modalidad: String,
    val instrucciones: String,
    val imagenDiagrama: String?,
    val completado: Boolean
)

@Entity(tableName = "elementos_arrastrables", indices = [Index("retoId")])
data class ElementoArrastrableEntity(
    @PrimaryKey val id: String,
    val retoId: String,
    val texto: String,
    val posicionCorrectaId: String
)

@Entity(tableName = "posiciones_destino", indices = [Index("retoId")])
data class PosicionDestinoEntity(
    @PrimaryKey val id: String,
    val retoId: String,
    val orden: Int,
    val etiqueta: String?,
    val x: Float?,
    val y: Float?
)

/** Intento terminado de un reto de arrastrar y soltar. */
@Entity(tableName = "resultados_reto", indices = [Index("retoId")])
data class ResultadoRetoEntity(
    @PrimaryKey val id: String,
    val retoId: String,
    val temaId: String,
    val puntaje: Int,
    val intentos: Int,
    val aprobado: Boolean,
    val fecha: Long
)
