package com.aesir.odin.domain.usecase.leccion

import com.aesir.odin.domain.model.Leccion
import com.aesir.odin.domain.usecase.roadmap.DesbloquearSiguienteTemaUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalizarLeccionUseCaseTest {

    private val leccionRepo = FakeLeccionRepository(
        listOf(Leccion("l1", "t1", "Lección 1", 1, emptyList(), completada = false))
    )
    private val roadmapRepo = FakeRoadmapRepository(
        listOf(
            tema("t1", 1, desbloqueado = true),
            tema("t2", 2, desbloqueado = false),
            tema("t3", 3, desbloqueado = false),
            tema("otro", 2, desbloqueado = false, mundoId = "m2")
        )
    )
    private val finalizar = FinalizarLeccionUseCase(
        leccionRepository = leccionRepo,
        registrarCompletadaUseCase = RegistrarLeccionCompletadaUseCase(roadmapRepo),
        desbloquearUseCase = DesbloquearSiguienteTemaUseCase(roadmapRepo),
        reloj = { 1000L }
    )

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
    fun `al aprobar marca la leccion y el tema como completados`() = runBlocking {
        finalizar("l1", totalEjercicios = 5, errores = 2)
        assertTrue(leccionRepo.lecciones.getValue("l1").completada)
        assertTrue(roadmapRepo.temas.getValue("t1").leccionCompletada)
    }

    @Test
    fun `al aprobar desbloquea solo el siguiente tema del mismo mundo`() = runBlocking {
        finalizar("l1", totalEjercicios = 5, errores = 0)
        assertTrue(roadmapRepo.temas.getValue("t2").desbloqueado)
        assertFalse(roadmapRepo.temas.getValue("t3").desbloqueado)
        assertFalse(roadmapRepo.temas.getValue("otro").desbloqueado)
    }

    @Test
    fun `con el limite de errores no aprueba ni desbloquea`() = runBlocking {
        val resultado = finalizar("l1", totalEjercicios = 5, errores = 3)
        assertFalse(resultado.aprobada)
        assertFalse(leccionRepo.lecciones.getValue("l1").completada)
        assertFalse(roadmapRepo.temas.getValue("t2").desbloqueado)
    }
}
