package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.tema
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArmarRetoContrarrelojUseCaseTest {

    private val banco = listOf(
        preguntaFlash("p1", "t1"),
        preguntaFlash("p2", "t1"),
        preguntaFlash("p3", "t2"),
        preguntaFlash("p4", "t3")
    )

    private fun armar(temas: FakeRoadmapRepository, preguntas: FakePreguntaFlashRepository) =
        ArmarRetoContrarrelojUseCase(temas, preguntas)

    @Test
    fun `sin temas estudiados falla con SinTemasEstudiados`() = runBlocking {
        val roadmap = FakeRoadmapRepository(listOf(tema("t1", 1, desbloqueado = true)))
        val resultado = armar(roadmap, FakePreguntaFlashRepository(banco))()
        assertTrue(resultado.exceptionOrNull() is SinTemasEstudiadosException)
    }

    @Test
    fun `solo toma preguntas de temas completados`() = runBlocking {
        val roadmap = FakeRoadmapRepository(
            listOf(
                tema("t1", 1, desbloqueado = true).copy(completado = true),
                tema("t2", 2, desbloqueado = true),
                tema("t3", 3, desbloqueado = true).copy(completado = true)
            )
        )
        val preguntas = FakePreguntaFlashRepository(banco)
        val reto = armar(roadmap, preguntas)().getOrThrow()

        assertEquals(listOf("t1", "t3"), preguntas.ultimosTemas)
        assertEquals(ArmarRetoContrarrelojUseCase.CANTIDAD_PREGUNTAS, preguntas.ultimaCantidad)
        assertEquals(setOf("p1", "p2", "p4"), reto.preguntas.map { it.id }.toSet())
        assertEquals(120, reto.duracionSegundos)
    }

    @Test
    fun `si el banco no tiene preguntas de los temas estudiados falla`() = runBlocking {
        val roadmap = FakeRoadmapRepository(listOf(tema("t9", 1, desbloqueado = true).copy(completado = true)))
        val resultado = armar(roadmap, FakePreguntaFlashRepository(banco))()
        assertTrue(resultado.exceptionOrNull() is SinPreguntasDisponiblesException)
    }
}
