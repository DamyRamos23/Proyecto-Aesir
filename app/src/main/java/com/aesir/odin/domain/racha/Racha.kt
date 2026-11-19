package com.aesir.odin.domain.racha

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Tipos del MOD-05 Sistema de racha · La carrera de Sól (diagrama de clases
 * MOD-05 v1.2). Kotlin puro: no depende de Android ni de Room.
 */

// ---------------------------------------------------------------------------
// Enumeraciones
// ---------------------------------------------------------------------------

/** Estado de la racha del día (tabla «Estado de la racha del día»). */
enum class EstadoRachaDia { APAGADA, EN_RIESGO, PROTEGIDA, ENCENDIDA }

enum class MotivoRechazo { HIDROMIEL_INSUFICIENTE, LIMITE_OFRENDAS }

/** Actividades que avisan al MOD-05. La pregunta flash (CU-06) no cuenta. */
enum class TipoActividad {
    NIVEL_ROADMAP,
    EVALUACION_DIAGNOSTICA,
    REPASO_MIMIR,
    RETO_CONTRARRELOJ,
    DUELO_ENIGMAS,
    MODO_VALHALLA,
    PREGUNTA_FLASH
}

/** Sonidos de la categoría Progreso del MOD-02. */
enum class SonidoRacha { RACHA_ENCENDIDA, HITO_RACHA }

// ---------------------------------------------------------------------------
// Eventos que recibe el módulo
// ---------------------------------------------------------------------------

data class AplicacionAbierta(val idUsuario: String, val fecha: LocalDate)

/** E5: [fechaFin] es el día en que terminó la actividad, aunque haya empezado el día anterior. */
data class ActividadTerminada(
    val idUsuario: String,
    val tipo: TipoActividad,
    val abandonada: Boolean,
    val fechaFin: LocalDate
)

/** Día local (00:00 a 23:59 del dispositivo) que contiene el instante [ms]. */
fun fechaLocal(ms: Long, zona: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(ms).atZone(zona).toLocalDate()

// ---------------------------------------------------------------------------
// DTOs
// ---------------------------------------------------------------------------

/** Hito de racha: días, bono de hidromiel y título opcional. */
data class Hito(val dias: Int, val bono: Int, val titulo: String? = null)

/**
 * Lo que el módulo regresa tras cada disparador.
 * - [ofrendasUsadas]: ofrendas que se gastaron para proteger días faltantes.
 * - [rachaPerdida]: racha que Sköll alcanzó (E1), 0 si no se perdió.
 * - [hito]: hito alcanzado y cobrado en esta actividad.
 */
data class EstadoRacha(
    val estado: EstadoRachaDia,
    val actual: Int,
    val maxima: Int,
    val ofrendas: Int,
    val ofrendasUsadas: Int = 0,
    val rachaPerdida: Int = 0,
    val hito: Hito? = null
)

data class ResultadoOfrenda(
    val costo: Int,
    val puedeComprar: Boolean,
    val comprada: Boolean,
    val motivo: MotivoRechazo?,
    val ofrendas: Int
)

/** Aviso para el alumno que la interfaz muestra en cualquier pantalla. */
data class AvisoRacha(val estado: EstadoRacha, val mensaje: String)

/** Lo que muestra la pantalla de la racha. */
data class ResumenRacha(
    val estado: EstadoRacha,
    val titulos: List<String>,
    val hitosCobrados: List<Int>,
    val hitos: List<Hito>
)

// ---------------------------------------------------------------------------
// Racha del alumno
// ---------------------------------------------------------------------------

/**
 * Racha de un alumno. [ultimaActividad] es el último día cubierto por la
 * racha: el de la última actividad que contó o, si se usaron ofrendas, el
 * último día que protegieron.
 */
class Racha(
    val idUsuario: String,
    actual: Int = 0,
    maxima: Int = 0,
    ultimaActividad: LocalDate? = null,
    ofrendas: Int = 0,
    titulos: List<String> = emptyList()
) {
    var actual: Int = actual.coerceAtLeast(0)
        private set
    /** RN-29: nunca disminuye. */
    var maxima: Int = maxima.coerceAtLeast(this.actual)
        private set
    var ultimaActividad: LocalDate? = ultimaActividad
        private set
    var ofrendas: Int = ofrendas.coerceIn(0, ReglasRacha.MAX_OFRENDAS)
        private set
    private val _titulos = titulos.toMutableList()
    val titulos: List<String> get() = _titulos

    /**
     * Días completos sin actividad entre la última actividad y [hoy] (sin
     * contar ninguno de los dos). E8: 0 si no hay racha; E6: 0 si la fecha va
     * hacia atrás.
     */
    fun diasFaltantes(hoy: LocalDate): Int {
        val ultima = ultimaActividad ?: return 0
        if (actual == 0) return 0
        return (ChronoUnit.DAYS.between(ultima, hoy) - 1).toInt().coerceAtLeast(0)
    }

    /** RN-25: ¿ya contó una actividad en [hoy]? */
    fun contoHoy(hoy: LocalDate): Boolean = ultimaActividad == hoy

    /** Suma un día a la racha y guarda la fecha; actualiza la máxima (RN-29). */
    fun sumarDia(hoy: LocalDate) {
        actual += 1
        ultimaActividad = hoy
        if (actual > maxima) maxima = actual
    }

    /** RN-27: cada ofrenda protege un día faltante; los días protegidos quedan cubiertos. */
    fun usarOfrendas(cantidad: Int) {
        require(cantidad in 0..ofrendas) { "No hay ofrendas suficientes" }
        ofrendas -= cantidad
        ultimaActividad = ultimaActividad?.plusDays(cantidad.toLong())
    }

    /** E1: Sköll alcanza a Sól. La máxima, las ofrendas y los títulos se conservan. */
    fun apagar() {
        actual = 0
    }

    fun agregarOfrenda() {
        check(ofrendas < ReglasRacha.MAX_OFRENDAS) { "Ya tienes el máximo de ofrendas" }
        ofrendas += 1
    }

    /** E7: los títulos obtenidos se conservan aunque se pierda la racha. */
    fun otorgarTitulo(titulo: String) {
        if (titulo !in _titulos) _titulos += titulo
    }
}

// ---------------------------------------------------------------------------
// Reglas y cálculo
// ---------------------------------------------------------------------------

/** Valores del MOD-05: hitos, costo de la ofrenda (2C = 40) y máximo de ofrendas (2). */
class ReglasRacha(val hitos: List<Hito> = HITOS) {

    fun hitoPara(dias: Int): Hito? = hitos.firstOrNull { it.dias == dias }

    /** E4 antes que E3: con 2 ofrendas no se puede comprar aunque alcance el hidromiel. */
    fun validarCompra(racha: Racha, saldo: Int): MotivoRechazo? = when {
        racha.ofrendas >= MAX_OFRENDAS -> MotivoRechazo.LIMITE_OFRENDAS
        saldo < COSTO_OFRENDA -> MotivoRechazo.HIDROMIEL_INSUFICIENTE
        else -> null
    }

    companion object {
        const val COSTO_OFRENDA = 40
        const val MAX_OFRENDAS = 2
        val HITOS = listOf(
            Hito(3, 10),
            Hito(7, 20),
            Hito(14, 40),
            Hito(30, 60, "Hijo de Sól"),
            Hito(100, 100, "Corredor del cielo")
        )
    }
}

/** Calcula el estado de la racha al abrir la app, al terminar una actividad o al consultar. */
class CalculadoraRacha {

    /**
     * Funcionamiento 1 y 2: APAGADA sin racha (E8), ENCENDIDA si ya contó hoy,
     * EN_RIESGO si contó ayer; con días faltantes usa una ofrenda por día
     * (PROTEGIDA) o, si no alcanzan, reinicia la racha (E1). E6: si la fecha
     * va hacia atrás no cambia nada.
     */
    fun alAbrir(racha: Racha, hoy: LocalDate): EstadoRacha {
        val ultima = racha.ultimaActividad
        if (racha.actual == 0 || ultima == null) return estado(racha, EstadoRachaDia.APAGADA)
        if (hoy.isBefore(ultima)) return estado(racha, EstadoRachaDia.EN_RIESGO)
        if (racha.contoHoy(hoy)) return estado(racha, EstadoRachaDia.ENCENDIDA)
        val faltan = racha.diasFaltantes(hoy)
        if (faltan == 0) return estado(racha, EstadoRachaDia.EN_RIESGO)
        if (racha.ofrendas >= faltan) {
            racha.usarOfrendas(faltan)
            return estado(racha, EstadoRachaDia.PROTEGIDA, ofrendasUsadas = faltan)
        }
        val perdida = racha.actual
        racha.apagar()
        return estado(racha, EstadoRachaDia.APAGADA, rachaPerdida = perdida)
    }

    /**
     * Funcionamiento 3 y 4: si la actividad cuenta (E2) y es la primera del
     * día (RN-25), suma 1 y la racha queda ENCENDIDA. Antes resuelve los días
     * faltantes, por si la app siguió abierta después de la medianoche.
     */
    fun alTerminar(racha: Racha, e: ActividadTerminada): EstadoRacha {
        val hoy = e.fechaFin
        if (!cuenta(e.tipo, e.abandonada)) return estado(racha, estadoPara(racha, hoy))
        val ultima = racha.ultimaActividad
        if (ultima != null && hoy.isBefore(ultima)) return estado(racha, estadoPara(racha, hoy)) // E6
        if (racha.contoHoy(hoy)) return estado(racha, EstadoRachaDia.ENCENDIDA)
        val previo = alAbrir(racha, hoy)
        racha.sumarDia(hoy)
        return estado(racha, EstadoRachaDia.ENCENDIDA, previo.ofrendasUsadas, previo.rachaPerdida)
    }

    /** Estado del día sin usar ofrendas ni cambiar nada (consultas y MOD-04). */
    fun estadoPara(racha: Racha, hoy: LocalDate): EstadoRachaDia {
        val ultima = racha.ultimaActividad
        return when {
            racha.actual == 0 || ultima == null -> EstadoRachaDia.APAGADA
            hoy.isBefore(ultima) -> EstadoRachaDia.EN_RIESGO
            racha.contoHoy(hoy) -> EstadoRachaDia.ENCENDIDA
            racha.diasFaltantes(hoy) == 0 -> EstadoRachaDia.EN_RIESGO
            racha.ofrendas >= racha.diasFaltantes(hoy) -> EstadoRachaDia.PROTEGIDA
            else -> EstadoRachaDia.APAGADA
        }
    }

    /** E2: lo abandonado no cuenta; RN-26: la pregunta flash tampoco. */
    fun cuenta(tipo: TipoActividad, abandonada: Boolean): Boolean =
        !abandonada && tipo != TipoActividad.PREGUNTA_FLASH

    private fun estado(racha: Racha, dia: EstadoRachaDia, ofrendasUsadas: Int = 0, rachaPerdida: Int = 0) =
        EstadoRacha(dia, racha.actual, racha.maxima, racha.ofrendas, ofrendasUsadas, rachaPerdida)
}

// ---------------------------------------------------------------------------
// Interfaces
// ---------------------------------------------------------------------------

/** Persistencia del MOD-05. */
interface AlmacenRacha {
    suspend fun leer(idUsuario: String): Racha?
    suspend fun guardar(racha: Racha): Boolean
    suspend fun hitoCobrado(idUsuario: String, dias: Int): Boolean
    suspend fun registrarHito(idUsuario: String, hito: Hito): Boolean
    /** Días de los hitos ya cobrados (para la pantalla de la racha). */
    suspend fun hitosCobrados(idUsuario: String): List<Int>
}

/** Lo que el MOD-04 consulta del MOD-05. */
interface ConsultaRacha {
    suspend fun estadoDelDia(idUsuario: String, hoy: LocalDate): EstadoRachaDia
}

/** MOD-02: sonidos de la categoría Progreso. */
interface ReproductorSonido {
    fun reproducirProgreso(sonido: SonidoRacha)

    object Silencio : ReproductorSonido {
        override fun reproducirProgreso(sonido: SonidoRacha) = Unit
    }
}
