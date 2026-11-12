package com.aesir.odin.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.domain.heimdall.ModoLectura
import com.aesir.odin.domain.heimdall.ModuloVisionHeimdall
import com.aesir.odin.domain.heimdall.PresentacionLectura
import kotlinx.coroutines.flow.StateFlow

/** ViewModel de AjustesScreen (por ahora solo los miembros del MOD-03). */
class AjustesViewModel(private val moduloVision: ModuloVisionHeimdall) : ViewModel() {

    val presentacion: StateFlow<PresentacionLectura> = moduloVision.presentacion

    /** RN-20: único punto desde donde se cambia el modo de lectura. */
    fun cambiarVisionHeimdall(activo: Boolean) {
        moduloVision.cambiarModo(if (activo) ModoLectura.VISION_HEIMDALL else ModoLectura.NORMAL)
    }

    companion object {
        fun factory(moduloVision: ModuloVisionHeimdall): ViewModelProvider.Factory =
            viewModelFactory { initializer { AjustesViewModel(moduloVision) } }
    }
}
