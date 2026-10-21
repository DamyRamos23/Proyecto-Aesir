package com.aesir.odin.ui.moneda

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

/**
 * Cómo está la moneda en un instante. Kotlin puro (sin Compose) para poder
 * probarlo y para que la versión de iOS use exactamente las mismas fórmulas.
 *
 * @property angulo giro alrededor del eje horizontal, en grados (0 = frente arriba).
 * @property alturaPx desplazamiento vertical respecto al reposo; negativo es hacia arriba.
 * @property escala tamaño relativo (sube hacia la cámara al levantarse).
 * @property bamboleo inclinación extra al asentarse en el suelo, en grados.
 * @property desdeAterrizaje segundos desde que tocó el suelo; negativo si sigue en el aire.
 */
data class PoseMoneda(
    val angulo: Float,
    val alturaPx: Float,
    val escala: Float,
    val bamboleo: Float,
    val desdeAterrizaje: Float
) {
    /** Ángulo que se dibuja: el giro más el bamboleo. */
    val anguloVisible: Float get() = angulo + bamboleo

    companion object {
        val Reposo = PoseMoneda(0f, 0f, 1f, 0f, 10f)
    }
}

/** Cualquier movimiento de la moneda: un lanzamiento, la caída de entrada o asentarse al soltarla. */
interface MovimientoMoneda {
    /** Segundos hasta que toca el suelo. */
    val enElAire: Float
    /** Segundos hasta que queda quieta. */
    val duracion: Float
    /** true si al terminar se ve la otra cara (la siguiente al voltearla hacia abajo, la anterior hacia arriba). */
    val cambiaCara: Boolean
    fun pose(t: Float): PoseMoneda
}

/**
 * Un vuelo completo de la moneda: sube (o cae del cielo en la entrada), gira
 * [medioGiros] medias vueltas, rebota dos veces y se bambolea hasta quedar quieta.
 *
 * - Lanzamiento normal: [medioGiros] impar, así al caer se ve el reverso,
 *   que es la siguiente cara del ciclo. Puede empezar desde donde la dejó el
 *   dedo ([anguloInicial], [alturaInicialPx], [escalaInicial]) y girar hacia
 *   arriba o hacia abajo ([direccion]).
 * - Caída de entrada ([caidaDelCielo]): [medioGiros] par, cae en la misma cara.
 */
class VueloMoneda(
    val medioGiros: Int,
    private val alturaPx: Float,
    private val rebotePx: Float,
    val caidaDelCielo: Boolean = false,
    private val anguloInicial: Float = 0f,
    private val alturaInicialPx: Float = 0f,
    private val escalaInicial: Float = 1f,
    private val direccion: Float = 1f
) : MovimientoMoneda {
    /** Segundos en el aire. Más vueltas, más tiempo (con tope). */
    override val enElAire: Float = if (caidaDelCielo) 1.35f else (0.55f + 0.045f * medioGiros).coerceAtMost(2.8f)
    private val rebote1 = if (caidaDelCielo) 0.34f else 0.26f
    private val rebote2 = 0.18f
    private val asentarse = 0.5f
    override val duracion: Float = enElAire + rebote1 + rebote2 + asentarse
    override val cambiaCara: Boolean get() = medioGiros % 2 == 1
    private val amplitudBamboleo = if (caidaDelCielo) 34f else 24f
    /**
     * Ángulo final: [medioGiros] medias vueltas más allá del inicial, redondeado al
     * múltiplo impar (o par en la entrada) de 180° más cercano, para que siempre
     * caiga en la cara que toca aunque se impulse a medio vuelo.
     */
    private val total: Float = run {
        val dir = if (direccion < 0f) -1f else 1f
        val base = anguloInicial + dir * 180f * medioGiros
        var k = kotlin.math.round(base / 180f).toInt()
        if ((k % 2 != 0) != (medioGiros % 2 != 0)) k += dir.toInt()
        180f * k
    }

    override fun pose(t: Float): PoseMoneda {
        if (t >= duracion) return PoseMoneda(total, 0f, 1f, 0f, t - enElAire)
        if (t < enElAire) {
            val u = (t / enElAire).coerceIn(0f, 1f)
            val arco = 4f * u * (1f - u)
            val y = if (caidaDelCielo) -alturaPx * (1f - u * u) else alturaInicialPx * (1f - u) - alturaPx * arco
            val escala = if (caidaDelCielo) 1.12f - 0.12f * u else escalaInicial + (1f - escalaInicial) * u + 0.22f * arco
            // Gira rápido al salir y frena al llegar: así se lee la cara al caer.
            val giro = 1f - (1f - u).pow(2f)
            return PoseMoneda(anguloInicial + (total - anguloInicial) * giro, y, escala, 0f, -1f)
        }
        val s = t - enElAire
        val y = when {
            s < rebote1 -> { val v = s / rebote1; -rebotePx * 4f * v * (1f - v) }
            s < rebote1 + rebote2 -> { val v = (s - rebote1) / rebote2; -rebotePx * 0.3f * 4f * v * (1f - v) }
            else -> 0f
        }
        val bamboleo = amplitudBamboleo * exp(-4.5f * s) * sin(2f * PI.toFloat() * 2.6f * s)
        return PoseMoneda(total, y, 1f, bamboleo, s)
    }

    companion object {
        /** Medias vueltas de un lanzamiento según la fuerza del gesto (dp/s). Siempre impar: 3 a 41. */
        fun medioGirosPorVelocidad(dpPorSegundo: Float): Int {
            val extra = (dpPorSegundo / 450f).toInt().coerceIn(0, 19)
            return 3 + 2 * extra
        }

        /** Medias vueltas de la caída de entrada (par: cae en la cara de mundos). */
        const val MEDIO_GIROS_ENTRADA = 10
    }
}

/**
 * Soltar la moneda despacio después de agarrarla: baja a la mesa y termina de
 * voltearse si ya pasó de canto (más de 90°) o regresa a su cara si no.
 */
class AsentarMoneda(
    private val anguloInicial: Float,
    private val alturaInicialPx: Float,
    private val escalaInicial: Float
) : MovimientoMoneda {
    private val anguloFinal = if (abs(anguloInicial) > 90f) sign(anguloInicial) * 180f else 0f
    override val cambiaCara: Boolean = anguloFinal != 0f
    override val enElAire: Float = 0.3f
    override val duracion: Float = 0.9f
    private val rebote = min(abs(alturaInicialPx) * 0.25f, 18f)
    private val amplitud = 6f + abs(anguloFinal - anguloInicial) * 0.12f

    override fun pose(t: Float): PoseMoneda {
        if (t >= duracion) return PoseMoneda(anguloFinal, 0f, 1f, 0f, t - enElAire)
        if (t < enElAire) {
            val u = (t / enElAire).coerceIn(0f, 1f)
            val giro = 1f - (1f - u).pow(3f)
            return PoseMoneda(
                anguloInicial + (anguloFinal - anguloInicial) * giro,
                alturaInicialPx * (1f - u * u),
                escalaInicial + (1f - escalaInicial) * u,
                0f, -1f
            )
        }
        val s = t - enElAire
        val y = if (s < 0.18f) { val v = s / 0.18f; -rebote * 4f * v * (1f - v) } else 0f
        val bamboleo = amplitud * exp(-6f * s) * sin(2f * PI.toFloat() * 3f * s)
        return PoseMoneda(anguloFinal, y, 1f, bamboleo, s)
    }
}

/**
 * Amago que recuerda que la moneda se puede voltear: siempre que está quieta,
 * cada [CADA] s se inclina un poco (alternando hacia abajo y hacia
 * arriba), regresa con un golpecito y se bambolea.
 */
object PistaMoneda {
    /** Segundos quieta antes del primer amago. */
    const val PAUSA_INICIAL = 2.5f
    /** Segundos entre el inicio de un amago y el siguiente. */
    const val CADA = 6f
    const val DURACION = 1.5f
    const val ANGULO = 58f
    private const val SUBIDA = 0.45f
    private const val BAJADA = 0.33f

    /** Pose del amago [t] segundos después de empezar; [sentido] 1 = hacia arriba, -1 = hacia abajo. */
    fun pose(t: Float, sentido: Float = 1f): PoseMoneda {
        if (t <= 0f || t >= DURACION) return PoseMoneda.Reposo
        val a = when {
            t < SUBIDA -> ANGULO * sin(PI.toFloat() / 2f * t / SUBIDA)
            t < SUBIDA + BAJADA -> ANGULO * kotlin.math.cos(PI.toFloat() / 2f * (t - SUBIDA) / BAJADA)
            else -> {
                val s = t - SUBIDA - BAJADA
                -9f * exp(-5f * s) * sin(2f * PI.toFloat() * 2.8f * s)
            }
        }
        val levantada = (a / ANGULO).coerceAtLeast(0f)
        return PoseMoneda(sentido * a, 0f, 1f + 0.04f * levantada, 0f, 10f)
    }

    /** Pose según los segundos que la moneda lleva quieta: un amago cada [CADA] s, alternando el sentido. */
    fun poseQuieta(segundosQuieta: Float): PoseMoneda {
        if (segundosQuieta < PAUSA_INICIAL) return PoseMoneda.Reposo
        val desde = segundosQuieta - PAUSA_INICIAL
        val ciclo = (desde / CADA).toInt()
        return pose(desde - ciclo * CADA, if (ciclo % 2 == 0) SENTIDO_SIGUIENTE else -SENTIDO_SIGUIENTE)
    }
}

/**
 * Agarrar la moneda con el dedo: se levanta poco a poco y se voltea según
 * cuánto se arrastra. Hacia abajo asoma la siguiente cara y hacia arriba la
 * anterior. Al soltarla rápido se lanza en ese sentido.
 */
object ArrastreMoneda {
    /** Grados de giro por dp arrastrado en vertical; tope para no pasar de media vuelta. */
    fun angulo(dyDp: Float): Float = (-dyDp * 0.9f).coerceIn(-170f, 170f)

    /** Cuánto sube (dp) según lo que se ha arrastrado. */
    fun alturaDp(distanciaDp: Float): Float = min(distanciaDp * 0.35f, 44f)

    /** Se acerca un poco a la cámara al levantarla. */
    fun escala(distanciaDp: Float): Float = 1f + min(distanciaDp, 120f) / 120f * 0.08f

    /** A partir de esta velocidad (dp/s) soltar la lanza; más lento solo la asienta. */
    const val VELOCIDAD_LANZAR = 700f
}

/** Ciclo de caras: escudo de mundos → 4 casos de uso → escudo otra vez. */
fun siguienteCara(indice: Int, total: Int): Int = (indice + 1) % total

/** La cara de antes en el ciclo (al voltear la moneda hacia arriba). */
fun anteriorCara(indice: Int, total: Int): Int = (indice - 1 + total) % total

/**
 * Cara que queda al voltear la moneda en un sentido: hacia abajo ([sentido] < 0,
 * ángulo negativo) la siguiente; hacia arriba la anterior.
 */
fun caraAlVoltear(indice: Int, total: Int, sentido: Float): Int =
    if (sentido < 0f) siguienteCara(indice, total) else anteriorCara(indice, total)

/** Sentido del giro que lleva a la siguiente cara (hacia abajo). */
const val SENTIDO_SIGUIENTE = -1f
