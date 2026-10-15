package com.aesir.odin.domain.usecase.memorama

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.EstadoTarjeta
import com.aesir.odin.domain.model.ParConcepto
import com.aesir.odin.domain.model.ResultadoMemorama
import com.aesir.odin.domain.model.TarjetaMemorama
import com.aesir.odin.domain.model.TipoTarjeta
import com.aesir.odin.domain.repository.MemoramaRepository
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.mundo
import com.aesir.odin.domain.usecase.leccion.tema
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FakeMemoramaRepository(private val pares: List<ParConcepto>) : MemoramaRepository {
    val resultados = mutableListOf<ResultadoMemorama>()

    override suspend fun obtenerParesPorMundo(mundoId: String) = pares.filter { it.mundoId == mundoId }
    override suspend fun mundoTieneMemorama(mundoId: String) =
        obtenerParesPorMundo(mundoId).size >= GenerarMemoramaUseCase.NUM_PARES
    override suspend fun guardarResultado(resultado: ResultadoMemorama) {
        resultados += resultado
    }
}

private fun banco(mundoId: String, n: Int) = (1..n).map { ParConcepto("p$it", mundoId, "Concepto $it", "Definición $it") }

class GenerarMemoramaUseCaseTest {

    @Test
    fun `genera seis pares con una tarjeta de concepto y una de definicion`() = runBlocking {
        val generar = GenerarMemoramaUseCase(FakeMemoramaRepository(banco("m1", 10)), Random(7))
        val tarjetas = generar("m1")

        assertEquals(12, tarjetas.size)
        assertTrue(tarjetas.all { it.estado == EstadoTarjeta.OCULTA })
        val porPar = tarjetas.groupBy { it.parId }
        assertEquals(GenerarMemoramaUseCase.NUM_PARES, porPar.size)
        porPar.values.forEach { par ->
            assertEquals(setOf(TipoTarjeta.CONCEPTO, TipoTarjeta.DEFINICION), par.map { it.tipo }.toSet())
        }
    }

    @Test
    fun `sin pares suficientes no genera tablero`() = runBlocking {
        val generar = GenerarMemoramaUseCase(FakeMemoramaRepository(banco("m1", 5)))
        assertTrue(generar("m1").isEmpty())
    }
}

class VerificarParejaUseCaseTest {
    private val verificar = VerificarParejaUseCase()
    private val concepto = TarjetaMemorama("p1_c", "p1", TipoTarjeta.CONCEPTO, "Clase")
    private val definicion = TarjetaMemorama("p1_d", "p1", TipoTarjeta.DEFINICION, "Plantilla…")
    private val otra = TarjetaMemorama("p2_d", "p2", TipoTarjeta.DEFINICION, "Otra")

    @Test
    fun `concepto y su definicion son pareja`() = assertTrue(verificar(concepto, definicion))

    @Test
    fun `tarjetas de pares distintos no son pareja`() = assertFalse(verificar(concepto, otra))

    @Test
    fun `la misma tarjeta no es pareja de si misma`() = assertFalse(verificar(concepto, concepto))
}

class CalcularPuntosMemoramaUseCaseTest {
    private val calcular = CalcularPuntosMemoramaUseCase()

    @Test
    fun `rapido obtiene 100`() {
        assertEquals(100, calcular(10))
        assertEquals(100, calcular(CalcularPuntosMemoramaUseCase.TIEMPO_MAX_PUNTAJE_SEG))
    }

    @Test
    fun `al limite o despues obtiene 0`() {
        assertEquals(0, calcular(CalcularPuntosMemoramaUseCase.TIEMPO_LIMITE_SEG))
        assertEquals(0, calcular(10_000))
    }

    @Test
    fun `a medio camino obtiene 50`() {
        val medio = (CalcularPuntosMemoramaUseCase.TIEMPO_MAX_PUNTAJE_SEG + CalcularPuntosMemoramaUseCase.TIEMPO_LIMITE_SEG) / 2
        assertEquals(50, calcular(medio))
    }

    @Test
    fun `los puntos nunca suben con mas tiempo`() {
        val puntos = (0L..300L).map { calcular(it) }
        assertTrue(puntos.zipWithNext().all { (a, b) -> b <= a })
        assertTrue(puntos.all { it in 0..100 })
    }
}

class FinalizarMemoramaUseCaseTest {

    // En el catálogo falso del MOD-01 los niveles se llaman "m1-nX"
    private val roadmap = FakeRoadmapRepository(
        temas = listOf(tema("m1-n1", 1, desbloqueado = true), tema("m1-n2", 2, desbloqueado = false)),
        mundos = listOf(mundo("m1"))
    )
    private val memoramaRepo = FakeMemoramaRepository(banco("m1", 6))
    private val hidromielRepo = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromielRepo)
    private val finalizar = FinalizarMemoramaUseCase(memoramaRepo, roadmap, CalcularPuntosMemoramaUseCase(), modulo, idUsuario = U)

    @Test
    fun `guarda tiempo y puntos, completa el nivel y avisa al MOD-01`() = runBlocking {
        val resultado = finalizar("m1", "m1-n2", tiempoInicio = 1_000L, tiempoFin = 31_000L)

        assertEquals(30L, resultado.tiempoEmpleadoSeg)
        assertEquals(100, resultado.puntos)
        assertEquals(listOf(resultado), memoramaRepo.resultados)
        assertTrue(roadmap.temas.getValue("m1-n2").completado)
        assertNotNull(hidromielRepo.superados[U to "m1-n2"])
    }
}
