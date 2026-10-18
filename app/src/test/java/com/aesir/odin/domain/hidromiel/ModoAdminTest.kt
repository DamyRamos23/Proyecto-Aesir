package com.aesir.odin.domain.hidromiel

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ModoAdminTest {

    private val repo = FakeHidromielRepository()
    private val catalogo = FakeCatalogo(linkedMapOf("m1" to 6))
    private val modulo = ModuloHidromiel(catalogo, repo, hoy = { LocalDate.of(2026, 10, 13) })

    @Before
    fun preparar() {
        ModoAdmin.alCambiar = null
        ModoAdmin.cargar(false)
    }

    @After
    fun limpiar() {
        ModoAdmin.cargar(false)
    }

    @Test
    fun `la combinacion se reconoce completa`() {
        val combinacion = CombinacionSecreta(listOf(0, 1, 3, 2, 0, 1, 3, 2))
        val toques = listOf(0, 1, 3, 2, 0, 1, 3, 2)
        val resultados = toques.mapIndexed { i, v -> combinacion.registrar(v, i * 300L) }
        assertEquals(List(7) { false } + true, resultados)
    }

    @Test
    fun `un toque equivocado o una pausa larga reinician la combinacion`() {
        val combinacion = CombinacionSecreta(listOf(0, 1, 2), pausaMaximaMs = 1_000)
        assertFalse(combinacion.registrar(0, 0))
        assertFalse(combinacion.registrar(3, 100))
        assertFalse(combinacion.registrar(1, 200))
        // pausa larga: se reinicia
        assertFalse(combinacion.registrar(0, 300))
        assertFalse(combinacion.registrar(1, 2_000))
        assertFalse(combinacion.registrar(2, 2_100))
        // completa
        assertFalse(combinacion.registrar(0, 3_000))
        assertFalse(combinacion.registrar(1, 3_100))
        assertTrue(combinacion.registrar(2, 3_200))
    }

    @Test
    fun `alternar guarda el cambio`() {
        var guardado: Boolean? = null
        ModoAdmin.alCambiar = { guardado = it }
        ModoAdmin.alternar()
        assertTrue(ModoAdmin.estaActivo)
        assertEquals(true, guardado)
        ModoAdmin.alternar()
        assertFalse(ModoAdmin.estaActivo)
        assertEquals(false, guardado)
    }

    @Test
    fun `en modo admin todos los niveles y el ragnarok son gratuitos`() = runBlocking {
        ModoAdmin.cargar(true)
        val estados = modulo.obtenerEstadoRoadmap(U, "m1").associate { it.idNivel to it.estado }
        assertEquals(EstadoNivel.GRATUITO, estados["m1-n3"])
        assertEquals(EstadoNivel.GRATUITO, estados["m1-ragnarok"])
        val acceso = modulo.cobrarEntrada(U, "m1-ragnarok")
        assertTrue(acceso.autorizado)
        assertEquals(40, repo.saldo())
    }
}
