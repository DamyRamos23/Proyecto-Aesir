package com.aesir.odin.domain.usecase.shared

import com.aesir.odin.domain.model.EstadoRespuesta
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidarRespuestaUseCaseTest {

    private val validar = ValidarRespuestaUseCase()

    @Test
    fun `respuesta unica correcta`() {
        assertEquals(EstadoRespuesta.CORRECTA, validar(listOf(1), listOf(1)).estado)
    }

    @Test
    fun `respuesta unica incorrecta`() {
        assertEquals(EstadoRespuesta.INCORRECTA, validar(listOf(0), listOf(1)).estado)
    }

    @Test
    fun `multiple correcta sin importar el orden`() {
        assertEquals(EstadoRespuesta.CORRECTA, validar(listOf(3, 0, 2), listOf(0, 2, 3)).estado)
    }

    @Test
    fun `multiple incompleta cuando falta una correcta`() {
        assertEquals(EstadoRespuesta.INCOMPLETA, validar(listOf(0, 2), listOf(0, 2, 3)).estado)
    }

    @Test
    fun `multiple incorrecta si incluye una opcion equivocada`() {
        assertEquals(EstadoRespuesta.INCORRECTA, validar(listOf(0, 1), listOf(0, 2, 3)).estado)
    }

    @Test
    fun `sin seleccion es incorrecta`() {
        assertEquals(EstadoRespuesta.INCORRECTA, validar(emptyList(), listOf(0)).estado)
    }

    @Test
    fun `devuelve las opciones correctas`() {
        assertEquals(listOf(0, 2, 3), validar(listOf(1), listOf(0, 2, 3)).opcionesCorrectas)
    }
}
