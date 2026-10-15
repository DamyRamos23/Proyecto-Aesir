package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.model.EstadoPreguntaFlash
import com.aesir.odin.domain.usecase.leccion.FakeHistorialRepository
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidarRespuestaPreguntaFlashUseCaseTest {

    private val repo = FakePreguntaFlashRepository()
    private val hidromiel = FakeHidromielFlashRepository(inicial = 10)
    private val historial = FakeHistorialRepository()
    private var n = 0
    private val useCase = ValidarRespuestaPreguntaFlashUseCase(
        repo, hidromiel, historial, ValidarRespuestaUseCase(),
        reloj = { 1_000L }, generarId = { "id${n++}" }
    )

    @Test
    fun correcta_suma5DeHidromiel_guardaIntento_yNoRegistraError() = runBlocking {
        val r = useCase(preguntaFlash(correctas = listOf(1)), listOf(1), 3_000L)

        assertEquals(EstadoPreguntaFlash.CORRECTA, r.estado)
        assertEquals(5, r.puntos)
        assertEquals(15, r.totalPuntos)
        assertEquals(15, hidromiel.saldo.value)
        assertEquals(listOf(EstadoPreguntaFlash.CORRECTA), hidromiel.aplicados)
        assertEquals(1, repo.intentos.size)
        assertEquals(5, repo.intentos[0].puntos)
        assertEquals(3_000L, repo.intentos[0].tiempoRespuestaMs)
        assertTrue(historial.errores.isEmpty())
    }

    @Test
    fun incorrecta_resta2DeHidromiel_yRegistraError() = runBlocking {
        val r = useCase(preguntaFlash(correctas = listOf(1)), listOf(2), 2_000L)

        assertEquals(EstadoPreguntaFlash.INCORRECTA, r.estado)
        assertEquals(-2, r.puntos)
        assertEquals(8, r.totalPuntos)
        assertEquals(8, hidromiel.saldo.value)
        assertEquals(listOf(1), r.opcionesCorrectas)
        assertEquals(1, historial.errores.size)
        assertEquals("PREGUNTA_FLASH", historial.errores[0].tipoDesafio)
        assertEquals("p1", historial.errores[0].actividadId)
        assertEquals(-2, repo.intentos[0].puntos)
    }

    @Test
    fun incompleta_resta2DeHidromiel_yRegistraError() = runBlocking {
        val r = useCase(preguntaFlash(correctas = listOf(0, 2)), listOf(0), 2_000L)

        assertEquals(EstadoPreguntaFlash.INCOMPLETA, r.estado)
        assertEquals(-2, r.puntos)
        assertEquals(8, hidromiel.saldo.value)
        assertEquals(listOf(0, 2), r.opcionesCorrectas)
        assertEquals(1, historial.errores.size)
        assertEquals(EstadoPreguntaFlash.INCOMPLETA, repo.intentos[0].estado)
    }

    @Test
    fun seleccionMultipleExacta_esCorrecta() = runBlocking {
        val r = useCase(preguntaFlash(correctas = listOf(0, 2)), listOf(2, 0), 1_000L)
        assertEquals(EstadoPreguntaFlash.CORRECTA, r.estado)
        assertEquals(15, hidromiel.saldo.value)
    }
}
