package com.aesir.odin.domain.usecase.reto

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalizarYAbandonarRetoTest {

    private val repo = FakeRetoContrarrelojRepository()

    @Test
    fun `finalizar registra puntaje aciertos y respondidas sin penalizacion`() = runBlocking {
        val resultado = FinalizarRetoContrarrelojUseCase(repo, reloj = { 5000L })(
            puntaje = 40, aciertos = 4, respondidas = 6
        )
        assertEquals(40, resultado.puntaje)
        assertEquals(4, resultado.aciertos)
        assertEquals(6, resultado.respondidas)
        assertEquals(0, resultado.penalizacion)
        assertFalse(resultado.abandonado)
        assertEquals(5000L, resultado.fecha)
        assertEquals(listOf(resultado), repo.resultados)
    }

    @Test
    fun `abandonar descarta el puntaje y registra la penalizacion de 20`() = runBlocking {
        val resultado = AbandonarRetoContrarrelojUseCase(repo, reloj = { 7000L })()
        assertEquals(0, resultado.puntaje)
        assertEquals(AbandonarRetoContrarrelojUseCase.PENALIZACION, resultado.penalizacion)
        assertEquals(20, resultado.penalizacion)
        assertTrue(resultado.abandonado)
        assertEquals(listOf(resultado), repo.resultados)
    }
}
