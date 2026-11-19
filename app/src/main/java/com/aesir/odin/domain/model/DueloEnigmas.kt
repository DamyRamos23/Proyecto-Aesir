package com.aesir.odin.domain.model

// CU-13 Resolver reto de enigmas (Duelo de enigmas). Modelos del diagrama de clases.

/** Concepto que el gigante puede esconder; [explicacion] se muestra al final del duelo. */
data class ConceptoEnigma(
    val id: String,
    val temaId: String,
    val nombre: String,
    val explicacion: String
)

enum class TipoPregunta { DIRECTA, ENIGMA }

/** Pregunta cerrada: la respuesta es «sí» solo para los conceptos de [conceptosSi]. */
data class PreguntaEnigma(
    val id: String,
    val texto: String,
    val tipo: TipoPregunta,
    val conceptosSi: List<String>
) {
    fun respuestaPara(conceptoId: String): Boolean = conceptoId in conceptosSi
}

data class TarjetaConcepto(
    val concepto: ConceptoEnigma,
    val descartada: Boolean = false
)

/** El cuerno del duelo: 6 sorbos al empezar (1 sorbo = 4 de hidromiel). */
class CuernoHidromiel(sorbos: Int = SORBOS_INICIALES) {
    var sorbos: Int = sorbos
        private set

    /** Resta sorbos sin bajar de 0. */
    fun beber(cantidad: Int) {
        sorbos = (sorbos - cantidad).coerceAtLeast(0)
    }

    fun estaVacio(): Boolean = sorbos == 0

    companion object {
        const val SORBOS_INICIALES = 6
    }
}

/**
 * Estado de un duelo en curso. El concepto verdadero es privado: la pantalla
 * solo puede preguntar si una tarjeta lo es ([esConceptoVerdadero]).
 */
class DueloEnigmas(
    val id: String,
    tarjetas: List<TarjetaConcepto>,
    val preguntas: List<PreguntaEnigma>,
    private val idConceptoVerdadero: String,
    val cuerno: CuernoHidromiel = CuernoHidromiel()
) {
    init {
        require(tarjetas.size == TOTAL_TARJETAS) { "El tablero lleva $TOTAL_TARJETAS tarjetas" }
        require(tarjetas.any { it.concepto.id == idConceptoVerdadero }) { "El concepto verdadero debe estar en el tablero" }
    }

    var tarjetas: List<TarjetaConcepto> = tarjetas
        private set
    val preguntasRealizadas = mutableListOf<String>()
    var adivinanzasFallidas = 0
        internal set
    /** A10.1: el cuerno quedó vacío por preguntar; solo queda una adivinanza. */
    var ultimaAdivinanza = false
        internal set

    fun esConceptoVerdadero(conceptoId: String): Boolean = conceptoId == idConceptoVerdadero

    fun conceptoVerdadero(): ConceptoEnigma = tarjetas.first { it.concepto.id == idConceptoVerdadero }.concepto

    fun puedePreguntar(): Boolean = !cuerno.estaVacio() && preguntasDisponibles().isNotEmpty()

    fun preguntasDisponibles(): List<PreguntaEnigma> = preguntas.filter { it.id !in preguntasRealizadas }

    /** A9.1 / A9.2: alterna la tarjeta entre descartada y posible; nunca avisa si es la verdadera. */
    fun voltear(conceptoId: String) {
        tarjetas = tarjetas.map { if (it.concepto.id == conceptoId) it.copy(descartada = !it.descartada) else it }
    }

    /** A12.1: la tarjeta elegida por error queda volteada (descartada). */
    internal fun descartar(conceptoId: String) {
        tarjetas = tarjetas.map { if (it.concepto.id == conceptoId) it.copy(descartada = true) else it }
    }

    companion object {
        const val TOTAL_TARJETAS = 12
    }
}

/** Resultado de los pasos 1 y 2: si se puede jugar y por qué no (E4.1). */
data class AccesoDuelo(
    val permitido: Boolean,
    val motivo: MotivoSinAcceso?,
    val apuesta: Int,
    val saldo: Int,
    val faltante: Int,
    val duelosRestantes: Int
)

enum class MotivoSinAcceso { SIN_RAGNAROK_SUPERADO, LIMITE_DIARIO_ALCANZADO, HIDROMIEL_INSUFICIENTE }

data class RespuestaGigante(val preguntaId: String, val esSi: Boolean, val sorbosRestantes: Int)

data class ResultadoAdivinanza(val correcta: Boolean, val sorbosRestantes: Int, val dueloTerminado: Boolean)

enum class EstadoFinalDuelo { GANADO, PERDIDO, ABANDONADO }

/** RF-196: lo que queda registrado de cada duelo. [resultado] null = en curso. */
data class ResultadoDuelo(
    val id: String,
    val conceptoId: String,
    val resultado: EstadoFinalDuelo?,
    val preguntasRealizadas: Int,
    val adivinanzasFallidas: Int,
    val sorbosSobrantes: Int,
    val hidromielObtenido: Int,
    val fecha: Long
)

/** Lo que muestra la pantalla de resultado (paso 13 y E12.1). */
data class CierreDuelo(
    val resultado: ResultadoDuelo,
    val conceptoVerdadero: ConceptoEnigma,
    val aviso: com.aesir.odin.domain.hidromiel.AvisoHidromiel?
)
