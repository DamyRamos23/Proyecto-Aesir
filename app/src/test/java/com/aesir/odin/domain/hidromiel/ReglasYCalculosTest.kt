package com.aesir.odin.domain.hidromiel

import com.aesir.odin.domain.model.Lectura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReglasYCalculosTest {

    private val politica = PoliticaCosto()
    private val calculadora = CalculadoraRecompensa(politica)

    @Test
    fun `valores del MOD-01`() {
        assertEquals(20, politica.costoDe(NivelInfo("n", "m", "N", TipoNivel.NORMAL, esPrimero = false)))
        assertEquals(60, politica.costoDe(NivelInfo("r", "m", "R", TipoNivel.RAGNAROK, esPrimero = false)))
        assertEquals(0, politica.costoDe(NivelInfo("p", "m", "P", TipoNivel.NORMAL, esPrimero = true)))
        assertEquals(40, politica.recompensaDiaria())
        assertEquals(40, politica.cantidadInicial())
        assertEquals(5, politica.costoAyuda(TipoAyuda.REVELAR_LETRA))
        assertEquals(15, politica.costoAyuda(TipoAyuda.REVELAR_PALABRA))
    }

    @Test
    fun `premios del Valhalla con el diezmo de Odin redondeado hacia abajo`() {
        // CU-02: apuesta ×3 menos el 10 %
        assertEquals(54, politica.premioForja(20))
        assertEquals(216, politica.premioForja(80))
        // CU-12: ×2.5 y ×1.5 con diezmo; Humo devuelve la apuesta; Maldición no paga
        assertEquals(PagoValhalla(100, 10, 90), politica.pagoVolva(40, Lectura.PROFECIA_CUMPLIDA))
        assertEquals(PagoValhalla(60, 6, 54), politica.pagoVolva(40, Lectura.LECTURA_CLARA))
        assertEquals(40, politica.pagoVolva(40, Lectura.HUMO_DEL_CALDERO).neto)
        assertEquals(0, politica.pagoVolva(40, Lectura.MALDICION).neto)
        // CU-01: apuesta × multiplicador en centésimas (×1.7 = 170)
        assertEquals(PagoValhalla(17, 1, 16), politica.pagoAlba(10, 170))
        assertEquals(PoliticaCosto.C * 2, PoliticaCosto.COSTO_OFRENDA)
    }

    @Test
    fun `el bono va de 0 a 10 y se redondea`() {
        assertEquals(0, calculadora.calcularBono(0))
        assertEquals(5, calculadora.calcularBono(50))
        assertEquals(5, calculadora.calcularBono(55)) // hacia abajo
        assertEquals(9, calculadora.calcularBono(99))
        assertEquals(10, calculadora.calcularBono(100))
        assertEquals(10, calculadora.calcularBono(250)) // se acota a 100
        assertEquals(0, calculadora.calcularBono(-10))
    }

    @Test
    fun `recompensa normal y al completar el mundo`() {
        assertEquals(28, calculadora.calcularRecompensa(80, completaMundo = false))
        assertEquals(68, calculadora.calcularRecompensa(80, completaMundo = true))
    }

    @Test
    fun `la reserva no deja saldo negativo`() {
        val reserva = ReservaHidromiel("u", 10, null)
        assertFalse(reserva.descontar(15, TipoMovimiento.ENTRADA_NIVEL))
        assertEquals(10, reserva.saldo)
        assertEquals(10, reserva.descontarHastaCero(15, TipoMovimiento.ABANDONO_CONTRARRELOJ))
        assertEquals(0, reserva.saldo)
        assertEquals(0, reserva.movimientosNuevos.last().saldoResultante)
    }

    @Test
    fun `la diaria se puede reclamar solo en una fecha posterior`() {
        val dia = LocalDate.of(2026, 10, 13)
        val reserva = ReservaHidromiel("u", 0, dia)
        assertFalse(reserva.puedeReclamarDiaria(dia))
        assertTrue(reserva.puedeReclamarDiaria(dia.plusDays(1)))
    }
}
