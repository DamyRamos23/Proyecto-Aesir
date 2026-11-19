package com.aesir.odin.domain.eastereggs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JuegosOcultosTest {

    private fun c(valor: Int, palo: Palo = Palo.PICAS) = Carta(valor, palo)

    /** Baraja fija: primero las [cartas] dadas y después cartas de relleno (2). */
    private fun baraja(vararg cartas: Carta) = Baraja(cartas.toList() + List(30) { c(2, Palo.TREBOLES) })

    // ------------------------------------------------------------ Combinaciones

    @Test
    fun `las combinaciones no chocan entre si ni con la del modo admin`() {
        val admin = listOf(0, 1, 3, 2, 0, 1, 3, 2)
        val todas = JuegoOculto.entries.map { it.combinacion } + listOf(admin)
        for (a in todas) for (b in todas) {
            if (a === b) continue
            // Ninguna termina con otra completa (se dispararían las dos)
            assertFalse("$a contiene a $b", a.size >= b.size && a.takeLast(b.size) == b)
        }
        // Nunca el mismo mundo dos veces seguidas (eso entra al mundo)
        JuegoOculto.entries.forEach { j -> assertTrue(j.combinacion.zipWithNext().none { (x, y) -> x == y }) }
    }

    @Test
    fun `el texto de la combinacion muestra el ciclo y las veces`() {
        assertEquals("UML → Modelos Tradicionales (×3)", JuegoOculto.RULETA.textoCombinacion())
        assertEquals(12, JuegoOculto.MAYOR_MENOR.combinacion.size)
    }

    // ------------------------------------------------------------ Pagos simples

    @Test
    fun `ruleta paga el doble menos el diezmo`() {
        assertEquals(36, Ruleta.premio(20, ColorRuleta.VERDE, ColorRuleta.VERDE))
        assertEquals(9, Ruleta.premio(5, ColorRuleta.BEIGE, ColorRuleta.BEIGE))
        assertEquals(0, Ruleta.premio(20, ColorRuleta.VERDE, ColorRuleta.BEIGE))
    }

    @Test
    fun `tragamonedas retorna cerca del 91 por ciento`() {
        val r = Tragamonedas.rodillo
        assertEquals(20, r.size)
        var total = 0L
        for (a in r) for (b in r) for (x in r) total += Tragamonedas.premio(listOf(a, b, x))
        val retorno = total.toDouble() / (r.size * r.size * r.size * Tragamonedas.COSTO)
        assertEquals(0.91, retorno, 0.02)
        assertEquals(1500, Tragamonedas.premio(List(3) { Simbolo.OJO }))
        assertEquals(5, Tragamonedas.premio(listOf(Simbolo.CUERNO, Simbolo.RUNA, Simbolo.CUERNO)))
        assertEquals(0, Tragamonedas.premio(listOf(Simbolo.OJO, Simbolo.OJO, Simbolo.RUNA)))
    }

    @Test
    fun `dados pagan segun la suma`() {
        assertEquals(20, Dados.premio(ApuestaDados.MENOR, 6))
        assertEquals(0, Dados.premio(ApuestaDados.MENOR, 7))
        assertEquals(50, Dados.premio(ApuestaDados.SIETE, 7))
        assertEquals(20, Dados.premio(ApuestaDados.MAYOR, 12))
        assertEquals(0, Dados.premio(ApuestaDados.MAYOR, 2))
    }

    // ------------------------------------------------------------ Blackjack

    @Test
    fun `el As vale 1 u 11 segun convenga`() {
        assertEquals(21, valorMano(listOf(c(14), c(13))))
        assertEquals(12, valorMano(listOf(c(14), c(14))))
        assertEquals(21, valorMano(listOf(c(14), c(14), c(9))))
        assertEquals(22, valorMano(listOf(c(13), c(12), c(2))))
    }

    @Test
    fun `natural paga 3 a 2`() {
        // Reparto: jugador A, casa 9, jugador K, casa 7
        val juego = Blackjack(10, baraja(c(14), c(9), c(13), c(7)))
        assertTrue(juego.terminado)
        assertEquals(25, juego.premio())
    }

    @Test
    fun `la casa pide hasta 17 y el jugador gana si la casa se pasa`() {
        // Jugador 10+8=18, casa 10+6=16 → pide 10 y se pasa
        val juego = Blackjack(10, baraja(c(10), c(10), c(8), c(6), c(10)))
        juego.plantarse()
        assertEquals(26, valorMano(juego.crupier))
        assertEquals(20, juego.premio())
    }

    @Test
    fun `la casa se planta en 17 suave y empate devuelve la apuesta`() {
        // Jugador 10+7=17, casa A+6 = 17 suave → se planta
        val juego = Blackjack(10, baraja(c(10), c(14), c(7), c(6)))
        juego.plantarse()
        assertEquals(2, juego.crupier.size)
        assertEquals(10, juego.premio())
    }

    @Test
    fun `pasarse pierde aunque la casa tambien se pase`() {
        val juego = Blackjack(10, baraja(c(10), c(10), c(6), c(6), c(10), c(10)))
        juego.pedir()
        assertTrue(juego.terminado)
        assertEquals(0, juego.premio())
        assertEquals("La casa no pide si el jugador ya se pasó", 2, juego.crupier.size)
    }

    @Test
    fun `doblar duplica la apuesta y da una sola carta`() {
        // Jugador 6+5=11, casa 10+7; dobla y recibe 10 → 21 contra 17
        val juego = Blackjack(10, baraja(c(6), c(10), c(5), c(7), c(10)))
        assertTrue(juego.puedeDoblar)
        juego.doblar()
        assertTrue(juego.terminado)
        assertEquals(40, juego.premio())
    }

    @Test
    fun `dividir un par juega dos manos y 21 tras dividir no es natural`() {
        // Jugador 8,8; casa 10,9. Manos: 8+3 (luego planta 11) y 8+10=18
        val juego = Blackjack(10, baraja(c(8), c(10), c(8, Palo.CORAZONES), c(9), c(3), c(10)))
        assertTrue(juego.puedeDividir)
        juego.dividir()
        assertEquals(2, juego.manos.size)
        juego.plantarse() // 11
        juego.plantarse() // 18
        assertTrue(juego.terminado)
        assertEquals("Pierde las dos contra 19", 0, juego.premio())
    }

    // ------------------------------------------------------------ Mayor o menor

    @Test
    fun `mayor o menor multiplica el bote al acertar y lo pierde al fallar`() {
        // Primera 7, luego 9 y 3; después una serie completa de corazones
        val juego = MayorMenor(10, Baraja(listOf(c(7), c(9), c(3)) + (2..14).map { c(it, Palo.CORAZONES) }))
        val mult = juego.multiplicadorPorCiento(Prediccion.MAYOR)
        assertTrue(juego.adivinar(Prediccion.MAYOR))
        assertEquals(minOf(10 * mult / 100, juego.tope), juego.bote)
        assertTrue(juego.puedeCobrar)
        assertFalse(juego.adivinar(Prediccion.MAYOR)) // 9 → 3
        assertEquals(0, juego.bote)
        assertTrue(juego.terminado)
    }

    @Test
    fun `el empate pierde y el bote no pasa del tope`() {
        val empate = MayorMenor(10, baraja(c(7), c(7, Palo.CORAZONES)))
        assertFalse(empate.adivinar(Prediccion.MENOR))

        // Con un As arriba, adivinar «menor» varias veces seguidas no supera 10 veces la apuesta
        val juego = MayorMenor(10, Baraja(listOf(c(3), c(2)) + (2..14).map { c(it, Palo.CORAZONES) }.reversed()))
        assertTrue(juego.adivinar(Prediccion.MENOR)) // 3 → 2
        while (!juego.terminado && juego.multiplicadorPorCiento(Prediccion.MAYOR) > 0) juego.adivinar(Prediccion.MAYOR)
        assertTrue(juego.bote <= juego.tope)
    }
}
