package com.aesir.odin.data.repository

import android.content.SharedPreferences
import com.aesir.odin.domain.eastereggs.AlmacenEastereggs
import com.aesir.odin.domain.eastereggs.JuegoOculto

/** Juegos ocultos descubiertos, en las mismas preferencias que el modo de lectura. */
class AlmacenEastereggsImpl(private val preferencias: SharedPreferences) : AlmacenEastereggs {

    override fun descubiertos(): Set<JuegoOculto> =
        preferencias.getStringSet(CLAVE, emptySet()).orEmpty()
            .mapNotNull { nombre -> JuegoOculto.entries.firstOrNull { it.name == nombre } }
            .toSet()

    override fun descubrir(juego: JuegoOculto) {
        preferencias.edit().putStringSet(CLAVE, descubiertos().map { it.name }.toSet() + juego.name).apply()
    }

    private companion object {
        const val CLAVE = "eastereggs_descubiertos"
    }
}
