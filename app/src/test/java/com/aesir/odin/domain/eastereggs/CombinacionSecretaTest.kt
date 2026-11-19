package com.aesir.odin.domain.eastereggs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CombinacionSecretaTest {

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
}
