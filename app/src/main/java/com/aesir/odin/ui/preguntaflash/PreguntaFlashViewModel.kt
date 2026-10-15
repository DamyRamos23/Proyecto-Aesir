package com.aesir.odin.ui.preguntaflash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.EstadoPreguntaFlash
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.ResultadoPreguntaFlash
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.preguntaflash.ObtenerPreguntaFlashUseCase
import com.aesir.odin.domain.usecase.preguntaflash.ObservarHidromielUseCase
import com.aesir.odin.domain.usecase.preguntaflash.RegistrarTiempoAgotadoPreguntaFlashUseCase
import com.aesir.odin.domain.usecase.preguntaflash.ValidarRespuestaPreguntaFlashUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.ui.navigation.OdinNavGraph
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * CU-06 Responder pregunta flash.
 *
 * Vive a nivel de la actividad (no de una pantalla): cuenta cada [INTERVALO_MS]
 * de uso de la app (RF-31), espera a que el alumno esté en Mundos o Roadmap
 * (RF-39), muestra la pregunta con un contador de [TIEMPO_LIMITE_S] segundos
 * (RF-40), valida o registra el tiempo agotado, y al cerrar reinicia el
 * temporizador (RF-43, RF-44).
 *
 * El tiempo "de uso" se detiene mientras la app está en segundo plano
 * ([pausar] / [reanudar]).
 */
class PreguntaFlashViewModel(
    private val obtenerUseCase: ObtenerPreguntaFlashUseCase,
    private val validarUseCase: ValidarRespuestaPreguntaFlashUseCase,
    private val tiempoAgotadoUseCase: RegistrarTiempoAgotadoPreguntaFlashUseCase,
    private val observarHidromielUseCase: ObservarHidromielUseCase,
    private val obtenerTemaUseCase: ObtenerIntroduccionTemaUseCase? = null,
    private val obtenerMundoUseCase: ObtenerMundoUseCase? = null,
    private val reloj: () -> Long = System::currentTimeMillis
) : ViewModel() {

    /** Tema y mundo de la pregunta, solo para el estilo visual de la pantalla. */
    data class ContextoPregunta(val tema: Tema, val mundo: Mundo)

    private val _pregunta = MutableStateFlow<PreguntaFlash?>(null)
    private val _seleccionadas = MutableStateFlow<List<Int>>(emptyList())
    private val _segundosRestantes = MutableStateFlow(TIEMPO_LIMITE_S)
    private val _resultado = MutableStateFlow<ResultadoPreguntaFlash?>(null)
    private val _hidromiel = MutableStateFlow(0)
    private val _contexto = MutableStateFlow<ContextoPregunta?>(null)
    private val _racha = MutableStateFlow(0)

    val pregunta: StateFlow<PreguntaFlash?> = _pregunta.asStateFlow()
    /** Índices ORIGINALES de las opciones elegidas. */
    val seleccionadas: StateFlow<List<Int>> = _seleccionadas.asStateFlow()
    val segundosRestantes: StateFlow<Int> = _segundosRestantes.asStateFlow()
    val resultado: StateFlow<ResultadoPreguntaFlash?> = _resultado.asStateFlow()
    /** Saldo de hidromiel: los puntos de la pregunta flash son hidromiel (MOD-01). */
    val hidromiel: StateFlow<Int> = _hidromiel.asStateFlow()
    val contexto: StateFlow<ContextoPregunta?> = _contexto.asStateFlow()
    /** Preguntas flash acertadas seguidas en esta sesión (se reinicia al fallar). */
    val racha: StateFlow<Int> = _racha.asStateFlow()

    private var enPantallaNavegacion = false
    private var preguntaPendiente = false
    private var tiempoInicio = 0L
    private var inicioPregunta = 0L
    private var pausadoEn: Long? = null
    private var cargando = false
    private var procesando = false
    private var bucleJob: Job? = null
    private var contadorJob: Job? = null

    init {
        // El saldo también cambia fuera de la pregunta flash (niveles, recompensa diaria...).
        viewModelScope.launch {
            observarHidromielUseCase().catch { }.collect { _hidromiel.value = it }
        }
    }

    /** Arranca (una sola vez) el conteo de [INTERVALO_MS] de uso. */
    fun iniciarTemporizador() {
        if (bucleJob?.isActive == true) return
        tiempoInicio = reloj()
        bucleJob = viewModelScope.launch {
            while (isActive) {
                delay(UN_SEGUNDO_MS)
                revisarTemporizador()
            }
        }
    }

    /** La app pasó a segundo plano: se congela el tiempo de uso y el contador. */
    fun pausar() {
        if (pausadoEn == null) pausadoEn = reloj()
    }

    fun reanudar() {
        val desde = pausadoEn ?: return
        val perdido = reloj() - desde
        tiempoInicio += perdido
        inicioPregunta += perdido
        pausadoEn = null
    }

    /** [ruta] es la ruta de destino actual del NavHost (null si aún no hay). */
    fun onPantallaCambiada(ruta: String?) {
        enPantallaNavegacion = ruta != null &&
            (ruta == OdinNavGraph.rutaMundos ||
                ruta == OdinNavGraph.rutaRoadmap ||
                ruta.startsWith("${OdinNavGraph.rutaRoadmap}/"))
        // Flujo alternativo A2.1: la pregunta pospuesta sale al volver a navegación.
        if (enPantallaNavegacion && preguntaPendiente) mostrarPregunta()
    }

    fun alternarOpcion(indice: Int) {
        val actual = _pregunta.value ?: return
        if (_resultado.value != null || procesando) return
        if (indice !in actual.opciones.indices) return
        _seleccionadas.value = when {
            !actual.esSeleccionMultiple -> listOf(indice)
            indice in _seleccionadas.value -> _seleccionadas.value - indice
            else -> _seleccionadas.value + indice
        }
    }

    fun confirmar() {
        val actual = _pregunta.value ?: return
        if (_resultado.value != null || procesando) return
        val elegidas = _seleccionadas.value.sorted()
        if (elegidas.isEmpty() || _segundosRestantes.value <= 0) return

        procesando = true
        contadorJob?.cancel()
        val tiempoMs = (reloj() - inicioPregunta).coerceAtLeast(0L)
        viewModelScope.launch {
            val res = validarUseCase(actual, elegidas, tiempoMs)
            _racha.value = if (res.estado == EstadoPreguntaFlash.CORRECTA) _racha.value + 1 else 0
            _resultado.value = res
            _hidromiel.value = res.totalPuntos
            procesando = false
        }
    }

    /** El alumno cierra el resultado: vuelve a la pantalla donde estaba y se reinicia el ciclo. */
    fun cerrar() {
        if (_resultado.value == null) return
        _pregunta.value = null
        _resultado.value = null
        _seleccionadas.value = emptyList()
        _contexto.value = null
        tiempoInicio = reloj()
        preguntaPendiente = false
    }

    // ---------------------------------------------------------------------
    // Internos
    // ---------------------------------------------------------------------

    private fun revisarTemporizador() {
        if (pausadoEn != null || _pregunta.value != null || cargando) return
        if (!preguntaPendiente && reloj() - tiempoInicio >= INTERVALO_MS) preguntaPendiente = true
        if (preguntaPendiente && enPantallaNavegacion) mostrarPregunta()
    }

    private fun mostrarPregunta() {
        if (cargando || _pregunta.value != null) return
        cargando = true
        viewModelScope.launch {
            val elegida = runCatching { obtenerUseCase() }.getOrNull()
            if (elegida == null) {
                // Aún no hay tema completado (o sin preguntas): se vuelve a intentar en otro ciclo.
                preguntaPendiente = false
                tiempoInicio = reloj()
                cargando = false
                return@launch
            }
            cargarContexto(elegida.temaId)
            if (!enPantallaNavegacion) {
                // El alumno entró a una actividad mientras se cargaba: se pospone (A2.1).
                cargando = false
                return@launch
            }
            _seleccionadas.value = emptyList()
            _resultado.value = null
            _segundosRestantes.value = TIEMPO_LIMITE_S
            inicioPregunta = reloj()
            preguntaPendiente = false
            _pregunta.value = elegida
            cargando = false
            iniciarContador()
        }
    }

    private suspend fun cargarContexto(temaId: String) {
        val obtenerTema = obtenerTemaUseCase ?: return
        val obtenerMundo = obtenerMundoUseCase ?: return
        _contexto.value = runCatching {
            val tema = obtenerTema(temaId) ?: return
            ContextoPregunta(tema, obtenerMundo(tema.mundoId))
        }.getOrNull()
    }

    private fun iniciarContador() {
        contadorJob?.cancel()
        contadorJob = viewModelScope.launch {
            while (isActive && _segundosRestantes.value > 0) {
                delay(UN_SEGUNDO_MS)
                if (pausadoEn == null) _segundosRestantes.value -= 1
            }
            if (isActive) onTiempoAgotado()
        }
    }

    private fun onTiempoAgotado() {
        val actual = _pregunta.value ?: return
        if (_resultado.value != null || procesando) return
        procesando = true
        viewModelScope.launch {
            val res = tiempoAgotadoUseCase(actual)
            _racha.value = 0
            _resultado.value = res
            _hidromiel.value = res.totalPuntos
            procesando = false
        }
    }

    companion object {
        /**
         * Cada cuánto tiempo de uso de la app sale una pregunta flash.
         * Para cambiarlo, edita este valor en milisegundos (60_000L = 1 minuto,
         * 300_000L = 5 minutos).
         */
        const val INTERVALO_MS = 60_000L
        const val TIEMPO_LIMITE_S = 10
        private const val UN_SEGUNDO_MS = 1_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PreguntaFlashViewModel(
                        obtenerUseCase = container.obtenerPreguntaFlashUseCase,
                        validarUseCase = container.validarRespuestaPreguntaFlashUseCase,
                        tiempoAgotadoUseCase = container.registrarTiempoAgotadoPreguntaFlashUseCase,
                        observarHidromielUseCase = container.observarHidromielUseCase,
                        obtenerTemaUseCase = container.obtenerIntroduccionTemaUseCase,
                        obtenerMundoUseCase = container.obtenerMundoUseCase
                    )
                }
            }
    }
}
