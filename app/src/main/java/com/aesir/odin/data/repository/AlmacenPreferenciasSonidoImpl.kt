package com.aesir.odin.data.repository

import android.content.SharedPreferences
import com.aesir.odin.domain.sonido.AlmacenPreferenciasSonido
import com.aesir.odin.domain.sonido.PreferenciasSonido

/** MOD-02, RN-15: las preferencias de sonido se conservan entre sesiones. */
class AlmacenPreferenciasSonidoImpl(private val preferencias: SharedPreferences) : AlmacenPreferenciasSonido {

    override fun leer(): PreferenciasSonido? {
        if (!preferencias.contains(CLAVE_ACTIVADO)) return null
        return PreferenciasSonido(
            activado = preferencias.getBoolean(CLAVE_ACTIVADO, true),
            volumen = preferencias.getFloat(CLAVE_VOLUMEN, PreferenciasSonido.VOLUMEN_PREDETERMINADO),
            musica = preferencias.getBoolean(CLAVE_MUSICA, true),
            volumenMusica = preferencias.getFloat(CLAVE_VOLUMEN_MUSICA, PreferenciasSonido.VOLUMEN_MUSICA_PREDETERMINADO)
        )
    }

    override fun guardar(preferencias: PreferenciasSonido): Boolean =
        this.preferencias.edit()
            .putBoolean(CLAVE_ACTIVADO, preferencias.activado)
            .putFloat(CLAVE_VOLUMEN, preferencias.volumen)
            .putBoolean(CLAVE_MUSICA, preferencias.musica)
            .putFloat(CLAVE_VOLUMEN_MUSICA, preferencias.volumenMusica)
            .commit()

    private companion object {
        const val CLAVE_ACTIVADO = "sonido_activado"
        const val CLAVE_VOLUMEN = "sonido_volumen"
        const val CLAVE_MUSICA = "musica_activada"
        const val CLAVE_VOLUMEN_MUSICA = "musica_volumen"
    }
}
