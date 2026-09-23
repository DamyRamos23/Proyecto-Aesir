package com.aesir.odin.ui.temaintro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val registrarIntroduccionVistaUseCase: RegistrarIntroduccionVistaUseCase
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

    fun onCerrarPulsado(onVolverAtras: () -> Unit) {
        val temaId = temaIdActual ?: return
        viewModelScope.launch {
            registrarIntroduccionVistaUseCase(temaId)
            onVolverAtras()
        }
    }

    fun onOmitirPulsado(onVolverAtras: () -> Unit) {
        onVolverAtras()
    }
}