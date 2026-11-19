package com.aesir.odin.domain.sonido

import com.aesir.odin.domain.hidromiel.AvisoHidromiel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MOD-02 Sistema de efectos de sonido. Cada caso de uso solo avisa qué evento
 * ocurrió ([avisar]); el módulo decide qué efecto suena y cómo.
 *
 * - Paso 1: [alAbrirAplicacion] carga el catálogo y las preferencias.
 * - Pasos 2-6: [avisar] busca el efecto, respeta el sonido desactivado (E1), la
 *   separación mínima (E4) y el máximo de efectos simultáneos (calla el de
 *   menor prioridad), y nunca detiene la actividad (RN-13).
 * - Paso 7: [cambiarPreferencias] aplica y guarda las preferencias.
 * - Paso 8 y E3: [alPasarASegundoPlano] / [alInterrumpir] detienen lo que suena.
 *
 * Además lleva la música de fondo (el tema principal en bucle): suena mientras la
 * app está en primer plano y la música está activada; se pausa en segundo plano
 * o si el sistema interrumpe el audio, y vuelve donde se quedó.
 */
class ModuloSonido(
    private val almacen: AlmacenPreferenciasSonido,
    private val reproductor: ReproductorEfectos,
    private val musica: ReproductorMusica? = null,
    private val ahoraMs: () -> Long = { System.nanoTime() / 1_000_000 },
    private val registrarError: (String) -> Unit = {}
) {
    private class Sonando(val id: Int, val evento: EventoSonido, val inicioMs: Long)

    private val _preferencias = MutableStateFlow(PreferenciasSonido())
    val preferencias: StateFlow<PreferenciasSonido> = _preferencias.asStateFlow()

    private val sonando = ArrayList<Sonando>()
    private val ultimaVez = HashMap<EventoSonido, Long>()
    private var enSegundoPlano = false

    /** Paso 1. Si la preferencia no se puede leer, quedan las predeterminadas. */
    fun alAbrirAplicacion() {
        _preferencias.value = runCatching { almacen.leer() }.getOrNull() ?: PreferenciasSonido()
        runCatching { reproductor.cargar(EventoSonido.entries) }
            .onFailure { registrarError("No se pudo cargar el catálogo de efectos: ${it.message}") }
    }

    /** Pasos 2-5: un solo evento. */
    @Synchronized
    fun avisar(evento: EventoSonido, velocidad: Float = 1f) {
        if (!puedeSonar()) return
        reproducirUno(evento, velocidad)
    }

    /** Paso 6: varios efectos de un mismo evento, del de mayor prioridad al de menor. */
    @Synchronized
    fun avisar(eventos: List<EventoSonido>) {
        if (!puedeSonar()) return
        eventos.sortedByDescending { it.prioridad }.forEach { reproducirUno(it, 1f) }
    }

    /** Disparador 3: un golpe de tambor por segundo, cada vez más rápido, en los últimos 5 s. */
    fun avisarTiempoRestante(segundos: Int) {
        if (segundos !in 1..SEGUNDOS_AVISO_TIEMPO) return
        avisar(EventoSonido.TAMBOR, velocidad = 1f + (SEGUNDOS_AVISO_TIEMPO - segundos) * 0.08f)
    }

    /** Disparador 4: el MOD-01 sumó o descontó hidromiel. */
    fun avisarHidromiel(aviso: AvisoHidromiel) {
        when {
            aviso.cantidad > 0 -> avisar(EventoSonido.HIDROMIEL_GANA)
            aviso.cantidad < 0 -> avisar(EventoSonido.HIDROMIEL_PIERDE)
        }
    }

    /** Paso 7 (RN-15): se aplican de inmediato y se guardan. */
    @Synchronized
    fun cambiarPreferencias(nuevas: PreferenciasSonido) {
        val limpias = nuevas.copy(
            volumen = nuevas.volumen.coerceIn(0f, 1f),
            volumenMusica = nuevas.volumenMusica.coerceIn(0f, 1f)
        )
        _preferencias.value = limpias
        if (!limpias.activado) detenerTodos()
        actualizarMusica()
        runCatching { almacen.guardar(limpias) }
            .onFailure { registrarError("No se pudieron guardar las preferencias de sonido: ${it.message}") }
    }

    /** Paso 8: la app pasa a segundo plano; lo que sonaba no se reanuda al volver. */
    @Synchronized
    fun alPasarASegundoPlano() {
        enSegundoPlano = true
        detenerTodos()
        runCatching { musica?.pausar() }
    }

    @Synchronized
    fun alVolverAPrimerPlano() {
        enSegundoPlano = false
        actualizarMusica()
    }

    /** E3: una llamada u otra app tomó el audio. */
    @Synchronized
    fun alInterrumpir() {
        detenerTodos()
        runCatching { musica?.pausar() }
    }

    /** Pista que corresponde a la pantalla actual (la elige la navegación). */
    var pista: PistaMusica = PistaMusica.PRINCIPAL
        private set

    /** La navegación avisa a qué tipo de pantalla se llegó; la música cambia con un fundido. */
    @Synchronized
    fun cambiarPista(nueva: PistaMusica) {
        if (nueva == pista) return
        pista = nueva
        actualizarMusica()
    }

    /** Suena la música si está activada y la app está al frente; si no, se pausa. */
    private fun actualizarMusica() {
        val p = _preferencias.value
        runCatching {
            if (p.musica && !enSegundoPlano) musica?.reproducir(pista, p.volumenMusica) else musica?.pausar()
        }.onFailure { registrarError("No se pudo reproducir la música: ${it.message}") }
    }

    /** Cuántos efectos siguen sonando (para las pruebas). */
    @Synchronized
    fun efectosSonando(): Int {
        limpiarTerminados(ahoraMs())
        return sonando.size
    }

    private fun puedeSonar(): Boolean = _preferencias.value.activado && !enSegundoPlano

    private fun reproducirUno(evento: EventoSonido, velocidad: Float) {
        val ahora = ahoraMs()
        limpiarTerminados(ahora)
        // E4: el mismo efecto pedido antes de la separación mínima se ignora
        val anterior = ultimaVez[evento]
        if (anterior != null && ahora - anterior < SEPARACION_MINIMA_MS) return
        ultimaVez[evento] = ahora
        // Paso 5: si ya hay el máximo sonando, se calla el de menor prioridad
        if (sonando.size >= MAXIMO_SIMULTANEOS) {
            val menor = sonando.minWith(compareBy<Sonando>({ it.evento.prioridad }, { it.inicioMs }))
            if (menor.evento.prioridad > evento.prioridad) return
            runCatching { reproductor.detener(menor.id) }
            sonando.remove(menor)
        }
        // E2 / RN-13: si falla, se registra y la actividad sigue
        val id = runCatching { reproductor.reproducir(evento, _preferencias.value.volumen, velocidad) }.getOrNull()
        if (id == null) {
            registrarError("No se pudo reproducir el efecto ${evento.archivo}")
            return
        }
        sonando.add(Sonando(id, evento, ahora))
    }

    private fun limpiarTerminados(ahora: Long) {
        sonando.removeAll { ahora - it.inicioMs >= minOf(it.evento.duracionMs, it.evento.categoria.duracionMaximaMs) }
    }

    private fun detenerTodos() {
        runCatching { reproductor.detenerTodos() }
        sonando.clear()
    }

    companion object {
        const val MAXIMO_SIMULTANEOS = 4
        const val SEPARACION_MINIMA_MS = 100L
        const val SEGUNDOS_AVISO_TIEMPO = 5

        /**
         * Instancia de la app (la pone el AppContainer). Los casos de uso avisan
         * por aquí sus eventos; en las pruebas es null y no suena nada.
         */
        @Volatile
        var compartido: ModuloSonido? = null

        fun avisar(evento: EventoSonido) {
            compartido?.avisar(evento)
        }

        fun avisar(vararg eventos: EventoSonido) {
            compartido?.avisar(eventos.toList())
        }
    }
}
