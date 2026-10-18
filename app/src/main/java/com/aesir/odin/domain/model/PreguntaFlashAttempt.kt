package com.aesir.odin.domain.model

/** Registro en el historial del alumno de cada pregunta flash (RF-38). */
data class PreguntaFlashAttempt(
    val id: String,
    val preguntaId: String,
    val respuestasElegidas: List<Int>,
    val estado: EstadoPreguntaFlash,
    val puntos: Int,
    val tiempoRespuestaMs: Long,
    val fecha: Long
)
