package com.aesir.odin.domain.model

/**
 * Modelos del CU-01 Sobrevivir oleadas (Hasta el Alba), según la versión 3.2
 * del SRS (RF-01 a RF-29).
 *
 * Los multiplicadores se guardan en centésimas (×1.2 = 120) para que el premio
 * se calcule con enteros y siempre se redondee hacia abajo.
 */

/** RF-07: las 7 oleadas de la noche, en orden, con sus golpes, segundos por pregunta y multiplicador. */
enum class Enemigo(
    val numero: Int,
    val nombre: String,
    val hora: String,
    val golpes: Int,
    val segundosPorPregunta: Int,
    val multiplicador: Int
) {
    DRAUGR(1, "Draugr", "Primera hora", 2, 20, 120),
    LOBO_DE_FENRIR(2, "Lobo de Fenrir", "Segunda hora", 2, 18, 140),
    JOTUN_DE_HIELO(3, "Jotun de hielo", "Tercera hora", 3, 16, 170),
    TROLL_DE_MONTANA(4, "Troll de montaña", "Cuarta hora", 3, 14, 200),
    VALQUIRIA_CAIDA(5, "Valquiria caída", "Quinta hora", 3, 12, 250),
    HIJO_DE_MUSPEL(6, "Hijo de Muspel", "Sexta hora", 4, 10, 300),
    EL_ALBA(7, "El alba", "Séptima hora", 4, 8, 400);

    val ultimo: Boolean get() = this == EL_ALBA

    fun siguiente(): Enemigo? = entries.getOrNull(ordinal + 1)

    companion object {
        fun deNumero(numero: Int): Enemigo = entries.first { it.numero == numero.coerceIn(1, 7) }
    }
}

/** Texto de un multiplicador en centésimas: 120 → «×1.2», 425 → «×4.25». */
fun textoMultiplicador(centesimas: Int): String {
    if (centesimas <= 0) return "×0"
    val entero = centesimas / 100
    val resto = centesimas % 100
    return when {
        resto == 0 -> "×$entero"
        resto % 10 == 0 -> "×$entero.${resto / 10}"
        else -> "×$entero.${resto.toString().padStart(2, '0')}"
    }
}

/** Artefactos que se forjan en la Apuesta de Brokkr (CU-02), uno por mundo en este orden. */
enum class Artefacto(val nombre: String, val mundo: Int) {
    MJOLNIR("Mjölnir", 1),
    GUNGNIR("Gungnir", 2),
    DRAUPNIR("Draupnir", 3),
    SKIDBLADNIR("Skíðblaðnir", 4);

    /** Mundo del que sale el artefacto en la forja (mundo 1 a 4, en este orden). */
    val nombreMundo: String get() = when (this) {
        MJOLNIR -> "UML"
        GUNGNIR -> "Casos de Uso"
        DRAUPNIR -> "Ciclo de Vida"
        SKIDBLADNIR -> "Modelos Tradicionales"
    }

    /** Id del mismo artefacto en la forja de Brokkr (CU-02): «art_mjolnir», etc. */
    val idForja: String get() = "art_" + name.lowercase()

    companion object {
        fun deIdForja(id: String): Artefacto? = entries.firstOrNull { it.idForja == id }
    }

    /**
     * Poder del artefacto en Hasta el Alba (A9.2 a A9.4, A15.1). Un artefacto
     * se tiene completo o no se tiene: no hay calidades.
     */
    val poder: String get() = when (this) {
        MJOLNIR -> "Derriba al enemigo de un golpe."
        GUNGNIR -> "Señala la respuesta correcta."
        DRAUPNIR -> "+0.5 al multiplicador con el que cobres."
        SKIDBLADNIR -> "Recupera un escudo roto."
    }
}

/** Un artefacto de la vitrina del salón (forjado en la Apuesta de Brokkr, CU-02). */
data class ArtefactoForjado(val artefacto: Artefacto)

/** Cómo terminó la noche (postcondiciones). */
enum class Desenlace(val nombre: String) {
    ALBA("Sobreviviste hasta el alba"),
    RETIRADA("Volviste al banquete"),
    CAIDA("Caíste en batalla")
}

/** Por qué no se puede apostar (A2.1, A2.2, A3.1 y la precondición de preguntas). */
enum class MotivoBloqueoAlba { VALHALLA_CERRADO, NOCHE_JUGADA, HIDROMIEL_INSUFICIENTE, SIN_PREGUNTAS }

/** En qué parte de la noche está el alumno; sirve para recuperar la noche si se cierra la app (E9.2, E15.1). */
enum class FaseNoche { EN_OLEADA, EN_DECISION, TERMINADA }

/** Lo que muestra la pantalla de inicio (paso 4). */
data class AccesoAlba(
    val motivo: MotivoBloqueoAlba?,
    val rango: RangoApuesta,
    val artefactos: List<ArtefactoForjado>,
    /** A3.1: cuánto hidromiel le falta para la apuesta mínima. */
    val faltante: Int = 0,
    /** A2.2: el resultado de la noche que ya jugó hoy. */
    val nocheDeHoy: ResultadoNoche? = null,
    /** Aviso de algo que se resolvió al abrir (noche sin terminar o premio pendiente). */
    val avisoRecuperacion: String? = null,
    /** A2.1 paso 2: el mundo en curso (el primero abierto cuyo Ragnarök falta). */
    val mundoEnCurso: String? = null
) {
    val permitido: Boolean get() = motivo == null
}

/**
 * Estado de una noche en curso. Es inmutable: cada acción devuelve una noche
 * nueva, así la lógica se puede probar sin la interfaz.
 */
data class NocheAlba(
    val id: String,
    val apuesta: Int,
    val fecha: Long,
    val enemigo: Enemigo = Enemigo.DRAUGR,
    val golpesRestantes: Int = Enemigo.DRAUGR.golpes,
    val escudos: Int = ESCUDOS_POR_NOCHE,
    /** Multiplicador de la última oleada superada, en centésimas (0 = aún ninguna). */
    val multiplicadorAlcanzado: Int = 0,
    /** A15.1: bono de Draupnir reservado para el cobro (50 perfecto, 25 con defecto). */
    val bonoDraupnir: Int = 0,
    val aciertos: Int = 0,
    val errores: Int = 0,
    val artefactos: List<ArtefactoForjado> = emptyList(),
    val artefactosUsados: Set<Artefacto> = emptySet(),
    val preguntasMostradas: List<String> = emptyList(),
    val preguntasFalladas: List<String> = emptyList(),
    val temasConError: Set<String> = emptySet(),
    val fase: FaseNoche = FaseNoche.EN_OLEADA
) {
    val oleadasSuperadas: Int get() = if (multiplicadorAlcanzado == 0) 0 else Enemigo.entries.first { it.multiplicador == multiplicadorAlcanzado }.numero
    val cayo: Boolean get() = escudos <= 0
    val enemigoDerrotado: Boolean get() = golpesRestantes <= 0

    /** Multiplicador con el que se cobraría ahora, con el bono de Draupnir. */
    val multiplicadorDeCobro: Int get() = if (multiplicadorAlcanzado == 0) 0 else multiplicadorAlcanzado + bonoDraupnir

    fun disponible(artefacto: Artefacto): ArtefactoForjado? =
        artefactos.firstOrNull { it.artefacto == artefacto }?.takeIf { artefacto !in artefactosUsados }

    /** Paso 9: la pregunta queda mostrada en la noche (no se repite, RF-09). */
    fun mostrar(preguntaId: String): NocheAlba =
        if (preguntaId in preguntasMostradas) this else copy(preguntasMostradas = preguntasMostradas + preguntaId)

    /** Pasos 11 y 12 (RF-11): un golpe al enemigo. */
    fun golpear(cantidad: Int = 1): NocheAlba = copy(golpesRestantes = (golpesRestantes - cantidad).coerceAtLeast(0))

    fun acierto(): NocheAlba = golpear().copy(aciertos = aciertos + 1)

    /** A11.1 (RF-12): se rompe un escudo; el enemigo no recibe el golpe. */
    fun error(preguntaId: String?, temaId: String?): NocheAlba = copy(
        escudos = (escudos - 1).coerceAtLeast(0),
        errores = errores + 1,
        preguntasFalladas = if (preguntaId == null || preguntaId in preguntasFalladas) preguntasFalladas else preguntasFalladas + preguntaId,
        temasConError = if (temaId == null) temasConError else temasConError + temaId
    )

    /** Paso 14: la oleada queda superada y el multiplicador sube al de ese enemigo. */
    fun superarOleada(): NocheAlba = copy(
        multiplicadorAlcanzado = enemigo.multiplicador,
        fase = FaseNoche.EN_DECISION
    )

    /** Pasos 16 y 8: sigue peleando contra el siguiente enemigo. */
    fun siguienteOleada(): NocheAlba {
        val siguiente = enemigo.siguiente() ?: return this
        return copy(enemigo = siguiente, golpesRestantes = siguiente.golpes, fase = FaseNoche.EN_OLEADA)
    }

    fun usar(artefacto: Artefacto): NocheAlba = copy(artefactosUsados = artefactosUsados + artefacto)

    companion object {
        const val ESCUDOS_POR_NOCHE = 3
        const val OLEADAS = 7
        /** A9.4: Skíðblaðnir con defecto detiene el contador este tiempo. */
        const val BONO_DRAUPNIR_PERFECTO = 50
    }
}

/** Resultado guardado de la noche (RF-29) y lo que muestra el resumen (RF-28). */
data class ResultadoNoche(
    val id: String,
    val fecha: Long,
    val apuesta: Int,
    val oleadaAlcanzada: Int,
    val oleadasSuperadas: Int,
    val desenlace: Desenlace,
    val multiplicador: Int,
    val aciertos: Int,
    val errores: Int,
    val escudosRestantes: Int,
    val artefactosUsados: List<Artefacto>,
    val premioBruto: Int,
    val diezmo: Int,
    val premioNeto: Int,
    val temasConError: List<String> = emptyList(),
    /** E20.1: el premio no se pudo acreditar y se intentará de nuevo al abrir la app. */
    val premioPendiente: Boolean = false,
    /** RF-18: es la primera vez que obtiene el logro y el título de Einherjar. */
    val einherjarNuevo: Boolean = false
)

/** Efecto de usar un artefacto durante la oleada (A9.2 a A9.4, A15.1). */
sealed class EfectoArtefacto {
    /** Mjölnir: la pregunta se descarta; [derrotado] si el enemigo ya no aguanta golpes. */
    data class GolpeDeMjolnir(val golpes: Int, val derrotado: Boolean) : EfectoArtefacto()
    /** Gungnir perfecto: [correctas] se señalan; con defecto: [eliminadas] se quitan. */
    data class Gungnir(val correctas: List<Int>, val eliminadas: List<Int>) : EfectoArtefacto()
    /** Skíðblaðnir perfecto: se recupera un escudo. */
    data object EscudoRecuperado : EfectoArtefacto()
    /** Draupnir: +[bono] centésimas al multiplicador de cobro. */
    data class BonoDraupnir(val bono: Int) : EfectoArtefacto()
    /** No se pudo usar (sin escudos rotos, ya usado o no forjado). */
    data class NoDisponible(val motivo: String) : EfectoArtefacto()
}
