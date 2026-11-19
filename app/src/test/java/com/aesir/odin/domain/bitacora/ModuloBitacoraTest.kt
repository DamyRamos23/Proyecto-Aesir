package com.aesir.odin.domain.bitacora

import com.aesir.odin.domain.model.EstadoError
import com.aesir.odin.domain.usecase.leccion.FakeHistorialRepository
import com.aesir.odin.domain.usecase.leccion.bitacoraInmediata
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOD-06: pasos 2 a 4, RN-32 y E2. */
class ModuloBitacoraTest {

    private val historial = FakeHistorialRepository()
    private val bitacora = bitacoraInmediata(historial)

    private fun fallo(origen: OrigenError, respuesta: String?, fecha: Long) = FalloBitacora(
        origen = origen, preguntaId = "p1", temaId = "t1", enunciado = "¿Cuál?",
        opciones = listOf("A", "B"), respuestaCorrecta = "B", respuestaAlumno = respuesta, fecha = fecha
    )

    @Test
    fun `un fallo nuevo queda pendiente con un fallo`() {
        bitacora.registrarFallo(fallo(OrigenError.LECCION, "A", 10))
        val error = historial.errores.single()
        assertEquals(EstadoError.PENDIENTE, error.estado)
        assertEquals(1, error.fallos)
        assertEquals("B", error.respuestaCorrecta)
    }

    @Test
    fun `RN-32 la misma pregunta fallada en otra actividad suma un fallo al mismo registro`() {
        bitacora.registrarFallo(fallo(OrigenError.LECCION, "A", 10))
        bitacora.registrarFallo(fallo(OrigenError.RETO_CONTRARRELOJ, null, 20))
        val error = historial.errores.single()
        assertEquals(2, error.fallos)
        // Paso 4: se actualizan el origen, la respuesta del alumno y la fecha
        assertEquals(OrigenError.RETO_CONTRARRELOJ, error.origen)
        assertEquals(null, error.respuestaAlumno)
        assertEquals(20L, error.fecha)
    }

    @Test
    fun `E2 si no se puede escribir se descarta sin romper la actividad`() = runBlocking {
        historial.fallarEscritura = true
        bitacora.registrarFallo(fallo(OrigenError.LECCION, "A", 10))
        assertTrue(bitacora.guardar(fallo(OrigenError.LECCION, "A", 10)).isFailure)
        assertTrue(historial.errores.isEmpty())
    }
}
