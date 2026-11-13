package com.aesir.odin.domain.usecase.volva

import com.aesir.odin.domain.hidromiel.ConsultaVolvaTerminada
import com.aesir.odin.domain.hidromiel.FakeCatalogo
import com.aesir.odin.domain.hidromiel.FakeHidromielRepository
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.PagoValhalla
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.TipoMovimiento
import com.aesir.odin.domain.hidromiel.U
import com.aesir.odin.domain.model.ConsultaVolva
import com.aesir.odin.domain.model.Fe
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.Lectura
import com.aesir.odin.domain.model.Norna
import com.aesir.odin.domain.model.RunaFuthark
import com.aesir.odin.domain.model.SalonRunas
import com.aesir.odin.domain.model.Vision
import com.aesir.odin.domain.repository.HistorialRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/** CU-12 Retar a la bruja: reglas del SRS v3.2 (RF-163, RF-169, RF-172 a RF-177). */
class VolvaTest {

    private val politica = PoliticaCosto()

    @Before
    fun sinModoAdmin() = ModoAdmin.cargar(false)

    // ---------------------------------------------------------------- Lectura y pago

    @Test
    fun `la lectura depende de los puntos de destino`() {
        assertEquals(Lectura.PROFECIA_CUMPLIDA, Lectura.dePuntos(14))
        assertEquals(Lectura.PROFECIA_CUMPLIDA, Lectura.dePuntos(24))
        assertEquals(Lectura.LECTURA_CLARA, Lectura.dePuntos(13))
        assertEquals(Lectura.LECTURA_CLARA, Lectura.dePuntos(8))
        assertEquals(Lectura.HUMO_DEL_CALDERO, Lectura.dePuntos(7))
        assertEquals(Lectura.HUMO_DEL_CALDERO, Lectura.dePuntos(1))
        assertEquals(Lectura.MALDICION, Lectura.dePuntos(0))
        assertEquals(Lectura.MALDICION, Lectura.dePuntos(-24))
    }

    @Test
    fun `profecia cumplida paga la apuesta por 2_5 menos el diezmo (ejemplo del SRS)`() {
        assertEquals(PagoValhalla(100, 10, 90), politica.pagoVolva(40, Lectura.PROFECIA_CUMPLIDA))
    }

    @Test
    fun `el premio y el diezmo se redondean hacia abajo`() {
        assertEquals(PagoValhalla(37, 3, 34), politica.pagoVolva(15, Lectura.PROFECIA_CUMPLIDA))
        assertEquals(PagoValhalla(22, 2, 20), politica.pagoVolva(15, Lectura.LECTURA_CLARA))
    }

    @Test
    fun `humo del caldero devuelve la apuesta sin diezmo y maldicion no paga`() {
        assertEquals(PagoValhalla(33, 0, 33), politica.pagoVolva(33, Lectura.HUMO_DEL_CALDERO))
        assertEquals(PagoValhalla(0, 0, 0), politica.pagoVolva(70, Lectura.MALDICION))
    }

    @Test
    fun `la fe suma o resta lo mismo segun acierte o falle`() {
        assertEquals(1 to -1, Fe.CAUTA.puntosAcierto to Fe.CAUTA.puntosFallo)
        assertEquals(2 to -2, Fe.FIRME.puntosAcierto to Fe.FIRME.puntosFallo)
        assertEquals(4 to -4, Fe.TOTAL.puntosAcierto to Fe.TOTAL.puntosFallo)
    }

    // ---------------------------------------------------------------- Runas

    @Test
    fun `la siguiente runa sigue el orden del Futhark`() {
        val salon = SalonRunas(RunaFuthark.TODAS.take(3))
        assertEquals("Ansuz", salon.siguienteRuna()?.nombre)
        assertEquals("Fehu", SalonRunas(emptyList()).siguienteRuna()?.nombre)
    }

    @Test
    fun `con las 24 runas ya no se entrega otra`() {
        val salon = SalonRunas(RunaFuthark.TODAS)
        assertTrue(salon.completo())
        assertNull(salon.siguienteRuna())
    }

    // ---------------------------------------------------------------- Visiones

    @Test
    fun `barajar las opciones conserva la respuesta correcta`() {
        val vision = vision("v1", correcta = 0)
        repeat(20) { semilla ->
            val barajada = vision.barajada(Random(semilla))
            assertEquals(vision.opcionCorrecta(), barajada.opcionCorrecta())
            assertEquals(vision.opciones.toSet(), barajada.opciones.toSet())
        }
    }

    @Test
    fun `una vision errada resta segun la fe y se avisa a la bitacora`() = runBlocking {
        val historial = HistorialFalso()
        val consulta = ConsultaVolva("c1", 40, listOf(vision("v1", correcta = 1)), fecha = 0)
        val respuesta = ResolverVisionUseCase(historial) { 0 }(consulta, Fe.TOTAL, 0)
        assertFalse(respuesta.cumplida)
        assertEquals(-4, respuesta.puntos)
        assertEquals("v1", historial.errores.single().actividadId)
        assertEquals(ResolverVisionUseCase.TIPO_RETAR_BRUJA, historial.errores.single().tipoDesafio)
    }

    @Test
    fun `si se acaba el tiempo la vision cuenta como errada`() = runBlocking {
        val historial = HistorialFalso()
        val consulta = ConsultaVolva("c1", 40, listOf(vision("v1", correcta = 1)), fecha = 0)
        val respuesta = ResolverVisionUseCase(historial) { 0 }(consulta, Fe.FIRME, null)
        assertFalse(respuesta.cumplida)
        assertEquals(-2, respuesta.puntos)
        assertEquals(1, historial.errores.size)
    }

    @Test
    fun `una vision cumplida suma segun la fe y no va a la bitacora`() = runBlocking {
        val historial = HistorialFalso()
        val consulta = ConsultaVolva("c1", 40, listOf(vision("v1", correcta = 2)), fecha = 0)
        val respuesta = ResolverVisionUseCase(historial) { 0 }(consulta, Fe.CAUTA, 2)
        assertTrue(respuesta.cumplida)
        assertEquals(1, respuesta.puntos)
        assertTrue(historial.errores.isEmpty())
    }

    // ---------------------------------------------------------------- MOD-01

    private val repo = FakeHidromielRepository()
    private val modulo = ModuloHidromiel(FakeCatalogo(linkedMapOf("m1" to 6)), repo)

    @Test
    fun `sin niveles disponibles el piso es 0 y el maximo es el saldo hasta 70`() = runBlocking {
        val rango = modulo.rangoApuestaVolva(U, listOf("m1"))
        assertEquals(0, rango.piso)
        assertEquals(15, rango.minimo)
        assertEquals(40, rango.maximo) // cantidad inicial 2C = 40
    }

    @Test
    fun `el piso es el costo mas bajo de los niveles disponibles (RN-08)`() = runBlocking {
        modulo.cobrarEntrada(U, "m1-n1")
        modulo.alFinalizarNivel(NivelFinalizado(U, "m1-n1", 100, ResultadoNivel.SUPERADO))
        repo.fijarSaldo(100)
        val rango = modulo.rangoApuestaVolva(U, listOf("m1"))
        assertEquals(PoliticaCosto.C, rango.piso)
        assertEquals(70, rango.maximo)
        repo.fijarSaldo(50)
        assertEquals(30, modulo.rangoApuestaVolva(U, listOf("m1")).maximo)
    }

    @Test
    fun `la apuesta se descuenta como APUESTA_VOLVA`() = runBlocking {
        val acceso = modulo.cobrarApuestaVolva(U, 30, listOf("m1"))
        assertTrue(acceso.autorizado)
        assertEquals(10, repo.saldo())
        assertEquals(TipoMovimiento.APUESTA_VOLVA, repo.historial.last().tipo)
    }

    @Test
    fun `una apuesta fuera del rango no se cobra`() = runBlocking {
        assertFalse(modulo.cobrarApuestaVolva(U, 10, listOf("m1")).autorizado)
        assertFalse(modulo.cobrarApuestaVolva(U, 45, listOf("m1")).autorizado)
        assertEquals(40, repo.saldo())
    }

    @Test
    fun `la profecia cumplida abona el premio neto como PREMIO_VOLVA`() = runBlocking {
        modulo.cobrarApuestaVolva(U, 40, listOf("m1"))
        val aviso = modulo.alTerminarConsultaVolva(ConsultaVolvaTerminada(U, 40, Lectura.PROFECIA_CUMPLIDA))
        assertEquals(90, aviso.cantidad)
        assertEquals(90, repo.saldo())
        assertEquals(TipoMovimiento.PREMIO_VOLVA, repo.historial.last().tipo)
    }

    @Test
    fun `la maldicion no abona nada`() = runBlocking {
        modulo.cobrarApuestaVolva(U, 20, listOf("m1"))
        val aviso = modulo.alTerminarConsultaVolva(ConsultaVolvaTerminada(U, 20, Lectura.MALDICION))
        assertEquals(0, aviso.cantidad)
        assertEquals(20, repo.saldo())
    }

    // ---------------------------------------------------------------- Apoyo

    private fun vision(id: String, correcta: Int) = Vision(
        id, "tema_uml_1", Norna.URD, "Caso", listOf("A", "B", "C"), correcta, "Pista", "Explicación"
    )

    private class HistorialFalso : HistorialRepository {
        val errores = mutableListOf<HistorialError>()
        override suspend fun obtenerErrores() = errores.toList()
        override suspend fun obtenerErroresParaRepaso() = errores.toList()
        override suspend fun registrarError(error: HistorialError) { errores += error }
    }

}
