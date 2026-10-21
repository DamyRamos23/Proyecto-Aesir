package com.aesir.odin.domain.hidromiel

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** CU-10 paso 9 y A9.1: el diagnóstico reconoce los niveles de los temas conocidos, anillo por anillo. */
class DiagnosticoArbolTest {

    /**
     * Lecciones L1-L3 en anillo; M1 conecta L1 y L2, M2 conecta L2 y L3;
     * R1 conecta M1 y M2.
     */
    private class CatalogoAnillos : CatalogoNiveles {
        private val niveles = listOf(
            NivelInfo("L1", "m", "L1", TipoNivel.NORMAL, esPrimero = true, anillo = 1),
            NivelInfo("L2", "m", "L2", TipoNivel.NORMAL, esPrimero = false, anillo = 1),
            NivelInfo("L3", "m", "L3", TipoNivel.NORMAL, esPrimero = false, anillo = 1),
            NivelInfo("M1", "m", "M1", TipoNivel.NORMAL, esPrimero = false, anillo = 2),
            NivelInfo("M2", "m", "M2", TipoNivel.NORMAL, esPrimero = false, anillo = 2),
            NivelInfo("R1", "m", "R1", TipoNivel.NORMAL, esPrimero = false, anillo = 3),
            NivelInfo("RAG", "m", "Ragnarök", TipoNivel.RAGNAROK, esPrimero = false)
        )
        private val conexiones = mapOf(
            "L1" to listOf("L2", "L3", "M1"), "L2" to listOf("L1", "L3", "M1", "M2"), "L3" to listOf("L1", "L2", "M2"),
            "M1" to listOf("L1", "L2", "R1"), "M2" to listOf("L2", "L3", "R1"), "R1" to listOf("M1", "M2"),
            "RAG" to listOf("L1", "L2", "L3", "M1", "M2", "R1")
        )
        override suspend fun obtener(idNivel: String) = niveles.first { it.idNivel == idNivel }
        override suspend fun nivelesDelMundo(idMundo: String) = niveles
        override suspend fun conectados(idNivel: String) = conexiones.getValue(idNivel)
        override suspend fun siguienteMundo(idMundo: String): String? = null
    }

    private val repo = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(CatalogoAnillos(), repo, hoy = { LocalDate.of(2026, 11, 12) })

    private suspend fun diagnosticar(vararg lecciones: String) =
        modulo.alCompletarDiagnostico(DiagnosticoCompletado(U, "m", lecciones.toList()))

    @Test
    fun `todas las lecciones conocidas reconocen minijuegos y retos y dan el bono A9_1`() = runBlocking {
        diagnosticar("L1", "L2", "L3")
        listOf("L1", "L2", "L3", "M1", "M2", "R1").forEach {
            assertEquals(OrigenSuperado.DIAGNOSTICO, repo.superados.getValue(U to it).origen)
        }
        assertNull(repo.superados[U to "RAG"])
        assertEquals(40 + PoliticaCosto.C * PoliticaCosto.MULT_RAGNAROK, repo.saldo())
        assertEquals(TipoMovimiento.BONO_DIAGNOSTICO, repo.historial.last().tipo)
        assertEquals(EstadoNivel.DISPONIBLE, modulo.obtenerEstadoRoadmap(U, "m").first { it.idNivel == "RAG" }.estado)
    }

    @Test
    fun `un minijuego solo cuenta si todas sus lecciones son conocidas`() = runBlocking {
        diagnosticar("L1", "L2")
        assertEquals(OrigenSuperado.DIAGNOSTICO, repo.superados.getValue(U to "M1").origen)
        assertNull(repo.superados[U to "M2"])
        assertNull("R1 necesita M1 y M2", repo.superados[U to "R1"])
        assertEquals("Sin bono si falta algún nivel", 40, repo.saldo())
    }
}
