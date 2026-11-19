package com.aesir.odin.data.sonido

import com.aesir.odin.domain.racha.ReproductorSonido
import com.aesir.odin.domain.racha.SonidoRacha
import com.aesir.odin.domain.sonido.EventoSonido
import com.aesir.odin.domain.sonido.ModuloSonido

/**
 * MOD-05 → MOD-02: los sonidos de Progreso de la racha pasan por el módulo de
 * sonido (respeta el volumen y el interruptor de Ajustes). Reemplaza a los tonos
 * del sistema que se usaban mientras el MOD-02 no existía.
 */
object ReproductorRachaMod02 : ReproductorSonido {
    override fun reproducirProgreso(sonido: SonidoRacha) = when (sonido) {
        SonidoRacha.RACHA_ENCENDIDA -> ModuloSonido.avisar(EventoSonido.DESBLOQUEO)
        SonidoRacha.HITO_RACHA -> ModuloSonido.avisar(EventoSonido.LOGRO)
    }
}
