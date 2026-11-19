package com.aesir.odin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * CU-01: cada noche de Hasta el Alba. Se guarda al cobrar la apuesta con
 * [terminada] = false, así cuenta como la noche del día (RF-02) y se puede
 * recuperar si la aplicación se cierra (E9.2, E15.1). Al terminar guarda el
 * resultado completo (RF-29).
 */
@Entity(tableName = "noches_alba")
data class NocheAlbaEntity(
    @PrimaryKey val id: String,
    val fecha: Long,
    val apuesta: Int,
    /** Número del enemigo de la oleada actual o alcanzada (1 a 7). */
    val oleada: Int,
    val golpesRestantes: Int,
    val escudos: Int,
    /** En centésimas: ×1.2 = 120. */
    val multiplicador: Int,
    val bonoDraupnir: Int,
    val aciertos: Int,
    val errores: Int,
    val artefactosUsados: List<String>,
    val preguntasMostradas: List<String>,
    val preguntasFalladas: List<String>,
    val temasConError: List<String>,
    /** EN_OLEADA, EN_DECISION o TERMINADA. */
    val fase: String,
    /** ALBA, RETIRADA o CAIDA cuando terminó. */
    val desenlace: String?,
    val premioBruto: Int,
    val diezmo: Int,
    val premioNeto: Int,
    val premioPendiente: Boolean,
    val terminada: Boolean
)

/** CU-02 y CU-01: vitrina del salón con los artefactos forjados y su calidad. */
@Entity(tableName = "artefactos_forjados")
data class ArtefactoForjadoEntity(
    /** MJOLNIR, GUNGNIR, DRAUPNIR o SKIDBLADNIR. */
    @PrimaryKey val artefacto: String,
    /** PERFECTO o CON_DEFECTO. */
    val calidad: String,
    val fecha: Long
)

/** Logros y títulos del salón (RF-19, RN-31). */
@Entity(tableName = "logros")
data class LogroEntity(
    @PrimaryKey val id: String,
    val fecha: Long
)
