package com.aesir.odin.domain.model

/**
 * Modelos del CU-03 Repasar conceptos con flashcards.
 *
 * Cada tarjeta muestra la DESCRIPCIÓN de un concepto y el alumno elige, entre
 * varias opciones, el CONCEPTO al que corresponde.
 */
data class TarjetaFlashcard(
    val id: String,
    val temaId: String,
    val descripcion: String,
    val concepto: String,
    val opciones: List<String>,
    val indiceCorrecto: Int
)

data class ResultadoFlashcards(
    val temaId: String,
    val puntaje: Int,
    val aciertosPrimerIntento: Int,
    val errores: Int,
    val totalTarjetas: Int,
    val fecha: Long
)

/** Retroalimentación inmediata de una respuesta (paso 5 del flujo normal). */
data class RespuestaFlashcard(
    val seleccionada: Int,
    val correcta: Boolean,
    val indiceCorrecto: Int,
    /** Veces que esta misma tarjeta se ha fallado, contando este intento. */
    val fallosDeLaTarjeta: Int,
    /** A5.2: la tarjeta se falló por tercera vez. */
    val sugerirRepaso: Boolean
)

enum class EstadoTarjeta { PENDIENTE, FALLADA, DOMINADA }

/**
 * Pila de tarjetas de una sesión de flashcards (pasos 2 a 7 del flujo normal).
 *
 * - [responder] valida la respuesta de la tarjeta superior.
 * - [continuar] aplica el resultado: la tarjeta acertada se retira de la
 *   pila (se marca como dominada) y la fallada regresa al final (A5.1).
 *
 * El puntaje es el porcentaje de tarjetas respondidas correctamente al primer
 * intento, de 0 a 100.
 */
class PilaFlashcards(val tarjetas: List<TarjetaFlashcard>) {

    private val pila = ArrayDeque(tarjetas)
    private val fallos = mutableMapOf<String, Int>()
    private val dominadas = mutableSetOf<String>()
    private var pendiente: RespuestaFlashcard? = null

    val total: Int get() = tarjetas.size

    /** Tarjeta superior de la pila; null cuando ya no quedan. */
    val actual: TarjetaFlashcard? get() = pila.firstOrNull()

    /** Tarjetas que aún están en la pila. */
    val restantes: Int get() = pila.size

    val dominadasCount: Int get() = dominadas.size

    /** Respuestas incorrectas dadas en toda la sesión. */
    val errores: Int get() = fallos.values.sum()

    /** Respuestas dadas en toda la sesión (aciertos más errores). */
    val intentos: Int get() = dominadas.size + errores

    val aciertosPrimerIntento: Int get() = dominadas.count { (fallos[it] ?: 0) == 0 }

    val terminada: Boolean get() = pila.isEmpty()

    val puntaje: Int get() = if (total == 0) 0 else aciertosPrimerIntento * 100 / total

    /** Precisión: respuestas correctas entre respuestas dadas, de 0 a 100. */
    val precision: Int get() = if (intentos == 0) 100 else dominadas.size * 100 / intentos

    fun estadoDe(tarjeta: TarjetaFlashcard): EstadoTarjeta = when {
        tarjeta.id in dominadas -> EstadoTarjeta.DOMINADA
        (fallos[tarjeta.id] ?: 0) > 0 -> EstadoTarjeta.FALLADA
        else -> EstadoTarjeta.PENDIENTE
    }

    /** Las tarjetas con más fallos, de mayor a menor (para el resumen final). */
    fun masDificiles(maximo: Int = 3): List<Pair<TarjetaFlashcard, Int>> =
        tarjetas.mapNotNull { t -> fallos[t.id]?.takeIf { it > 0 }?.let { t to it } }
            .sortedByDescending { it.second }
            .take(maximo)

    /**
     * Valida la opción [indice] de la tarjeta superior. Hasta que se llame a
     * [continuar] no se acepta otra respuesta (devuelve null).
     */
    fun responder(indice: Int): RespuestaFlashcard? {
        val tarjeta = actual ?: return null
        if (pendiente != null) return null
        val correcta = indice == tarjeta.indiceCorrecto
        val fallosTarjeta = if (correcta) fallos[tarjeta.id] ?: 0 else (fallos[tarjeta.id] ?: 0) + 1
        if (!correcta) fallos[tarjeta.id] = fallosTarjeta
        return RespuestaFlashcard(
            seleccionada = indice,
            correcta = correcta,
            indiceCorrecto = tarjeta.indiceCorrecto,
            fallosDeLaTarjeta = fallosTarjeta,
            sugerirRepaso = !correcta && fallosTarjeta == FALLOS_PARA_REPASO
        ).also { pendiente = it }
    }

    /** Pasa a la siguiente tarjeta según lo respondido. */
    fun continuar() {
        val respuesta = pendiente ?: return
        pendiente = null
        val tarjeta = pila.removeFirst()
        if (respuesta.correcta) dominadas += tarjeta.id else pila.addLast(tarjeta)
    }

    companion object {
        const val FALLOS_PARA_REPASO = 3
    }
}
