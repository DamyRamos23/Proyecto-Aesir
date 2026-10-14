package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.Leccion
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalizarLeccionUseCaseTest {

    private fun leccion(id: String, temaId: String) = Leccion(
        id = id,
        temaId = temaId,
        titulo = "Lección $id",
        introduccion = "Introducción",
        orden = 1,
        ejercicios = emptyList(),
        completada = false
    )

    // En el catálogo falso, cada tema es un nivel "m1-nX"
    private val leccionRepo = FakeLeccionRepository(listOf(leccion("l1", "m1-n1"), leccion("l2", "m1-n2")))
    private val hidromielRepo = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromielRepo)
    private val finalizar = FinalizarLeccionUseCase(leccionRepo, modulo, idUsuario = U, reloj = { 1000L })
    private val sinSuperar = RegistrarNivelSinSuperarUseCase(leccionRepo, modulo, idUsuario = U)

    @Test
    fun `sin errores el puntaje es 100`() = runBlocking {
        val resultado = finalizar("l1", totalEjercicios = 5, errores = 0)
        assertEquals(100, resultado.puntaje)
        assertTrue(resultado.aprobada)
    }

    @Test
    fun `el puntaje depende de los aciertos`() = runBlocking {
        assertEquals(60, finalizar("l1", totalEjercicios = 5, errores = 2).puntaje)
    }

    @Test
    fun `guarda el resultado`() = runBlocking {
        finalizar("l1", totalEjercicios = 5, errores = 1)
        assertEquals(1, leccionRepo.resultados.size)
        assertEquals(1, leccionRepo.resultados.first().erroresCometidos)
        assertEquals(1000L, leccionRepo.resultados.first().fecha)
    }

    @Test
    fun `al aprobar marca la leccion y avisa al modulo que el nivel se supero`() = runBlocking {
        finalizar("l1", totalEjercicios = 5, errores = 0)
        assertTrue(leccionRepo.lecciones.getValue("l1").completada)
        assertNotNull(hidromielRepo.superados[U to "m1-n1"])
        // 40 iniciales + 20 + bono de 10 (puntaje 100)
        assertEquals(70, hidromielRepo.saldo())
    }

    @Test
    fun `con el limite de errores no aprueba ni otorga hidromiel`() = runBlocking {
        val resultado = finalizar("l1", totalEjercicios = 5, errores = 3)
        assertFalse(resultado.aprobada)
        assertFalse(leccionRepo.lecciones.getValue("l1").completada)
        assertNull(hidromielRepo.superados[U to "m1-n1"])
        assertEquals(40, hidromielRepo.saldo())
    }

    @Test
    fun `abandonar una leccion pagada pierde la entrada`() = runBlocking {
        finalizar("l1", totalEjercicios = 5, errores = 0) // 70
        assertTrue(modulo.cobrarEntrada(U, "m1-n2").autorizado) // 50
        sinSuperar("l2", ResultadoNivel.ABANDONADO)
        assertEquals(50, hidromielRepo.saldo())
        assertNull(hidromielRepo.superados[U to "m1-n2"])
    }
}
