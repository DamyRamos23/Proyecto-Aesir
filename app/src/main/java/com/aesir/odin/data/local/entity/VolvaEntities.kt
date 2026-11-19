package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** CU-12: banco de visiones de la Völva (contenido semilla en DatosVolva). */
@Entity(tableName = "visiones_volva")
data class VisionEntity(
    @PrimaryKey val id: String,
    val temaId: String,
    /** URD, VERDANDI o SKULD. */
    val norna: String,
    val caso: String,
    val opciones: List<String>,
    val indiceCorrecta: Int,
    val pista: String,
    val explicacion: String
)

/** CU-12: runas del Futhark que el alumno ya tiene en su salón (paso 14). */
@Entity(tableName = "runas_obtenidas")
data class RunaObtenidaEntity(
    @PrimaryKey val orden: Int,
    val fecha: Long
)

/**
 * CU-12: cada consulta a la Völva (RF-180). Se guarda al cobrar la apuesta con
 * [terminada] = false, así cuenta en el límite diario aunque se abandone, y se
 * puede detectar si la aplicación se cerró a la mitad (E6.2).
 */
@Entity(tableName = "consultas_volva")
data class ConsultaVolvaEntity(
    @PrimaryKey val id: String,
    val apuesta: Int,
    val puntosDestino: Int,
    val visionesCumplidas: Int,
    val visionesErradas: Int,
    val lectura: String?,
    val pago: Int,
    val runaOrden: Int?,
    val abandonada: Boolean,
    val terminada: Boolean,
    val fecha: Long
)
