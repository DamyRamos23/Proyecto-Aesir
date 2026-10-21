package com.aesir.odin.domain.usecase.alba

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.PagoValhalla
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.TipoMovimiento
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.Artefacto
import com.aesir.odin.domain.model.ArtefactoForjado
import com.aesir.odin.domain.model.Desenlace
import com.aesir.odin.domain.model.EfectoArtefacto
import com.aesir.odin.domain.model.Enemigo
import com.aesir.odin.domain.model.FaseNoche
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.NocheAlba
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.ResultadoNoche
import com.aesir.odin.domain.model.textoMultiplicador
import com.aesir.odin.domain.repository.AlbaRepository
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.PreguntaFlashAttempt
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/** CU-01 Sobrevivir oleadas (Hasta el Alba): reglas del SRS v3.2 (RF-03 a RF-24). */
class HastaElAlbaTest {

    private val politica = PoliticaCosto()
    private val alba = AlbaFalso()
    private val historial = HistorialFalso()
    private val responder = ResponderPreguntaAlbaUseCase(ValidarRespuestaUseCase(), historial, alba) { 0 }
    private val pregunta = PreguntaFlash("p1", "tema_1", "¿Cuáles?", listOf("A", "B", "C", "D"), listOf(0, 2))

    @Before
    fun sinModoAdmin() = ModoAdmin.cargar(false)

    // ---------------------------------------------------------------- Oleadas y pago

    @Test
    fun `las 7 oleadas tienen los golpes, segundos y multiplicadores del RF-07`() {
        val tabla = Enemigo.entries.map { Triple(it.golpes, it.segundosPorPregunta, it.multiplicador) }
        assertEquals(
            listOf(Triple(2, 20, 120), Triple(2, 18, 140), Triple(3, 16, 170), Triple(3, 14, 200),
                Triple(3, 12, 250), Triple(4, 10, 300), Triple(4, 8, 400)),
            tabla
        )
        assertTrue(Enemigo.EL_ALBA.ultimo)
    }

    @Test
    fun `el premio es apuesta por multiplicador menos el diezmo, redondeado hacia abajo`() {
        assertEquals(PagoValhalla(36, 3, 33), politica.pagoAlba(30, 120))
        assertEquals(PagoValhalla(240, 24, 216), politica.pagoAlba(60, 400))
        assertEquals(PagoValhalla(17, 1, 16), politica.pagoAlba(10, 170))
        assertEquals(PagoValhalla(0, 0, 0), politica.pagoAlba(40, 0))
    }

    @Test
    fun `el texto del multiplicador muestra los decimales justos`() {
        assertEquals("×1.2", textoMultiplicador(120))
        assertEquals("×4", textoMultiplicador(400))
        assertEquals("×4.25", textoMultiplicador(425))
    }

    // ---------------------------------------------------------------- Responder

    @Test
    fun `un acierto golpea al enemigo y al vencerlo sube el multiplicador`() = runBlocking {
        var noche = NocheAlba("n1", 30, 0)
        val r1 = responder(noche, pregunta, listOf(0, 2))
        assertTrue(r1.golpeAcertado)
        assertEquals(1, r1.noche.golpesRestantes)
        assertFalse(r1.oleadaSuperada)
        noche = r1.noche
        val r2 = responder(noche, pregunta.copy(id = "p2"), listOf(2, 0))
        assertTrue(r2.oleadaSuperada)
        assertEquals(120, r2.noche.multiplicadorAlcanzado)
        assertEquals(FaseNoche.EN_DECISION, r2.noche.fase)
        assertEquals(33, premioNetoAlCobrar(r2.noche))
    }

    @Test
    fun `una respuesta incompleta rompe un escudo y va a la bitacora`() = runBlocking {
        val r = responder(NocheAlba("n1", 30, 0), pregunta, listOf(0))
        assertFalse(r.golpeAcertado)
        assertEquals(2, r.noche.escudos)
        assertEquals(Enemigo.DRAUGR.golpes, r.noche.golpesRestantes)
        assertEquals(ResponderPreguntaAlbaUseCase.TIPO_HASTA_EL_ALBA, historial.errores.single().tipoDesafio)
        assertEquals(listOf("p1"), r.noche.preguntasFalladas)
    }

    @Test
    fun `si se acaba el tiempo cuenta como error`() = runBlocking {
        val r = responder(NocheAlba("n1", 30, 0), pregunta, null)
        assertEquals(null, r.estado)
        assertEquals(2, r.noche.escudos)
        assertEquals(1, historial.errores.size)
    }

    @Test
    fun `al romperse el tercer escudo cae en batalla`() = runBlocking {
        val r = responder(NocheAlba("n1", 30, 0, escudos = 1), pregunta, listOf(1))
        assertTrue(r.cayo)
        val resultado = CaerEnBatallaAlbaUseCase(alba)(r.noche)
        assertEquals(Desenlace.CAIDA, resultado.desenlace)
        assertEquals(0, resultado.premioNeto)
    }

    // ---------------------------------------------------------------- Preguntas

    @Test
    fun `no repite preguntas de la noche y al agotarse empieza por las falladas`() = runBlocking {
        val banco = listOf(pregunta, pregunta.copy(id = "p2"), pregunta.copy(id = "p3"))
        val obtener = ObtenerPreguntaAlbaUseCase(RoadmapFalso(), PreguntasFalsas(banco), Random(1))
        val noche = NocheAlba("n1", 30, 0, preguntasMostradas = listOf("p1", "p2"))
        assertEquals("p3", obtener(noche)?.id)
        val agotada = noche.copy(preguntasMostradas = listOf("p1", "p2", "p3"), preguntasFalladas = listOf("p2"))
        assertEquals("p2", obtener(agotada)?.id)
    }

    // ---------------------------------------------------------------- Artefactos

    private val usar = UsarArtefactoAlbaUseCase(alba, Random(3))

    @Test
    fun `Mjolnir derrota al enemigo de un golpe`() = runBlocking {
        val noche = NocheAlba("n1", 30, 0, enemigo = Enemigo.JOTUN_DE_HIELO, golpesRestantes = 3,
            artefactos = listOf(ArtefactoForjado(Artefacto.MJOLNIR)))
        val (n1, e1) = usar(noche, Artefacto.MJOLNIR, pregunta)
        assertTrue((e1 as EfectoArtefacto.GolpeDeMjolnir).derrotado)
        assertEquals(170, n1.multiplicadorAlcanzado)
        assertTrue(Artefacto.MJOLNIR in n1.artefactosUsados)
    }

    @Test
    fun `un artefacto solo se usa una vez por noche y solo si se llevo`() = runBlocking {
        val noche = NocheAlba("n1", 30, 0, artefactos = listOf(ArtefactoForjado(Artefacto.MJOLNIR)))
        val (usada, _) = usar(noche, Artefacto.MJOLNIR, pregunta)
        val (_, efecto) = usar(usada, Artefacto.MJOLNIR, pregunta)
        assertTrue(efecto is EfectoArtefacto.NoDisponible)
        val (_, sinLlevar) = usar(noche, Artefacto.GUNGNIR, pregunta)
        assertTrue(sinLlevar is EfectoArtefacto.NoDisponible)
    }

    @Test
    fun `Gungnir senala la respuesta correcta`() = runBlocking {
        val noche = NocheAlba("n1", 30, 0, artefactos = listOf(ArtefactoForjado(Artefacto.GUNGNIR)))
        val (_, e) = usar(noche, Artefacto.GUNGNIR, pregunta)
        assertEquals(pregunta.respuestasCorrectas, (e as EfectoArtefacto.Gungnir).correctas)
        assertTrue(e.eliminadas.isEmpty())
    }

    @Test
    fun `Skidbladnir no se activa con los 3 escudos`() = runBlocking {
        val noche = NocheAlba("n1", 30, 0, artefactos = listOf(ArtefactoForjado(Artefacto.SKIDBLADNIR)))
        val (sinUso, efecto) = usar(noche, Artefacto.SKIDBLADNIR, pregunta)
        assertTrue(efecto is EfectoArtefacto.NoDisponible)
        assertFalse(Artefacto.SKIDBLADNIR in sinUso.artefactosUsados)
        val (recuperada, _) = usar(noche.copy(escudos = 1), Artefacto.SKIDBLADNIR, pregunta)
        assertEquals(2, recuperada.escudos)
    }

    @Test
    fun `Draupnir suma su bono al cobrar y solo en la decision`() = runBlocking {
        val noche = NocheAlba("n1", 40, 0, multiplicadorAlcanzado = 200, fase = FaseNoche.EN_DECISION,
            artefactos = listOf(ArtefactoForjado(Artefacto.DRAUPNIR)))
        val (conBono, _) = usar(noche, Artefacto.DRAUPNIR, null)
        assertEquals(250, conBono.multiplicadorDeCobro)
        val (_, enOleada) = usar(noche.copy(fase = FaseNoche.EN_OLEADA), Artefacto.DRAUPNIR, null)
        assertTrue(enOleada is EfectoArtefacto.NoDisponible)
    }

    // ---------------------------------------------------------------- MOD-01 y cobro

    private val repo = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(linkedMapOf("m1" to 6)), repo)

    @Test
    fun `la apuesta va de 10 al menor entre 60 y el saldo menos el piso`() = runBlocking {
        val rango = modulo.rangoApuestaAlba(U, listOf("m1"))
        assertEquals(10, rango.minimo)
        assertEquals(40, rango.maximo) // cantidad inicial 40, sin niveles disponibles
        repo.fijarSaldo(200)
        assertEquals(60, modulo.rangoApuestaAlba(U, listOf("m1")).maximo)
    }

    @Test
    fun `la apuesta se descuenta como APUESTA_ALBA y fuera de rango no se cobra`() = runBlocking {
        assertFalse(modulo.cobrarApuestaAlba(U, 5, listOf("m1")).autorizado)
        assertTrue(modulo.cobrarApuestaAlba(U, 30, listOf("m1")).autorizado)
        assertEquals(10, repo.saldo())
        assertEquals(TipoMovimiento.APUESTA_ALBA, repo.historial.last().tipo)
    }

    @Test
    fun `volver al banquete cobra el multiplicador alcanzado como PREMIO_ALBA`() = runBlocking {
        val cobrar = CobrarNocheAlbaUseCase(alba, modulo) { 0 }
        repo.fijarSaldo(0)
        val noche = NocheAlba("n1", 30, 0, enemigo = Enemigo.LOBO_DE_FENRIR, multiplicadorAlcanzado = 140, fase = FaseNoche.EN_DECISION)
        val r = cobrar(noche, Desenlace.RETIRADA)
        assertEquals(42, r.premioBruto)
        assertEquals(4, r.diezmo)
        assertEquals(38, r.premioNeto)
        assertEquals(38, repo.saldo())
        assertEquals(TipoMovimiento.PREMIO_ALBA, repo.historial.last().tipo)
        assertFalse(r.einherjarNuevo)
    }

    @Test
    fun `al llegar al alba cobra por 4 y otorga el titulo solo la primera vez`() = runBlocking {
        val cobrar = CobrarNocheAlbaUseCase(alba, modulo) { 0 }
        val noche = NocheAlba("n1", 50, 0, enemigo = Enemigo.EL_ALBA, multiplicadorAlcanzado = 400, fase = FaseNoche.EN_DECISION)
        val r1 = cobrar(noche, Desenlace.ALBA)
        assertEquals(200, r1.premioBruto)
        assertEquals(180, r1.premioNeto)
        assertEquals(NocheAlba.OLEADAS, r1.oleadasSuperadas)
        assertTrue(r1.einherjarNuevo)
        assertFalse(cobrar(noche.copy(id = "n2"), Desenlace.ALBA).einherjarNuevo)
    }

    // ---------------------------------------------------------------- Apoyo

    private class HistorialFalso : HistorialRepository {
        val errores = mutableListOf<HistorialError>()
        override suspend fun obtenerErrores() = errores.toList()
        override suspend fun obtenerErroresParaRepaso() = errores.toList()
        override suspend fun registrarError(error: HistorialError) { errores += error }
    }

    /** Un solo tema de lección superado: tema_1. */
    private class RoadmapFalso : RoadmapRepository {
        private val tema = Tema("tema_1", "m1", "Tema 1", "", 1, desbloqueado = true, completado = true, introduccionVista = true)
        override suspend fun obtenerTemas() = listOf(tema)
        override suspend fun obtenerTema(temaId: String) = tema
        override suspend fun obtenerMundos() = emptyList<Mundo>()
        override suspend fun obtenerMundo(mundoId: String): Mundo = error("sin mundos")
        override suspend fun obtenerTemasPorMundo(mundoId: String) = listOf(tema)
        override suspend fun marcarLeccionCompletada(temaId: String) = Unit
        override suspend fun marcarIntroduccionVista(temaId: String) = Unit
        override suspend fun desbloquearTema(temaId: String) = Unit
        override suspend fun mundoDesbloqueado(mundoId: String) = true
        override suspend fun marcarMemoramaCompletado(nivelId: String) = Unit
        override suspend fun marcarTemasConocidos(mundoId: String, temaIds: List<String>) = Unit
        override suspend fun haSuperadoAlgunRagnarok() = true
    }

    private class PreguntasFalsas(private val banco: List<PreguntaFlash>) : PreguntaFlashRepository {
        override suspend fun obtenerPreguntaAleatoriaDeTema(temaId: String) = banco.firstOrNull { it.temaId == temaId }
        override suspend fun obtenerPreguntasParaReto(temaIds: List<String>, cantidad: Int) = banco.filter { it.temaId in temaIds }.take(cantidad)
        override suspend fun guardarIntento(intento: PreguntaFlashAttempt) = Unit
    }

    private class AlbaFalso : AlbaRepository {
        val enCurso = mutableMapOf<String, NocheAlba>()
        val resultados = mutableMapOf<String, ResultadoNoche>()
        var einherjar = false
        override suspend fun jugoNocheEnElDia(inicio: Long, fin: Long) = (enCurso.values.map { it.fecha } + resultados.values.map { it.fecha }).any { it in inicio until fin }
        override suspend fun resultadoDelDia(inicio: Long, fin: Long) = resultados.values.firstOrNull { it.fecha in inicio until fin }
        override suspend fun guardarNocheEnCurso(noche: NocheAlba) { enCurso[noche.id] = noche }
        override suspend fun obtenerNocheSinTerminar() = enCurso.values.firstOrNull { it.id !in resultados }
        override suspend fun guardarResultado(resultado: ResultadoNoche) { resultados[resultado.id] = resultado }
        override suspend fun obtenerPremiosPendientes() = resultados.values.filter { it.premioPendiente }
        override suspend fun obtenerArtefactos() = emptyList<ArtefactoForjado>()
        override suspend fun tieneEinherjar() = einherjar
        override suspend fun otorgarEinherjar(fecha: Long) { einherjar = true }
    }
}
