package com.aesir.odin.domain.model

/**
 * @property opcionesCorrectas índices ORIGINALES de [PreguntaFlash.opciones].
 * @property puntos lo que cambió el total con esta pregunta: +5 al acertar, 0 si
 * falló, o un valor negativo (hasta -2) si se agotó el tiempo.
 * @property totalPuntos total acumulado ya actualizado.
 */
data class ResultadoPreguntaFlash(
    val estado: EstadoPreguntaFlash,
    val opcionesCorrectas: List<Int>,
    val puntos: Int,
    val totalPuntos: Int
)
