package com.aesir.odin.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Árbol de cuatro anillos del SRS v3.2: 4 lecciones, 6 minijuegos, 2 retos y 6 lecciones. */
class ArbolHabilidadesTest {

    private val lecciones = listOf("L1", "L2", "L3", "L4")
    private val minijuegos = listOf("Cruci1", "Memo1", "Flash1", "Cruci2", "Memo2", "Flash2")
    private val retos = listOf("R1", "R2")
    private val cuarto = (5..10).map { "L$it" }

    /** Tema de cada minijuego, como en DatosCrucigrama (ArbolHabilidades.TEMA_DE_MINIJUEGO). */
    private val temaDe = minijuegos.zip(ArbolHabilidades.TEMA_DE_MINIJUEGO.map { "L$it" }).toMap()
    private val conexiones = ArbolHabilidades.conexiones(lecciones, minijuegos, retos, cuarto) { temaDe[it] }

    @Test
    fun `son 18 niveles por mundo`() {
        assertEquals(ArbolHabilidades.NIVELES_POR_MUNDO, lecciones.size + minijuegos.size + retos.size + cuarto.size)
        assertEquals(ArbolHabilidades.NIVELES_POR_MUNDO, conexiones.keys.size)
    }

    @Test
    fun `las lecciones del anillo interior forman un anillo cerrado`() {
        assertEquals(setOf("L2", "L4"), conexiones.getValue("L1").filter { it in lecciones }.toSet())
        assertEquals(setOf("L3", "L1"), conexiones.getValue("L4").filter { it in lecciones }.toSet())
    }

    @Test
    fun `cada minijuego se conecta solo con la leccion de su tema`() {
        minijuegos.forEach { mj ->
            assertEquals(setOf(temaDe.getValue(mj)), conexiones.getValue(mj).filter { it in lecciones }.toSet())
        }
    }

    @Test
    fun `los temas de los minijuegos son los que quedan junto a su leccion en el escudo`() {
        minijuegos.indices.forEach { k ->
            val cercana = ArbolHabilidades.leccionesCercanas(k, lecciones.size, minijuegos.size).first() + 1
            assertEquals(ArbolHabilidades.TEMA_DE_MINIJUEGO[k], cercana)
        }
    }

    @Test
    fun `los dos minijuegos de una misma actividad son de temas distintos`() {
        val t = ArbolHabilidades.TEMA_DE_MINIJUEGO
        assertTrue(t[0] != t[3] && t[1] != t[4] && t[2] != t[5])
        assertEquals(setOf(1, 2, 3, 4), t.toSet())
    }

    @Test
    fun `sin tema conocido el minijuego usa la leccion mas cercana`() {
        val geometrico = ArbolHabilidades.conexiones(lecciones, minijuegos, retos, cuarto)
        minijuegos.forEach { assertEquals(conexiones.getValue(it), geometrico.getValue(it)) }
    }

    @Test
    fun `cada reto se conecta con los dos minijuegos mas cercanos`() {
        assertEquals(setOf("Flash2", "Cruci1"), conexiones.getValue("R1").filter { it in minijuegos }.toSet())
        assertEquals(setOf("Flash1", "Cruci2"), conexiones.getValue("R2").filter { it in minijuegos }.toSet())
        retos.forEach { r -> assertFalse(conexiones.getValue(r).any { it in lecciones || it in retos }) }
    }

    @Test
    fun `las lecciones del cuarto anillo se conectan con sus vecinas y con un reto o un minijuego`() {
        cuarto.forEachIndexed { i, l ->
            val vecinas = setOf(cuarto[(i + 1) % cuarto.size], cuarto[(i + cuarto.size - 1) % cuarto.size])
            assertEquals(vecinas, conexiones.getValue(l).filter { it in cuarto }.toSet())
            val interiores = conexiones.getValue(l).filter { it in retos || it in minijuegos }
            assertEquals(1, interiores.size)
            assertFalse(conexiones.getValue(l).any { it in lecciones })
        }
        // Las que quedan alineadas con un reto se conectan con él
        assertTrue("R1" in conexiones.getValue("L5"))
        assertTrue("R2" in conexiones.getValue("L8"))
    }

    @Test
    fun `todo el arbol se alcanza desde la primera leccion`() {
        val vistos = mutableSetOf("L1")
        val cola = ArrayDeque(listOf("L1"))
        while (cola.isNotEmpty()) conexiones[cola.removeFirst()].orEmpty().forEach { if (vistos.add(it)) cola += it }
        assertEquals(ArbolHabilidades.NIVELES_POR_MUNDO, vistos.size)
    }

    @Test
    fun `las conexiones van en ambos sentidos`() {
        conexiones.forEach { (a, vecinos) -> vecinos.forEach { assertTrue(a in conexiones.getValue(it)) } }
        assertFalse("L1" in conexiones.getValue("L1"))
    }

    @Test
    fun `actividad desconocida se toma como leccion`() {
        assertEquals(Actividad.LECCION, Actividad.desde("OTRA"))
        assertEquals(Actividad.ARRASTRAR, Actividad.desde("ARRASTRAR"))
        assertTrue(Actividad.entries.all { it.jugable })
    }

    @Test
    fun `una leccion es su propio tema y un minijuego pertenece al de su leccion`() {
        val leccion = Tema("L5", "m", "Tema 5", "", 5, false, false, false, anillo = ArbolHabilidades.ANILLO_LECCIONES_NUEVAS)
        val memo = Tema("Memo1", "m", "Memorama", "", 2, false, false, false, anillo = 2, actividad = Actividad.MEMORAMA, temaBaseId = "L2")
        assertTrue(leccion.esLeccion)
        assertFalse(leccion.esMinijuego)
        assertEquals("L5", leccion.idTema)
        assertTrue(memo.esMinijuego)
        assertEquals("L2", memo.idTema)
    }
}
