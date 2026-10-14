package com.aesir.odin.domain.usecase.retofinmundo

import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelSuperado
import com.aesir.odin.domain.hidromiel.OrigenSuperado
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.DisponibilidadReto
import com.aesir.odin.domain.model.Ejercicio
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.IntentoRetoFinMundo
import com.aesir.odin.domain.model.Leccion
import com.aesir.odin.domain.model.PreguntaReto
import com.aesir.odin.domain.model.ReglasReto
import com.aesir.odin.domain.model.RetoFinMundo
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.RetoFinMundoRepository
import com.aesir.odin.domain.usecase.leccion.FakeHistorialRepository
import com.aesir.odin.domain.usecase.leccion.FakeLeccionRepository
import com.aesir.odin.domain.usecase.leccion.FakeRoadmapRepository
import com.aesir.odin.domain.usecase.leccion.mundo
import com.aesir.odin.domain.usecase.leccion.tema
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class FakeRetoFinMundoRepository : RetoFinMundoRepository {
    val intentos = mutableListOf<IntentoRetoFinMundo>()
    override suspend fun guardarIntento(intento: IntentoRetoFinMundo) { intentos += intento }
    override suspend fun obtenerIntentos(mundoId: String) = intentos.filter { it.mundoId == mundoId }
}

private fun ragnarok(mundoId: String = "m1") = Tema(
    id = "$mundoId-ragnarok", mundoId = mundoId, nombre = "Ragnarök", descripcion = "", orden = 99,
    desbloqueado = false, completado = false, introduccionVista = false, esRagnarok = true, anillo = 0
)

/** Cada tema tiene una lección con [porTema] ejercicios; los pares son de selección múltiple. */
private fun lecciones(temas: List<String>, porTema: Int = 5) = temas.map { t ->
    Leccion(
        id = "l-$t", temaId = t, titulo = "L", introduccion = "i", orden = 1, completada = false,
        ejercicios = (1..porTema).map { i ->
            Ejercicio(
                id = "e-$t-$i", leccionId = "l-$t", enunciado = "Pregunta $i de $t",
                opciones = listOf("A$i", "B$i", "C$i", "D$i"),
                respuestasCorrectas = if (i % 2 == 0) listOf(0, 2) else listOf(1),
                orden = i
            )
        }
    )
}

private val TEMAS = listOf("m1-n1", "m1-n2", "m1-n3", "m1-n4")

private fun roadmap() = FakeRoadmapRepository(
    TEMAS.mapIndexed { i, id -> tema(id, i + 1, true) } + ragnarok(),
    listOf(mundo("m1", 1, nombre = "UML"), mundo("m2", 2, nombre = "Casos de Uso"))
)

class ObtenerRetoFinMundoUseCaseTest {

    private val hidromiel = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4, "m2" to 4)), hidromiel)
    private fun usecase(seed: Int = 1) =
        ObtenerRetoFinMundoUseCase(roadmap(), FakeLeccionRepository(lecciones(TEMAS)), modulo, U, Random(seed))

    private fun superarTodos() {
        TEMAS.forEach {
            hidromiel.superados[U to it] = NivelSuperado(U, it, OrigenSuperado.JUGADO, 0, LocalDate.now())
        }
    }

    @Test
    fun `A2_1 con temas sin completar el reto esta bloqueado y lista los pendientes`() = runBlocking {
        hidromiel.superados[U to "m1-n1"] = NivelSuperado(U, "m1-n1", OrigenSuperado.JUGADO, 0, LocalDate.now())

        val r = usecase()("m1")

        assertTrue(r is DisponibilidadReto.Bloqueado)
        assertEquals(listOf("Tema 2", "Tema 3", "Tema 4"), (r as DisponibilidadReto.Bloqueado).temasPendientes)
    }

    @Test
    fun `con todos los temas superados arma un reto de 10 preguntas`() = runBlocking {
        superarTodos()
        val r = usecase()("m1")

        assertTrue(r is DisponibilidadReto.Disponible)
        val reto = (r as DisponibilidadReto.Disponible).reto
        assertEquals(ReglasReto.PREGUNTAS, reto.preguntas.size)
        assertEquals("m1-ragnarok", reto.ragnarokId)
        assertEquals("m1", reto.mundoId)
    }

    @Test
    fun `RF-56 el reto cubre todos los temas del mundo`() = runBlocking {
        superarTodos()
        repeat(20) { seed ->
            val reto = (usecase(seed)("m1") as DisponibilidadReto.Disponible).reto
            assertEquals("seed $seed", TEMAS.toSet(), reto.preguntas.map { it.temaId }.toSet())
            assertEquals(reto.preguntas.size, reto.preguntas.distinctBy { it.id }.size)
        }
    }

    @Test
    fun `las opciones se mezclan y las respuestas correctas siguen apuntando a su texto`() = runBlocking {
        superarTodos()
        val reto = (usecase(7)("m1") as DisponibilidadReto.Disponible).reto
        val originales = lecciones(TEMAS).flatMap { it.ejercicios }.associateBy { it.id }

        reto.preguntas.forEach { p ->
            val original = originales.getValue(p.id)
            val textosCorrectos = original.respuestasCorrectas.map { original.opciones[it] }.toSet()
            assertEquals(original.opciones.toSet(), p.opciones.toSet())
            assertEquals(textosCorrectos, p.respuestasCorrectas.map { p.opciones[it] }.toSet())
        }
        assertTrue(reto.preguntas.any { p -> p.opciones != originales.getValue(p.id).opciones })
    }

    @Test
    fun `RF-64 un reintento prefiere preguntas distintas a las del intento anterior`() = runBlocking {
        superarTodos()
        val primero = (usecase(3)("m1") as DisponibilidadReto.Disponible).reto
        val ids = primero.preguntas.map { it.id }.toSet()

        val segundo = (usecase(4)("m1", excluir = ids) as DisponibilidadReto.Disponible).reto

        // Hay 20 preguntas: caben 10 completamente nuevas
        assertTrue(segundo.preguntas.none { it.id in ids })
    }

    @Test
    fun `si un tema tiene pocas preguntas usa todas las disponibles`() = runBlocking {
        superarTodos()
        val u = ObtenerRetoFinMundoUseCase(roadmap(), FakeLeccionRepository(lecciones(TEMAS, porTema = 2)), modulo, U, Random(1))
        val reto = (u("m1") as DisponibilidadReto.Disponible).reto
        assertEquals(8, reto.preguntas.size)
    }

    @Test
    fun `sin preguntas registradas no hay reto`() = runBlocking {
        superarTodos()
        val u = ObtenerRetoFinMundoUseCase(roadmap(), FakeLeccionRepository(emptyList()), modulo, U)
        assertEquals(DisponibilidadReto.SinPreguntas, u("m1"))
    }
}

class FinalizarRetoFinMundoUseCaseTest {

    private val retoRepo = FakeRetoFinMundoRepository()
    private val roadmap = roadmap()
    private val hidromiel = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4, "m2" to 4)), hidromiel)
    private val finalizar = FinalizarRetoFinMundoUseCase(retoRepo, roadmap, modulo, U) { 11L }

    private val preguntas = (1..10).map { i ->
        PreguntaReto("p$i", TEMAS[i % 4], "Tema ${i % 4 + 1}", "Pregunta $i", listOf("a", "b", "c", "d"), listOf(0))
    }
    private val reto = RetoFinMundo("m1", "m1-ragnarok", preguntas)

    /** Las primeras [aciertos] preguntas bien; el resto con [estadoFallo]. */
    private fun respuestas(aciertos: Int, estadoFallo: EstadoRespuesta = EstadoRespuesta.INCORRECTA) =
        preguntas.mapIndexed { i, p -> p to if (i < aciertos) EstadoRespuesta.CORRECTA else estadoFallo }

    private fun dejarLosTemasSuperados() {
        TEMAS.forEach { hidromiel.superados[U to it] = NivelSuperado(U, it, OrigenSuperado.JUGADO, 0, LocalDate.now()) }
    }

    @Test
    fun `con 70 o mas supera el reto, marca el mundo y desbloquea el siguiente`() = runBlocking {
        dejarLosTemasSuperados()
        val r = finalizar(reto, respuestas(7))

        assertTrue(r.superado)
        assertEquals(70, r.puntaje)
        assertEquals(7, r.aciertos)
        assertEquals(3, r.errores)
        assertEquals("m2", r.mundoDesbloqueadoId)
        assertEquals("Casos de Uso", r.mundoDesbloqueadoNombre)
        assertFalse(r.ultimoMundo)
        assertTrue(roadmap.temas.getValue("m1-ragnarok").completado)
        assertNotNull(hidromiel.superados[U to "m1-ragnarok"])
        assertEquals("m2", hidromiel.mundosDesbloqueados.single().idMundo)
    }

    @Test
    fun `con 60 no se supera ni se desbloquea nada y se listan los temas con errores`() = runBlocking {
        dejarLosTemasSuperados()
        val r = finalizar(reto, respuestas(6))

        assertFalse(r.superado)
        assertEquals(60, r.puntaje)
        assertNull(r.mundoDesbloqueadoId)
        assertFalse(roadmap.temas.getValue("m1-ragnarok").completado)
        assertTrue(hidromiel.mundosDesbloqueados.isEmpty())
        assertNull(hidromiel.superados[U to "m1-ragnarok"])
        assertEquals(4, r.errores)
        assertEquals(4, r.temasConErrores.sumOf { it.errores })
        assertTrue(r.temasConErrores.zipWithNext().all { (a, b) -> a.errores >= b.errores })
    }

    @Test
    fun `una respuesta incompleta no cuenta como acierto`() = runBlocking {
        dejarLosTemasSuperados()
        val r = finalizar(reto, respuestas(6, EstadoRespuesta.INCOMPLETA))
        assertEquals(60, r.puntaje)
        assertFalse(r.superado)
    }

    @Test
    fun `RF-66 cada intento queda registrado`() = runBlocking {
        dejarLosTemasSuperados()
        finalizar(reto, respuestas(6))
        finalizar(reto, respuestas(9))

        val intentos = retoRepo.obtenerIntentos("m1")
        assertEquals(listOf(60, 90), intentos.map { it.puntaje })
        assertEquals(listOf(false, true), intentos.map { it.superado })
        assertTrue(intentos.none { it.abandonado })
        assertEquals(11L, intentos.first().fecha)
    }

    @Test
    fun `A10_2 al superar el reto del ultimo mundo se avisa que no hay otro`() = runBlocking {
        val r = finalizar(RetoFinMundo("m2", "m2-ragnarok", preguntas), respuestas(10))
        assertTrue(r.superado)
        assertTrue(r.ultimoMundo)
        assertNull(r.mundoDesbloqueadoId)
    }
}

class RetoFinMundoAbandonoYErroresTest {

    private val preguntas = listOf(PreguntaReto("p1", "m1-n2", "Modelos", "Pregunta", listOf("a", "b"), listOf(1)))

    @Test
    fun `A6_1 abandonar registra el intento como no superado sin puntaje`() = runBlocking {
        val retoRepo = FakeRetoFinMundoRepository()
        val hidromiel = FakeHidromielRepository()
        val modulo = ModuloHidromiel(FakeCatalogo(mapOf("m1" to 4)), hidromiel)
        val reto = RetoFinMundo("m1", "m1-ragnarok", preguntas)

        AbandonarRetoFinMundoUseCase(retoRepo, modulo, U) { 5L }(reto, aciertosHastaAhora = 0)

        val intento = retoRepo.intentos.single()
        assertEquals(0, intento.puntaje)
        assertTrue(intento.abandonado)
        assertFalse(intento.superado)
        assertNull(hidromiel.superados[U to "m1-ragnarok"])
        assertTrue(hidromiel.mundosDesbloqueados.isEmpty())
    }

    @Test
    fun `A7_1 registra el error con el tema de la pregunta`() = runBlocking {
        val historial = FakeHistorialRepository()
        val registrar = RegistrarErrorRetoUseCase(historial) { 8L }

        registrar(preguntas.single(), EstadoRespuesta.INCOMPLETA)
        registrar(preguntas.single(), EstadoRespuesta.INCORRECTA)

        assertEquals(2, historial.errores.size)
        val primero = historial.errores.first()
        assertEquals(RegistrarErrorRetoUseCase.TIPO_RETO_FIN_MUNDO, primero.tipoDesafio)
        assertEquals("p1", primero.actividadId)
        assertTrue(primero.descripcion.contains("Modelos"))
        assertTrue(primero.descripcion.contains("incompleta"))
        assertTrue(historial.errores.last().descripcion.contains("incorrecta"))
    }
}
