package com.aesir.odin.domain.usecase.retoarrastrar

import com.aesir.odin.domain.hidromiel.EstadoNivel
import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.ElementoArrastrable
import com.aesir.odin.domain.model.ModalidadReto
import com.aesir.odin.domain.model.PosicionDestino
import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.model.RetoArrastrar
import com.aesir.odin.domain.repository.RetoRepository
import com.aesir.odin.domain.usecase.leccion.FakeHistorialRepository
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase
import com.aesir.odin.domain.usecase.leccion.tema
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class FakeRetoRepository(private val retos: List<RetoArrastrar>) : RetoRepository {
    val resultados = mutableListOf<ResultadoReto>()
    val completados = mutableSetOf<String>()
    override suspend fun obtenerRetoPorTema(temaId: String) = retos.first { it.temaId == temaId }
    override suspend fun guardarResultado(resultado: ResultadoReto) {
        resultados += resultado
        if (resultado.aprobado) completados += resultado.retoId
    }
}

/** Reto de ordenar un proceso de [n] etapas en el nivel "m1-n1". */
private fun reto(n: Int = 5) = RetoArrastrar(
    id = "r1",
    temaId = "m1-n1",
    modalidad = ModalidadReto.ORDENAR_PROCESO,
    instrucciones = "Ordena",
    imagenDiagrama = null,
    elementos = (1..n).map { ElementoArrastrable("e$it", "r1", "Etapa $it", "p$it") },
    posiciones = (1..n).map { PosicionDestino("p$it", "r1", it) },
    completado = false
)

class ValidarYPuntajeTest {

    private val validar = ValidarColocacionUseCase()
    private val puntaje = CalcularPuntajeRetoUseCase()

    @Test
    fun `todo en su lugar no deja elementos incorrectos`() {
        val r = reto()
        assertTrue(validar(r, r.solucion).isEmpty())
    }

    @Test
    fun `se senalan los mal colocados y los que faltan`() {
        val r = reto(4)
        val colocaciones = mapOf("e1" to "p2", "e2" to "p1", "e3" to "p3")
        assertEquals(listOf("e1", "e2", "e4"), validar(r, colocaciones))
    }

    @Test
    fun `100 menos 20 por intento incorrecto y nunca negativo`() {
        assertEquals(100, puntaje(0))
        assertEquals(80, puntaje(1))
        assertEquals(60, puntaje(2))
        assertEquals(0, puntaje(9))
    }
}

class ObtenerRetoTest {

    @Test
    fun `los elementos se revuelven y nunca quedan en el orden de la solucion`() = runBlocking {
        val r = reto()
        repeat(20) { semilla ->
            val obtenido = ObtenerRetoUseCase(FakeRetoRepository(listOf(r)), Random(semilla))("m1-n1")
            assertEquals(r.elementos.toSet(), obtenido.elementos.toSet())
            assertNotEquals(r.elementos.map { it.id }, obtenido.elementos.map { it.id })
        }
    }

    @Test
    fun `RF-70 compara contra la solucion aunque la base devuelva los elementos desordenados`() = runBlocking {
        // La consulta no tiene ORDER BY: simulamos que la base los devuelve en otro orden
        val base = reto()
        val r = base.copy(elementos = base.elementos.reversed())
        val solucion = base.elementos.map { it.id } // e1..e5 = p1..p5
        repeat(200) { semilla ->
            val obtenido = ObtenerRetoUseCase(FakeRetoRepository(listOf(r)), Random(semilla))("m1-n1")
            assertEquals(r.elementos.toSet(), obtenido.elementos.toSet())
            assertNotEquals(solucion, obtenido.elementos.map { it.id })
        }
    }

    @Test
    fun `RF-70 con dos etapas que llegan invertidas nunca devuelve la solucion`() = runBlocking {
        // Con dos etapas la única mezcla válida es la inversa de la solución,
        // sin importar el orden en que las devuelva la base de datos
        val base = reto(2)
        val r = base.copy(elementos = base.elementos.reversed())
        repeat(50) { semilla ->
            val obtenido = ObtenerRetoUseCase(FakeRetoRepository(listOf(r)), Random(semilla))("m1-n1")
            assertEquals(listOf("e2", "e1"), obtenido.elementos.map { it.id })
        }
    }
}

class RegistrarErrorRetoTest {

    @Test
    fun `registra cada intento y avisa al tercero`() = runBlocking {
        val historial = FakeHistorialRepository()
        val registrar = RegistrarErrorRetoArrastrarUseCase(historial) { 0L }
        assertFalse(registrar("r1", listOf("Diseño"), 1))
        assertFalse(registrar("r1", listOf("Diseño"), 2))
        assertTrue(registrar("r1", listOf("Diseño"), 3))
        assertEquals(3, historial.errores.size)
        assertTrue(historial.errores.all { it.tipoDesafio == RegistrarErrorRetoArrastrarUseCase.TIPO_ARRASTRAR && it.actividadId == "r1" })
        assertTrue(historial.errores.first().descripcion.contains("Diseño"))
    }
}

class FinalizarRetoTest {

    private val repo = FakeRetoRepository(listOf(reto()))
    private val roadmap = FakeRoadmapRepository(listOf(tema("m1-n1", 1, true)))
    private val hidromiel = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(linkedMapOf("m1" to 4)), hidromiel, hoy = { LocalDate.of(2026, 10, 14) })
    private val finalizar = FinalizarRetoUseCase(
        repo, CalcularPuntajeRetoUseCase(), RegistrarLeccionCompletadaUseCase(roadmap), modulo, U
    ) { 42L }

    @Test
    fun `aprobado guarda el puntaje, marca completado y supera el nivel`() = runBlocking {
        val resultado = finalizar(reto(), intentosFallidos = 1, aprobado = true)
        assertEquals(80, resultado.puntaje)
        assertEquals(2, resultado.intentos)
        assertTrue(resultado.aprobado)
        assertEquals(listOf(resultado), repo.resultados)
        assertTrue("r1" in repo.completados)
        assertTrue(roadmap.temas.getValue("m1-n1").completado)
        val estados = modulo.obtenerEstadoRoadmap(U, "m1").associate { it.idNivel to it.estado }
        assertEquals(EstadoNivel.SUPERADO, estados["m1-n1"])
    }

    @Test
    fun `al llegar al limite termina sin puntaje ni completado`() = runBlocking {
        val resultado = finalizar(reto(), intentosFallidos = 3, aprobado = false)
        assertEquals(0, resultado.puntaje)
        assertFalse(resultado.aprobado)
        assertFalse("r1" in repo.completados)
        assertFalse(roadmap.temas.getValue("m1-n1").completado)
        val estados = modulo.obtenerEstadoRoadmap(U, "m1").associate { it.idNivel to it.estado }
        assertNotEquals(EstadoNivel.SUPERADO, estados["m1-n1"])
    }
}
