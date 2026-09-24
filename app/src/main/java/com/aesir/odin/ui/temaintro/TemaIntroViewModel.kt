package com.aesir.odin.ui.temaintro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.RegistrarIntroduccionVistaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TemaIntroUiState {
    object Cargando : TemaIntroUiState
    data class Contenido(val nombre: String, val descripcion: String) : TemaIntroUiState
    object Error : TemaIntroUiState
}

class TemaIntroViewModel(
    private val obtenerIntroduccionTemaUseCase: ObtenerIntroduccionTemaUseCase,
    private val registrarIntroduccionVistaUseCase: RegistrarIntroduccionVistaUseCase,
    private val leccionRepository: LeccionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TemaIntroUiState>(TemaIntroUiState.Cargando)
    val uiState: StateFlow<TemaIntroUiState> = _uiState.asStateFlow()

    private var temaIdActual: String? = null

    fun cargarIntroduccion(temaId: String) {
        temaIdActual = temaId
        viewModelScope.launch {
            _uiState.value = TemaIntroUiState.Cargando
            val tema = obtenerIntroduccionTemaUseCase(temaId)
            _uiState.value = if (tema != null) {
                TemaIntroUiState.Contenido(nombre = tema.nombre, descripcion = tema.descripcion)
            } else {
                TemaIntroUiState.Error
            }
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
            leccionRepository: LeccionRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TemaIntroViewModel(
                    obtenerIntroduccionTemaUseCase,
                    registrarIntroduccionVistaUseCase,
                    leccionRepository
                )
            }
        }
    }
}