package com.aesir.odin.ui.eastereggs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Cobra y paga las apuestas de los juegos ocultos a través del MOD-01. */
class JuegoOcultoViewModel(private val modulo: ModuloHidromiel) : ViewModel() {

    val saldo: Flow<Int> = modulo.observarSaldo(USUARIO_LOCAL)

    private val _aviso = MutableStateFlow<String?>(null)
    /** Motivo de una apuesta rechazada (sin hidromiel o debajo del piso, RN-08). */
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    /** Cobra [cantidad]; si el MOD-01 la acepta, ejecuta [alAceptar]. */
    fun apostar(cantidad: Int, alAceptar: () -> Unit) {
        viewModelScope.launch {
            val r = modulo.apostarJuegoOculto(USUARIO_LOCAL, cantidad)
            _aviso.value = if (r.autorizado) null else r.motivo
            if (r.autorizado) alAceptar()
        }
    }

    fun pagar(premio: Int) {
        viewModelScope.launch { modulo.pagarJuegoOculto(USUARIO_LOCAL, premio) }
    }

    companion object {
        fun factory(modulo: ModuloHidromiel): ViewModelProvider.Factory =
            viewModelFactory { initializer { JuegoOcultoViewModel(modulo) } }
    }
}
