package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.TipoMovimiento
import com.aesir.odin.domain.hidromiel.U
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalizarYAbandonarRetoTest {

    private val repo = FakeRetoContrarrelojRepository()
    private val hidromielRepo = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromielRepo)
    private val pagar = PagarEntradaRetoContrarrelojUseCase(modulo, idUsuario = U)
    private val finalizar = FinalizarRetoContrarrelojUseCase(repo, modulo, idUsuario = U, reloj = { 5000L })
    private val abandonar = AbandonarRetoContrarrelojUseCase(repo, modulo, idUsuario = U, reloj = { 7000L })

    @Test
    fun `finalizar registra puntaje aciertos y respondidas sin penalizacion`() = runBlocking {
        val resultado = finalizar(puntaje = 8, aciertos = 4, respondidas = 6)
        assertEquals(8, resultado.puntaje)
        assertEquals(4, resultado.aciertos)
        assertEquals(6, resultado.respondidas)
        assertEquals(0, resultado.penalizacion)
        assertFalse(resultado.abandonado)
        assertEquals(5000L, resultado.fecha)
        assertEquals(listOf(resultado), repo.resultados)
    }

    @Test
    fun `la entrada cuesta 10 de hidromiel`() = runBlocking {
        hidromielRepo.fijarSaldo(40)
        assertTrue(pagar().autorizado)
        assertEquals(30, hidromielRepo.saldo())
        assertEquals(TipoMovimiento.ENTRADA_CONTRARRELOJ, hidromielRepo.historial.last().tipo)
    }

    @Test
    fun `sin hidromiel suficiente no se cobra la entrada`() = runBlocking {
        hidromielRepo.fijarSaldo(7)
        val acceso = pagar()
        assertFalse(acceso.autorizado)
        assertEquals(3, acceso.faltante)
        assertEquals(7, hidromielRepo.saldo())
    }

    @Test
    fun `al terminar cada correcta da 2 de hidromiel`() = runBlocking {
        hidromielRepo.fijarSaldo(40)
        pagar()
        finalizar(puntaje = 10, aciertos = 5, respondidas = 7)
        assertEquals(40 - 10 + 5 * 2, hidromielRepo.saldo())
    }

    @Test
    fun `abandonar pierde la entrada y resta 20 de hidromiel`() = runBlocking {
        hidromielRepo.fijarSaldo(40)
        pagar()
        val resultado = abandonar()
        assertEquals(0, resultado.puntaje)
        assertEquals(20, resultado.penalizacion)
        assertTrue(resultado.abandonado)
        assertEquals(40 - 10 - 20, hidromielRepo.saldo())
        assertEquals(listOf(resultado), repo.resultados)
    }

    @Test
    fun `abandonar nunca deja el hidromiel negativo`() = runBlocking {
        hidromielRepo.fijarSaldo(15)
        pagar()
        val resultado = abandonar()
        assertEquals(0, hidromielRepo.saldo())
        assertEquals(5, resultado.penalizacion)
    }
}
