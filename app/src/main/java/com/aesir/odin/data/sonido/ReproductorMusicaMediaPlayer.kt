package com.aesir.odin.data.sonido

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.aesir.odin.R
import com.aesir.odin.domain.sonido.PistaMusica
import com.aesir.odin.domain.sonido.ReproductorMusica

/**
 * Las pistas de res/raw/musica_*.ogg en bucle con MediaPlayer. Al cambiar de
 * pista la anterior se apaga y la nueva sube en [FUNDIDO_MS] (sin cortes).
 */
class ReproductorMusicaMediaPlayer(private val context: Context) : ReproductorMusica {

    private val hilo = Handler(Looper.getMainLooper())
    private var actual: MediaPlayer? = null
    private var pistaActual: PistaMusica? = null
    private var volumen = 0f

    private fun recurso(pista: PistaMusica): Int = when (pista) {
        PistaMusica.PRINCIPAL -> R.raw.musica_principal
        PistaMusica.BATALLA -> R.raw.musica_batalla
        PistaMusica.CONTRARRELOJ -> R.raw.musica_contrarreloj
        PistaMusica.LECCION -> R.raw.musica_leccion
        PistaMusica.VOLVA -> R.raw.musica_volva
    }

    private fun crear(pista: PistaMusica): MediaPlayer? = runCatching {
        MediaPlayer.create(
            context,
            recurso(pista),
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            0
        )?.apply { isLooping = true }
    }.onFailure { Log.w("ModuloSonido", "No se pudo cargar la música ${pista.archivo}", it) }.getOrNull()

    override fun reproducir(pista: PistaMusica, volumen: Float) {
        hilo.post {
            this.volumen = volumen
            val mp = actual
            if (mp != null && pista == pistaActual) {
                mp.setVolume(volumen, volumen)
                if (!mp.isPlaying) mp.start()
                return@post
            }
            val nueva = crear(pista) ?: return@post
            nueva.setVolume(0f, 0f)
            nueva.start()
            val vieja = actual?.takeIf { it.isPlaying }
            if (vieja == null) actual?.release()
            actual = nueva
            pistaActual = pista
            fundir(nueva, vieja)
        }
    }

    /** Sube [entra] y apaga [sale] en pasos de 50 ms. */
    private fun fundir(entra: MediaPlayer, sale: MediaPlayer?) {
        val pasos = FUNDIDO_MS / PASO_MS
        for (i in 1..pasos) {
            hilo.postDelayed({
                val f = i.toFloat() / pasos
                if (entra === actual) runCatching { entra.setVolume(volumen * f, volumen * f) }
                runCatching { sale?.setVolume(volumen * (1 - f), volumen * (1 - f)) }
                if (i == pasos) runCatching { sale?.stop(); sale?.release() }
            }, (i * PASO_MS).toLong())
        }
    }

    override fun pausar() {
        hilo.post { actual?.takeIf { it.isPlaying }?.pause() }
    }

    override fun cambiarVolumen(volumen: Float) {
        hilo.post {
            this.volumen = volumen
            actual?.setVolume(volumen, volumen)
        }
    }

    private companion object {
        const val FUNDIDO_MS = 1_200
        const val PASO_MS = 50
    }
}
