package com.aesir.odin.domain.usecase.crucigrama

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.Celda
import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.Crucigrama
import com.aesir.odin.domain.model.Direccion
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.PalabraCrucigrama
import com.aesir.odin.domain.model.ResultadoCrucigrama
import com.aesir.odin.domain.repository.CrucigramaRepository
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase
import com.aesir.odin.domain.usecase.leccion.tema
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FakeCrucigramaRepository(private val banco: List<ConceptoCrucigrama>) : CrucigramaRepository {
    val resultados = mutableListOf<ResultadoCrucigrama>()
    override suspend fun obtenerBancoPorTemas(temaIds: List<String>) = banco.filter { it.temaId in temaIds }
    override suspend fun guardarResultado(resultado: ResultadoCrucigrama) { resultados += resultado }
    override suspend fun obtenerMejorResultado(temaId: String) =
        resultados.filter { it.temaId == temaId }.maxByOrNull { it.puntajeFinal }
}

private val BANCO = listOf(
    "COHESION", "ACOPLAMIENTO", "INTERFAZ", "COMPONENTE", "ARQUITECTURA", "MODULO",
    "REQUISITO", "FUNCIONAL", "PRUEBA", "DEFECTO", "REGRESION", "INTEGRACION"
).mapIndexed { i, r -> ConceptoCrucigrama("c$i", if (i < 6) "t1" else "t2", r, "Pista de $r") }

class GenerarCrucigramaUseCaseTest {

    /** Comprueba que ninguna casilla tenga dos letras distintas y que todo cruce. */
    private fun verificar(c: Crucigrama) {
        val letras = mutableMapOf<Celda, Char>()
        c.palabras.forEach { p ->
            p.celdas.forEachIndexed { i, celda ->
                val previa = letras.put(celda, p.respuesta[i])
                if (previa != null) assertEquals("Choque en $celda", previa, p.respuesta[i])
                assertTrue(celda.fila in 0 until c.filas && celda.columna in 0 until c.columnas)
            }
        }
        assertTrue(c.filas <= GenerarCrucigramaUseCase.MAX_LADO && c.columnas <= GenerarCrucigramaUseCase.MAX_LADO)
        if (c.palabras.size > 1) c.palabras.forEach { p ->
            val cruza = c.palabras.any { o -> o !== p && o.celdas.any { it in p.celdas } }
            assertTrue("${p.respuesta} no cruza con ninguna", cruza)
        }
        // Dos palabras en la misma dirección nunca comparten casilla
        c.palabras.forEach { p ->
            c.palabras.filter { it !== p && it.direccion == p.direccion }
                .forEach { o -> assertTrue(o.celdas.none { it in p.celdas }) }
        }
    }

    @Test
    fun `genera crucigramas validos con conceptos de los temas incluidos`() = runBlocking {
        repeat(30) { semilla ->
            val generar = GenerarCrucigramaUseCase(FakeCrucigramaRepository(BANCO), Random(semilla))
            val c = generar("nivel", listOf("t1", "t2")).getOrThrow()
            assertTrue(c.palabras.size >= GenerarCrucigramaUseCase.MINIMO_PALABRAS)
            assertTrue(c.palabras.size <= GenerarCrucigramaUseCase.CANTIDAD_PALABRAS)
            verificar(c)
        }
    }

    @Test
    fun `casi siempre logra acomodar las seis palabras`() = runBlocking {
        val completos = (0 until 30).count { semilla ->
            GenerarCrucigramaUseCase(FakeCrucigramaRepository(BANCO), Random(semilla))("n", listOf("t1", "t2"))
                .getOrThrow().palabras.size == GenerarCrucigramaUseCase.CANTIDAD_PALABRAS
        }
        assertTrue("Solo $completos de 30", completos >= 25)
    }

    @Test
    fun `A2_1 con banco insuficiente regresa error`() = runBlocking {
        val r = GenerarCrucigramaUseCase(FakeCrucigramaRepository(BANCO))("n", listOf("t1")).let { it }
        // t1 tiene exactamente 6: alcanza
        assertTrue(r.isSuccess)
        val vacio = GenerarCrucigramaUseCase(FakeCrucigramaRepository(BANCO.take(3)))("n", listOf("t1"))
        assertTrue(vacio.isFailure)
    }

    @Test
    fun `las respuestas se normalizan al generar`() = runBlocking {
        val banco = listOf("Cohesión", "acoplamiento", "Interfaz", "Componente", "Módulo", "Diseño")
            .mapIndexed { i, r -> ConceptoCrucigrama("c$i", "t", r, "p") }
        val c = GenerarCrucigramaUseCase(FakeCrucigramaRepository(banco), Random(1))("t", listOf("t")).getOrThrow()
        c.palabras.forEach { assertTrue(it.respuesta.all { ch -> ch in 'A'..'Z' || ch == 'Ñ' }) }
    }

    @Test
    fun `los numeros de pista siguen el orden de lectura`() = runBlocking {
        val c = GenerarCrucigramaUseCase(FakeCrucigramaRepository(BANCO), Random(3))("n", listOf("t1", "t2")).getOrThrow()
        val ordenados = c.palabras.sortedWith(compareBy({ it.fila }, { it.columna }))
        assertEquals(1, c.numeros.getValue(ordenados.first().id))
    }
}

class ValidarYPuntajeTest {
    private val validar = ValidarPalabraUseCase()
    private val puntaje = ActualizarPuntajeUseCase()
    private val palabra = PalabraCrucigrama("p", "c", "t", "DISENO", "pista", 0, 0, Direccion.HORIZONTAL)

    @Test
    fun `normaliza acentos y mayusculas pero conserva la enie`() {
        assertEquals("COHESION", validar.normalizar(" cohesión "))
        assertEquals("DISEÑO", validar.normalizar("diseño"))
        assertEquals("PINGUINO", validar.normalizar("pingüino"))
    }

    @Test
    fun `valida correcta incompleta e incorrecta`() {
        assertEquals(EstadoRespuesta.CORRECTA, validar(palabra, "diseno"))
        assertEquals(EstadoRespuesta.INCOMPLETA, validar(palabra, "DIS"))
        assertEquals(EstadoRespuesta.INCORRECTA, validar(palabra, "DISEÑA"))
    }

    @Test
    fun `suma por acierto y penaliza sin bajar de cero`() {
        assertEquals(10, puntaje(0, EstadoRespuesta.CORRECTA))
        assertEquals(7, puntaje(10, EstadoRespuesta.INCORRECTA))
        assertEquals(0, puntaje(2, EstadoRespuesta.INCOMPLETA))
    }
}

class FinalizarCrucigramaUseCaseTest {

    @Test
    fun `guarda el resultado, marca el tema y avisa al modulo de hidromiel`() = runBlocking {
        val repo = FakeCrucigramaRepository(emptyList())
        val roadmap = FakeRoadmapRepository(listOf(tema("m1-n1", 1, desbloqueado = true)))
        val hidromiel = FakeHidromielRepository()
        val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromiel)
        val finalizar = FinalizarCrucigramaUseCase(repo, RegistrarLeccionCompletadaUseCase(roadmap), modulo, U) { 5L }

        val palabras = List(6) { i ->
            PalabraCrucigrama("p$i", "c$i", "m1-n1", "ABC", "p", i * 2, 0, Direccion.HORIZONTAL, resuelta = true, revelada = i == 0)
        }
        val c = Crucigrama("x", "m1-n1", listOf("t"), 12, 3, palabras)
        val r = finalizar(c, puntaje = 45, errores = 2)

        assertEquals(5, r.aciertos) // la revelada no cuenta
        assertEquals(45, r.puntajeFinal)
        assertEquals(r, repo.resultados.single())
        assertTrue(roadmap.temas.getValue("m1-n1").completado)
        assertNotNull(hidromiel.superados[U to "m1-n1"])
        // 45 de 60 = 75 % → bono 8; 40 iniciales + 20 + 8
        assertEquals(68, hidromiel.saldo())
    }
}
