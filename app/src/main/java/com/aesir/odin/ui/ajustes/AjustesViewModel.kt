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
import com.aesir.odin.domain.notificaciones.ModuloCuervos
import com.aesir.odin.domain.notificaciones.PreferenciasCuervos
import com.aesir.odin.domain.sonido.PreferenciasSonido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** ViewModel de AjustesScreen: preferencias del MOD-02 (sonido), del MOD-03 (lectura) y del MOD-04 (notificaciones). */
class AjustesViewModel(
    private val moduloVision: ModuloVisionHeimdall,
    private val moduloSonido: ModuloSonido,
    private val moduloCuervos: ModuloCuervos? = null
) : ViewModel() {

    private val _cuervos = MutableStateFlow(moduloCuervos?.preferencias ?: PreferenciasCuervos())
    val cuervos: StateFlow<PreferenciasCuervos> = _cuervos.asStateFlow()

    /** MOD-04, disparador 4: activar o desactivar los avisos (E2: al desactivar se cancelan). */
    fun cambiarCuervosActivados(activados: Boolean) = guardarCuervos(_cuervos.value.copy(activadas = activados))

    /** MOD-04: hora del recordatorio diario (19:00 por defecto). */
    fun cambiarHoraCuervos(hora: Int, minuto: Int) = guardarCuervos(_cuervos.value.copy(hora = hora, minuto = minuto))

    private fun guardarCuervos(nuevas: PreferenciasCuervos) {
        moduloCuervos?.cambiarPreferencias(nuevas)
        _cuervos.value = moduloCuervos?.preferencias ?: nuevas
    }

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
        fun factory(
            moduloVision: ModuloVisionHeimdall,
            moduloSonido: ModuloSonido,
            moduloCuervos: ModuloCuervos? = null
        ): ViewModelProvider.Factory =
            viewModelFactory { initializer { AjustesViewModel(moduloVision, moduloSonido, moduloCuervos) } }
    }
}
