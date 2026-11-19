package com.aesir.odin.data.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aesir.odin.MainActivity
import com.aesir.odin.R
import com.aesir.odin.domain.notificaciones.TipoNotificacion

/** MOD-04: muestra el aviso de los cuervos; al tocarlo se abre la app en Mundos (paso 4). */
object NotificadorCuervos {

    private const val CANAL = "cuervos_de_odin"
    private const val ID_NOTIFICACION = 1902

    /** E1: sin permiso del sistema no se envía nada y la app sigue igual. */
    fun tienePermiso(contexto: Context): Boolean =
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(contexto).areNotificationsEnabled()

    fun mostrar(contexto: Context, tipo: TipoNotificacion) {
        if (!tienePermiso(contexto)) return
        crearCanal(contexto)
        // La pantalla de inicio de la app es Mundos
        val abrir = PendingIntent.getActivity(
            contexto,
            0,
            Intent(contexto, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion_cuervo)
            .setContentTitle(tipo.titulo)
            .setContentText(tipo.mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tipo.mensaje))
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching { NotificationManagerCompat.from(contexto).notify(ID_NOTIFICACION, notificacion) }
    }

    fun cancelarVisibles(contexto: Context) {
        NotificationManagerCompat.from(contexto).cancel(ID_NOTIFICACION)
    }

    private fun crearCanal(contexto: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val gestor = contexto.getSystemService(NotificationManager::class.java) ?: return
        if (gestor.getNotificationChannel(CANAL) != null) return
        gestor.createNotificationChannel(
            NotificationChannel(CANAL, "Cuervos de Odín", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Recordatorios de estudio: racha en riesgo, hidromiel del día y regreso al roadmap."
            }
        )
    }
}
