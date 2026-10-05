package com.aesir.odin.ui.leccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.leccion.ObtenerLeccionUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerTemasPorMundoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Resumen de una lección completada (RF-21, RF-22). Los números (puntaje,
 * errores, total) llegan por navegación; este ViewModel solo carga el tema,
 * el mundo y los temas del mundo para la animación de la piedra rúnica y
 * para ofrecer el siguiente tema que FinalizarLeccionUseCase ya desbloqueó.
 */
class ResumenLeccionViewModel(
    private val obtenerLeccionUseCase: ObtenerLeccionUseCase,
    private val obtenerTemaUseCase: ObtenerIntroduccionTemaUseCase,
    private val obtenerMundoUseCase: ObtenerMundoUseCase,
    private val obtenerTemasPorMundoUseCase: ObtenerTemasPorMundoUseCase
) : ViewModel() {

    data class ContextoResumen(val tema: Tema, val mundo: Mundo, val temas: List<Tema>) {
        /** Tema que sigue dentro del mismo mundo, o null si era el último. */
        val siguiente: Tema? get() = temas.filter { it.orden > tema.orden }.minByOrNull { it.orden }
    }

    private val _contexto = MutableStateFlow<ContextoResumen?>(null)
    val contexto: StateFlow<ContextoResumen?> = _contexto.asStateFlow()

    private val _fallo = MutableStateFlow(false)
    val fallo: StateFlow<Boolean> = _fallo.asStateFlow()

    fun cargar(leccionId: String) {
        viewModelScope.launch {
            val resultado = runCatching {
                val leccion = obtenerLeccionUseCase(leccionId) ?: throw IllegalStateException("Lección no encontrada")
                val tema = obtenerTemaUseCase(leccion.temaId) ?: throw IllegalStateException("Tema no encontrado")
                ContextoResumen(
                    tema = tema,
                    mundo = obtenerMundoUseCase(tema.mundoId),
                    temas = obtenerTemasPorMundoUseCase(tema.mundoId)
                )
            }
            _contexto.value = resultado.getOrNull()
            _fallo.value = resultado.isFailure
        }
    }

    companion object {
        fun factory(container: AppContainer, leccionId: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ResumenLeccionViewModel(
                        obtenerLeccionUseCase = container.obtenerLeccionUseCase,
                        obtenerTemaUseCase = container.obtenerIntroduccionTemaUseCase,
                        obtenerMundoUseCase = container.obtenerMundoUseCase,
                        obtenerTemasPorMundoUseCase = container.obtenerTemasPorMundoUseCase
                    ).apply { cargar(leccionId) }
                }
            }
    }
}
