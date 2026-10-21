package com.aesir.odin.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.domain.heimdall.ModoLectura
import com.aesir.odin.domain.heimdall.ModuloVisionHeimdall
import com.aesir.odin.domain.heimdall.PresentacionLectura
import com.aesir.odin.domain.sonido.EventoSonido
import com.aesir.odin.domain.sonido.ModuloSonido
import com.aesir.odin.domain.sonido.PreferenciasSonido
import kotlinx.coroutines.flow.StateFlow

/** ViewModel de AjustesScreen: preferencias del MOD-02 (sonido) y del MOD-03 (lectura). */
class AjustesViewModel(
    private val moduloVision: ModuloVisionHeimdall,
    private val moduloSonido: ModuloSonido
) : ViewModel() {

    val presentacion: StateFlow<PresentacionLectura> = moduloVision.presentacion
    val sonido: StateFlow<PreferenciasSonido> = moduloSonido.preferencias

    /** RN-20: único punto desde donde se cambia el modo de lectura. */
    fun cambiarVisionHeimdall(activo: Boolean) {
        moduloVision.cambiarModo(if (activo) ModoLectura.VISION_HEIMDALL else ModoLectura.NORMAL)
    }

    /** MOD-02, paso 7: se aplica de inmediato y se guarda. */
    fun cambiarSonidoActivado(activo: Boolean) {
        moduloSonido.cambiarPreferencias(sonido.value.copy(activado = activo))
        if (activo) moduloSonido.avisar(EventoSonido.TOQUE)
    }

    /** Mientras se arrastra el control solo se aplica; al soltarlo suena una muestra. */
    fun cambiarVolumen(volumen: Float) {
        moduloSonido.cambiarPreferencias(sonido.value.copy(volumen = volumen))
    }

    fun probarVolumen() = moduloSonido.avisar(EventoSonido.ACIERTO)

    /** Tema principal de fondo. */
    fun cambiarMusicaActivada(activa: Boolean) {
        moduloSonido.cambiarPreferencias(sonido.value.copy(musica = activa))
    }

    fun cambiarVolumenMusica(volumen: Float) {
        moduloSonido.cambiarPreferencias(sonido.value.copy(volumenMusica = volumen))
    }

    companion object {
        fun factory(moduloVision: ModuloVisionHeimdall, moduloSonido: ModuloSonido): ViewModelProvider.Factory =
            viewModelFactory { initializer { AjustesViewModel(moduloVision, moduloSonido) } }
    }
}
