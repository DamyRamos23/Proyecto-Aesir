package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.bitacora.OrigenError
import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.Leccion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrarErrorLeccionUseCaseTest {

    private val historial = FakeHistorialRepository()
    private val registrarError = RegistrarErrorLeccionUseCase(bitacoraInmediata(historial), reloj = { 1000L })
    private fun ejercicio(id: String) = Ejercicio(id, "l1", "¿Cuál?", listOf("A", "B", "C"), listOf(1), 0)
    private val leccion = Leccion("l1", "t1", "Tema 1", "", 0, listOf(ejercicio("e1"), ejercicio("e2")), false)

    @Test
    fun `antes del limite avisa el error a la bitacora y permite continuar`() {
        assertFalse(registrarError(leccion, ejercicio("e1"), listOf(0), erroresActuales = 1))
        assertFalse(registrarError(leccion, ejercicio("e2"), listOf(2), erroresActuales = 2))
        assertEquals(listOf("e1", "e2"), historial.errores.map { it.preguntaId })
        assertTrue(historial.errores.all { it.origen == OrigenError.LECCION && it.temaId == "t1" })
        assertEquals("A", historial.errores.first().respuestaAlumno)
        assertEquals("B", historial.errores.first().respuestaCorrecta)
    }

    @Test
    fun `al tercer error indica el limite (RF-75)`() {
        assertTrue(registrarError(leccion, ejercicio("e1"), listOf(0), erroresActuales = 3))
    }
}
