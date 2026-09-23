package com.aesir.odin.domain.usecase.leccion

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrarErrorLeccionUseCaseTest {

    private val historial = FakeHistorialRepository()
    private val registrarError = RegistrarErrorLeccionUseCase(historial, reloj = { 1000L })

    @Test
    fun `antes del limite registra el error y permite continuar`() = runBlocking {
        assertFalse(registrarError("l1", "e1", erroresActuales = 1))
        assertFalse(registrarError("l1", "e2", erroresActuales = 2))
        assertEquals(2, historial.errores.size)
        assertTrue(historial.errores.all { it.tipoDesafio == RegistrarErrorLeccionUseCase.TIPO_EJERCICIO })
    }

    @Test
    fun `al tercer error indica el limite y registra el intento fallido`() = runBlocking {
        assertTrue(registrarError("l1", "e3", erroresActuales = 3))
        val tipos = historial.errores.map { it.tipoDesafio }
        assertEquals(
            listOf(RegistrarErrorLeccionUseCase.TIPO_EJERCICIO, RegistrarErrorLeccionUseCase.TIPO_LECCION_FALLIDA),
            tipos
        )
        assertEquals("l1", historial.errores.last().actividadId)
    }
}
