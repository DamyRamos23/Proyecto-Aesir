package com.aesir.odin.domain.model

/**
 * Resultado de un Reto Contrarreloj. Si [abandonado] es true, [puntaje] es 0
 * (se descarta) y [penalizacion] lleva los puntos que se restan al alumno.
 */
data class ResultadoContrarreloj(
    val id: String,
    val puntaje: Int,
    val penalizacion: Int,
    val aciertos: Int,
    val respondidas: Int,
    val abandonado: Boolean,
    val fecha: Long
)
