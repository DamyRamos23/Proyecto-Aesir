package com.aesir.odin.domain.usecase.flashcards

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.EstadoTarjeta
import com.aesir.odin.domain.model.PilaFlashcards
import com.aesir.odin.domain.model.ResultadoFlashcards
import com.aesir.odin.domain.model.TarjetaFlashcard
import com.aesir.odin.domain.repository.FlashcardsRepository
import com.aesir.odin.domain.usecase.leccion.FakeHistorialRepository
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase
import com.aesir.odin.domain.usecase.leccion.mundo
import com.aesir.odin.domain.usecase.leccion.tema
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.random.Random

class FakeFlashcardsRepository(private val banco: List<ConceptoCrucigrama>) : FlashcardsRepository {
    val resultados = mutableListOf<ResultadoFlashcards>()
    override suspend fun obtenerConceptosPorTemas(temaIds: List<String>) = banco.filter { it.temaId in temaIds }
    override suspend fun guardarResultado(resultado: ResultadoFlashcards) { resultados += resultado }
    override suspend fun obtenerMejorResultado(temaId: String) =
        resultados.filter { it.temaId == temaId }.maxByOrNull { it.puntaje }
}

private fun tarjeta(i: Int, correcta: Int = 0) = TarjetaFlashcard(
    id = "c$i", temaId = "m1-n1", descripcion = "Descripción $i", concepto = "Concepto $i",
    opciones = listOf("Concepto $i", "Otro A", "Otro B", "Otro C"), indiceCorrecto = correcta
)

private val BANCO = (1..10).map { i ->
    ConceptoCrucigrama("c$i", if (i <= 5) "m1-n1" else "m1-n2", "Concepto$i", "Descripción $i")
}

class PilaFlashcardsTest {

    @Test
    fun `acertar todo al primer intento da 100 y vacia la pila`() {
        val pila = PilaFlashcards(List(3) { tarjeta(it) })
        repeat(3) {
            assertTrue(pila.responder(0)!!.correcta)
            pila.continuar()
        }
        assertTrue(pila.terminada)
        assertEquals(100, pila.puntaje)
        assertEquals(0, pila.errores)
        assertEquals(3, pila.dominadasCount)
    }

    @Test
    fun `A5_1 una tarjeta fallada regresa al final de la pila y cuenta el error`() {
        val pila = PilaFlashcards(listOf(tarjeta(1), tarjeta(2), tarjeta(3)))
        val r = pila.responder(2)!!
        assertFalse(r.correcta)
        pila.continuar()
        assertEquals("c2", pila.actual!!.id)
        assertEquals(3, pila.restantes)
        assertEquals(1, pila.errores)
        assertEquals(EstadoTarjeta.FALLADA, pila.estadoDe(tarjeta(1)))
        // La tarjeta fallada es la última de la pila
        pila.responder(0); pila.continuar()
        pila.responder(0); pila.continuar()
        assertEquals("c1", pila.actual!!.id)
    }

    @Test
    fun `el puntaje cuenta solo las tarjetas acertadas al primer intento`() {
        val pila = PilaFlashcards(listOf(tarjeta(1), tarjeta(2), tarjeta(3), tarjeta(4)))
        pila.responder(3); pila.continuar()                 // c1 falla
        repeat(3) { pila.responder(0); pila.continuar() }   // c2, c3, c4 aciertan
        pila.responder(0); pila.continuar()                 // c1 acierta al segundo intento
        assertTrue(pila.terminada)
        assertEquals(3, pila.aciertosPrimerIntento)
        assertEquals(75, pila.puntaje)
        assertEquals(1, pila.errores)
        assertEquals(80, pila.precision) // 4 aciertos en 5 respuestas
    }

    @Test
    fun `no se acepta otra respuesta hasta continuar`() {
        val pila = PilaFlashcards(listOf(tarjeta(1), tarjeta(2)))
        assertNotNull(pila.responder(0))
        assertNull(pila.responder(1))
        pila.continuar()
        assertNotNull(pila.responder(0))
    }

    @Test
    fun `A5_2 a la tercera falla de la misma tarjeta se sugiere repasar`() {
        val pila = PilaFlashcards(listOf(tarjeta(1)))
        val fallos = (1..3).map {
            val r = pila.responder(1)!!
            pila.continuar()
            r
        }
        assertEquals(listOf(false, false, true), fallos.map { it.sugerirRepaso })
        assertEquals(3, fallos.last().fallosDeLaTarjeta)
        // Si el alumno sigue, la cuarta falla ya no vuelve a sugerirlo
        assertFalse(pila.responder(1)!!.sugerirRepaso)
    }

    @Test
    fun `las mas dificiles van de mayor a menor numero de fallos`() {
        val pila = PilaFlashcards(listOf(tarjeta(1), tarjeta(2), tarjeta(3)))
        pila.responder(1); pila.continuar() // c1 falla 1
        pila.responder(1); pila.continuar() // c2 falla 1
        pila.responder(0); pila.continuar() // c3 acierta
        pila.responder(1); pila.continuar() // c1 falla 2
        val dificiles = pila.masDificiles()
        assertEquals(listOf("c1" to 2, "c2" to 1), dificiles.map { it.first.id to it.second })
    }
}

class GenerarFlashcardsUseCaseTest {

    private val roadmap = FakeRoadmapRepository(
        listOf(
            tema("m1-n1", 1, true), tema("m1-n2", 2, true),
            tema("m1-n3", 3, true).copy(), tema("mj", 9, true).copy(anillo = 2, actividad = com.aesir.odin.domain.model.Actividad.FLASHCARDS)
        ),
        listOf(mundo("m1"))
    )

    @Test
    fun `arma una tarjeta por concepto con cuatro opciones y la correcta marcada`() {
        val usecase = GenerarFlashcardsUseCase(FakeFlashcardsRepository(BANCO), roadmap, Random(1))
        val tarjetas = usecase.construir(BANCO.take(5), BANCO)

        assertEquals(5, tarjetas.size)
        tarjetas.forEach { t ->
            assertEquals(GenerarFlashcardsUseCase.OPCIONES, t.opciones.size)
            assertEquals(t.opciones.size, t.opciones.distinctBy { it.lowercase() }.size)
            assertEquals(t.concepto, t.opciones[t.indiceCorrecto])
        }
    }

    @Test
    fun `la tarjeta muestra la descripcion y la respuesta es el concepto`() {
        val usecase = GenerarFlashcardsUseCase(FakeFlashcardsRepository(BANCO), roadmap, Random(2))
        val t = usecase.construir(listOf(BANCO[0]), BANCO).single()
        assertEquals("Descripción 1", t.descripcion)
        assertEquals("Concepto1", t.concepto)
    }

    @Test
    fun `como maximo hay 12 tarjetas`() {
        val banco = (1..30).map { ConceptoCrucigrama("c$it", "m1-n1", "Concepto$it", "Descripción $it") }
        val usecase = GenerarFlashcardsUseCase(FakeFlashcardsRepository(banco), roadmap, Random(3))
        assertEquals(GenerarFlashcardsUseCase.MAX_TARJETAS, usecase.construir(banco, banco).size)
    }

    @Test
    fun `el orden de la pila es aleatorio`() {
        val a = GenerarFlashcardsUseCase(FakeFlashcardsRepository(BANCO), roadmap, Random(10)).construir(BANCO, BANCO)
        val b = GenerarFlashcardsUseCase(FakeFlashcardsRepository(BANCO), roadmap, Random(11)).construir(BANCO, BANCO)
        assertTrue(a.map { it.id } != b.map { it.id })
    }

    @Test
    fun `invoke toma los conceptos de los temas incluidos`() = runBlocking {
        val usecase = GenerarFlashcardsUseCase(FakeFlashcardsRepository(BANCO), roadmap, Random(4))
        val tarjetas = usecase("mj", listOf("m1-n1")).getOrThrow()
        assertEquals(5, tarjetas.size)
        assertTrue(tarjetas.all { it.temaId == "m1-n1" })
    }

    @Test
    fun `sin conceptos registrados regresa error`() = runBlocking {
        val usecase = GenerarFlashcardsUseCase(FakeFlashcardsRepository(emptyList()), roadmap)
        val r = usecase("mj", listOf("m1-n1"))
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull()!!.message!!.contains("tarjetas"))
    }
}

class FinalizarFlashcardsUseCaseTest {

    private fun entorno(): Triple<FakeFlashcardsRepository, FakeRoadmapRepository, FakeHidromielRepository> =
        Triple(
            FakeFlashcardsRepository(emptyList()),
            FakeRoadmapRepository(listOf(tema("m1-n1", 1, true))),
            FakeHidromielRepository()
        )

    @Test
    fun `guarda el resultado, marca el tema y avisa al modulo de hidromiel`() = runBlocking {
        val (repo, roadmap, hidromiel) = entorno()
        val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromiel)
        val finalizar = FinalizarFlashcardsUseCase(repo, RegistrarLeccionCompletadaUseCase(roadmap), modulo, U) { 9L }

        val pila = PilaFlashcards(List(4) { tarjeta(it) })
        pila.responder(1); pila.continuar() // una falla
        repeat(3) { pila.responder(0); pila.continuar() }
        pila.responder(0); pila.continuar()
        val r = finalizar("m1-n1", pila)

        assertEquals(ResultadoFlashcards("m1-n1", 75, 3, 1, 4, 9L), r)
        assertEquals(r, repo.resultados.single())
        assertTrue(roadmap.temas.getValue("m1-n1").completado)
        assertNotNull(hidromiel.superados[U to "m1-n1"])
        // 75 → bono 8 (redondeado); 40 iniciales + 20 + 8
        assertEquals(68, hidromiel.saldo())
    }

    @Test
    fun `no se puede finalizar con tarjetas pendientes`() = runBlocking {
        val (repo, roadmap, hidromiel) = entorno()
        val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromiel)
        val finalizar = FinalizarFlashcardsUseCase(repo, RegistrarLeccionCompletadaUseCase(roadmap), modulo, U)
        try {
            finalizar("m1-n1", PilaFlashcards(listOf(tarjeta(1))))
            fail("Debió rechazar una pila con tarjetas")
        } catch (e: IllegalArgumentException) {
            assertTrue(repo.resultados.isEmpty())
        }
    }

    @Test
    fun `A4_1 abandonar no guarda resultado ni marca el tema y no da hidromiel`() = runBlocking {
        val (repo, roadmap, hidromiel) = entorno()
        val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromiel)
        val sinSuperar = RegistrarFlashcardsSinSuperarUseCase(modulo, U)

        sinSuperar("m1-n1", ResultadoNivel.ABANDONADO)

        assertTrue(repo.resultados.isEmpty())
        assertFalse(roadmap.temas.getValue("m1-n1").completado)
        assertNull(hidromiel.superados[U to "m1-n1"])
    }

    @Test
    fun `sin superar rechaza el resultado superado`() = runBlocking {
        val (_, _, hidromiel) = entorno()
        val sinSuperar = RegistrarFlashcardsSinSuperarUseCase(ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromiel), U)
        try {
            sinSuperar("m1-n1", ResultadoNivel.SUPERADO)
            fail("Debió rechazar SUPERADO")
        } catch (e: IllegalArgumentException) {
            // esperado
        }
    }
}

class RegistrarErrorFlashcardUseCaseTest {
    @Test
    fun `A5_1 registra el error en el historial con la tarjeta`() = runBlocking {
        val historial = FakeHistorialRepository()
        RegistrarErrorFlashcardUseCase(historial) { 3L }(tarjeta(7), "Otro A")

        val error = historial.errores.single()
        assertEquals(RegistrarErrorFlashcardUseCase.TIPO_FLASHCARD, error.tipoDesafio)
        assertEquals("c7", error.actividadId)
        assertEquals(3L, error.fecha)
        assertTrue(error.descripcion.contains("Concepto 7"))
    }
}
