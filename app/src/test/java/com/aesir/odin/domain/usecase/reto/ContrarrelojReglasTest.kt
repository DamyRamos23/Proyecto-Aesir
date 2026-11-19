package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.ResultadoContrarreloj
import com.aesir.odin.domain.model.RetoContrareloj
import com.aesir.odin.domain.repository.RetoContrarrelojRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** CU-11: puntaje e hidromiel por separado (RF-154), límite diario (RF-147) y cierre de la app (E7.2). */
class ContrarrelojReglasTest {

    @Test
    fun `acierto suma 10 puntos y 2 de hidromiel, error resta 5 y 1 sin bajar de 0`() {
        var p = 0 to 0
        p = RetoContrareloj.acumular(p.first, p.second, correcta = false)
        assertEquals(0 to 0, p)
        p = RetoContrareloj.acumular(p.first, p.second, correcta = true)
        assertEquals(10 to 2, p)
        p = RetoContrareloj.acumular(p.first, p.second, correcta = false)
        assertEquals(5 to 1, p)
        assertEquals(20, RetoContrareloj.MAX_PREGUNTAS)
        assertEquals(120, RetoContrareloj.DURACION_SEGUNDOS)
    }

    private class RepoMemoria : RetoContrarrelojRepository {
        val filas = mutableMapOf<String, Pair<Long, Boolean>>() // id → (fecha, terminado)
        val resultados = mutableListOf<ResultadoContrarreloj>()
        override suspend fun guardarResultado(resultado: ResultadoContrarreloj) {
            resultados += resultado
            filas[resultado.id] = resultado.fecha to true
        }
        override suspend fun contarRetosDelDia(inicioDia: Long) = filas.values.count { it.first >= inicioDia }
        override suspend fun registrarInicio(id: String, fecha: Long) { filas[id] = fecha to false }
        override suspend fun retosSinTerminar() = filas.filterValues { !it.second }.keys.toList()
    }

    @Test
    fun `solo 3 retos por dia y el que quedo en curso cuenta como abandono`() = runBlocking {
        val hidromiel = FakeHidromielRepository()
        val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 2)), hidromiel)
        val repo = RepoMemoria()
        val ahora = 1_800_000_000_000L
        val pagar = PagarEntradaRetoContrarrelojUseCase(modulo, repo, U) { ahora }
        repeat(PoliticaCosto.MAX_CONTRARRELOJ_DIA) { i -> assertTrue(pagar("r$i").autorizado) }
        assertEquals(0, pagar.restantesHoy())
        assertFalse(pagar("r9").autorizado)

        val saldoAntes = hidromiel.saldo()!!
        CerrarContrarrelojSinTerminarUseCase(repo, AbandonarRetoContrarrelojUseCase(repo, modulo, U) { ahora })()
        assertTrue(repo.retosSinTerminar().isEmpty())
        assertEquals(3, repo.resultados.count { it.abandonado })
        assertEquals((saldoAntes - 3 * PoliticaCosto.ABANDONO_CONTRARRELOJ).coerceAtLeast(0), hidromiel.saldo())
    }
}
