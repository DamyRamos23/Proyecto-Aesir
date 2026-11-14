package com.aesir.odin.data.sonido

import android.media.AudioManager
import android.media.ToneGenerator
import com.aesir.odin.domain.racha.ReproductorSonido
import com.aesir.odin.domain.racha.SonidoRacha

/**
 * Sonidos de la categoría Progreso para el MOD-05 mientras el MOD-02 no esté
 * implementado: tonos cortos del sistema. Cuando exista el MOD-02, basta con
 * pasar su reproductor en AppContainer.
 */
class ReproductorSonidoTonos : ReproductorSonido {

    private val tonos: ToneGenerator? = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 70) }.getOrNull()

    override fun reproducirProgreso(sonido: SonidoRacha) {
        val tono = when (sonido) {
            SonidoRacha.RACHA_ENCENDIDA -> ToneGenerator.TONE_PROP_ACK
            SonidoRacha.HITO_RACHA -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
        }
        runCatching { tonos?.startTone(tono, 250) }
    }
}
