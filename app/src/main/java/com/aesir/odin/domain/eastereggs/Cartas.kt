package com.aesir.odin.domain.eastereggs

import kotlin.random.Random

enum class Palo(val glifo: String, val rojo: Boolean) {
    PICAS("♠", false), CORAZONES("♥", true), DIAMANTES("♦", true), TREBOLES("♣", false)
}

/** [valor] de 2 a 14; 11 J, 12 Q, 13 K y 14 As (el As es la más alta en «mayor o menor»). */
data class Carta(val valor: Int, val palo: Palo) {
    val etiqueta: String
        get() = when (valor) {
            11 -> "J"; 12 -> "Q"; 13 -> "K"; 14 -> "A"
            else -> valor.toString()
        }

    /** Puntos en blackjack (el As vale 11 aquí; [valorMano] lo baja a 1 si hace falta). */
    val puntos: Int get() = if (valor == 14) 11 else minOf(valor, 10)
}

/** Baraja de 52 revuelta; [orden] permite fijar las cartas en las pruebas. */
class Baraja(orden: List<Carta>) {
    constructor(azar: Random) : this(completa().shuffled(azar))

    private val cartas = ArrayDeque(orden)
    val restantes: List<Carta> get() = cartas.toList()

    fun sacar(): Carta = cartas.removeFirst()

    companion object {
        fun completa(): List<Carta> = Palo.entries.flatMap { p -> (2..14).map { Carta(it, p) } }
    }
}

// ---------------------------------------------------------------------------
// Blackjack
// ---------------------------------------------------------------------------

fun valorMano(cartas: List<Carta>): Int {
    var total = cartas.sumOf { it.puntos }
    var asesComoOnce = cartas.count { it.valor == 14 }
    while (total > 21 && asesComoOnce > 0) {
        total -= 10
        asesComoOnce--
    }
    return total
}

class ManoJugador(val cartas: MutableList<Carta>, var apuesta: Int, val deDivision: Boolean = false) {
    var terminada = false
    val valor: Int get() = valorMano(cartas)
    val sePaso: Boolean get() = valor > 21
    /** Blackjack natural: solo con las dos cartas iniciales y sin haber dividido. */
    val natural: Boolean get() = !deDivision && cartas.size == 2 && valor == 21
}

/**
 * Una mano de blackjack contra la casa con una baraja de 52 nueva.
 * El crupier pide hasta 16 y se planta en 17 (también en 17 suave).
 * Se puede doblar con dos cartas y dividir una vez un par. Sin seguro ni rendición.
 * Pagos: natural 3:2 (recibe 2.5×), gana 2×, empate devuelve la apuesta.
 */
class Blackjack(apuesta: Int, private val baraja: Baraja) {
    constructor(apuesta: Int, azar: Random) : this(apuesta, Baraja(azar))

    val manos = mutableListOf(ManoJugador(mutableListOf(), apuesta))
    val crupier = mutableListOf<Carta>()
    var indiceActual = 0
        private set

    val terminado: Boolean get() = manos.all { it.terminada }
    val manoActual: ManoJugador? get() = manos.getOrNull(indiceActual)?.takeUnless { it.terminada }

    init {
        manos[0].cartas += baraja.sacar()
        crupier += baraja.sacar()
        manos[0].cartas += baraja.sacar()
        crupier += baraja.sacar()
        // Con un natural (del alumno o de la casa) la mano se resuelve de inmediato.
        if (manos[0].natural || valorMano(crupier) == 21) terminarTodo()
    }

    val puedeDoblar: Boolean get() = manoActual?.cartas?.size == 2
    val puedeDividir: Boolean
        get() = manos.size == 1 && manoActual?.let { it.cartas.size == 2 && it.cartas[0].valor == it.cartas[1].valor } == true

    fun pedir() {
        val mano = manoActual ?: return
        mano.cartas += baraja.sacar()
        if (mano.valor >= 21) siguienteMano()
    }

    fun plantarse() {
        if (manoActual != null) siguienteMano()
    }

    /** La apuesta extra ya se cobró afuera (MOD-01). */
    fun doblar() {
        val mano = manoActual?.takeIf { puedeDoblar } ?: return
        mano.apuesta *= 2
        mano.cartas += baraja.sacar()
        siguienteMano()
    }

    /** La apuesta de la segunda mano ya se cobró afuera (MOD-01). Ases divididos reciben una sola carta. */
    fun dividir() {
        if (!puedeDividir) return
        val original = manos[0]
        val segunda = ManoJugador(mutableListOf(original.cartas.removeAt(1)), original.apuesta, deDivision = true)
        val primera = ManoJugador(mutableListOf(original.cartas[0]), original.apuesta, deDivision = true)
        manos.clear()
        manos += listOf(primera, segunda)
        manos.forEach { it.cartas += baraja.sacar() }
        if (primera.cartas[0].valor == 14) terminarTodo()
        else if (primera.valor == 21) siguienteMano()
    }

    /** Premio total a pagar al final (suma de las manos). */
    fun premio(): Int {
        check(terminado)
        val casa = valorMano(crupier)
        val casaNatural = crupier.size == 2 && casa == 21
        return manos.sumOf { m ->
            when {
                m.natural && casaNatural -> m.apuesta
                m.natural -> m.apuesta * 5 / 2
                casaNatural || m.sePaso -> 0
                casa > 21 || m.valor > casa -> m.apuesta * 2
                m.valor == casa -> m.apuesta
                else -> 0
            }
        }
    }

    private fun siguienteMano() {
        manos[indiceActual].terminada = true
        indiceActual++
        if (indiceActual >= manos.size) jugarCasa()
        else if (manos[indiceActual].valor == 21) siguienteMano()
    }

    private fun terminarTodo() {
        manos.forEach { it.terminada = true }
        indiceActual = manos.size
        jugarCasa()
    }

    private fun jugarCasa() {
        val alguienSigue = manos.any { !it.sePaso && !it.natural }
        if (alguienSigue && !(crupier.size == 2 && valorMano(crupier) == 21)) {
            while (valorMano(crupier) < 17) crupier += baraja.sacar()
        }
    }
}

// ---------------------------------------------------------------------------
// Mayor o menor
// ---------------------------------------------------------------------------

enum class Prediccion { MAYOR, MENOR }

/**
 * Se ve una carta y se adivina si la siguiente es mayor o menor (empate
 * pierde). Cada acierto multiplica el bote por 0.9 × restantes / favorables;
 * se puede cobrar después de acertar al menos una. Tope: 10 veces la apuesta.
 */
class MayorMenor(private val apuesta: Int, private val baraja: Baraja) {
    constructor(apuesta: Int, azar: Random) : this(apuesta, Baraja(azar))

    var actual: Carta = baraja.sacar()
        private set
    var bote: Int = apuesta
        private set
    var aciertos = 0
        private set
    var terminado = false
        private set
    /** Carta que salió en la última jugada. */
    var ultima: Carta? = null
        private set

    val tope: Int get() = apuesta * TOPE_VECES
    val puedeCobrar: Boolean get() = !terminado && aciertos > 0

    fun favorables(p: Prediccion): Int = baraja.restantes.count {
        if (p == Prediccion.MAYOR) it.valor > actual.valor else it.valor < actual.valor
    }

    /** Multiplicador ×100 (para mostrarlo: 145 = ×1.45). 0 si no hay cartas que ganen. */
    fun multiplicadorPorCiento(p: Prediccion): Int {
        val fav = favorables(p)
        return if (fav == 0) 0 else 90 * baraja.restantes.size / fav
    }

    /** @return true si acertó. */
    fun adivinar(p: Prediccion): Boolean {
        check(!terminado)
        val multiplicador = multiplicadorPorCiento(p)
        require(multiplicador > 0) { "No hay cartas que ganen con esa predicción" }
        val siguiente = baraja.sacar()
        ultima = siguiente
        val acerto = if (p == Prediccion.MAYOR) siguiente.valor > actual.valor else siguiente.valor < actual.valor
        actual = siguiente
        if (!acerto) {
            bote = 0
            terminado = true
            return false
        }
        aciertos++
        bote = minOf(bote * multiplicador / 100, tope)
        if (bote >= tope || baraja.restantes.isEmpty()) terminado = true
        return true
    }

    /** Termina y devuelve lo que se gana (el bote). */
    fun cobrar(): Int {
        terminado = true
        return bote
    }

    companion object {
        const val TOPE_VECES = 10
    }
}
