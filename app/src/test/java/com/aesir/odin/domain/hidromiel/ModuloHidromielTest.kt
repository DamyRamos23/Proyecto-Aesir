package com.aesir.odin.domain.hidromiel

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ModuloHidromielTest {

    private var hoy = LocalDate.of(2026, 10, 13)
    private val repo = FakeHidromielRepository()
    private val catalogo = FakeCatalogo(linkedMapOf("m1" to 6, "m2" to 6))
    private val modulo = ModuloHidromiel(catalogo, repo, hoy = { hoy })

    private suspend fun estados(mundo: String = "m1") =
        modulo.obtenerEstadoRoadmap(U, mundo).associate { it.idNivel to it.estado }

    /** Paga la entrada (si hace falta) y supera el nivel con el puntaje dado. */
    private suspend fun superar(idNivel: String, puntaje: Int = 100): AvisoHidromiel {
        assertTrue("No se pudo entrar a $idNivel", modulo.cobrarEntrada(U, idNivel).autorizado)
        return modulo.alFinalizarNivel(NivelFinalizado(U, idNivel, puntaje, ResultadoNivel.SUPERADO))
    }

    // ---------------------------------------------------------------- Precondición

    @Test
    fun `la primera vez se asigna la cantidad inicial de 40`() = runBlocking {
        modulo.obtenerEstadoRoadmap(U, "m1")
        assertEquals(40, repo.saldo())
        assertEquals(TipoMovimiento.SALDO_INICIAL, repo.historial.single().tipo)
    }

    @Test
    fun `antes de jugar el saldo observado es la cantidad inicial`() = runBlocking {
        assertEquals(40, modulo.observarSaldo(U).first())
    }

    // ---------------------------------------------------------------- Estados del roadmap

    @Test
    fun `al inicio el primer nivel es gratuito y los demas estan bloqueados`() = runBlocking {
        val e = estados()
        assertEquals(EstadoNivel.GRATUITO, e["m1-n1"])
        assertEquals(EstadoNivel.BLOQUEADO, e["m1-n2"])
        assertEquals(EstadoNivel.BLOQUEADO, e["m1-n6"])
        assertEquals(EstadoNivel.BLOQUEADO, e["m1-ragnarok"])
    }

    @Test
    fun `el arbol es un anillo - superar el primero abre el segundo y el ultimo`() = runBlocking {
        superar("m1-n1")
        val e = estados()
        assertEquals(EstadoNivel.DISPONIBLE, e["m1-n2"])
        assertEquals(EstadoNivel.DISPONIBLE, e["m1-n6"])
        assertEquals(EstadoNivel.BLOQUEADO, e["m1-n3"])
        assertEquals(EstadoNivel.BLOQUEADO, e["m1-n5"])
    }

    @Test
    fun `el roadmap informa el costo de cada nivel`() = runBlocking {
        superar("m1-n1")
        val r = modulo.obtenerEstadoRoadmap(U, "m1").associateBy { it.idNivel }
        assertEquals(0, r.getValue("m1-n1").costo)
        assertEquals(EstadoNivel.DISPONIBLE, r.getValue("m1-n2").estado)
        assertEquals(20, r.getValue("m1-n2").costo)
    }

    @Test
    fun `E4 un nivel bloqueado indica que nivel conectado hay que superar`() = runBlocking {
        val r = modulo.obtenerEstadoRoadmap(U, "m1").associateBy { it.idNivel }
        assertTrue(r.getValue("m1-n2").motivo.contains("Nivel 1"))
        assertFalse(r.getValue("m1-n2").autorizado)
    }

    @Test
    fun `E4 el Ragnarok bloqueado indica cuantos niveles faltan`() = runBlocking {
        superar("m1-n1")
        val ragnarok = modulo.obtenerEstadoRoadmap(U, "m1").first { it.idNivel == "m1-ragnarok" }
        assertEquals(EstadoNivel.BLOQUEADO, ragnarok.estado)
        assertEquals(5, ragnarok.faltante)
    }

    // ---------------------------------------------------------------- Cobro de entrada

    @Test
    fun `RN-04 no se puede pagar un nivel que no esta conectado a uno superado`() = runBlocking {
        val r = modulo.cobrarEntrada(U, "m1-n3")
        assertFalse(r.autorizado)
        assertEquals(EstadoNivel.BLOQUEADO, r.estado)
        assertEquals(40, repo.saldo())
    }

    @Test
    fun `E2 el primer nivel no cobra entrada`() = runBlocking {
        val r = modulo.cobrarEntrada(U, "m1-n1")
        assertTrue(r.autorizado)
        assertEquals(0, r.costo)
        assertEquals(40, repo.saldo())
    }

    @Test
    fun `entrar a un nivel disponible descuenta C`() = runBlocking {
        superar("m1-n1", puntaje = 0) // +20 → 60
        val r = modulo.cobrarEntrada(U, "m1-n2")
        assertTrue(r.autorizado)
        assertEquals(20, r.costo)
        assertEquals(40, repo.saldo())
        assertEquals(TipoMovimiento.ENTRADA_NIVEL, repo.historial.last().tipo)
        assertEquals(-20, repo.historial.last().cantidad)
    }

    @Test
    fun `E1 con hidromiel insuficiente no inicia e indica cuanto falta`() = runBlocking {
        superar("m1-n1", puntaje = 0)
        repo.fijarSaldo(15)
        val r = modulo.cobrarEntrada(U, "m1-n2")
        assertFalse(r.autorizado)
        assertEquals(5, r.faltante)
        assertEquals(15, repo.saldo())
    }

    @Test
    fun `E2 repetir un nivel superado no cobra`() = runBlocking {
        superar("m1-n1")
        superar("m1-n2")
        val antes = repo.saldo()
        val r = modulo.cobrarEntrada(U, "m1-n2")
        assertTrue(r.autorizado)
        assertEquals(EstadoNivel.SUPERADO, r.estado)
        assertEquals(antes, repo.saldo())
    }

    @Test
    fun `volver a entrar a un nivel ya pagado y sin terminar no cobra otra vez`() = runBlocking {
        superar("m1-n1", puntaje = 0) // 60
        assertTrue(modulo.cobrarEntrada(U, "m1-n2").autorizado) // 40
        val otraVez = modulo.cobrarEntrada(U, "m1-n2")
        assertTrue(otraVez.autorizado)
        assertEquals(40, repo.saldo())
        assertEquals(0, modulo.obtenerEstadoRoadmap(U, "m1").first { it.idNivel == "m1-n2" }.costo)

        // Al terminar (aunque no lo supere) la siguiente entrada vuelve a cobrarse
        modulo.alFinalizarNivel(NivelFinalizado(U, "m1-n2", 0, ResultadoNivel.NO_SUPERADO))
        assertEquals(20, modulo.cobrarEntrada(U, "m1-n2").costo)
        assertEquals(20, repo.saldo())
    }

    // ---------------------------------------------------------------- Recompensa

    @Test
    fun `superar un nivel da C mas bono proporcional al puntaje`() = runBlocking {
        val aviso = superar("m1-n1", puntaje = 55) // 20 + round(5.5) = 26
        assertEquals(26, aviso.cantidad)
        assertEquals(66, repo.saldo())
        assertEquals(26, repo.superados.getValue(U to "m1-n1").recompensaOtorgada)
        assertEquals(OrigenSuperado.JUGADO, repo.superados.getValue(U to "m1-n1").origen)
    }

    @Test
    fun `RN-02 repetir un nivel superado no da recompensa`() = runBlocking {
        superar("m1-n1")
        val saldo = repo.saldo()
        val aviso = modulo.alFinalizarNivel(NivelFinalizado(U, "m1-n1", 100, ResultadoNivel.SUPERADO))
        assertEquals(0, aviso.cantidad)
        assertEquals(saldo, repo.saldo())
    }

    @Test
    fun `no superar o abandonar pierde la entrada y no da hidromiel`() = runBlocking {
        superar("m1-n1", puntaje = 0) // 60
        modulo.cobrarEntrada(U, "m1-n2") // 40
        val noSuperado = modulo.alFinalizarNivel(NivelFinalizado(U, "m1-n2", 40, ResultadoNivel.NO_SUPERADO))
        assertEquals(40, repo.saldo())
        assertEquals(0, noSuperado.cantidad)
        assertTrue(noSuperado.mensaje.contains("20"))

        modulo.cobrarEntrada(U, "m1-n2") // 20
        modulo.alFinalizarNivel(NivelFinalizado(U, "m1-n2", 0, ResultadoNivel.ABANDONADO))
        assertEquals(20, repo.saldo())
        assertNull(repo.superados[U to "m1-n2"])
    }

    @Test
    fun `completar los niveles del mundo da 3C mas bono y abre el Ragnarok`() = runBlocking {
        for (i in 1..5) superar("m1-n$i", puntaje = 0)
        val antes = repo.saldo()!!
        val aviso = superar("m1-n6", puntaje = 100) // entrada 20, recompensa 60 + 10
        assertEquals(70, aviso.cantidad)
        assertEquals(antes - 20 + 70, repo.saldo())
        assertEquals(EstadoNivel.DISPONIBLE, estados()["m1-ragnarok"])
    }

    @Test
    fun `RN-05 el Ragnarok cuesta 3C y superarlo desbloquea el siguiente mundo`() = runBlocking {
        for (i in 1..6) superar("m1-n$i", puntaje = 0)
        val saldo = repo.saldo()!!
        val entrada = modulo.cobrarEntrada(U, "m1-ragnarok")
        assertEquals(60, entrada.costo)
        assertEquals(saldo - 60, repo.saldo())

        val aviso = modulo.alFinalizarNivel(NivelFinalizado(U, "m1-ragnarok", 100, ResultadoNivel.SUPERADO))
        assertEquals(30, aviso.cantidad) // C + B, no completa el mundo
        assertEquals("m2", repo.mundosDesbloqueados.single().idMundo)
        assertEquals(EstadoNivel.GRATUITO, estados("m2")["m2-n1"])
    }

    @Test
    fun `superar el Ragnarok del ultimo mundo no desbloquea nada`() = runBlocking {
        for (i in 1..6) superar("m2-n$i")
        superar("m2-ragnarok")
        assertTrue(repo.mundosDesbloqueados.isEmpty())
    }

    // ---------------------------------------------------------------- Diagnóstico

    @Test
    fun `RN-06 el diagnostico marca niveles como superados sin cobrar ni otorgar`() = runBlocking {
        modulo.obtenerEstadoRoadmap(U, "m1")
        modulo.alCompletarDiagnostico(DiagnosticoCompletado(U, "m1", listOf("m1-n3", "m1-ragnarok", "m2-n1")))
        assertEquals(40, repo.saldo())
        val e = estados()
        assertEquals(EstadoNivel.SUPERADO, e["m1-n3"])
        // Conectados a un nivel conocido quedan disponibles
        assertEquals(EstadoNivel.DISPONIBLE, e["m1-n2"])
        assertEquals(EstadoNivel.DISPONIBLE, e["m1-n4"])
        // El Ragnarök y niveles de otro mundo no se reconocen
        assertEquals(EstadoNivel.BLOQUEADO, e["m1-ragnarok"])
        assertNull(repo.superados[U to "m2-n1"])
        assertEquals(OrigenSuperado.DIAGNOSTICO, repo.superados.getValue(U to "m1-n3").origen)
    }

    @Test
    fun `un nivel conocido por diagnostico no da recompensa al jugarlo`() = runBlocking {
        modulo.alCompletarDiagnostico(DiagnosticoCompletado(U, "m1", listOf("m1-n2")))
        val aviso = modulo.alFinalizarNivel(NivelFinalizado(U, "m1-n2", 100, ResultadoNivel.SUPERADO))
        assertEquals(0, aviso.cantidad)
    }

    // ---------------------------------------------------------------- E3 diaria

    @Test
    fun `E3 RN-03 la recompensa diaria se reclama una vez por dia y no se acumula`() = runBlocking {
        assertNull("El primer día ya incluye la cantidad inicial", modulo.reclamarRecompensaDiaria(U))
        assertEquals(40, repo.saldo())

        hoy = hoy.plusDays(3)
        assertEquals(40, modulo.reclamarRecompensaDiaria(U)?.cantidad)
        assertNull(modulo.reclamarRecompensaDiaria(U))
        assertEquals(80, repo.saldo())
    }

    // ---------------------------------------------------------------- E5 ayudas

    @Test
    fun `E5 revelar letra cuesta 5 y palabra 15`() = runBlocking {
        assertTrue(modulo.cobrarAyuda(U, TipoAyuda.REVELAR_LETRA).autorizado)
        assertTrue(modulo.cobrarAyuda(U, TipoAyuda.REVELAR_PALABRA).autorizado)
        assertEquals(20, repo.saldo())
    }

    @Test
    fun `E5 sin hidromiel suficiente la ayuda no se entrega`() = runBlocking {
        modulo.obtenerEstadoRoadmap(U, "m1")
        repo.fijarSaldo(10)
        val r = modulo.cobrarAyuda(U, TipoAyuda.REVELAR_PALABRA)
        assertFalse(r.autorizado)
        assertEquals(5, r.faltante)
        assertEquals(10, repo.saldo())
    }

    // ---------------------------------------------------------------- E6 y RN-01

    @Test
    fun `E6 pregunta flash correcta suma 5 y sin respuesta resta 2`() = runBlocking {
        assertEquals(5, modulo.alResolverPreguntaFlash(PreguntaFlashResuelta(U, ResultadoFlash.CORRECTA)).cantidad)
        assertEquals(-2, modulo.alResolverPreguntaFlash(PreguntaFlashResuelta(U, ResultadoFlash.SIN_RESPUESTA)).cantidad)
        assertEquals(0, modulo.alResolverPreguntaFlash(PreguntaFlashResuelta(U, ResultadoFlash.INCORRECTA)).cantidad)
        assertEquals(43, repo.saldo())
    }

    @Test
    fun `E6 abandonar el contrarreloj resta 20`() = runBlocking {
        modulo.alAbandonarContrarreloj(ContrarrelojAbandonado(U))
        assertEquals(20, repo.saldo())
    }

    @Test
    fun `RN-01 el hidromiel nunca queda negativo`() = runBlocking {
        modulo.obtenerEstadoRoadmap(U, "m1")
        repo.fijarSaldo(12)
        val aviso = modulo.alAbandonarContrarreloj(ContrarrelojAbandonado(U))
        assertEquals(0, repo.saldo())
        assertEquals(-12, aviso.cantidad)
        modulo.alResolverPreguntaFlash(PreguntaFlashResuelta(U, ResultadoFlash.SIN_RESPUESTA))
        assertEquals(0, repo.saldo())
    }

    // ---------------------------------------------------------------- Avisos

    @Test
    fun `cada aviso tambien se publica en el flujo de avisos`() = runBlocking {
        val recibido = async { modulo.avisos.first() }
        yield()
        modulo.alResolverPreguntaFlash(PreguntaFlashResuelta(U, ResultadoFlash.CORRECTA))
        assertNotNull(recibido.await())
        assertEquals(45, recibido.await().saldoActual)
    }
}
