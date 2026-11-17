package com.aesir.odin.data.notificaciones

import android.content.SharedPreferences
import com.aesir.odin.domain.notificaciones.AlmacenCuervos
import com.aesir.odin.domain.notificaciones.EstadoCuervos
import com.aesir.odin.domain.notificaciones.PreferenciasCuervos
import java.time.LocalDate

/** MOD-04: la configuración y lo que recuerda el módulo se guardan entre sesiones. */
class AlmacenCuervosImpl(private val preferencias: SharedPreferences) : AlmacenCuervos {

    override fun leerPreferencias(): PreferenciasCuervos = PreferenciasCuervos(
        activadas = preferencias.getBoolean(CLAVE_ACTIVADAS, true),
        hora = preferencias.getInt(CLAVE_HORA, PreferenciasCuervos().hora),
        minuto = preferencias.getInt(CLAVE_MINUTO, PreferenciasCuervos().minuto)
    )

    override fun guardarPreferencias(preferencias: PreferenciasCuervos) {
        this.preferencias.edit()
            .putBoolean(CLAVE_ACTIVADAS, preferencias.activadas)
            .putInt(CLAVE_HORA, preferencias.hora)
            .putInt(CLAVE_MINUTO, preferencias.minuto)
            .apply()
    }

    override fun leerEstado(): EstadoCuervos = EstadoCuervos(
        ultimaApertura = fecha(CLAVE_ULTIMA_APERTURA),
        ultimoEnvio = fecha(CLAVE_ULTIMO_ENVIO),
        inactividadAvisada = fecha(CLAVE_INACTIVIDAD_AVISADA),
        permisoSolicitado = preferencias.getBoolean(CLAVE_PERMISO_SOLICITADO, false)
    )

    override fun guardarEstado(estado: EstadoCuervos) {
        preferencias.edit()
            .putFecha(CLAVE_ULTIMA_APERTURA, estado.ultimaApertura)
            .putFecha(CLAVE_ULTIMO_ENVIO, estado.ultimoEnvio)
            .putFecha(CLAVE_INACTIVIDAD_AVISADA, estado.inactividadAvisada)
            .putBoolean(CLAVE_PERMISO_SOLICITADO, estado.permisoSolicitado)
            .apply()
    }

    private fun fecha(clave: String): LocalDate? =
        if (preferencias.contains(clave)) LocalDate.ofEpochDay(preferencias.getLong(clave, 0)) else null

    private fun SharedPreferences.Editor.putFecha(clave: String, fecha: LocalDate?): SharedPreferences.Editor =
        if (fecha == null) remove(clave) else putLong(clave, fecha.toEpochDay())

    private companion object {
        const val CLAVE_ACTIVADAS = "cuervos_activadas"
        const val CLAVE_HORA = "cuervos_hora"
        const val CLAVE_MINUTO = "cuervos_minuto"
        const val CLAVE_ULTIMA_APERTURA = "cuervos_ultima_apertura"
        const val CLAVE_ULTIMO_ENVIO = "cuervos_ultimo_envio"
        const val CLAVE_INACTIVIDAD_AVISADA = "cuervos_inactividad_avisada"
        const val CLAVE_PERMISO_SOLICITADO = "cuervos_permiso_solicitado"
    }
}
