package com.aesir.odin.domain.model

/**
 * Modelos del CU-04 Resolver memorama de conceptos (diagrama de clases
 * CU-04, paquete Models).
 */

/** Par del banco del memorama: un concepto y su definición (RF-06). */
data class ParConcepto(
    val id: String,
    val mundoId: String,
    val concepto: String,
    val definicion: String
)

enum class TipoTarjeta { CONCEPTO, DEFINICION }

/**
 * - OCULTA: boca abajo.
 * - VISIBLE: volteada mientras el alumno elige su pareja (RF-08, RF-09).
 * - ENCONTRADA: forma parte de un par ya descubierto (RF-11).
 */
enum class EstadoTarjeta { OCULTA, VISIBLE, ENCONTRADA }

/** Tarjeta del tablero. Dos tarjetas son pareja si comparten [parId]. */
data class TarjetaMemorama(
    val id: String,
    val parId: String,
    val tipo: TipoTarjeta,
    val contenido: String,
    val estado: EstadoTarjeta = EstadoTarjeta.OCULTA
)

/**
 * Resultado de un memorama terminado (RF-13 a RF-17).
 * [nivelId] no aparece en el diagrama de clases: el memorama es un nivel del
 * árbol de habilidades y el MOD-01 necesita saber cuál se terminó.
 */
data class ResultadoMemorama(
    val mundoId: String,
    val nivelId: String,
    val tiempoEmpleadoSeg: Long,
    val puntos: Int,
    val fecha: Long
)
