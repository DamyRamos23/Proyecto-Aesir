package com.aesir.odin.ui.racha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.racha.ModuloCarreraSol
import com.aesir.odin.domain.racha.MotivoRechazo
import com.aesir.odin.domain.racha.ResultadoOfrenda
import com.aesir.odin.domain.racha.ResumenRacha
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Pantalla de la racha (MOD-05): estado del día, racha actual y máxima,
 * ofrendas, títulos e hitos. La ofrenda se valida sin cobrar, se muestra el
 * costo y solo se cobra si el alumno confirma.
 */
class RachaViewModel(
    private val modulo: ModuloCarreraSol,
    saldoHidromiel: Flow<Int>,
    private val idUsuario: String = USUARIO_LOCAL,
    private val hoy: () -> LocalDate = LocalDate::now
) : ViewModel() {

    private val _resumen = MutableStateFlow<ResumenRacha?>(null)
    private val _confirmacion = MutableStateFlow<ResultadoOfrenda?>(null)
    private val _mensaje = MutableStateFlow<String?>(null)
    private val _procesando = MutableStateFlow(false)

    val resumen: StateFlow<ResumenRacha?> = _resumen.asStateFlow()
    /** Ofrenda validada que espera la confirmación del alumno. */
    val confirmacion: StateFlow<ResultadoOfrenda?> = _confirmacion.asStateFlow()
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()
    val procesando: StateFlow<Boolean> = _procesando.asStateFlow()
    val saldo: StateFlow<Int> = saldoHidromiel.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun cargar() {
        viewModelScope.launch { _resumen.value = modulo.resumen(idUsuario, hoy()) }
    }

    /** «Ofrenda a Sól»: valida sin cobrar (E3, E4). */
    fun solicitarOfrenda() {
        if (_procesando.value) return
        _procesando.value = true
        viewModelScope.launch {
            try {
                val validacion = modulo.validarOfrenda(idUsuario)
                if (validacion.puedeComprar) _confirmacion.value = validacion
                else _mensaje.value = motivo(validacion)
            } finally {
                _procesando.value = false
            }
        }
    }

    fun cancelarOfrenda() {
        _confirmacion.value = null
    }

    fun confirmarOfrenda() {
        if (_confirmacion.value == null || _procesando.value) return
        _confirmacion.value = null
        _procesando.value = true
        viewModelScope.launch {
            try {
                val compra = modulo.comprarOfrenda(idUsuario)
                _mensaje.value = if (compra.comprada) {
                    "Sól aceptó tu ofrenda. Tienes ${compra.ofrendas} de ${com.aesir.odin.domain.racha.ReglasRacha.MAX_OFRENDAS} guardadas."
                } else motivo(compra)
                _resumen.value = modulo.resumen(idUsuario, hoy())
            } finally {
                _procesando.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    private fun motivo(r: ResultadoOfrenda): String = when (r.motivo) {
        MotivoRechazo.LIMITE_OFRENDAS -> "Ya tienes 2 ofrendas guardadas: es el máximo."
        MotivoRechazo.HIDROMIEL_INSUFICIENTE -> "Te faltan ${(r.costo - saldo.value).coerceAtLeast(1)} de hidromiel para la ofrenda."
        null -> "No se pudo comprar la ofrenda."
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RachaViewModel(
                    modulo = container.moduloCarreraSol,
                    saldoHidromiel = container.moduloHidromiel.observarSaldo(USUARIO_LOCAL)
                ).apply { cargar() }
            }
        }
    }
}
