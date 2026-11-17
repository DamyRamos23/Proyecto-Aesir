package com.aesir.odin.domain.notificaciones

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * MOD-04 Sistema de Notificaciones (Cuervos de Odín).
 *
 * Envía avisos del sistema operativo cuando la aplicación no está en primer
 * plano, como Huginn y Muninn, los cuervos que le llevaban noticias a Odín.
 * No interviene dentro de la aplicación (la pregunta flash no es una
 * notificación) y nunca modifica el hidromiel ni el progreso (RN-24).
 *
 * Funcionamiento (SRS v3.2):
 * 1. Al abrir la aplicación por primera vez se solicita el permiso (lo hace la
 *    interfaz; el módulo solo recuerda que ya se pidió).
 * 2. Cada vez que el alumno abre la aplicación, registra la fecha y
 *    reprograma el aviso del día ([alAbrirAplicacion]).
 * 3. A la hora configurada revisa qué tipos aplican y envía solo el de mayor
 *    prioridad ([alLlegarLaHora]).
 * 4. Tocar la notificación abre la aplicación en Mundos (lo hace la interfaz).
 */
class ModuloCuervos(
    private val almacen: AlmacenCuervos,
    private val programador: ProgramadorCuervos,
    private val reloj: () -> LocalDateTime = LocalDateTime::now
) {

    val preferencias: PreferenciasCuervos get() = almacen.leerPreferencias()

    /** Paso 1: la primera vez que se abre la app se pide el permiso; después ya no. */
    fun debeSolicitarPermiso(): Boolean = !almacen.leerEstado().permisoSolicitado

    fun registrarPermisoSolicitado() {
        almacen.guardarEstado(almacen.leerEstado().copy(permisoSolicitado = true))
    }

    /** Paso 2: registra la apertura (termina un periodo de inactividad, E5) y reprograma. */
    fun alAbrirAplicacion() {
        val hoy = reloj().toLocalDate()
        val estado = almacen.leerEstado()
        // E7 del MOD-01: con la fecha hacia atrás no se mueve la última apertura
        if (estado.ultimaApertura == null || !hoy.isBefore(estado.ultimaApertura)) {
            almacen.guardarEstado(estado.copy(ultimaApertura = hoy))
        }
        reprogramar()
    }

    /** Disparador 4: el alumno cambia la configuración en Ajustes. */
    fun cambiarPreferencias(nuevas: PreferenciasCuervos) {
        almacen.guardarPreferencias(nuevas.normalizadas())
        reprogramar()
    }

    /**
     * Paso 3: llegó la hora del recordatorio. Decide el aviso, lo registra
     * como enviado y programa el del día siguiente.
     *
     * @param enPrimerPlano la aplicación está abierta (E4: no se envía nada).
     * @param racha estado de la racha (MOD-05) en este momento.
     * @return el aviso que hay que mostrar, o null si no aplica ninguno.
     */
    fun alLlegarLaHora(enPrimerPlano: Boolean, racha: SituacionRacha): TipoNotificacion? {
        val ahora = reloj()
        val estado = almacen.leerEstado()
        val prefs = almacen.leerPreferencias()
        val aviso = decidir(ahora.toLocalDate(), estado, racha, prefs, enPrimerPlano)
        if (aviso != null) {
            almacen.guardarEstado(
                estado.copy(
                    ultimoEnvio = ahora.toLocalDate(),
                    inactividadAvisada = if (aviso == TipoNotificacion.INACTIVIDAD) estado.ultimaApertura else estado.inactividadAvisada
                )
            )
        }
        programarDesde(ahora.plusMinutes(1), prefs)
        return aviso
    }

    /** Después de reiniciar el teléfono las alarmas se pierden: se vuelve a programar. */
    fun alReiniciarDispositivo() = reprogramar()

    private fun reprogramar() = programarDesde(reloj(), almacen.leerPreferencias())

    private fun programarDesde(desde: LocalDateTime, prefs: PreferenciasCuervos) {
        // E2: con las notificaciones desactivadas se cancela todo lo programado
        if (!prefs.activadas) {
            programador.cancelar()
            return
        }
        programador.programar(proximaHora(desde, prefs))
    }

    companion object {

        /** Hora del recordatorio hoy si aún no pasa; si ya pasó, mañana. */
        fun proximaHora(desde: LocalDateTime, prefs: PreferenciasCuervos): LocalDateTime {
            val hoy = desde.toLocalDate().atTime(LocalTime.of(prefs.hora, prefs.minuto))
            return if (hoy.isAfter(desde)) hoy else hoy.plusDays(1)
        }

        /**
         * Reglas del MOD-04 para decidir qué aviso enviar a la hora configurada:
         * - E2: desactivadas, nada. E4: con la app abierta, nada.
         * - RN-21: no más de una notificación al día.
         * - E5 y RN-23: después del aviso de inactividad no se envía nada hasta
         *   que el alumno vuelva a abrir la aplicación.
         * - RN-22: si aplican varios, solo el de mayor prioridad (inactividad,
         *   racha en riesgo, recompensa diaria).
         * - E3: si ya abrió la app y ya hizo una actividad que cuenta, nada.
         */
        fun decidir(
            hoy: LocalDate,
            estado: EstadoCuervos,
            racha: SituacionRacha,
            prefs: PreferenciasCuervos,
            enPrimerPlano: Boolean
        ): TipoNotificacion? {
            if (!prefs.activadas || enPrimerPlano) return null
            if (estado.ultimoEnvio == hoy) return null
            val ultimaApertura = estado.ultimaApertura
            // Sin aperturas registradas todavía no hay nada que recordar
            if (ultimaApertura == null || hoy.isBefore(ultimaApertura)) return null
            // E5: el periodo de inactividad ya se avisó
            if (estado.inactividadAvisada == ultimaApertura) return null

            val diasSinAbrir = ChronoUnit.DAYS.between(ultimaApertura, hoy)
            val abrioHoy = diasSinAbrir == 0L
            val actividadHoy = racha.ultimaActividad == hoy

            val aplican = buildList {
                if (diasSinAbrir >= ReglasCuervos.DIAS_INACTIVIDAD) add(TipoNotificacion.INACTIVIDAD)
                if (racha.actual > 0 && !actividadHoy) add(TipoNotificacion.RACHA_EN_RIESGO)
                if (!abrioHoy) add(TipoNotificacion.RECOMPENSA_DIARIA)
            }
            return aplican.minByOrNull { it.prioridad }
        }
    }
}

/** Valores del MOD-04. */
object ReglasCuervos {
    const val HORA_POR_DEFECTO = 19
    const val MINUTO_POR_DEFECTO = 0
    const val DIAS_INACTIVIDAD = 3
    const val MAXIMO_POR_DIA = 1
}

/** Tipos de notificación del MOD-04, de mayor a menor prioridad. */
enum class TipoNotificacion(val prioridad: Int, val titulo: String, val mensaje: String) {
    INACTIVIDAD(1, "Huginn te busca", "Huginn no te ha visto en 3 días. El roadmap sigue esperándote."),
    RACHA_EN_RIESGO(2, "Tu racha está en riesgo", "Sköll se acerca: termina una actividad hoy para que Sól no se apague."),
    RECOMPENSA_DIARIA(3, "Tu hidromiel del día", "Muninn trae noticias: tu hidromiel del día te espera.")
}

/** Configuración del alumno (Ajustes): activado o desactivado y hora del recordatorio. */
data class PreferenciasCuervos(
    val activadas: Boolean = true,
    val hora: Int = ReglasCuervos.HORA_POR_DEFECTO,
    val minuto: Int = ReglasCuervos.MINUTO_POR_DEFECTO
) {
    fun normalizadas() = copy(hora = hora.coerceIn(0, 23), minuto = minuto.coerceIn(0, 59))
    val textoHora: String get() = "%02d:%02d".format(hora, minuto)
}

/** Lo que el módulo recuerda entre días. */
data class EstadoCuervos(
    val ultimaApertura: LocalDate? = null,
    val ultimoEnvio: LocalDate? = null,
    /** Apertura cuyo periodo de inactividad ya se avisó (RN-23). */
    val inactividadAvisada: LocalDate? = null,
    val permisoSolicitado: Boolean = false
)

/** Estado de la racha del día que consulta el módulo (MOD-05). */
data class SituacionRacha(val actual: Int, val ultimaActividad: LocalDate?)

/** Persistencia de las preferencias y del estado del MOD-04. */
interface AlmacenCuervos {
    fun leerPreferencias(): PreferenciasCuervos
    fun guardarPreferencias(preferencias: PreferenciasCuervos)
    fun leerEstado(): EstadoCuervos
    fun guardarEstado(estado: EstadoCuervos)
}

/** Programa la siguiente revisión a la hora configurada (en Android, con AlarmManager). */
interface ProgramadorCuervos {
    fun programar(momento: LocalDateTime)
    fun cancelar()
}
