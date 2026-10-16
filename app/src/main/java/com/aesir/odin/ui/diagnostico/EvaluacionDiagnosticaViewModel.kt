package com.aesir.odin.ui.diagnostico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.model.RespuestaDiagnostica
import com.aesir.odin.domain.model.ResultadoDiagnostico
import com.aesir.odin.domain.usecase.diagnostico.FinalizarDiagnosticoUseCase
import com.aesir.odin.domain.usecase.diagnostico.ObtenerPreguntasDiagnosticoUseCase
import com.aesir.odin.domain.usecase.diagnostico.OmitirDiagnosticoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerTemasPorMundoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CU-10 Realizar evaluación diagnóstico (EvaluacionDiagnosticaViewModel del
 * diagrama de clases).
 *
 * - iniciar: carga las 10 preguntas del mundo (RF-82).
 * - responder: registra la respuesta y avanza sin dar retroalimentación
 *   (RF-83, RF-84, A4.1). Con la última, finaliza (RF-86 a RF-91).
 * - solicitarOmision / confirmarOmision / cancelarOmision: A1.1 y A1.2.
 * - abandonar: descarta lo respondido sin registrar nada (A6.1, RF-96, RF-97).
 *
 * Además del diagrama: el mundo y los nombres de los temas, para el
 * encabezado y para mostrar qué temas se reconocieron (RF-91).
 */
class EvaluacionDiagnosticaViewModel(
    private val obtenerPreguntasUseCase: ObtenerPreguntasDiagnosticoUseCase,
    private val finalizarUseCase: FinalizarDiagnosticoUseCase,
    private val omitirUseCase: OmitirDiagnosticoUseCase,
    private val obtenerMundoUseCase: ObtenerMundoUseCase? = null,
    private val obtenerTemasPorMundoUseCase: ObtenerTemasPorMundoUseCase? = null
) : ViewModel() {

    private val _preguntas = MutableStateFlow<List<PreguntaDiagnostica>>(emptyList())
    val preguntas: StateFlow<List<PreguntaDiagnostica>> = _preguntas.asStateFlow()

    private val _indiceActual = MutableStateFlow(0)
    val indiceActual: StateFlow<Int> = _indiceActual.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoDiagnostico?>(null)
    val resultado: StateFlow<ResultadoDiagnostico?> = _resultado.asStateFlow()

    private val _confirmandoOmision = MutableStateFlow(false)
    val confirmandoOmision: StateFlow<Boolean> = _confirmandoOmision.asStateFlow()

    /** true cuando la omisión quedó registrada; la pantalla navega al roadmap. */
    private val _omitida = MutableStateFlow(false)
    val omitida: StateFlow<Boolean> = _omitida.asStateFlow()

    private val _mundo = MutableStateFlow<Mundo?>(null)
    val mundo: StateFlow<Mundo?> = _mundo.asStateFlow()

    /** Nombre de cada tema del mundo, por id. */
    private val _nombresTemas = MutableStateFlow<Map<String, String>>(emptyMap())
    val nombresTemas: StateFlow<Map<String, String>> = _nombresTemas.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private var mundoId: String = ""
    private val respuestas = mutableListOf<RespuestaDiagnostica>()
    private var finalizando = false

    fun iniciar(mundoId: String) {
        if (this.mundoId == mundoId && _preguntas.value.isNotEmpty()) return // ya iniciado (p. ej. al rotar)
        this.mundoId = mundoId
        respuestas.clear()
        _indiceActual.value = 0
        viewModelScope.launch {
            _mundo.value = runCatching { obtenerMundoUseCase?.invoke(mundoId) }.getOrNull()
            _nombresTemas.value = runCatching {
                obtenerTemasPorMundoUseCase?.invoke(mundoId).orEmpty().associate { it.id to it.nombre }
            }.getOrDefault(emptyMap())
            val preguntas = obtenerPreguntasUseCase(mundoId)
            if (preguntas.isEmpty()) _mensajeError.value = "Este mundo todavía no tiene preguntas de diagnóstico."
            _preguntas.value = preguntas
        }
    }

    /** RF-83 / RF-84: guarda la selección de la pregunta actual y avanza. */
    fun responder(opciones: List<Int>) {
        val pregunta = _preguntas.value.getOrNull(_indiceActual.value) ?: return
        if (finalizando || opciones.isEmpty()) return
        respuestas += RespuestaDiagnostica(pregunta.id, opciones.distinct().sorted())
        _indiceActual.value += 1

        if (_indiceActual.value >= _preguntas.value.size) {
            finalizando = true
            viewModelScope.launch {
                _resultado.value = finalizarUseCase(mundoId, _preguntas.value, respuestas.toList())
            }
        }
    }

    fun solicitarOmision() {
        _confirmandoOmision.value = true
    }

    /** A1.1: registra la omisión; los niveles se quedan en su estado inicial. */
    fun confirmarOmision() {
        viewModelScope.launch {
            omitirUseCase(mundoId)
            _confirmandoOmision.value = false
            _omitida.value = true
        }
    }

    /** A1.2: la evaluación sigue disponible. */
    fun cancelarOmision() {
        _confirmandoOmision.value = false
    }

    /**
     * A6.1: descarta las respuestas y el avance. No se registra nada, así que
     * al volver a entrar la evaluación empieza desde la primera pregunta (RF-97).
     */
    fun abandonar() {
        respuestas.clear()
        _indiceActual.value = 0
        _preguntas.value = emptyList()
        mundoId = ""
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EvaluacionDiagnosticaViewModel(
                    obtenerPreguntasUseCase = container.obtenerPreguntasDiagnosticoUseCase,
                    finalizarUseCase = container.finalizarDiagnosticoUseCase,
                    omitirUseCase = container.omitirDiagnosticoUseCase,
                    obtenerMundoUseCase = container.obtenerMundoUseCase,
                    obtenerTemasPorMundoUseCase = container.obtenerTemasPorMundoUseCase
                )
            }
        }
    }
}
