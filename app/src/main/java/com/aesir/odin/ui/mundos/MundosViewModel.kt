package com.aesir.odin.ui.mundos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.usecase.diagnostico.ValidarAccesoDiagnosticoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de MundosScreen. Corresponde a `MundosViewModel` del diagrama
 * de clases.
 */
class MundosViewModel(
    private val obtenerMundosUseCase: ObtenerMundosUseCase,
    private val validarAccesoDiagnosticoUseCase: ValidarAccesoDiagnosticoUseCase? = null
) : ViewModel() {

    private val _mundos = MutableStateFlow<List<Mundo>>(emptyList())
    val mundos: StateFlow<List<Mundo>> = _mundos.asStateFlow()

    fun cargarMundos() {
        viewModelScope.launch {
            _mundos.value = obtenerMundosUseCase()
        }
    }

    /**
     * CU-10, RF-81: true si al entrar al mundo se debe presentar la evaluación
     * diagnóstica (mundo desbloqueado y evaluación pendiente). Si es false se
     * entra directo al roadmap.
     */
    suspend fun intentarAbrirDiagnostico(mundoId: String): Boolean =
        validarAccesoDiagnosticoUseCase?.invoke(mundoId) == true

    companion object {
        fun factory(
            obtenerMundosUseCase: ObtenerMundosUseCase,
            validarAccesoDiagnosticoUseCase: ValidarAccesoDiagnosticoUseCase? = null
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { MundosViewModel(obtenerMundosUseCase, validarAccesoDiagnosticoUseCase) }
            }
    }
}
