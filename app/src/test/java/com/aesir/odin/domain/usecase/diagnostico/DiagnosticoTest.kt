package com.aesir.odin.domain.usecase.diagnostico

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.OrigenSuperado
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.EstadoDiagnostico
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.model.RespuestaDiagnostica
import com.aesir.odin.domain.model.ResultadoDiagnostico
import com.aesir.odin.domain.repository.DiagnosticoRepository
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.mundo
import com.aesir.odin.domain.usecase.leccion.tema
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeDiagnosticoRepository(private val preguntas: List<PreguntaDiagnostica> = emptyList()) : DiagnosticoRepository {
    val resultados = mutableMapOf<String, ResultadoDiagnostico>()
    val omitidos = mutableSetOf<String>()

    override suspend fun obtenerPreguntasPorMundo(mundoId: String, cantidad: Int) =
        preguntas.filter { it.mundoId == mundoId }.take(cantidad)
    override suspend fun estadoDiagnostico(mundoId: String) = when {
        mundoId in omitidos -> EstadoDiagnostico.OMITIDA
        mundoId in resultados -> resultados.getValue(mundoId).estado
        else -> EstadoDiagnostico.PENDIENTE
    }
    override suspend fun guardarResultado(resultado: ResultadoDiagnostico) {
        resultados[resultado.mundoId] = resultado
    }
    override suspend fun registrarOmision(mundoId: String) {
        omitidos += mundoId
    }
}

/** 10 preguntas: temas n1 y n2 con 3, n3 y n4 con 2. La opción correcta siempre es la 0. */
private val preguntas: List<PreguntaDiagnostica> =
    listOf("m1-n1" to 3, "m1-n2" to 3, "m1-n3" to 2, "m1-n4" to 2).flatMap { (temaId, n) ->
        (1..n).map { i -> PreguntaDiagnostica("$temaId-p$i", "m1", temaId, "¿Pregunta $i?", listOf("Sí", "No", "Tal vez"), listOf(0)) }
    }

private fun responder(correctas: Set<String>) =
    preguntas.map { RespuestaDiagnostica(it.id, if (it.id in correctas) listOf(0) else listOf(1)) }

class EvaluarDiagnosticoUseCaseTest {
    private val evaluar = EvaluarDiagnosticoUseCase(ValidarRespuestaUseCase(), reloj = { 5L })

    @Test
    fun `todo correcto reconoce todos los temas`() {
        val r = evaluar("m1", preguntas, responder(preguntas.map { it.id }.toSet()))
        assertEquals(10, r.aciertos)
        assertEquals(10, r.totalPreguntas)
        assertEquals(setOf("m1-n1", "m1-n2", "m1-n3", "m1-n4"), r.temasConocidos.toSet())
        assertEquals(EstadoDiagnostico.COMPLETADA, r.estado)
    }

    @Test
    fun `un tema se reconoce al llegar al umbral de aciertos`() {
        // n1: 2 de 3 (sí), n2: 1 de 3 (no), n3: 2 de 2 (sí), n4: 1 de 2 (no)
        val r = evaluar("m1", preguntas, responder(setOf("m1-n1-p1", "m1-n1-p2", "m1-n2-p1", "m1-n3-p1", "m1-n3-p2", "m1-n4-p1")))
        assertEquals(6, r.aciertos)
        assertEquals(setOf("m1-n1", "m1-n3"), r.temasConocidos.toSet())
    }

    @Test
    fun `una respuesta incompleta cuenta como incorrecta`() {
        val multiple = PreguntaDiagnostica("x", "m1", "m1-n1", "¿?", listOf("A", "B", "C"), listOf(0, 1))
        val r = evaluar("m1", listOf(multiple), listOf(RespuestaDiagnostica("x", listOf(0))))
        assertEquals(0, r.aciertos)
    }
}

class FinalizarDiagnosticoUseCaseTest {
    private val roadmap = FakeRoadmapRepository(
        temas = (1..4).map { tema("m1-n$it", it, desbloqueado = it == 1) },
        mundos = listOf(mundo("m1"))
    )
    private val diagnosticoRepo = FakeDiagnosticoRepository(preguntas)
    private val hidromielRepo = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromielRepo)
    private val finalizar = FinalizarDiagnosticoUseCase(
        EvaluarDiagnosticoUseCase(ValidarRespuestaUseCase()), diagnosticoRepo, roadmap, modulo, idUsuario = U
    )

    @Test
    fun `registra la evaluacion, marca temas conocidos y los reconoce en el MOD-01 sin hidromiel`() = runBlocking {
        val r = finalizar("m1", preguntas, responder(setOf("m1-n1-p1", "m1-n1-p2", "m1-n1-p3")))

        assertEquals(listOf("m1-n1"), r.temasConocidos)
        assertEquals(EstadoDiagnostico.COMPLETADA, diagnosticoRepo.estadoDiagnostico("m1"))
        assertTrue(roadmap.temas.getValue("m1-n1").conocido)
        assertFalse(roadmap.temas.getValue("m1-n2").conocido)
        val superado = hidromielRepo.superados.getValue(U to "m1-n1")
        assertEquals(OrigenSuperado.DIAGNOSTICO, superado.origen)
        assertEquals(0, superado.recompensaOtorgada)
    }

    @Test
    fun `sin temas conocidos se completa sin tocar el progreso (A7_1)`() = runBlocking {
        val r = finalizar("m1", preguntas, responder(emptySet()))

        assertTrue(r.temasConocidos.isEmpty())
        assertEquals(EstadoDiagnostico.COMPLETADA, diagnosticoRepo.estadoDiagnostico("m1"))
        assertTrue(roadmap.temas.values.none { it.conocido })
        assertTrue(hidromielRepo.superados.isEmpty())
    }
}

class AccesoYOmisionDiagnosticoTest {
    private val roadmap = FakeRoadmapRepository(mundos = listOf(mundo("m1"), mundo("m2", 2, desbloqueado = false)))
    private val repo = FakeDiagnosticoRepository(preguntas)
    private val validar = ValidarAccesoDiagnosticoUseCase(roadmap, repo)

    @Test
    fun `se presenta en un mundo desbloqueado y pendiente`() = runBlocking {
        assertTrue(validar("m1"))
        assertFalse(validar("m2"))
    }

    @Test
    fun `al omitirla ya no se vuelve a presentar (A1_1)`() = runBlocking {
        OmitirDiagnosticoUseCase(repo)("m1")
        assertEquals(EstadoDiagnostico.OMITIDA, repo.estadoDiagnostico("m1"))
        assertFalse(validar("m1"))
        assertNull(repo.resultados["m1"])
    }

    @Test
    fun `obtiene diez preguntas del mundo`() = runBlocking {
        assertEquals(ObtenerPreguntasDiagnosticoUseCase.NUM_PREGUNTAS, ObtenerPreguntasDiagnosticoUseCase(repo)("m1").size)
    }
}
