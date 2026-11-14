package com.aesir.odin.domain.usecase.forja

import com.aesir.odin.domain.bitacora.ModuloBitacora
import com.aesir.odin.domain.hidromiel.CatalogoNiveles
import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelSuperado
import com.aesir.odin.domain.hidromiel.OrigenSuperado
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.TipoMovimiento
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.Artefacto
import com.aesir.odin.domain.model.ArtefactoVitrina
import com.aesir.odin.domain.model.AsignacionForja
import com.aesir.odin.domain.model.CalidadArtefacto
import com.aesir.odin.domain.model.ElementoArrastrable
import com.aesir.odin.domain.model.EstadoFinalForja
import com.aesir.odin.domain.model.EtapaCalentar
import com.aesir.odin.domain.model.EtapaMartillar
import com.aesir.odin.domain.model.EtapaTemplar
import com.aesir.odin.domain.model.Forja
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.HuecoTemple
import com.aesir.odin.domain.model.ModalidadReto
import com.aesir.odin.domain.model.MotivoBloqueoForja
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.PiezaDiseno
import com.aesir.odin.domain.model.PosicionDestino
import com.aesir.odin.domain.model.RangoApuesta
import com.aesir.odin.domain.model.RespuestaEtapa
import com.aesir.odin.domain.model.ResultadoForja
import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.model.RetoArrastrar
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.model.TipoEtapa
import com.aesir.odin.domain.racha.ModuloCarreraSol
import com.aesir.odin.domain.repository.ForjaRepository
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.RetoRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Pruebas del CU-02 Forjar artefactos en la apuesta de Brokkr (RF-30 a RF-46). */
class ForjaTest {

    // ------------------------------------------------------------- Fakes

    private class FakeForjaRepository(val artefactos: List<Artefacto>) : ForjaRepository {
        val vitrina = mutableMapOf<String, ArtefactoVitrina>()
        val forjas = mutableMapOf<String, Pair<Long, String>>() // id → (fecha, estado)
        val resultados = mutableListOf<ResultadoForja>()

        override suspend fun obtenerArtefactos(mundoIds: List<String>) = artefactos.filter { it.mundoId in mundoIds }
        override suspend fun obtenerVitrina() = vitrina.values.toList()
        override suspend fun contarForjasDelDia(inicio: Long, fin: Long) = forjas.values.count { it.first in inicio until fin }
        override suspend fun registrarForja(forja: Forja, fecha: Long) {
            forjas[forja.id] = fecha to "EN_CURSO"
        }
        override suspend fun obtenerProblemaMartillar(mundoId: String) = martillar(mundoId)
        override suspend fun obtenerTemplado(mundoId: String) = templar(mundoId)
        override suspend fun guardarEnVitrina(artefactoId: String, calidad: CalidadArtefacto, fecha: Long) {
            vitrina[artefactoId] = ArtefactoVitrina(artefactos.first { it.id == artefactoId }, calidad, fecha)
        }
        override suspend fun guardarResultado(resultado: ResultadoForja) {
            resultados += resultado
            forjas[resultado.id] = (forjas[resultado.id]?.first ?: resultado.fecha) to resultado.resultado.name
        }
        override suspend fun marcarForjasEnCursoComoRotas(fecha: Long): Int {
            val enCurso = forjas.filterValues { it.second == "EN_CURSO" }
            enCurso.forEach { (id, v) -> forjas[id] = v.first to "ROTO" }
            return enCurso.size
        }
    }

    private class FakeRoadmap(var superados: List<String>) : RoadmapRepository {
        override suspend fun obtenerMundosSuperados() = superados
        override suspend fun obtenerMundos() = listOf("m1", "m2").mapIndexed { i, id -> Mundo(id, id, "", i + 1, true) }
        override suspend fun obtenerTemas(): List<Tema> = emptyList()
        override suspend fun obtenerTema(temaId: String): Tema = error("no usado")
        override suspend fun obtenerMundo(mundoId: String): Mundo = error("no usado")
        override suspend fun obtenerTemasPorMundo(mundoId: String): List<Tema> = emptyList()
        override suspend fun marcarLeccionCompletada(temaId: String) = Unit
        override suspend fun marcarIntroduccionVista(temaId: String) = Unit
        override suspend fun desbloquearTema(temaId: String) = Unit
        override suspend fun mundoDesbloqueado(mundoId: String) = true
        override suspend fun marcarMemoramaCompletado(nivelId: String) = Unit
        override suspend fun marcarTemasConocidos(mundoId: String, temaIds: List<String>) = Unit
    }

    private class FakeRetos : RetoRepository {
        override suspend fun obtenerRetoPorTema(temaId: String): RetoArrastrar = error("no usado")
        override suspend fun guardarResultado(resultado: ResultadoReto) = Unit
        override suspend fun obtenerRetoOrdenar(mundoId: String) = reto(mundoId)
    }

    private class FakeHistorial : HistorialRepository {
        val errores = mutableListOf<HistorialError>()
        override suspend fun obtenerErrores() = errores
        override suspend fun obtenerErroresParaRepaso() = errores
        override suspend fun registrarError(error: HistorialError) {
            errores += error
        }
    }

    // ------------------------------------------------------------- Escenario

    private val ahora = 1_800_000_000_000L
    private val mjolnir = Artefacto("art_mjolnir", "m1", "UML", "Mjölnir", "poder completo", "poder reducido")
    private val gungnir = Artefacto("art_gungnir", "m2", "Casos de uso", "Gungnir", "poder completo", "poder reducido")

    private val hidromielRepo = FakeHidromielRepository()
    private val catalogo = object : CatalogoNiveles by FakeCatalogo(linkedMapOf("m1" to 6, "m2" to 6)) {
        override suspend fun idsMundos() = listOf("m1", "m2")
    }
    private val modulo = ModuloHidromiel(catalogo, hidromielRepo)
    private val forjaRepo = FakeForjaRepository(listOf(mjolnir, gungnir))
    private val roadmap = FakeRoadmap(listOf("m1"))
    private val historial = FakeHistorial()
    private val bitacora = ModuloBitacora(historial)

    private val validarAcceso = ValidarAccesoForjaUseCase(forjaRepo, roadmap, modulo) { ahora }
    private val asignar = AsignarArtefactoUseCase(forjaRepo, roadmap, Random(1))
    private val rango = CalcularRangoApuestaUseCase(modulo)
    private val armar = ArmarForjaUseCase(forjaRepo, FakeRetos(), Random(1))
    private val apostar = ApostarForjaUseCase(modulo, forjaRepo) { ahora }
    private val validarEtapa = ValidarEtapaForjaUseCase(bitacora) { ahora }
    private val enfriar = EnfriarMetalUseCase(bitacora) { ahora }
    private val finalizar = FinalizarForjaUseCase(forjaRepo, modulo, ModuloCarreraSol.Pendiente) { ahora }
    private val abandonar = AbandonarForjaUseCase(forjaRepo) { ahora }

    @After
    fun apagarAdmin() = ModoAdmin.cargar(false)

    private suspend fun forjaApostada(apuesta: Int = 20): Forja {
        val asignacion = asignar(forjaRepo.obtenerVitrina())
        val forja = armar(asignacion, apuesta)
        assertTrue(apostar(forja, rango(modulo.saldo())).cobrada)
        return forja
    }

    private suspend fun resolverTodoBien(forja: Forja) {
        val calentar = forja.etapaEnCurso() as EtapaCalentar
        validarEtapa(forja, RespuestaEtapa(TipoEtapa.CALENTAR, ordenPasos = listOf("e1", "e2", "e3")))
        validarEtapa(forja, RespuestaEtapa(TipoEtapa.MARTILLAR, piezaId = "1"))
        validarEtapa(forja, RespuestaEtapa(TipoEtapa.TEMPLAR, huecos = mapOf("h1" to "factory method")))
        assertEquals(TipoEtapa.CALENTAR, calentar.tipo)
    }

    // ------------------------------------------------------------- Reglas puras

    @Test
    fun `premio de la forja es la apuesta por 3 menos el diezmo redondeado hacia abajo`() {
        val politica = PoliticaCosto()
        assertEquals(54, politica.premioForja(20)) // 60 − 6
        assertEquals(68, politica.premioForja(25)) // 75 − 7
        assertEquals(216, politica.premioForja(80)) // 240 − 24
    }

    @Test
    fun `el rango de apuesta va de 20 al menor entre 80 y saldo menos piso`() {
        val r = RangoApuesta(pisoApuesta = 20, maximoPermitido = minOf(80, 70 - 20))
        assertTrue(r.contiene(20))
        assertTrue(r.contiene(50))
        assertFalse(r.contiene(19))
        assertFalse(r.contiene(51))
    }

    @Test
    fun `calentar valida el orden y nunca presenta los pasos ya ordenados`() {
        val etapa = EtapaCalentar("m1", reto("m1"), Random(3))
        assertTrue(etapa.pasosIniciales.map { it.id } != listOf("e1", "e2", "e3"))
        assertTrue(etapa.validar(RespuestaEtapa(TipoEtapa.CALENTAR, ordenPasos = listOf("e1", "e2", "e3"))))
        assertFalse(etapa.validar(RespuestaEtapa(TipoEtapa.CALENTAR, ordenPasos = listOf("e2", "e1", "e3"))))
    }

    @Test
    fun `templar acepta sin importar mayusculas ni acentos y rechaza el metal frio`() {
        val etapa = templar("m1")
        assertTrue(etapa.validar(RespuestaEtapa(TipoEtapa.TEMPLAR, huecos = mapOf("h1" to "  FÁCTORY   method "))))
        assertFalse(etapa.validar(RespuestaEtapa(TipoEtapa.TEMPLAR, metalFrio = true)))
    }

    // ------------------------------------------------------------- Acceso (A2.1, A2.2)

    @Test
    fun `sin ragnarok superado no se puede forjar`() = runBlocking {
        roadmap.superados = emptyList()
        assertEquals(MotivoBloqueoForja.SIN_RAGNAROK_SUPERADO, validarAcceso().motivo)
    }

    @Test
    fun `A2_1 con dos forjas en el dia la forja queda apagada`() = runBlocking {
        hidromielRepo.fijarSaldo(500, USUARIO_LOCAL)
        repeat(2) { forjaApostada() }
        val acceso = validarAcceso()
        assertFalse(acceso.autorizado)
        assertEquals(MotivoBloqueoForja.FORJAS_AGOTADAS, acceso.motivo)
    }

    @Test
    fun `A2_2 el hidromiel menos el piso debe alcanzar la apuesta minima`() = runBlocking {
        // Superó el primer nivel de m1: hay niveles DISPONIBLE de 20, así que el piso es 20
        hidromielRepo.superados[USUARIO_LOCAL to "m1-n1"] =
            NivelSuperado(USUARIO_LOCAL, "m1-n1", OrigenSuperado.JUGADO, 0, java.time.LocalDate.now())
        hidromielRepo.fijarSaldo(35, USUARIO_LOCAL)
        assertEquals(20, modulo.calcularPisoApuesta())
        val acceso = validarAcceso()
        assertEquals(MotivoBloqueoForja.HIDROMIEL_INSUFICIENTE, acceso.motivo)
        assertEquals(5, acceso.hidromielFaltante)
        assertEquals(15, rango(35).maximoPermitido)
    }

    // ------------------------------------------------------------- Asignación (RF-30, RF-31)

    @Test
    fun `asigna artefactos de mundos superados que no tiene o tiene con defecto`() = runBlocking {
        roadmap.superados = listOf("m1", "m2")
        forjaRepo.guardarEnVitrina("art_mjolnir", CalidadArtefacto.PERFECTO, ahora)
        forjaRepo.guardarEnVitrina("art_gungnir", CalidadArtefacto.CON_DEFECTO, ahora)
        val asignacion = asignar(forjaRepo.obtenerVitrina())
        assertEquals("art_gungnir", asignacion.artefacto?.id)
        assertFalse(asignacion.soloHidromiel)
    }

    @Test
    fun `A3_1 con todos los artefactos perfectos la forja es solo por hidromiel`() = runBlocking {
        forjaRepo.guardarEnVitrina("art_mjolnir", CalidadArtefacto.PERFECTO, ahora)
        val asignacion = asignar(forjaRepo.obtenerVitrina())
        assertNull(asignacion.artefacto)
        assertTrue(asignacion.soloHidromiel)
        assertEquals("m1", asignacion.mundoId)
    }

    // ------------------------------------------------------------- Flujo completo

    @Test
    fun `forja sin errores sale perfecta, paga el premio y guarda el artefacto`() = runBlocking {
        hidromielRepo.fijarSaldo(100, USUARIO_LOCAL)
        val forja = forjaApostada(20)
        assertEquals(80, modulo.saldo())
        assertEquals(TipoMovimiento.APUESTA_FORJA, hidromielRepo.historial.last().tipo)

        resolverTodoBien(forja)
        assertTrue(forja.terminada())
        val resultado = finalizar(forja)

        assertEquals(EstadoFinalForja.PERFECTO, resultado.resultado)
        assertEquals(54, resultado.hidromielGanado)
        assertEquals(134, modulo.saldo())
        assertEquals(TipoMovimiento.PREMIO_FORJA, hidromielRepo.historial.last().tipo)
        assertEquals(CalidadArtefacto.PERFECTO, forjaRepo.vitrina["art_mjolnir"]?.calidad)
        assertTrue(historial.errores.isEmpty())
    }

    @Test
    fun `A9_1 un error rompe el artefacto, se pierde la apuesta y va a la bitacora`() = runBlocking {
        hidromielRepo.fijarSaldo(100, USUARIO_LOCAL)
        val forja = forjaApostada(30)
        val r1 = validarEtapa(forja, RespuestaEtapa(TipoEtapa.CALENTAR, ordenPasos = listOf("e3", "e2", "e1")))
        assertFalse(r1.correcta)
        assertEquals(1, r1.erroresAcumulados)
        validarEtapa(forja, RespuestaEtapa(TipoEtapa.MARTILLAR, piezaId = "1"))
        validarEtapa(forja, RespuestaEtapa(TipoEtapa.TEMPLAR, huecos = mapOf("h1" to "Factory Method")))
        val resultado = finalizar(forja)

        assertEquals(EstadoFinalForja.ROTO, resultado.resultado)
        assertEquals(30, resultado.hidromielPerdido)
        assertEquals(70, modulo.saldo())
        assertTrue(forjaRepo.vitrina.isEmpty())
        assertEquals("FORJA", historial.errores.single().tipoDesafio)
        // RF-39: la bitácora no guarda la respuesta correcta
        assertFalse(historial.errores.single().descripcion.contains("e1, e2, e3"))
    }

    @Test
    fun `A14_1 si el metal se enfria la etapa cuenta como incorrecta`() = runBlocking {
        hidromielRepo.fijarSaldo(100, USUARIO_LOCAL)
        val forja = forjaApostada()
        validarEtapa(forja, RespuestaEtapa(TipoEtapa.CALENTAR, ordenPasos = listOf("e1", "e2", "e3")))
        validarEtapa(forja, RespuestaEtapa(TipoEtapa.MARTILLAR, piezaId = "1"))
        val r = enfriar(forja)
        assertTrue(r.metalFrio)
        assertTrue(r.forjaTerminada)
        assertEquals(EstadoFinalForja.ROTO, finalizar(forja).resultado)
    }

    @Test
    fun `A7_1 abandonar rompe la forja y cuenta en el limite del dia`() = runBlocking {
        hidromielRepo.fijarSaldo(100, USUARIO_LOCAL)
        val forja = forjaApostada(40)
        val resultado = abandonar(forja)
        assertTrue(resultado.abandonada)
        assertEquals(EstadoFinalForja.ROTO, resultado.resultado)
        assertEquals(60, modulo.saldo())
        assertEquals(1, validarAcceso().forjasRestantes)
    }

    @Test
    fun `RF-45 las forjas en curso al abrir la app quedan rotas`() = runBlocking {
        hidromielRepo.fijarSaldo(100, USUARIO_LOCAL)
        forjaApostada()
        assertEquals(1, CerrarForjasInconclusasUseCase(forjaRepo) { ahora }())
        assertEquals("ROTO", forjaRepo.forjas.values.single().second)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `A5_1 no se acepta una apuesta fuera del rango`() {
        runBlocking {
            hidromielRepo.fijarSaldo(100, USUARIO_LOCAL)
            val forja = armar(AsignacionForja(mjolnir, "m1", false), 90)
            apostar(forja, rango(modulo.saldo()))
        }
    }

    companion object {
        fun reto(mundoId: String) = RetoArrastrar(
            id = "reto_$mundoId", temaId = "rt_$mundoId", modalidad = ModalidadReto.ORDENAR_PROCESO,
            instrucciones = "Ordena las fases", imagenDiagrama = null,
            elementos = listOf(
                ElementoArrastrable("e3", "r", "Probar", "p3"),
                ElementoArrastrable("e1", "r", "Analizar", "p1"),
                ElementoArrastrable("e2", "r", "Diseñar", "p2")
            ),
            posiciones = (1..3).map { PosicionDestino("p$it", "r", it) },
            completado = false
        )

        fun martillar(mundoId: String) = EtapaMartillar(
            mundoId, "¿Qué patrón encapsula la creación de objetos?",
            listOf(PiezaDiseno("0", "Observer"), PiezaDiseno("1", "Factory Method"), PiezaDiseno("2", "Singleton")),
            piezaCorrectaId = "1"
        )

        fun templar(mundoId: String) = EtapaTemplar(
            mundoId, "Completa la definición", "Una clase que define una interfaz para crear un objeto: {h1}.",
            listOf(HuecoTemple("h1", "Factory Method")), listOf("Factory Method", "Singleton")
        )
    }
}
