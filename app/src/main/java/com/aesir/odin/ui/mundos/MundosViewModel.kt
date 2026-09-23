package com.aesir.odin.ui.mundos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.domain.model.Mundo
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
    private val obtenerMundosUseCase: ObtenerMundosUseCase
) : ViewModel() {

    private val _mundos = MutableStateFlow<List<Mundo>>(emptyList())
    val mundos: StateFlow<List<Mundo>> = _mundos.asStateFlow()

    fun cargarMundos() {
        viewModelScope.launch {
            _mundos.value = obtenerMundosUseCase()
        }
    }

    companion object {
        fun factory(obtenerMundosUseCase: ObtenerMundosUseCase): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { MundosViewModel(obtenerMundosUseCase) }
            }
    }
}
