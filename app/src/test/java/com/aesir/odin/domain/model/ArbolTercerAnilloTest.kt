package com.aesir.odin.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tercer anillo del árbol: 8 retos alrededor de los minijuegos. */
class ArbolTercerAnilloTest {

    private val lecciones = listOf("L1", "L2", "L3", "L4")
    private val minijuegos = listOf("Cruci1", "Memo1", "Flash1", "Cruci2", "Memo2", "Flash2")
    private val retos = (1..8).map { "R$it" }
    private val conexiones = ArbolHabilidades.conexiones(lecciones, minijuegos, retos)

    @Test
    fun `cada reto se conecta con los dos minijuegos mas cercanos`() {
        retos.forEach { reto ->
            val vecinos = conexiones.getValue(reto)
            assertEquals(2, vecinos.size)
            assertTrue(vecinos.all { it in minijuegos })
        }
    }

    @Test
    fun `los retos de arrastrar quedan junto a minijuegos jugables`() {
        // Posiciones 1 y 5 del anillo (arriba y abajo), como en DatosArrastrar
        assertEquals(setOf("Flash2", "Cruci1"), conexiones.getValue("R1"))
        assertEquals(setOf("Flash1", "Cruci2"), conexiones.getValue("R5"))
    }

    @Test
    fun `el tercer anillo no cambia las conexiones de los otros dos`() {
        val sinRetos = ArbolHabilidades.conexiones(lecciones, minijuegos)
        lecciones.forEach { assertEquals(sinRetos.getValue(it), conexiones.getValue(it)) }
        minijuegos.forEach { mj -> assertEquals(sinRetos.getValue(mj), conexiones.getValue(mj).filter { it in lecciones }.toSet()) }
    }

    @Test
    fun `los retos no se conectan entre si ni con las lecciones`() {
        retos.forEach { reto -> assertFalse(conexiones.getValue(reto).any { it in retos || it in lecciones }) }
    }

    @Test
    fun `lecciones cercanas a un reto`() {
        assertEquals(listOf(0, 1), ArbolHabilidades.leccionesCercanasAReto(0, 8, 4))
        assertEquals(2, ArbolHabilidades.leccionesCercanasAReto(4, 8, 4).first())
    }

    @Test
    fun `actividades nuevas`() {
        assertTrue(Actividad.ARRASTRAR.jugable)
        assertFalse(Actividad.POR_DEFINIR.jugable)
        assertEquals(Actividad.ARRASTRAR, Actividad.desde("ARRASTRAR"))
    }
}
