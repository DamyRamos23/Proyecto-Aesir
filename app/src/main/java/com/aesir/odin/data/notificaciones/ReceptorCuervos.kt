package com.aesir.odin.data.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aesir.odin.data.local.OdinDatabase
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.notificaciones.ModuloCuervos
import com.aesir.odin.domain.notificaciones.SituacionRacha
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * MOD-04: recibe la alarma de la hora del recordatorio (disparadores 1 y 2) y,
 * al reiniciar el teléfono o actualizar la app, vuelve a programarla.
 */
class ReceptorCuervos : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val contexto = context.applicationContext
        val modulo = crearModulo(contexto)
        when (intent.action) {
            ACCION_HORA -> {
                val pendiente = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val aviso = modulo.alLlegarLaHora(EstadoApp.enPrimerPlano, racha(contexto))
                        if (aviso != null) NotificadorCuervos.mostrar(contexto, aviso)
                    } finally {
                        pendiente.finish()
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> modulo.alReiniciarDispositivo()
        }
    }

    /** Estado de la racha del día (MOD-05) leído de la base de datos local. */
    private suspend fun racha(contexto: Context): SituacionRacha {
        val fila = runCatching { OdinDatabase.getInstance(contexto).rachaDao().leer(USUARIO_LOCAL) }.getOrNull()
        return SituacionRacha(
            actual = fila?.actual ?: 0,
            ultimaActividad = fila?.ultimaActividad?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        )
    }

    companion object {
        const val ACCION_HORA = "com.aesir.odin.CUERVOS_HORA"

        fun crearModulo(contexto: Context) = ModuloCuervos(
            AlmacenCuervosImpl(contexto.getSharedPreferences("odin", Context.MODE_PRIVATE)),
            ProgramadorCuervosAlarma(contexto)
        )
    }
}

/** E4: si la app está en primer plano a la hora del aviso, no se envía. */
object EstadoApp {
    @Volatile
    var enPrimerPlano: Boolean = false
}
