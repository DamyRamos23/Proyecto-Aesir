package com.aesir.odin.ui.temaintro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerTemasPorMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.RegistrarIntroduccionVistaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TemaIntroUiState {
    object Cargando : TemaIntroUiState

    /**
     * @param tituloLeccion título de la primera lección del tema (encabezado del pergamino).
     * @param parrafos introducción de la lección dividida en párrafos.
     * @param totalTemas temas del mundo, para mostrar "Tema N de M".
     */
    data class Contenido(
        val tema: Tema,
        val mundo: Mundo,
        val tituloLeccion: String?,
        val parrafos: List<String>,
        val totalTemas: Int
    ) : TemaIntroUiState {
        val nombre: String get() = tema.nombre
        val descripcion: String get() = tema.descripcion
    }

    object Error : TemaIntroUiState
}

class TemaIntroViewModel(
    private val obtenerIntroduccionTemaUseCase: ObtenerIntroduccionTemaUseCase,
    private val registrarIntroduccionVistaUseCase: RegistrarIntroduccionVistaUseCase,
    private val leccionRepository: LeccionRepository,
    private val obtenerMundoUseCase: ObtenerMundoUseCase,
    private val obtenerTemasPorMundoUseCase: ObtenerTemasPorMundoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TemaIntroUiState>(TemaIntroUiState.Cargando)
    val uiState: StateFlow<TemaIntroUiState> = _uiState.asStateFlow()

    private var temaIdActual: String? = null

    fun cargarIntroduccion(temaId: String) {
        temaIdActual = temaId
        viewModelScope.launch {
            _uiState.value = TemaIntroUiState.Cargando
            _uiState.value = runCatching {
                val tema = obtenerIntroduccionTemaUseCase(temaId) ?: return@runCatching TemaIntroUiState.Error
                val mundo = obtenerMundoUseCase(tema.mundoId)
                val leccion = leccionRepository.obtenerLeccionesPorTema(temaId).firstOrNull()
                TemaIntroUiState.Contenido(
                    tema = tema,
                    mundo = mundo,
                    tituloLeccion = leccion?.titulo,
                    parrafos = leccion?.introduccion
                        ?.split("\n\n")
                        ?.map { it.trim() }
                        ?.filter { it.isNotEmpty() }
                        .orEmpty(),
                    totalTemas = obtenerTemasPorMundoUseCase(tema.mundoId).size
                )
            }.getOrElse { TemaIntroUiState.Error }
        }
    }

    fun onComenzarLeccionPulsado(onNavegarLeccion: (String) -> Unit) {
        val temaId = temaIdActual ?: return
        viewModelScope.launch {
            registrarIntroduccionVistaUseCase(temaId)
            // Obtener la primera lección del tema
            val primeraLeccion = leccionRepository.obtenerLeccionesPorTema(temaId).firstOrNull()
            if (primeraLeccion != null) {
                onNavegarLeccion(primeraLeccion.id)
            }
        }
    }

    fun onCerrarPulsado(onVolverAtras: () -> Unit) {
        val temaId = temaIdActual ?: return
        viewModelScope.launch {
            registrarIntroduccionVistaUseCase(temaId)
            onVolverAtras()
        }
    }

    companion object {
        fun factory(
            obtenerIntroduccionTemaUseCase: ObtenerIntroduccionTemaUseCase,
            registrarIntroduccionVistaUseCase: RegistrarIntroduccionVistaUseCase,
            leccionRepository: LeccionRepository,
            obtenerMundoUseCase: ObtenerMundoUseCase,
            obtenerTemasPorMundoUseCase: ObtenerTemasPorMundoUseCase
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TemaIntroViewModel(
                    obtenerIntroduccionTemaUseCase,
                    registrarIntroduccionVistaUseCase,
                    leccionRepository,
                    obtenerMundoUseCase,
                    obtenerTemasPorMundoUseCase
                )
            }
        }
    }
}
