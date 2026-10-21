package com.aesir.odin.domain.hidromiel

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** RN-08 piso de apuesta y apuestas de los juegos ocultos. */
class ApuestasOcultasTest {

    private class CatalogoAbierto : CatalogoNiveles by FakeCatalogo(linkedMapOf("m1" to 6)) {
        override suspend fun mundosDesbloqueados() = listOf("m1")
    }

    private val repo = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(CatalogoAbierto(), repo, hoy = { LocalDate.of(2026, 11, 12) })

    @Test
    fun `el piso es el costo del nivel disponible mas barato`() = runBlocking {
        // Al inicio solo el primer nivel es gratuito; sus vecinos están bloqueados → sin disponibles
        assertEquals(0, modulo.calcularPisoApuesta(U))
        modulo.alCompletarDiagnostico(DiagnosticoCompletado(U, "m1", listOf("m1-n1")))
        assertEquals(PoliticaCosto.C, modulo.calcularPisoApuesta(U))
    }

    @Test
    fun `una apuesta no puede dejar el saldo debajo del piso`() = runBlocking {
        modulo.alCompletarDiagnostico(DiagnosticoCompletado(U, "m1", listOf("m1-n1")))
        // Saldo 40, piso 20: se pueden apostar hasta 20
        val rechazada = modulo.apostarJuegoOculto(U, 25)
        assertFalse(rechazada.autorizado)
        assertEquals(5, rechazada.faltante)
        assertEquals(40, repo.saldo())

        assertTrue(modulo.apostarJuegoOculto(U, 20).autorizado)
        assertEquals(20, repo.saldo())
        modulo.pagarJuegoOculto(U, 36)
        assertEquals(56, repo.saldo())
        assertEquals(TipoMovimiento.PREMIO_OCULTO, repo.historial.last().tipo)
    }
}
