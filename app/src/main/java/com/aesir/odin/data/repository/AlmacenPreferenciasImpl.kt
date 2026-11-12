package com.aesir.odin.data.repository

import android.content.SharedPreferences
import com.aesir.odin.domain.heimdall.AlmacenPreferencias
import com.aesir.odin.domain.heimdall.ModoLectura
import com.aesir.odin.domain.heimdall.PreferenciaLectura
import java.time.LocalDateTime

/** MOD-03: la preferencia de lectura vive junto al modo admin en SharedPreferences. */
class AlmacenPreferenciasImpl(private val preferencias: SharedPreferences) : AlmacenPreferencias {

    override fun leer(): PreferenciaLectura? {
        val modo = preferencias.getString(CLAVE_MODO, null) ?: return null
        return PreferenciaLectura(
            modo = ModoLectura.valueOf(modo),
            fechaCambio = LocalDateTime.parse(preferencias.getString(CLAVE_FECHA, null))
        )
    }

    override fun guardar(preferencia: PreferenciaLectura): Boolean =
        preferencias.edit()
            .putString(CLAVE_MODO, preferencia.modo.name)
            .putString(CLAVE_FECHA, preferencia.fechaCambio.toString())
            .commit()

    private companion object {
        const val CLAVE_MODO = "lectura_modo"
        const val CLAVE_FECHA = "lectura_fecha_cambio"
    }
}
