package com.aesir.odin.domain.usecase.duelo

import com.aesir.odin.data.local.DatosEnigmas
import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.TipoMovimiento
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.ConceptoEnigma
import com.aesir.odin.domain.model.DueloEnigmas
import com.aesir.odin.domain.model.EstadoFinalDuelo
import com.aesir.odin.domain.model.MotivoSinAcceso
import com.aesir.odin.domain.model.PreguntaEnigma
import com.aesir.odin.domain.model.ResultadoDuelo
import com.aesir.odin.domain.model.TarjetaConcepto
import com.aesir.odin.domain.model.TipoPregunta
import com.aesir.odin.domain.repository.DueloRepository
import com.aesir.odin.domain.usecase.leccion.FakeHistorialRepository
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.tema
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

class DueloEnigmasTest {

    private class FakeDueloRepository(
        private val conceptos: List<ConceptoEnigma>,
        private val preguntas: List<PreguntaEnigma>
    ) : DueloRepository {
        val guardados = linkedMapOf<String, ResultadoDuelo>()
        override suspend fun obtenerConceptos(temaIds: List<String>) = conceptos.filter { it.temaId in temaIds }
        override suspend fun obtenerPreguntas() = preguntas
        override suspend fun contarDuelosDelDia(inicioDia: Long) = guardados.values.count { it.fecha >= inicioDia }
        override suspend fun registrarDueloIniciado(duelo: DueloEnigmas, fecha: Long) {
            guardados[duelo.id] = ResultadoDuelo(duelo.id, duelo.conceptoVerdadero().id, null, 0, 0, duelo.cuerno.sorbos, 0, fecha)
        }
        override suspend fun obtenerDueloSinTerminar() = guardados.values.lastOrNull { it.resultado == null }
        override suspend fun guardarResultado(resultado: ResultadoDuelo) { guardados[resultado.id] = resultado }
    }

    private val ahora = LocalDate.of(2026, 11, 12).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val conceptos = (1..14).map { ConceptoEnigma("c$it", if (it <= 7) "t1" else "t2", "Concepto $it", "Explicación $it") }
    private val preguntas = listOf(
        PreguntaEnigma("p_t1", "¿Es del tema 1?", TipoPregunta.DIRECTA, (1..7).map { "c$it" }),
        PreguntaEnigma("p_par", "«Soy par»", TipoPregunta.ENIGMA, (2..14 step 2).map { "c$it" }),
        PreguntaEnigma("p_todos", "Inútil: todos dicen sí", TipoPregunta.DIRECTA, (1..14).map { "c$it" })
    )
    private val roadmap = FakeRoadmapRepository(listOf(tema("t1", 1, true).copy(completado = true), tema("t2", 2, true).copy(conocido = true)))
    private val repo = FakeDueloRepository(conceptos, preguntas)
    private val hidromiel = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(linkedMapOf("m1" to 6)), hidromiel, hoy = { LocalDate.of(2026, 11, 12) })
    private val historial = FakeHistorialRepository()

    private fun tablero(verdadero: String = "c1") = DueloEnigmas(
        "d1", conceptos.take(12).map { TarjetaConcepto(it) }, preguntas.take(2), verdadero
    )

    // ------------------------------------------------------------ Acceso (pasos 1-2, E4.1)

    @Test
    fun `sin Ragnarok superado no hay duelo`() = runBlocking {
        val acceso = ValidarAccesoDueloUseCase(roadmap, repo, modulo, reloj = { ahora })()
        assertFalse(acceso.permitido)
        assertEquals(MotivoSinAcceso.SIN_RAGNAROK_SUPERADO, acceso.motivo)
    }

    @Test
    fun `con Ragnarok y saldo suficiente se permite y quedan 3 duelos`() = runBlocking {
        roadmap.ragnarokSuperado = true
        val acceso = ValidarAccesoDueloUseCase(roadmap, repo, modulo, reloj = { ahora })()
        assertTrue(acceso.permitido)
        assertEquals(3, acceso.duelosRestantes)
        assertEquals(PoliticaCosto.APUESTA_DUELO, acceso.apuesta)
    }

    @Test
    fun `el cuarto duelo del dia se rechaza (RF-182)`() = runBlocking {
        roadmap.ragnarokSuperado = true
        repeat(3) { repo.guardarResultado(ResultadoDuelo("x$it", "c1", EstadoFinalDuelo.PERDIDO, 0, 0, 0, 0, ahora)) }
        val acceso = ValidarAccesoDueloUseCase(roadmap, repo, modulo, reloj = { ahora })()
        assertEquals(MotivoSinAcceso.LIMITE_DIARIO_ALCANZADO, acceso.motivo)
        assertEquals(0, acceso.duelosRestantes)
    }

    @Test
    fun `los duelos de ayer no cuentan (RN-10)`() = runBlocking {
        roadmap.ragnarokSuperado = true
        repeat(3) { repo.guardarResultado(ResultadoDuelo("x$it", "c1", EstadoFinalDuelo.PERDIDO, 0, 0, 0, 0, ahora - 86_400_000L)) }
        assertTrue(ValidarAccesoDueloUseCase(roadmap, repo, modulo, reloj = { ahora })().permitido)
    }

    // ------------------------------------------------------------ Armar y apostar (pasos 4-5)

    @Test
    fun `el tablero lleva 12 conceptos distintos de temas superados y solo preguntas utiles`() = runBlocking {
        val duelo = ArmarDueloUseCase(roadmap, repo, Random(7))().getOrThrow()
        assertEquals(12, duelo.tarjetas.map { it.concepto.id }.distinct().size)
        assertTrue(duelo.preguntas.none { it.id == "p_todos" })
    }

    @Test
    fun `sin 12 conceptos no se arma el duelo`() = runBlocking {
        val pocos = FakeDueloRepository(conceptos.take(11), preguntas)
        assertTrue(ArmarDueloUseCase(roadmap, pocos)().isFailure)
    }

    @Test
    fun `la apuesta descuenta 24 y cuenta el duelo en el dia`() = runBlocking {
        val duelo = tablero()
        val r = ApostarDueloUseCase(modulo, repo, reloj = { ahora })(duelo)
        assertTrue(r.autorizado)
        assertEquals(40 - 24, hidromiel.saldo(USUARIO_LOCAL))
        assertEquals(TipoMovimiento.APUESTA_DUELO, hidromiel.historial.last().tipo)
        assertEquals(1, repo.contarDuelosDelDia(inicioDelDia(ahora)))
        assertEquals(6, duelo.cuerno.sorbos)
    }

    // ------------------------------------------------------------ Jugar (pasos 7-12)

    @Test
    fun `preguntar cuesta 1 sorbo, responde segun el concepto y no se repite`() {
        val duelo = tablero("c2")
        val r1 = PreguntarGiganteUseCase()(duelo, "p_t1")
        assertTrue(r1.esSi)
        assertEquals(5, r1.sorbosRestantes)
        val r2 = PreguntarGiganteUseCase()(duelo, "p_par")
        assertTrue(r2.esSi)
        assertTrue(duelo.preguntasDisponibles().isEmpty())
    }

    @Test
    fun `voltear alterna y no revela nada`() {
        val duelo = tablero()
        duelo.voltear("c1")
        assertTrue(duelo.tarjetas.first { it.concepto.id == "c1" }.descartada)
        duelo.voltear("c1")
        assertFalse(duelo.tarjetas.first { it.concepto.id == "c1" }.descartada)
    }

    @Test
    fun `una adivinanza fallida resta 2 sorbos y voltea la tarjeta (A12_1)`() {
        val duelo = tablero("c1")
        val r = RevelarConceptoUseCase()(duelo, "c5")
        assertFalse(r.correcta)
        assertEquals(4, r.sorbosRestantes)
        assertFalse(r.dueloTerminado)
        assertTrue(duelo.tarjetas.first { it.concepto.id == "c5" }.descartada)
        assertEquals(1, duelo.adivinanzasFallidas)
    }

    @Test
    fun `con el cuerno vacio por preguntar queda una ultima adivinanza (A10_1)`() {
        val muchas = (1..6).map { i -> PreguntaEnigma("q$i", "?", TipoPregunta.DIRECTA, listOf("c$i")) }
        val duelo = DueloEnigmas("d", conceptos.take(12).map { TarjetaConcepto(it) }, muchas, "c1")
        muchas.forEach { PreguntarGiganteUseCase()(duelo, it.id) }
        assertTrue(duelo.ultimaAdivinanza)
        assertFalse(duelo.puedePreguntar())
        assertTrue(RevelarConceptoUseCase()(duelo, "c9").dueloTerminado)
    }

    // ------------------------------------------------------------ Terminar (paso 13, E12.1, E7)

    @Test
    fun `ganar paga 24 mas 4 por sorbo sin diezmo`() = runBlocking {
        val duelo = tablero("c1")
        ApostarDueloUseCase(modulo, repo, reloj = { ahora })(duelo)
        PreguntarGiganteUseCase()(duelo, "p_t1")
        val cierre = FinalizarDueloUseCase(repo, modulo, historial, reloj = { ahora })(duelo, ganado = true)
        assertEquals(24 + 4 * 5, cierre.resultado.hidromielObtenido)
        assertEquals(16 + 44, hidromiel.saldo(USUARIO_LOCAL))
        assertEquals(EstadoFinalDuelo.GANADO, repo.guardados.getValue("d1").resultado)
        assertTrue(historial.obtenerErrores().isEmpty())
    }

    @Test
    fun `perder revela el concepto y lo manda a la bitacora (E12_1)`() = runBlocking {
        val duelo = tablero("c3")
        val cierre = FinalizarDueloUseCase(repo, modulo, historial, reloj = { ahora })(duelo, ganado = false, tarjetaElegida = "c4")
        assertEquals("c3", cierre.conceptoVerdadero.id)
        assertNull(cierre.aviso)
        val error = historial.obtenerErrores().single()
        assertEquals(FinalizarDueloUseCase.TIPO_BITACORA, error.tipoDesafio)
        assertTrue(error.descripcion.contains("Concepto 3") && error.descripcion.contains("Concepto 4"))
    }

    @Test
    fun `abandonar registra ABANDONADO y al reabrir se cierra el duelo en curso (E7)`() = runBlocking {
        val duelo = tablero()
        ApostarDueloUseCase(modulo, repo, reloj = { ahora })(duelo)
        val cerrado = AbandonarDueloUseCase(repo, reloj = { ahora }).cerrarDueloSinTerminar()
        assertEquals(EstadoFinalDuelo.ABANDONADO, cerrado?.resultado)
        assertNull(repo.obtenerDueloSinTerminar())
        assertEquals("La apuesta no se devuelve", 16, hidromiel.saldo(USUARIO_LOCAL))
    }

    // ------------------------------------------------------------ Contenido semilla

    @Test
    fun `cada mundo tiene al menos 12 conceptos distintos y las preguntas usan conceptos reales`() {
        val porMundo = DatosEnigmas.conceptos.groupBy { it.temaId.substringBeforeLast("_") }
        porMundo.values.forEach { lista -> assertTrue(lista.map { it.nombre.lowercase() }.distinct().size >= 12) }
        val ids = DatosEnigmas.conceptos.map { it.id }.toSet()
        DatosEnigmas.preguntas.forEach { p ->
            assertTrue("${p.id} sin conceptos", p.conceptosSi.isNotEmpty())
            assertTrue("${p.id} con ids inexistentes", ids.containsAll(p.conceptosSi))
        }
        // Los homónimos responden igual: «Actor» de UML y de Casos de Uso
        val actores = DatosEnigmas.conceptos.filter { it.nombre == "Actor" }.map { it.id }
        DatosEnigmas.preguntas.forEach { p -> assertEquals(1, actores.map { it in p.conceptosSi }.distinct().size) }
    }
}
