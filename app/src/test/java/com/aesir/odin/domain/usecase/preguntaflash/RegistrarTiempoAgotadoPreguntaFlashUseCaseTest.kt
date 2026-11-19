package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.usecase.leccion.FakeHistorialRepository
import com.aesir.odin.domain.usecase.leccion.bitacoraInmediata
import com.aesir.odin.domain.model.EstadoPreguntaFlash
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrarTiempoAgotadoPreguntaFlashUseCaseTest {

    private val repo = FakePreguntaFlashRepository()
    private val historial = FakeHistorialRepository()

    private fun useCase(hidromiel: FakeHidromielFlashRepository) =
        RegistrarTiempoAgotadoPreguntaFlashUseCase(repo, hidromiel, bitacoraInmediata(historial), reloj = { 5L }, generarId = { "x" })

    @Test
    fun resta2DeHidromiel_yRegistraSinRespuesta() = runBlocking {
        val hidromiel = FakeHidromielFlashRepository(inicial = 20)
        val r = useCase(hidromiel)(preguntaFlash(correctas = listOf(1)))

        assertEquals(EstadoPreguntaFlash.SIN_RESPUESTA, r.estado)
        assertEquals(-2, r.puntos)
        assertEquals(18, r.totalPuntos)
        assertEquals(18, hidromiel.saldo.value)
        assertEquals(listOf(EstadoPreguntaFlash.SIN_RESPUESTA), hidromiel.aplicados)
        assertEquals(listOf(1), r.opcionesCorrectas)
        assertEquals(1, repo.intentos.size)
        assertEquals(EstadoPreguntaFlash.SIN_RESPUESTA, repo.intentos[0].estado)
        assertTrue(repo.intentos[0].respuestasElegidas.isEmpty())
        // MOD-06, disparador 2: se avisa a la bitácora sin respuesta del alumno
        assertEquals(null, historial.errores.single().respuestaAlumno)
    }

    @Test
    fun nuncaBajaDeCero() = runBlocking {
        val hidromiel = FakeHidromielFlashRepository(inicial = 1)
        val r = useCase(hidromiel)(preguntaFlash())

        assertEquals(0, r.totalPuntos)
        assertEquals(-1, r.puntos)
        assertEquals(0, hidromiel.saldo.value)
    }
}
