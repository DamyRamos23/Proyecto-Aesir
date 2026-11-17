package com.aesir.odin.domain.usecase.ragnarok

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.ReglasReto
import com.aesir.odin.domain.usecase.leccion.FakeLeccionRepository
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.tema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** CU-08, RF-106: el reto se arma con 2 preguntas al azar de cada uno de los 10 temas. */
class ArmarRagnarokTest {

    private val caso = ObtenerRetoFinMundoUseCase(
        FakeRoadmapRepository(),
        FakeLeccionRepository(emptyList()),
        ModuloHidromiel(FakeCatalogo(mapOf("m1" to 1)), FakeHidromielRepository()),
        random = Random(3)
    )

    private val temas = (1..10).map { tema("t$it", it, desbloqueado = true) }
    private fun ejercicios(t: Int) = (1..5).map { Ejercicio("e${t}_$it", "l$t", "¿$t.$it?", listOf("a", "b", "c"), listOf(0), it) }

    @Test
    fun `veinte preguntas, dos de cada tema`() {
        val preguntas = caso.seleccionar(temas.mapIndexed { i, t -> t to ejercicios(i + 1) })
        assertEquals(20, ReglasReto.PREGUNTAS)
        assertEquals(ReglasReto.PREGUNTAS, preguntas.size)
        assertEquals(temas.map { it.id }.toSet(), preguntas.map { it.temaId }.toSet())
        assertTrue(preguntas.groupBy { it.temaId }.values.all { it.size == ReglasReto.PREGUNTAS_POR_TEMA })
    }

    @Test
    fun `el reintento prefiere preguntas nuevas`() {
        val porTema = temas.mapIndexed { i, t -> t to ejercicios(i + 1) }
        val primero = caso.seleccionar(porTema).map { it.id }.toSet()
        val segundo = caso.seleccionar(porTema, excluir = primero).map { it.id }.toSet()
        assertTrue((primero intersect segundo).isEmpty())
    }
}
