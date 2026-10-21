package com.aesir.odin.data.sonido

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.aesir.odin.domain.sonido.EventoSonido
import com.aesir.odin.domain.sonido.ModuloSonido
import com.aesir.odin.domain.sonido.ReproductorEfectos
import java.util.concurrent.ConcurrentHashMap

/**
 * MOD-02 en Android: SoundPool con los efectos de res/raw (.ogg). Solo usa el
 * volumen de efectos del juego; nunca toca el volumen del dispositivo (E5).
 */
class ReproductorSoundPool(private val context: Context) : ReproductorEfectos {

    private val pool = SoundPool.Builder()
        .setMaxStreams(ModuloSonido.MAXIMO_SIMULTANEOS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    /** Efecto → id de SoundPool, solo de los que ya terminaron de cargar. */
    private val listos = ConcurrentHashMap<EventoSonido, Int>()
    private val cargando = ConcurrentHashMap<Int, EventoSonido>()
    private val sonando = ConcurrentHashMap.newKeySet<Int>()

    init {
        pool.setOnLoadCompleteListener { _, idSonido, estado ->
            val evento = cargando.remove(idSonido) ?: return@setOnLoadCompleteListener
            if (estado == 0) listos[evento] = idSonido
            else Log.w(TAG, "No se pudo cargar ${evento.archivo} (estado $estado)")
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun cargar(catalogo: List<EventoSonido>) {
        for (evento in catalogo) {
            if (listos.containsKey(evento) || cargando.containsValue(evento)) continue
            val recurso = context.resources.getIdentifier(evento.archivo, "raw", context.packageName)
            if (recurso == 0) {
                Log.w(TAG, "Falta el efecto ${evento.archivo}")
                continue
            }
            cargando[pool.load(context, recurso, 1)] = evento
        }
    }

    override fun reproducir(evento: EventoSonido, volumen: Float, velocidad: Float): Int? {
        // E2: si no se encontró o aún no carga, no suena nada y la actividad sigue
        val id = listos[evento] ?: return null
        val flujo = pool.play(id, volumen, volumen, evento.prioridad, 0, velocidad.coerceIn(0.5f, 2f))
        if (flujo == 0) return null
        sonando.add(flujo)
        return flujo
    }

    override fun detener(id: Int) {
        pool.stop(id)
        sonando.remove(id)
    }

    override fun detenerTodos() {
        sonando.forEach { pool.stop(it) }
        sonando.clear()
        pool.autoPause()
    }

    private companion object {
        const val TAG = "ModuloSonido"
    }
}
