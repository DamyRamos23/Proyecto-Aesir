package com.aesir.odin.ui.retocontrareloj

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.RetoContrareloj
import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.usecase.reto.AbandonarRetoContrarrelojUseCase
import com.aesir.odin.domain.usecase.reto.ArmarRetoContrarrelojUseCase
import com.aesir.odin.domain.usecase.reto.FinalizarRetoContrarrelojUseCase
import com.aesir.odin.domain.usecase.reto.SinPreguntasDisponiblesException
import com.aesir.odin.domain.usecase.reto.SinTemasEstudiadosException
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CU-11 Resolver reto contrarreloj.
 *
 * Arma el reto, lleva un contador regresivo general (no por pregunta), valida
 * cada respuesta confirmada, suma puntaje solo si es correcta y pasa de
 * inmediato a la siguiente pregunta sin detener el contador. El reto termina
 * al agotarse el tiempo o las preguntas, o si el alumno lo abandona (E3.1).
 */
class RetoContrarrelojViewModel(
    private val armarRetoUseCase: ArmarRetoContrarrelojUseCase,
    private val validadorUseCase: ValidarRespuestaUseCase,
    private val finalizarRetoUseCase: FinalizarRetoContrarrelojUseCase,
    private val abandonarRetoUseCase: AbandonarRetoContrarrelojUseCase
) : ViewModel() {

    sealed interface Estado {
        data object Cargando : Estado
        data object SinTemasEstudiados : Estado
        data object SinPreguntas : Estado
        data object EnCurso : Estado
        /** El reto ya terminó y se está guardando el resultado. */
        data object Guardando : Estado
        data object Finalizado : Estado
        data object Abandonado : Estado
        data class Error(val mensaje: String) : Estado
    }

    private val _estado = MutableStateFlow<Estado>(Estado.Cargando)
    private val _preguntaActual = MutableStateFlow<PreguntaFlash?>(null)
    private val _numeroPregunta = MutableStateFlow(0)
    private val _seleccionadas = MutableStateFlow<Set<Int>>(emptySet())
    private val _tiempoRestanteSeg = MutableStateFlow(RetoContrareloj.DURACION_SEGUNDOS)
    private val _puntaje = MutableStateFlow(0)
    private val _resultado = MutableStateFlow<ResultadoReto?>(null)

    val estado: StateFlow<Estado> = _estado.asStateFlow()
    val preguntaActual: StateFlow<PreguntaFlash?> = _preguntaActual.asStateFlow()
    /** Número (desde 1) de la pregunta que se está mostrando. */
    val numeroPregunta: StateFlow<Int> = _numeroPregunta.asStateFlow()
    /** Índices ORIGINALES de las opciones seleccionadas en la pregunta actual. */
    val seleccionadas: StateFlow<Set<Int>> = _seleccionadas.asStateFlow()
    val tiempoRestanteSeg: StateFlow<Int> = _tiempoRestanteSeg.asStateFlow()
    val puntaje: StateFlow<Int> = _puntaje.asStateFlow()
    val resultado: StateFlow<ResultadoReto?> = _resultado.asStateFlow()

    /** Duración total del reto, para dibujar la barra de tiempo. */
    var duracionSegundos: Int = RetoContrareloj.DURACION_SEGUNDOS
        private set

    private var reto: RetoContrareloj? = null
    private var indicePregunta = 0
    private var aciertos = 0
    private var respondidas = 0
    private var cerrando = false
    private var temporizador: Job? = null

    fun iniciarReto() {
        if (_estado.value == Estado.EnCurso) return
        viewModelScope.launch {
            _estado.value = Estado.Cargando
            val armado = try {
                armarRetoUseCase()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
            armado
                .onSuccess { nuevo -> comenzar(nuevo) }
                .onFailure { causa ->
                    _estado.value = when (causa) {
                        is SinTemasEstudiadosException -> Estado.SinTemasEstudiados
                        is SinPreguntasDisponiblesException -> Estado.SinPreguntas
                        else -> Estado.Error(causa.message ?: "No se pudo armar el reto")
                    }
                }
        }
    }

    private fun comenzar(nuevo: RetoContrareloj) {
        reto = nuevo
        duracionSegundos = nuevo.duracionSegundos
        indicePregunta = 0
        aciertos = 0
        respondidas = 0
        cerrando = false
        _puntaje.value = 0
        _resultado.value = null
        _seleccionadas.value = emptySet()
        _tiempoRestanteSeg.value = nuevo.duracionSegundos
        _numeroPregunta.value = 1
        _preguntaActual.value = nuevo.preguntas.first()
        _estado.value = Estado.EnCurso
        iniciarContador()
    }

    fun seleccionarOpcion(indice: Int) {
        val pregunta = _preguntaActual.value ?: return
        if (!enCurso()) return
        _seleccionadas.value = when {
            !pregunta.esSeleccionMultiple -> setOf(indice)
            indice in _seleccionadas.value -> _seleccionadas.value - indice
            else -> _seleccionadas.value + indice
        }
    }

    fun confirmarRespuesta() {
        val pregunta = _preguntaActual.value ?: return
        val actual = reto ?: return
        if (!enCurso() || _seleccionadas.value.isEmpty()) return

        val validacion = validadorUseCase(_seleccionadas.value.sorted(), pregunta.respuestasCorrectas)
        respondidas++
        if (validacion.estado == EstadoRespuesta.CORRECTA) {
            aciertos++
            _puntaje.value += actual.puntosPorAcierto
        }

        indicePregunta++
        if (indicePregunta < actual.preguntas.size) {
            _seleccionadas.value = emptySet()
            _numeroPregunta.value = indicePregunta + 1
            _preguntaActual.value = actual.preguntas[indicePregunta]
        } else {
            // A7.2: se agotaron las preguntas antes de acabar el tiempo.
            terminarReto()
        }
    }

    /** E3.1: el alumno abandona a mitad del reto. */
    fun abandonarReto() {
        if (!enCurso()) return
        cerrando = true
        temporizador?.cancel()
        _estado.value = Estado.Guardando
        viewModelScope.launch {
            guardar(Estado.Abandonado) { abandonarRetoUseCase() }
        }
    }

    private fun iniciarContador() {
        temporizador?.cancel()
        temporizador = viewModelScope.launch {
            while (_tiempoRestanteSeg.value > 0) {
                delay(1000)
                _tiempoRestanteSeg.value -= 1
            }
            terminarReto()
        }
    }

    private fun terminarReto() {
        if (!enCurso()) return
        cerrando = true
        temporizador?.cancel()
        _estado.value = Estado.Guardando
        val puntajeFinal = _puntaje.value
        // Corrutina aparte: cancelar el temporizador cancela la que lo ejecuta.
        viewModelScope.launch {
            guardar(Estado.Finalizado) { finalizarRetoUseCase(puntajeFinal, aciertos, respondidas) }
        }
    }

    private suspend fun guardar(destino: Estado, accion: suspend () -> ResultadoReto) {
        try {
            _resultado.value = accion()
            _estado.value = destino
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _estado.value = Estado.Error(e.message ?: "No se pudo guardar el resultado")
        }
    }

    private fun enCurso() = _estado.value == Estado.EnCurso && !cerrando

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    RetoContrarrelojViewModel(
                        armarRetoUseCase = container.armarRetoContrarrelojUseCase,
                        validadorUseCase = container.validarRespuestaUseCase,
                        finalizarRetoUseCase = container.finalizarRetoContrarrelojUseCase,
                        abandonarRetoUseCase = container.abandonarRetoContrarrelojUseCase
                    ).apply { iniciarReto() }
                }
            }
    }
}
