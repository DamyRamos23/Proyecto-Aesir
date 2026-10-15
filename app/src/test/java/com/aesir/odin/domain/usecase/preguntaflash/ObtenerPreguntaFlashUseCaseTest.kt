package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.tema
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ObtenerPreguntaFlashUseCaseTest {

    private fun temaCompletado(id: String, orden: Int) = tema(id, orden, desbloqueado = true).copy(completado = true)

    @Test
    fun sinTemasCompletados_devuelveNull() = runBlocking {
        val roadmap = FakeRoadmapRepository(temas = listOf(tema("t1", 1, desbloqueado = true)))
        val repo = FakePreguntaFlashRepository(listOf(preguntaFlash(temaId = "t1")))

        assertNull(ObtenerPreguntaFlashUseCase(roadmap, repo)())
    }

    @Test
    fun soloSaleDeTemasCompletados() = runBlocking {
        val roadmap = FakeRoadmapRepository(
            temas = listOf(temaCompletado("t1", 1), tema("t2", 2, desbloqueado = true))
        )
        val repo = FakePreguntaFlashRepository(
            listOf(preguntaFlash(id = "p1", temaId = "t1"), preguntaFlash(id = "p2", temaId = "t2"))
        )
        val useCase = ObtenerPreguntaFlashUseCase(roadmap, repo)

        repeat(20) { assertEquals("t1", useCase()?.temaId) }
    }

    @Test
    fun siUnTemaCompletadoNoTienePreguntas_usaOtro() = runBlocking {
        val roadmap = FakeRoadmapRepository(temas = listOf(temaCompletado("t1", 1), temaCompletado("t2", 2)))
        val repo = FakePreguntaFlashRepository(listOf(preguntaFlash(id = "p2", temaId = "t2")))
        val useCase = ObtenerPreguntaFlashUseCase(roadmap, repo)

        repeat(20) { assertNotNull(useCase()); assertEquals("p2", useCase()?.id) }
    }

    @Test
    fun noRepiteLasPreguntasRecientesMientrasHayaOtras() = runBlocking {
        val roadmap = FakeRoadmapRepository(temas = listOf(temaCompletado("t1", 1)))
        val total = ObtenerPreguntaFlashUseCase.MEMORIA_RECIENTES + 5
        val repo = FakePreguntaFlashRepository((1..total).map { preguntaFlash(id = "p$it", temaId = "t1") })
        val useCase = ObtenerPreguntaFlashUseCase(roadmap, repo)

        val vistas = (1..total).map { useCase()!!.id }

        // Ventanas de MEMORIA_RECIENTES + 1 preguntas seguidas nunca tienen repetidas.
        vistas.windowed(ObtenerPreguntaFlashUseCase.MEMORIA_RECIENTES + 1).forEach { ventana ->
            assertEquals(ventana.size, ventana.toSet().size)
        }
    }

    @Test
    fun conPocasPreguntas_noSeQuedaSinOpciones() = runBlocking {
        val roadmap = FakeRoadmapRepository(temas = listOf(temaCompletado("t1", 1)))
        val repo = FakePreguntaFlashRepository(
            listOf(preguntaFlash(id = "p1", temaId = "t1"), preguntaFlash(id = "p2", temaId = "t1"))
        )
        val useCase = ObtenerPreguntaFlashUseCase(roadmap, repo)

        val vistas = (1..10).map { useCase()!!.id }

        // Con dos preguntas se alternan: nunca sale la misma dos veces seguidas.
        vistas.zipWithNext().forEach { (a, b) -> assertNotEquals(a, b) }
    }
}
