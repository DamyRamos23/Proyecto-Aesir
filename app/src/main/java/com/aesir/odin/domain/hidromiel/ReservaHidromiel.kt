package com.aesir.odin.domain.hidromiel

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Hidromiel de un alumno. Cada cambio de saldo deja un [MovimientoHidromiel]
 * en [movimientosNuevos], que el repositorio guarda en el historial.
 *
 * RN-01: el saldo nunca es negativo.
 *  - [descontar] devuelve false y no cambia nada si no alcanza (entradas, ayudas).
 *  - [descontarHastaCero] deja el saldo en 0 si no alcanza (penalizaciones).
 */
class ReservaHidromiel(
    val idUsuario: String,
    saldo: Int,
    fechaUltimaDiaria: LocalDate?,
    private val ahora: () -> LocalDateTime = LocalDateTime::now
) {
    var saldo: Int = saldo.coerceAtLeast(0)
        private set

    var fechaUltimaDiaria: LocalDate? = fechaUltimaDiaria
        private set

    private val _movimientosNuevos = mutableListOf<MovimientoHidromiel>()
    val movimientosNuevos: List<MovimientoHidromiel> get() = _movimientosNuevos

    fun tieneSuficiente(cantidad: Int): Boolean = saldo >= cantidad

    fun descontar(cantidad: Int, tipo: TipoMovimiento): Boolean {
        require(cantidad >= 0) { "La cantidad no puede ser negativa" }
        if (!tieneSuficiente(cantidad)) return false
        saldo -= cantidad
        registrar(tipo, -cantidad)
        return true
    }

    /** @return lo que realmente se descontó. */
    fun descontarHastaCero(cantidad: Int, tipo: TipoMovimiento): Int {
        require(cantidad >= 0) { "La cantidad no puede ser negativa" }
        val real = minOf(cantidad, saldo)
        saldo -= real
        registrar(tipo, -real)
        return real
    }

    fun abonar(cantidad: Int, tipo: TipoMovimiento) {
        require(cantidad >= 0) { "La cantidad no puede ser negativa" }
        saldo += cantidad
        registrar(tipo, cantidad)
    }

    /** RN-03: una vez por fecha. */
    fun puedeReclamarDiaria(fecha: LocalDate): Boolean =
        fechaUltimaDiaria.let { it == null || fecha.isAfter(it) }

    fun marcarDiariaReclamada(fecha: LocalDate) {
        fechaUltimaDiaria = fecha
    }

    /** Lo llama el repositorio después de guardar, para no duplicar el historial. */
    fun marcarMovimientosGuardados() {
        _movimientosNuevos.clear()
    }

    private fun registrar(tipo: TipoMovimiento, cantidad: Int) {
        _movimientosNuevos += MovimientoHidromiel(tipo, cantidad, ahora(), saldo)
    }
}
