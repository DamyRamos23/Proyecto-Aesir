package com.aesir.odin.ui.roadmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.roadmap.ObtenerTemasPorMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ValidarAccesoNivelUseCase
import com.aesir.odin.domain.usecase.roadmap.ValidarAccesoTemaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel de RoadmapScreen. Corresponde a `RoadmapViewModel` del diagrama
 * de clases, y reproduce el flujo del diagrama de secuencia de CU-1:
 *
 * 1. validarAccesoNivel(nivelId): comprueba si el mundo elegido está
 *    desbloqueado antes de mostrar sus temas.
 * 2. cargarRoadmap(mundoId): carga los temas del mundo si el nivel está
 *    desbloqueado.
 * 3. intentarAbrirTema(temaId): valida si el tema tocado está desbloqueado;
 *    si no lo está, escribe el aviso en mensajeBloqueo en vez de navegar.
 *
 * `validarAccesoNivel` no aparece como campo en el diagrama de clases
 * (que solo lista ObtenerTemasPorMundoUseCase y ValidarAccesoTemaUseCase),
 * pero el diagrama de secuencia sí exige esa validación, así que se añadió
 * ValidarAccesoNivelUseCase como tercera dependencia.
 */
class RoadmapViewModel(
    private val obtenerTemasUseCase: ObtenerTemasPorMundoUseCase,
    private val validarAccesoTemaUseCase: ValidarAccesoTemaUseCase,
    private val validarAccesoNivelUseCase: ValidarAccesoNivelUseCase
) : ViewModel() {

    private val _temas = MutableStateFlow<List<Tema>>(emptyList())
    val temas: StateFlow<List<Tema>> = _temas.asStateFlow()

    private val _mensajeBloqueo = MutableStateFlow<String?>(null)
    val mensajeBloqueo: StateFlow<String?> = _mensajeBloqueo.asStateFlow()

    suspend fun validarAccesoNivel(nivelId: String): Boolean {
        val desbloqueado = validarAccesoNivelUseCase(nivelId)
        if (!desbloqueado) {
            _mensajeBloqueo.value = "Este nivel todavía está bloqueado. Completa el nivel anterior para desbloquearlo."
        }
        return desbloqueado
    }

    suspend fun cargarRoadmap(mundoId: String) {
        _temas.value = obtenerTemasUseCase(mundoId)
    }

    suspend fun intentarAbrirTema(temaId: String): Boolean {
        val desbloqueado = validarAccesoTemaUseCase(temaId)
        if (!desbloqueado) {
            _mensajeBloqueo.value = "Este tema todavía está bloqueado. Completa los temas anteriores para desbloquearlo."
        }
        return desbloqueado
    }

    fun limpiarMensajeBloqueo() {
        _mensajeBloqueo.value = null
    }

    companion object {
        fun factory(
            obtenerTemasUseCase: ObtenerTemasPorMundoUseCase,
            validarAccesoTemaUseCase: ValidarAccesoTemaUseCase,
            validarAccesoNivelUseCase: ValidarAccesoNivelUseCase
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RoadmapViewModel(obtenerTemasUseCase, validarAccesoTemaUseCase, validarAccesoNivelUseCase)
            }
        }
    }
}
