package com.aesir.odin.domain.racha

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.TipoMovimiento
import com.aesir.odin.domain.hidromiel.U
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Pruebas del MOD-05 Sistema de racha · La carrera de Sól. */
class RachaTest {

    private class FakeAlmacen : AlmacenRacha {
        var racha: Racha? = null
        val hitos = mutableMapOf<Int, Hito>()
        override suspend fun leer(idUsuario: String) = racha?.let {
            Racha(it.idUsuario, it.actual, it.maxima, it.ultimaActividad, it.ofrendas, it.titulos)
        }
        override suspend fun guardar(racha: Racha): Boolean {
            this.racha = racha
            return true
        }
        override suspend fun hitoCobrado(idUsuario: String, dias: Int) = dias in hitos
        override suspend fun registrarHito(idUsuario: String, hito: Hito): Boolean = hitos.put(hito.dias, hito) == null
        override suspend fun hitosCobrados(idUsuario: String) = hitos.keys.sorted()
    }

    private class FakeSonido : ReproductorSonido {
        val sonidos = mutableListOf<SonidoRacha>()
        override fun reproducirProgreso(sonido: SonidoRacha) {
            sonidos += sonido
        }
    }

    private val hoy = LocalDate.of(2026, 11, 14)
    private val almacen = FakeAlmacen()
    private val sonido = FakeSonido()
    private val hidromielRepo = FakeHidromielRepository()
    private val hidromiel = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromielRepo)
    private val modulo = ModuloCarreraSol(almacen, hidromiel, sonido)

    @After
    fun apagarAdmin() = ModoAdmin.cargar(false)

    private suspend fun terminar(fecha: LocalDate, tipo: TipoActividad = TipoActividad.NIVEL_ROADMAP, abandonada: Boolean = false) =
        modulo.alTerminarActividad(ActividadTerminada(U, tipo, abandonada, fecha))

    private suspend fun abrir(fecha: LocalDate) = modulo.alAbrirAplicacion(AplicacionAbierta(U, fecha))

    private fun racha(actual: Int, ultima: LocalDate?, ofrendas: Int = 0, maxima: Int = actual) {
        almacen.racha = Racha(U, actual, maxima, ultima, ofrendas)
    }

    // ---------------------------------------------------------------- Al abrir

    @Test
    fun `E8 primer uso queda apagada sin calcular dias`() = runBlocking {
        val estado = abrir(hoy)
        assertEquals(EstadoRachaDia.APAGADA, estado.estado)
        assertEquals(0, estado.actual)
    }

    @Test
    fun `si la ultima actividad fue ayer la racha queda en riesgo`() = runBlocking {
        racha(5, hoy.minusDays(1))
        assertEquals(EstadoRachaDia.EN_RIESGO, abrir(hoy).estado)
    }

    @Test
    fun `si ya conto hoy la racha esta encendida`() = runBlocking {
        racha(5, hoy)
        assertEquals(EstadoRachaDia.ENCENDIDA, abrir(hoy).estado)
    }

    @Test
    fun `RN-27 una ofrenda por cada dia faltante protege la racha`() = runBlocking {
        racha(5, hoy.minusDays(3), ofrendas = 2) // faltaron 2 días
        val estado = abrir(hoy)
        assertEquals(EstadoRachaDia.PROTEGIDA, estado.estado)
        assertEquals(5, estado.actual)
        assertEquals(2, estado.ofrendasUsadas)
        assertEquals(0, estado.ofrendas)
        // Volver a abrir el mismo día no gasta más ofrendas
        assertEquals(EstadoRachaDia.EN_RIESGO, abrir(hoy).estado)
    }

    @Test
    fun `E1 sin ofrendas suficientes la racha se reinicia y la maxima se conserva`() = runBlocking {
        racha(9, hoy.minusDays(3), ofrendas = 1, maxima = 12)
        val estado = abrir(hoy)
        assertEquals(EstadoRachaDia.APAGADA, estado.estado)
        assertEquals(0, estado.actual)
        assertEquals(9, estado.rachaPerdida)
        assertEquals(12, estado.maxima)
        assertEquals(1, estado.ofrendas) // no se gastan si no alcanzan
    }

    @Test
    fun `E6 con la fecha hacia atras no se usan ofrendas ni se suma`() = runBlocking {
        racha(4, hoy, ofrendas = 1)
        abrir(hoy.minusDays(5))
        assertEquals(1, almacen.racha!!.ofrendas)
        terminar(hoy.minusDays(5))
        assertEquals(4, almacen.racha!!.actual)
    }

    // ---------------------------------------------------------------- Al terminar

    @Test
    fun `la primera actividad del dia suma uno y enciende la racha`() = runBlocking {
        racha(2, hoy.minusDays(1))
        val estado = terminar(hoy)
        assertEquals(EstadoRachaDia.ENCENDIDA, estado.estado)
        assertEquals(3, estado.actual)
        assertEquals(3, estado.maxima)
        assertTrue(SonidoRacha.RACHA_ENCENDIDA in sonido.sonidos)
    }

    @Test
    fun `RN-25 solo una actividad por dia suma`() = runBlocking {
        terminar(hoy)
        terminar(hoy)
        assertEquals(1, almacen.racha!!.actual)
    }

    @Test
    fun `E2 abandonar y la pregunta flash no cuentan`() = runBlocking {
        terminar(hoy, abandonada = true)
        terminar(hoy, TipoActividad.PREGUNTA_FLASH)
        assertEquals(0, almacen.racha?.actual ?: 0)
    }

    @Test
    fun `si la app siguio abierta varios dias se resuelven antes los dias faltantes`() = runBlocking {
        racha(6, hoy.minusDays(3)) // sin ofrendas
        val estado = terminar(hoy)
        assertEquals(1, estado.actual)
        assertEquals(6, estado.rachaPerdida)
    }

    // ---------------------------------------------------------------- Hitos

    @Test
    fun `al llegar a 7 dias se abona el bono una sola vez`() = runBlocking {
        hidromielRepo.fijarSaldo(0)
        racha(6, hoy.minusDays(1))
        val estado = terminar(hoy)
        assertEquals(7, estado.hito?.dias)
        assertEquals(20, hidromielRepo.saldo())
        assertEquals(TipoMovimiento.BONO_RACHA, hidromielRepo.historial.last().tipo)
        assertTrue(SonidoRacha.HITO_RACHA in sonido.sonidos)

        // E7: pierde la racha y vuelve a llegar a 7: no hay bono otra vez
        racha(6, hoy.plusDays(9))
        assertNull(terminar(hoy.plusDays(10)).hito)
        assertEquals(20, hidromielRepo.saldo())
    }

    @Test
    fun `el hito de 30 dias otorga el titulo y se conserva al perder la racha`() = runBlocking {
        racha(29, hoy.minusDays(1))
        terminar(hoy)
        assertEquals(listOf("Hijo de Sól"), almacen.racha!!.titulos)
        abrir(hoy.plusDays(5))
        assertEquals(0, almacen.racha!!.actual)
        assertEquals(listOf("Hijo de Sól"), almacen.racha!!.titulos)
    }

    // ---------------------------------------------------------------- Ofrendas

    @Test
    fun `la ofrenda se valida sin cobrar y se cobra al confirmar`() = runBlocking {
        hidromielRepo.fijarSaldo(100)
        val validacion = modulo.validarOfrenda(U)
        assertTrue(validacion.puedeComprar)
        assertEquals(40, validacion.costo)
        assertEquals(100, hidromielRepo.saldo())

        val compra = modulo.comprarOfrenda(U)
        assertTrue(compra.comprada)
        assertEquals(1, compra.ofrendas)
        assertEquals(60, hidromielRepo.saldo())
        assertEquals(TipoMovimiento.OFRENDA_SOL, hidromielRepo.historial.last().tipo)
    }

    @Test
    fun `E3 sin hidromiel suficiente la ofrenda no se entrega`() = runBlocking {
        hidromielRepo.fijarSaldo(30)
        assertEquals(MotivoRechazo.HIDROMIEL_INSUFICIENTE, modulo.validarOfrenda(U).motivo)
        val compra = modulo.comprarOfrenda(U)
        assertFalse(compra.comprada)
        assertEquals(30, hidromielRepo.saldo())
    }

    @Test
    fun `E4 con dos ofrendas guardadas no se puede comprar otra`() = runBlocking {
        hidromielRepo.fijarSaldo(500)
        racha(3, hoy, ofrendas = 2)
        assertEquals(MotivoRechazo.LIMITE_OFRENDAS, modulo.validarOfrenda(U).motivo)
        assertFalse(modulo.comprarOfrenda(U).comprada)
        assertEquals(500, hidromielRepo.saldo())
    }

    @Test
    fun `MOD-04 consulta el estado del dia sin usar ofrendas`() = runBlocking {
        racha(5, hoy.minusDays(2), ofrendas = 1)
        assertEquals(EstadoRachaDia.PROTEGIDA, modulo.estadoDelDia(U, hoy))
        assertEquals(1, almacen.racha!!.ofrendas)
    }
}
