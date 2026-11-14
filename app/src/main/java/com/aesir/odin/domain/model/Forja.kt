package com.aesir.odin.domain.model

import com.aesir.odin.domain.hidromiel.AvisoHidromiel
import java.text.Normalizer
import kotlin.random.Random

/**
 * Modelos del CU-02 Forjar artefactos en la apuesta de Brokkr (diagrama de
 * clases CU-02 v1.2, capa Negocio · Models). Trazabilidad: RF-30 a RF-46.
 */

// ---------------------------------------------------------------------------
// Artefactos y vitrina
// ---------------------------------------------------------------------------

enum class CalidadArtefacto { PERFECTO, CON_DEFECTO }

/**
 * Artefacto asociado a un mundo (mundo 1 Mjölnir, 2 Gungnir, 3 Draupnir y
 * 4 Skíðblaðnir) con el poder que otorga en Hasta el Alba en cada calidad.
 */
data class Artefacto(
    val id: String,
    val mundoId: String,
    val mundoNombre: String,
    val nombre: String,
    val poderPerfecto: String,
    val poderConDefecto: String
) {
    fun poder(calidad: CalidadArtefacto): String = when (calidad) {
        CalidadArtefacto.PERFECTO -> poderPerfecto
        CalidadArtefacto.CON_DEFECTO -> poderConDefecto
    }
}

/** Artefacto que el alumno ya tiene en la vitrina del salón, con su calidad. */
data class ArtefactoVitrina(
    val artefacto: Artefacto,
    val calidad: CalidadArtefacto,
    val fechaObtencion: Long,
    val ultimaNocheUsada: Long? = null
) {
    /** Se puede usar una vez por noche en Hasta el Alba. [hoy] es el inicio del día en milisegundos. */
    fun disponibleEstaNoche(hoy: Long): Boolean = ultimaNocheUsada == null || ultimaNocheUsada < hoy

    fun esPerfecto(): Boolean = calidad == CalidadArtefacto.PERFECTO
}

/**
 * Artefacto que asignó el sistema (RF-30). Si el alumno ya tiene perfectos
 * todos los artefactos de los mundos superados, [artefacto] es null y la forja
 * es solo por hidromiel (A3.1, RF-31). [mundoId] es el mundo del que salen las
 * preguntas de las tres etapas.
 */
data class AsignacionForja(
    val artefacto: Artefacto?,
    val mundoId: String,
    val soloHidromiel: Boolean
)

// ---------------------------------------------------------------------------
// Acceso y apuesta
// ---------------------------------------------------------------------------

enum class MotivoBloqueoForja { SIN_RAGNAROK_SUPERADO, FORJAS_AGOTADAS, HIDROMIEL_INSUFICIENTE }

/**
 * Resultado de validar las precondiciones del CU-02.
 * [hidromielFaltante]: cuánto le falta para la apuesta mínima (A2.2).
 */
data class ResultadoAccesoForja(
    val autorizado: Boolean,
    val motivo: MotivoBloqueoForja? = null,
    val hidromielFaltante: Int = 0,
    val forjasRestantes: Int = 0
)

/**
 * Rango de apuesta (RF-32): de [MINIMO] al menor entre [MAXIMO] y el
 * hidromiel menos el piso de apuesta (MOD-01, RN-08).
 */
data class RangoApuesta(
    val pisoApuesta: Int,
    val maximoPermitido: Int
) {
    val minimo: Int get() = MINIMO

    fun contiene(cantidad: Int): Boolean = cantidad in MINIMO..maximoPermitido

    companion object {
        const val MINIMO = 20
        const val MAXIMO = 80
    }
}

// ---------------------------------------------------------------------------
// Forja y etapas
// ---------------------------------------------------------------------------

enum class TipoEtapa(val nombre: String) {
    CALENTAR("Calentar el metal"),
    MARTILLAR("Martillar"),
    TEMPLAR("Templar")
}

/**
 * Lo que el alumno reveló en una etapa. Solo se llena el campo de la mecánica
 * de esa etapa: [ordenPasos] (ids de los elementos en el orden elegido),
 * [piezaId] o [huecos] (huecoId → valor). [metalFrio] indica que se acabaron
 * los 60 segundos del temple sin respuesta (A14.1).
 */
data class RespuestaEtapa(
    val tipo: TipoEtapa,
    val ordenPasos: List<String> = emptyList(),
    val piezaId: String? = null,
    val huecos: Map<String, String> = emptyMap(),
    val metalFrio: Boolean = false
)

/** Lo que el sistema indica tras validar una etapa. Nunca incluye la respuesta correcta (RF-39). */
data class ResultadoEtapa(
    val tipo: TipoEtapa,
    val correcta: Boolean,
    val erroresAcumulados: Int,
    val forjaTerminada: Boolean,
    val metalFrio: Boolean = false
)

/** Una de las tres etapas de la forja. Cada una sabe validar la respuesta del alumno. */
sealed class EtapaForja(
    val tipo: TipoEtapa,
    val mundoId: String,
    val enunciado: String
) {
    abstract fun validar(respuesta: RespuestaEtapa): Boolean
}

/**
 * Etapa 1: ordenar los pasos de un proceso del mundo con la mecánica de
 * arrastrar y soltar del CU-09 (RF-35). Un solo intento y sin límite de tiempo.
 */
class EtapaCalentar(
    mundoId: String,
    val reto: RetoArrastrar,
    azar: Random = Random.Default
) : EtapaForja(TipoEtapa.CALENTAR, mundoId, reto.instrucciones) {

    private val ordenCorrecto: List<String> = run {
        val ordenPosicion = reto.posiciones.associate { it.id to it.orden }
        reto.elementos.sortedBy { ordenPosicion[it.posicionCorrectaId] ?: Int.MAX_VALUE }.map { it.id }
    }

    /** Pasos en el orden en que se le presentan al alumno (nunca ya ordenados). */
    val pasosIniciales: List<ElementoArrastrable> = run {
        var mezcla = reto.elementos.shuffled(azar)
        var intentos = 0
        while (reto.elementos.size > 1 && mezcla.map { it.id } == ordenCorrecto && intentos < 10) {
            mezcla = reto.elementos.shuffled(azar)
            intentos++
        }
        if (reto.elementos.size > 1 && mezcla.map { it.id } == ordenCorrecto) mezcla = mezcla.reversed()
        mezcla
    }

    override fun validar(respuesta: RespuestaEtapa): Boolean =
        respuesta.tipo == TipoEtapa.CALENTAR && respuesta.ordenPasos == ordenCorrecto

    companion object {
        const val INTENTOS = 1
    }
}

/** Pieza que se puede elegir al martillar: un patrón, una relación UML, etc. */
data class PiezaDiseno(val id: String, val texto: String)

/** Etapa 2: un problema de diseño del mundo y las piezas posibles (RF-36), sin límite de tiempo. */
class EtapaMartillar(
    mundoId: String,
    enunciado: String,
    val piezas: List<PiezaDiseno>,
    private val piezaCorrectaId: String
) : EtapaForja(TipoEtapa.MARTILLAR, mundoId, enunciado) {

    override fun validar(respuesta: RespuestaEtapa): Boolean =
        respuesta.tipo == TipoEtapa.MARTILLAR && respuesta.piezaId == piezaCorrectaId
}

/** Hueco de la definición o del diagrama que se completa al templar. */
class HuecoTemple(
    val id: String,
    private val respuestaCorrecta: String
) {
    /** Compara sin distinguir mayúsculas, acentos ni espacios de más. */
    fun acepta(valor: String): Boolean = normalizar(valor) == normalizar(respuestaCorrecta)

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("\\s+"), " ")
}

/**
 * Etapa 3: una definición o un diagrama incompleto del mundo que se completa
 * antes de que se enfríe el metal (RF-37). [plantilla] marca cada hueco con
 * `{id}`; [opciones] son las piezas que el alumno puede colocar en los huecos.
 */
class EtapaTemplar(
    mundoId: String,
    enunciado: String,
    val plantilla: String,
    val huecos: List<HuecoTemple>,
    val opciones: List<String>
) : EtapaForja(TipoEtapa.TEMPLAR, mundoId, enunciado) {

    override fun validar(respuesta: RespuestaEtapa): Boolean =
        respuesta.tipo == TipoEtapa.TEMPLAR && !respuesta.metalFrio &&
            huecos.all { it.acepta(respuesta.huecos[it.id].orEmpty()) }

    companion object {
        const val SEGUNDOS_ENFRIADO = 60
    }
}

/**
 * Una forja en curso: el artefacto asignado, la apuesta y sus tres etapas.
 * Lleva la etapa actual y los errores acumulados.
 */
class Forja(
    val id: String,
    val asignacion: AsignacionForja,
    val apuesta: Int,
    val etapas: List<EtapaForja>
) {
    init {
        require(etapas.size == TOTAL_ETAPAS) { "La forja tiene $TOTAL_ETAPAS etapas" }
    }

    var etapaActual: Int = 0
        private set

    var errores: Int = 0
        private set

    fun etapaEnCurso(): EtapaForja = etapas[etapaActual.coerceAtMost(etapas.lastIndex)]

    fun registrarError() {
        errores++
    }

    fun avanzar() {
        if (etapaActual < etapas.size) etapaActual++
    }

    fun terminada(): Boolean = etapaActual >= etapas.size

    companion object {
        const val TOTAL_ETAPAS = 3
    }
}

// ---------------------------------------------------------------------------
// Resultado
// ---------------------------------------------------------------------------

/** RF-40: PERFECTO con 0 errores y ROTO con 1 o más. */
enum class EstadoFinalForja { PERFECTO, ROTO }

/** Lo que se muestra al terminar la forja (RF-46). */
data class ResultadoForja(
    val id: String,
    val artefacto: Artefacto?,
    val resultado: EstadoFinalForja,
    val errores: Int,
    val apuesta: Int,
    val hidromielGanado: Int,
    val hidromielPerdido: Int,
    val abandonada: Boolean,
    val aviso: AvisoHidromiel?,
    val fecha: Long
)
