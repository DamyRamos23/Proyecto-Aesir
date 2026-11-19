package com.aesir.odin.data.local

import com.aesir.odin.domain.model.ArbolHabilidades
import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.ResultadoCrucigrama
import com.aesir.odin.domain.repository.CrucigramaRepository
import com.aesir.odin.domain.usecase.crucigrama.GenerarCrucigramaUseCase
import com.aesir.odin.domain.usecase.crucigrama.normalizarPalabra
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** El contenido sembrado cumple la estructura del roadmap del SRS v3.2. */
class ContenidoRoadmapTest {

    private val temas = DatosIniciales.temas

    private fun delMundo(mundoId: String) = temas.filter { it.mundoId == mundoId && it.tipo != "RAGNAROK" }

    private fun idsTemas(mundoId: String) = delMundo(mundoId).filter { it.actividad == "LECCION" }.map { it.id }

    @Test
    fun `cada mundo tiene 18 niveles en cuatro anillos y un Ragnarok`() {
        DatosIniciales.mundos.forEach { mundo ->
            val niveles = delMundo(mundo.id)
            assertEquals(ArbolHabilidades.NIVELES_POR_MUNDO, niveles.size)
            assertEquals(mapOf(1 to 4, 2 to 6, 3 to 2, 4 to 6), niveles.groupingBy { it.anillo }.eachCount())
            assertEquals(1, temas.count { it.mundoId == mundo.id && it.tipo == "RAGNAROK" })
        }
    }

    @Test
    fun `cada mundo tiene 10 temas y todo nivel pertenece a uno`() {
        DatosIniciales.mundos.forEach { mundo ->
            val ids = idsTemas(mundo.id)
            assertEquals(ArbolHabilidades.TEMAS_POR_MUNDO, ids.size)
            delMundo(mundo.id).filter { it.anillo == 2 || it.anillo == 3 }.forEach { nivel ->
                assertTrue("${nivel.id} sin tema", nivel.temaBaseId in ids)
            }
            // Los minijuegos son de los temas 1 a 4 y quedan junto a su lección
            val minijuegos = delMundo(mundo.id).filter { it.anillo == 2 }.sortedBy { it.orden }
            assertEquals(
                ArbolHabilidades.TEMA_DE_MINIJUEGO.map { DatosIniciales.idTema(mundo.id, it) },
                minijuegos.map { it.temaBaseId }
            )
        }
    }

    @Test
    fun `cada tema tiene leccion con 5 ejercicios, 2 preguntas diagnosticas y preguntas flash`() {
        DatosIniciales.mundos.forEach { mundo ->
            idsTemas(mundo.id).forEach { tema ->
                val lecciones = DatosIniciales.lecciones.filter { it.temaId == tema }
                assertEquals(tema, 1, lecciones.size)
                val ejercicios = DatosIniciales.ejercicios.filter { e -> e.leccionId == lecciones.first().id }
                assertTrue(tema, ejercicios.size >= 5)
                ejercicios.forEach { e -> assertTrue(e.id, e.respuestasCorrectas.isNotEmpty() && e.respuestasCorrectas.all { it in e.opciones.indices }) }
                assertTrue(tema, DatosDiagnostico.preguntas.count { it.temaId == tema } >= 2)
                assertTrue(tema, BancoPreguntas.preguntas.count { it.temaId == tema } >= 2)
            }
        }
    }

    @Test
    fun `cada tema tiene al menos 6 conceptos validos y su crucigrama se puede armar`() = runBlocking {
        DatosIniciales.mundos.forEach { mundo ->
            idsTemas(mundo.id).forEach { tema ->
                val banco = DatosCrucigrama.conceptos.filter { it.temaId == tema }
                    .map { ConceptoCrucigrama(it.id, it.temaId, it.respuesta, it.descripcion) }
                val validos = banco.map { normalizarPalabra(it.respuesta) }.filter { it.length in 3..12 }.distinct()
                assertTrue("$tema: ${validos.size} conceptos", validos.size >= GenerarCrucigramaUseCase.MINIMO_PALABRAS)
                val repo = object : CrucigramaRepository {
                    override suspend fun obtenerBancoPorTemas(temaIds: List<String>) = banco
                    override suspend fun guardarResultado(resultado: ResultadoCrucigrama) = Unit
                    override suspend fun obtenerMejorResultado(temaId: String): ResultadoCrucigrama? = null
                }
                repeat(10) { semilla ->
                    assertTrue("$tema semilla $semilla", GenerarCrucigramaUseCase(repo, Random(semilla))(tema, listOf(tema)).isSuccess)
                }
            }
        }
    }

    @Test
    fun `los ids del contenido no se repiten`() {
        assertEquals(temas.size, temas.map { it.id }.toSet().size)
        assertEquals(DatosIniciales.ejercicios.size, DatosIniciales.ejercicios.map { it.id }.toSet().size)
        assertEquals(BancoPreguntas.preguntas.size, BancoPreguntas.preguntas.map { it.id }.toSet().size)
        assertEquals(DatosDiagnostico.preguntas.size, DatosDiagnostico.preguntas.map { it.id }.toSet().size)
        assertEquals(DatosCrucigrama.conceptos.size, DatosCrucigrama.conceptos.map { it.id }.toSet().size)
    }
}
