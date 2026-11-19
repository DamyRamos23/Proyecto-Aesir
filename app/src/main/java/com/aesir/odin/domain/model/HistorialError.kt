package com.aesir.odin.domain.model

import com.aesir.odin.domain.bitacora.FalloBitacora
import com.aesir.odin.domain.bitacora.OrigenError

enum class EstadoError { PENDIENTE }

/**
 * Un error de la bitácora (MOD-06). Copia todos los datos de la pregunta, así
 * que se conserva aunque la pregunta se borre del banco (E4).
 */
data class HistorialError(
    val id: String,
    val preguntaId: String,
    val origen: OrigenError,
    val temaId: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestaCorrecta: String,
    val respuestaAlumno: String?,
    val pista: String,
    val explicacion: String?,
    val estado: EstadoError,
    val fallos: Int,
    val fecha: Long
) {
    /** Paso 4 (RN-32): suma un fallo y actualiza origen, respuesta del alumno y fecha. */
    fun conOtroFallo(fallo: FalloBitacora): HistorialError =
        copy(fallos = fallos + 1, origen = fallo.origen, respuestaAlumno = fallo.respuestaAlumno, fecha = fallo.fecha)
}
