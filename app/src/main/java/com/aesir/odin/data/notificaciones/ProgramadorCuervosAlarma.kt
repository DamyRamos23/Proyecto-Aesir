package com.aesir.odin.data.notificaciones

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aesir.odin.domain.notificaciones.ProgramadorCuervos
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * MOD-04: programa la revisión diaria con AlarmManager. Es una alarma
 * inexacta (no necesita el permiso de alarmas exactas): basta con que llegue
 * cerca de la hora configurada, incluso con el teléfono en reposo.
 */
class ProgramadorCuervosAlarma(private val contexto: Context) : ProgramadorCuervos {

    override fun programar(momento: LocalDateTime) {
        val alarmas = contexto.getSystemService(AlarmManager::class.java) ?: return
        val millis = momento.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        runCatching { alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intencion()) }
    }

    override fun cancelar() {
        val alarmas = contexto.getSystemService(AlarmManager::class.java) ?: return
        alarmas.cancel(intencion())
        NotificadorCuervos.cancelarVisibles(contexto)
    }

    private fun intencion(): PendingIntent = PendingIntent.getBroadcast(
        contexto,
        CODIGO,
        Intent(contexto, ReceptorCuervos::class.java).setAction(ReceptorCuervos.ACCION_HORA),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private companion object {
        const val CODIGO = 1901
    }
}
