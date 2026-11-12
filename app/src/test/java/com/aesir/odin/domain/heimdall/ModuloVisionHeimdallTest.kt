package com.aesir.odin.domain.heimdall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ModuloVisionHeimdallTest {

    private class AlmacenEnMemoria(var guardada: PreferenciaLectura? = null, val falla: Boolean = false) : AlmacenPreferencias {
        override fun leer(): PreferenciaLectura? = if (falla) error("disco ilegible") else guardada
        override fun guardar(preferencia: PreferenciaLectura): Boolean {
            if (falla) error("disco lleno")
            guardada = preferencia
            return true
        }
    }

    private val fecha = LocalDateTime.of(2026, 11, 12, 10, 0)

    @Test
    fun `al instalar el modo esta desactivado`() {
        val modulo = ModuloVisionHeimdall(AlmacenEnMemoria())
        assertFalse(modulo.alAbrirAplicacion().activa)
    }

    @Test
    fun `activar guarda la preferencia y se conserva al reabrir (RN-18)`() {
        val almacen = AlmacenEnMemoria()
        ModuloVisionHeimdall(almacen, ahora = { fecha }).cambiarModo(ModoLectura.VISION_HEIMDALL)

        assertEquals(PreferenciaLectura(ModoLectura.VISION_HEIMDALL, fecha), almacen.guardada)
        assertTrue(ModuloVisionHeimdall(almacen).alAbrirAplicacion().activa)
    }

    @Test
    fun `desactivar regresa a la presentacion normal (paso 5)`() {
        val modulo = ModuloVisionHeimdall(AlmacenEnMemoria())
        modulo.cambiarModo(ModoLectura.VISION_HEIMDALL)
        modulo.cambiarModo(ModoLectura.NORMAL)
        assertEquals(EstiloNormal().crearPresentacion(), modulo.presentacion.value)
    }

    @Test
    fun `preferencia ilegible inicia desactivado (E3)`() {
        assertFalse(ModuloVisionHeimdall(AlmacenEnMemoria(falla = true)).alAbrirAplicacion().activa)
    }

    @Test
    fun `si no se puede guardar el modo se aplica igual en la sesion`() {
        val modulo = ModuloVisionHeimdall(AlmacenEnMemoria(falla = true))
        assertTrue(modulo.cambiarModo(ModoLectura.VISION_HEIMDALL).activa)
    }

    @Test
    fun `Vision de Heimdall usa los valores de la tabla del MOD-03`() {
        val p = SelectorPresentacion().seleccionar(ModoLectura.VISION_HEIMDALL)
        assertEquals("Lexend", p.fuente)
        assertEquals(19, (16 * p.escalaTexto).toInt()) // 16 sp + 20 % = 19 sp, redondeado hacia abajo
        assertEquals(0.12f, p.espacioLetras)
        assertEquals(0.16f, p.espacioPalabras)
        assertEquals(1.5f, p.interlineado)
        assertEquals(2f, p.espacioParrafos)
        assertEquals(Alineacion.IZQUIERDA, p.alineacion)
        assertEquals(TipoEnfasis.NEGRITAS, p.enfasis)
        // Sin fondo ni color de texto propios: se conservan los de cada pantalla.
        assertEquals(null, p.colorFondo)
        assertEquals(null, p.colorTexto)
    }

    @Test
    fun `el contraste del modo supera 4_5 a 1 (RN-19)`() {
        val contraste = ValidadorContraste().calcularContraste("#FAF6E9", "#2B2B2B")
        assertEquals(13.1, contraste, 0.05)
    }

    @Test
    fun `un estilo sin contraste suficiente cae a la presentacion normal`() {
        val gris = EstiloLectura { EstiloHeimdall().crearPresentacion().copy(colorFondo = "#FFFFFF", colorTexto = "#EEEEEE") }
        val selector = SelectorPresentacion(mapOf(ModoLectura.NORMAL to EstiloNormal(), ModoLectura.VISION_HEIMDALL to gris))
        assertFalse(selector.seleccionar(ModoLectura.VISION_HEIMDALL).activa)
    }
}
