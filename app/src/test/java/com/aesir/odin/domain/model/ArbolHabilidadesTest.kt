package com.aesir.odin.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArbolHabilidadesTest {

    private val lecciones = listOf("L1", "L2", "L3", "L4")
    private val minijuegos = listOf("Cruci1", "Memo1", "Flash1", "Cruci2", "Memo2", "Flash2")
    private val conexiones = ArbolHabilidades.conexiones(lecciones, minijuegos)

    @Test
    fun `las lecciones forman un anillo cerrado`() {
        assertEquals(setOf("L2", "L4"), conexiones.getValue("L1").filter { it.startsWith("L") }.toSet())
        assertEquals(setOf("L3", "L1"), conexiones.getValue("L4").filter { it.startsWith("L") }.toSet())
    }

    @Test
    fun `cada minijuego se conecta con las dos lecciones mas cercanas`() {
        assertEquals(setOf("L1", "L2"), conexiones.getValue("Cruci1"))
        assertEquals(setOf("L2", "L3"), conexiones.getValue("Memo1"))
        assertEquals(setOf("L2", "L3"), conexiones.getValue("Flash1"))
        assertEquals(setOf("L3", "L4"), conexiones.getValue("Cruci2"))
        assertEquals(setOf("L4", "L1"), conexiones.getValue("Memo2"))
        assertEquals(setOf("L4", "L1"), conexiones.getValue("Flash2"))
    }

    @Test
    fun `los dos crucigramas cubren las cuatro lecciones`() {
        assertEquals(lecciones.toSet(), conexiones.getValue("Cruci1") + conexiones.getValue("Cruci2"))
    }

    @Test
    fun `los minijuegos no se conectan entre si`() {
        minijuegos.forEach { mj -> assertTrue(conexiones.getValue(mj).all { it.startsWith("L") }) }
    }

    @Test
    fun `las conexiones van en ambos sentidos`() {
        conexiones.forEach { (a, vecinos) -> vecinos.forEach { assertTrue(a in conexiones.getValue(it)) } }
        assertFalse("L1" in conexiones.getValue("L1"))
    }

    @Test
    fun `actividad desconocida se toma como leccion`() {
        assertEquals(Actividad.LECCION, Actividad.desde("OTRA"))
        assertFalse(Actividad.MEMORAMA.jugable)
        assertTrue(Actividad.CRUCIGRAMA.jugable)
    }
}
