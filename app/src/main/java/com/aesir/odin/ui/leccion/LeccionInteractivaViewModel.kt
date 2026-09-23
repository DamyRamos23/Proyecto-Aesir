package com.aesir.odin.ui.leccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.Leccion
import com.aesir.odin.domain.model.ResultadoLeccion
import com.aesir.odin.domain.model.ResultadoRespuesta
import com.aesir.odin.domain.usecase.leccion.FinalizarLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.ObtenerLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarErrorLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.ValidarRespuestaEjercicioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CU-05 Realizar una lección interactiva.
 *
 * Presenta los ejercicios en orden (RF-14), valida cada respuesta (RF-16),
 * cuenta los errores (RF-17) y termina la lección al tercer error (RF-18),
 * o la finaliza con su resultado al completar todos los ejercicios (RF-21 a RF-24).
 */
class LeccionInteractivaViewModel(
    private val obtenerUseCase: ObtenerLeccionUseCase,
    private val validarUseCase: ValidarRespuestaEjercicioUseCase,
    private val registrarErrorUseCase: RegistrarErrorLeccionUseCase,
    private val finalizarUseCase: FinalizarLeccionUseCase
) : ViewModel() {

    private val _leccion = MutableStateFlow<Leccion?>(null)
    private val _ejercicioActual = MutableStateFlow(0)
    private val _errores = MutableStateFlow(0)
    private val _ultimoResultado = MutableStateFlow<ResultadoRespuesta?>(null)
    private val _resultadoFinal = MutableStateFlow<ResultadoLeccion?>(null)
    private val _leccionFallida = MutableStateFlow(false)

    val leccion: StateFlow<Leccion?> = _leccion.asStateFlow()
    val ejercicioActual: StateFlow<Int> = _ejercicioActual.asStateFlow()
    val errores: StateFlow<Int> = _errores.asStateFlow()
    val ultimoResultado: StateFlow<ResultadoRespuesta?> = _ultimoResultado.asStateFlow()
    val resultadoFinal: StateFlow<ResultadoLeccion?> = _resultadoFinal.asStateFlow()

    /** true cuando se alcanzó el límite de errores (flujo alternativo A4.1). */
    val leccionFallida: StateFlow<Boolean> = _leccionFallida.asStateFlow()

    private var finalizando = false

    fun cargarLeccion(leccionId: String) {
        viewModelScope.launch {
            _leccion.value = obtenerUseCase(leccionId)
            _ejercicioActual.value = 0
            _errores.value = 0
            _ultimoResultado.value = null
            _resultadoFinal.value = null
            _leccionFallida.value = false
            finalizando = false
        }
    }

    fun responder(opciones: List<Int>) {
        val leccion = _leccion.value ?: return
        if (_ultimoResultado.value != null || _leccionFallida.value) return
        val ejercicio = leccion.ejercicios.getOrNull(_ejercicioActual.value) ?: return

        val resultado = validarUseCase(ejercicio, opciones)
        _ultimoResultado.value = resultado

        if (resultado.estado != EstadoRespuesta.CORRECTA) {
            val errores = _errores.value + 1
            _errores.value = errores
            // Se interrumpe de inmediato, sin esperar a que termine de guardarse el error.
            if (errores >= RegistrarErrorLeccionUseCase.LIMITE_ERRORES) _leccionFallida.value = true
            viewModelScope.launch {
                registrarErrorUseCase(leccion.id, ejercicio.id, errores)
            }
        }
    }

    fun siguienteEjercicio() {
        val leccion = _leccion.value ?: return
        if (_ultimoResultado.value == null || _leccionFallida.value) return

        val siguiente = _ejercicioActual.value + 1
        if (siguiente < leccion.ejercicios.size) {
            _ultimoResultado.value = null
            _ejercicioActual.value = siguiente
        } else if (!finalizando) {
            finalizando = true
            viewModelScope.launch {
                _resultadoFinal.value = finalizarUseCase(leccion.id, leccion.ejercicios.size, _errores.value)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer, leccionId: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    LeccionInteractivaViewModel(
                        obtenerUseCase = container.obtenerLeccionUseCase,
                        validarUseCase = container.validarRespuestaEjercicioUseCase,
                        registrarErrorUseCase = container.registrarErrorLeccionUseCase,
                        finalizarUseCase = container.finalizarLeccionUseCase
                    ).apply { cargarLeccion(leccionId) }
                }
            }
    }
}
