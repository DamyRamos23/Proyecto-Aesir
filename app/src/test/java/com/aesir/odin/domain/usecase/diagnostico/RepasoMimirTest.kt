package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.model.Actividad
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.tema
import com.aesir.odin.domain.usecase.reto.FakePreguntaFlashRepository
import com.aesir.odin.domain.usecase.reto.preguntaFlash
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Repaso del pozo de Mímir: preguntas repartidas entre las lecciones del mundo. */
class RepasoMimirTest {

    private val roadmap = FakeRoadmapRepository(
        listOf(
            tema("t1", 1, desbloqueado = true),
            tema("t2", 2, desbloqueado = true),
            tema("mj", 3, desbloqueado = true).copy(anillo = 2, actividad = Actividad.CRUCIGRAMA)
        )
    )

    @Test
    fun `reparte las preguntas entre todas las lecciones`() = runBlocking {
        val banco = (1..8).map { preguntaFlash("a$it", "t1") } + (1..8).map { preguntaFlash("b$it", "t2") }
        val caso = ObtenerPreguntasRepasoUseCase(roadmap, FakePreguntaFlashRepository(banco), FakeDiagnosticoRepository(), Random(7))
        val preguntas = caso("m1")

        assertEquals(ObtenerPreguntasRepasoUseCase.NUM_PREGUNTAS, preguntas.size)
        assertEquals(5, preguntas.count { it.temaId == "t1" })
        assertEquals(5, preguntas.count { it.temaId == "t2" })
        assertEquals(preguntas.size, preguntas.map { it.id }.toSet().size)
        // Ordenadas por lección
        assertTrue(preguntas.takeWhile { it.temaId == "t1" }.size == 5)
    }

    @Test
    fun `sin banco usa las preguntas del diagnostico`() = runBlocking {
        val diagnostico = listOf(PreguntaDiagnostica("d1", "m1", "t1", "¿?", listOf("Sí", "No"), listOf(0)))
        val caso = ObtenerPreguntasRepasoUseCase(roadmap, FakePreguntaFlashRepository(emptyList()), FakeDiagnosticoRepository(diagnostico))
        assertEquals(listOf("d1"), caso("m1").map { it.id })
    }
}
