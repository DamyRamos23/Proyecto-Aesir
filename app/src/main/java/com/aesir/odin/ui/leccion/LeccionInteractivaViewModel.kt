package com.aesir.odin.ui.leccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.Leccion
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.ResultadoLeccion
import com.aesir.odin.domain.model.ResultadoRespuesta
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.leccion.FinalizarLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.ObtenerLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarErrorLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarNivelSinSuperarUseCase
import com.aesir.odin.domain.usecase.leccion.ValidarRespuestaEjercicioUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
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
 *
 * Al terminar avisa al MOD-01 (Sistema de Hidromiel): superada, con
 * FinalizarLeccionUseCase; con el límite de errores (NO_SUPERADO) o al salir
 * antes de terminar (ABANDONADO), con RegistrarNivelSinSuperarUseCase.
 *
 * Además carga el tema y el mundo de la lección ([contexto]) solo para que la
 * pantalla use el estilo visual del mundo; no interviene en la lógica.
 */
class LeccionInteractivaViewModel(
    private val obtenerUseCase: ObtenerLeccionUseCase,
    private val validarUseCase: ValidarRespuestaEjercicioUseCase,
    private val registrarErrorUseCase: RegistrarErrorLeccionUseCase,
    private val finalizarUseCase: FinalizarLeccionUseCase,
    private val registrarSinSuperarUseCase: RegistrarNivelSinSuperarUseCase? = null,
    private val obtenerTemaUseCase: ObtenerIntroduccionTemaUseCase? = null,
    private val obtenerMundoUseCase: ObtenerMundoUseCase? = null
) : ViewModel() {

    /** Tema y mundo a los que pertenece la lección (para el estilo visual). */
    data class ContextoLeccion(val tema: Tema, val mundo: Mundo)

    private val _contexto = MutableStateFlow<ContextoLeccion?>(null)
    val contexto: StateFlow<ContextoLeccion?> = _contexto.asStateFlow()

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

    /** Evita avisar dos veces al MOD-01 por el mismo intento. */
    private var nivelReportado = false

    fun cargarLeccion(leccionId: String) {
        viewModelScope.launch {
            val leccion = obtenerUseCase(leccionId)
            // El contexto se carga antes de publicar la lección para que la
            // pantalla aparezca directamente con el estilo de su mundo.
            cargarContexto(leccion?.temaId)
            _ejercicioActual.value = 0
            _errores.value = 0
            _ultimoResultado.value = null
            _resultadoFinal.value = null
            _leccionFallida.value = false
            finalizando = false
            nivelReportado = false
            _leccion.value = leccion
        }
    }

    private suspend fun cargarContexto(temaId: String?) {
        val obtenerTema = obtenerTemaUseCase ?: return
        val obtenerMundo = obtenerMundoUseCase ?: return
        if (temaId == null) return
        _contexto.value = runCatching {
            val tema = obtenerTema(temaId) ?: return
            ContextoLeccion(tema, obtenerMundo(tema.mundoId))
        }.getOrNull()
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
            val fallida = errores >= RegistrarErrorLeccionUseCase.LIMITE_ERRORES
            if (fallida) _leccionFallida.value = true
            viewModelScope.launch {
                registrarErrorUseCase(leccion.id, ejercicio.id, errores)
                if (fallida) reportarSinSuperar(ResultadoNivel.NO_SUPERADO)
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
            nivelReportado = true
            viewModelScope.launch {
                _resultadoFinal.value = finalizarUseCase(leccion.id, leccion.ejercicios.size, _errores.value)
            }
        }
    }

    /**
     * El alumno sale de la lección. Si no la había terminado, la entrada se
     * pierde (ABANDONADO). Se espera a que el módulo registre el abandono
     * antes de [alTerminar], para que el roadmap ya muestre el saldo correcto.
     */
    fun abandonar(alTerminar: () -> Unit) {
        viewModelScope.launch {
            if (_leccion.value != null && !_leccionFallida.value) reportarSinSuperar(ResultadoNivel.ABANDONADO)
            alTerminar()
        }
    }

    private suspend fun reportarSinSuperar(resultado: ResultadoNivel) {
        val leccion = _leccion.value ?: return
        val registrar = registrarSinSuperarUseCase ?: return
        if (nivelReportado) return
        nivelReportado = true
        val total = leccion.ejercicios.size
        val aciertos = (_ejercicioActual.value + (if (_ultimoResultado.value != null) 1 else 0) - _errores.value).coerceAtLeast(0)
        registrar(leccion.id, resultado, if (total > 0) aciertos * 100 / total else 0)
    }

    companion object {
        fun factory(container: AppContainer, leccionId: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    LeccionInteractivaViewModel(
                        obtenerUseCase = container.obtenerLeccionUseCase,
                        validarUseCase = container.validarRespuestaEjercicioUseCase,
                        registrarErrorUseCase = container.registrarErrorLeccionUseCase,
                        finalizarUseCase = container.finalizarLeccionUseCase,
                        registrarSinSuperarUseCase = container.registrarNivelSinSuperarUseCase,
                        obtenerTemaUseCase = container.obtenerIntroduccionTemaUseCase,
                        obtenerMundoUseCase = container.obtenerMundoUseCase
                    ).apply { cargarLeccion(leccionId) }
                }
            }
    }
}
