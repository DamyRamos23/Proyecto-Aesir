package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.model.Actividad
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.tema
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** En modo admin el contrarreloj enciende todas las runas: entran todas las lecciones. */
class ContrarrelojModoAdminTest {

    @Before
    fun preparar() {
        ModoAdmin.alCambiar = null
        ModoAdmin.cargar(true)
    }

    @After
    fun limpiar() = ModoAdmin.cargar(false)

    @Test
    fun `en modo admin entran todas las lecciones aunque no esten completadas`() = runBlocking {
        val roadmap = FakeRoadmapRepository(
            listOf(
                tema("t1", 1, desbloqueado = false),
                tema("t2", 2, desbloqueado = false),
                tema("mj", 3, desbloqueado = false).copy(anillo = 2, actividad = Actividad.MEMORAMA),
                tema("rg", 4, desbloqueado = false).copy(esRagnarok = true, anillo = 0)
            )
        )
        val preguntas = FakePreguntaFlashRepository(listOf(preguntaFlash("p1", "t1"), preguntaFlash("p2", "t2")))
        val reto = ArmarRetoContrarrelojUseCase(roadmap, preguntas)().getOrThrow()

        assertEquals(listOf("t1", "t2"), preguntas.ultimosTemas)
        assertEquals(2, reto.preguntas.size)
    }
}
