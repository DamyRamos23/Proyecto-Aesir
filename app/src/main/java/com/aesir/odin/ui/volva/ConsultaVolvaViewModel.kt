package com.aesir.odin.ui.volva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.AccesoVolva
import com.aesir.odin.domain.model.ConsultaVolva
import com.aesir.odin.domain.model.Fe
import com.aesir.odin.domain.model.MotivoBloqueoVolva
import com.aesir.odin.domain.model.ResultadoConsulta
import com.aesir.odin.domain.model.VisionRespondida
import com.aesir.odin.domain.usecase.volva.AbandonarConsultaVolvaUseCase
import com.aesir.odin.domain.usecase.volva.ApostarVolvaUseCase
import com.aesir.odin.domain.usecase.volva.ArmarConsultaVolvaUseCase
import com.aesir.odin.domain.usecase.volva.FinalizarConsultaVolvaUseCase
import com.aesir.odin.domain.usecase.volva.ResolverVisionUseCase
import com.aesir.odin.domain.usecase.volva.ValidarAccesoVolvaUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estados de la consulta (enumeración EstadoConsulta del diagrama de clases CU-12). */
enum class EstadoConsulta {
    CARGANDO,
    CALDERO_APAGADO,
    VALHALLA_CERRADO,
    HIDROMIEL_INSUFICIENTE,
    VISIONES_INSUFICIENTES,
    APOSTANDO,
    DECLARANDO_FE,
    ELIGIENDO_OPCION,
    VISION_RESUELTA,
    LECTURA,
    ERROR
}

/**
 * CU-12 Retar a la bruja (La Völva). Lleva el flujo del caldero (apuesta), las
 * seis visiones (fe → opciones con 15 s → resultado) y la lectura final.
 */
class ConsultaVolvaViewModel(
    private val validarAcceso: ValidarAccesoVolvaUseCase,
    private val apostar: ApostarVolvaUseCase,
    private val armarConsulta: ArmarConsultaVolvaUseCase,
    private val resolverVision: ResolverVisionUseCase,
    private val finalizarConsulta: FinalizarConsultaVolvaUseCase,
    private val abandonarConsulta: AbandonarConsultaVolvaUseCase,
    saldoHidromiel: Flow<Int>
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoConsulta.CARGANDO)
    private val _acceso = MutableStateFlow<AccesoVolva?>(null)
    private val _apuesta = MutableStateFlow(0)
    private val _consulta = MutableStateFlow<ConsultaVolva?>(null)
    private val _fe = MutableStateFlow<Fe?>(null)
    private val _segundosRestantes = MutableStateFlow(ConsultaVolva.SEGUNDOS_POR_VISION)
    private val _ultimaRespuesta = MutableStateFlow<VisionRespondida?>(null)
    private val _resultado = MutableStateFlow<ResultadoConsulta?>(null)
    private val _aviso = MutableStateFlow<String?>(null)
    private val _confirmandoAbandono = MutableStateFlow(false)

    val estado: StateFlow<EstadoConsulta> = _estado.asStateFlow()
    val acceso: StateFlow<AccesoVolva?> = _acceso.asStateFlow()
    val apuesta: StateFlow<Int> = _apuesta.asStateFlow()
    val consulta: StateFlow<ConsultaVolva?> = _consulta.asStateFlow()
    val fe: StateFlow<Fe?> = _fe.asStateFlow()
    val segundosRestantes: StateFlow<Int> = _segundosRestantes.asStateFlow()
    val ultimaRespuesta: StateFlow<VisionRespondida?> = _ultimaRespuesta.asStateFlow()
    val resultado: StateFlow<ResultadoConsulta?> = _resultado.asStateFlow()
    val aviso: StateFlow<String?> = _aviso.asStateFlow()
    val confirmandoAbandono: StateFlow<Boolean> = _confirmandoAbandono.asStateFlow()
    val saldo: StateFlow<Int> = saldoHidromiel.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private var temporizador: Job? = null

    /** Pasos 1 y 2: abre «El caldero del destino». */
    fun cargarCaldero() {
        viewModelScope.launch {
            _estado.value = EstadoConsulta.CARGANDO
            val acceso = try {
                validarAcceso()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aviso.value = e.message ?: "No se pudo abrir el caldero"
                _estado.value = EstadoConsulta.ERROR
                return@launch
            }
            _acceso.value = acceso
            _apuesta.value = acceso.rango.minimo
            _estado.value = when (acceso.motivo) {
                MotivoBloqueoVolva.CALDERO_APAGADO -> EstadoConsulta.CALDERO_APAGADO
                MotivoBloqueoVolva.VALHALLA_CERRADO -> EstadoConsulta.VALHALLA_CERRADO
                MotivoBloqueoVolva.HIDROMIEL_INSUFICIENTE -> EstadoConsulta.HIDROMIEL_INSUFICIENTE
                MotivoBloqueoVolva.VISIONES_INSUFICIENTES -> EstadoConsulta.VISIONES_INSUFICIENTES
                null -> EstadoConsulta.APOSTANDO
            }
        }
    }

    /** Paso 3. El valor puede salir del rango: A4.1 lo marca y no deja continuar. */
    fun cambiarApuesta(nueva: Int) {
        if (_estado.value != EstadoConsulta.APOSTANDO) return
        _apuesta.value = nueva.coerceIn(0, 999)
        _aviso.value = null
    }

    fun apuestaValida(): Boolean = _acceso.value?.rango?.contiene(_apuesta.value) == true

    /** Pasos 4 a 6: valida la apuesta, la cobra y arma la consulta. */
    fun acercarseAlCaldero() {
        if (_estado.value != EstadoConsulta.APOSTANDO) return
        val cantidad = _apuesta.value
        val rango = _acceso.value?.rango ?: return
        if (!rango.contiene(cantidad)) {
            // A4.1
            _aviso.value = "La apuesta debe ir de ${rango.minimo} a ${rango.maximo} de hidromiel."
            return
        }
        viewModelScope.launch {
            _estado.value = EstadoConsulta.CARGANDO
            try {
                val pago = apostar(cantidad)
                val id = pago.consultaId
                if (id == null) {
                    _aviso.value = pago.acceso.motivo
                    _estado.value = EstadoConsulta.APOSTANDO
                    return@launch
                }
                armarConsulta(id, if (pago.acceso.costo == 0) 0 else cantidad, pago.fecha)
                    .onSuccess { nueva ->
                        _consulta.value = nueva
                        presentarVision()
                    }
                    .onFailure { causa ->
                        _aviso.value = causa.message
                        _estado.value = EstadoConsulta.VISIONES_INSUFICIENTES
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aviso.value = e.message ?: "No se pudo cobrar la apuesta"
                _estado.value = EstadoConsulta.ERROR
            }
        }
    }

    /** Paso 7 (RF-166): la fe se declara antes de ver las opciones. */
    fun declararFe(fe: Fe) {
        if (_estado.value != EstadoConsulta.DECLARANDO_FE) return
        _fe.value = fe
        _estado.value = EstadoConsulta.ELIGIENDO_OPCION
        iniciarContador(ConsultaVolva.SEGUNDOS_POR_VISION)
    }

    /** Pasos 9 y 10. */
    fun elegirOpcion(indiceOpcion: Int) {
        if (_estado.value != EstadoConsulta.ELIGIENDO_OPCION || _confirmandoAbandono.value) return
        resolver(indiceOpcion)
    }

    /** Paso 11: pasa a la siguiente visión o, si ya fueron seis, termina la consulta. */
    fun siguienteVision() {
        if (_estado.value != EstadoConsulta.VISION_RESUELTA) return
        val actual = _consulta.value ?: return
        if (actual.terminada()) terminarConsulta() else presentarVision()
    }

    /** E6.1: detiene el contador y pide confirmación. */
    fun solicitarAbandono() {
        if (!enConsulta()) return
        temporizador?.cancel()
        _confirmandoAbandono.value = true
    }

    fun cancelarAbandono() {
        if (!_confirmandoAbandono.value) return
        _confirmandoAbandono.value = false
        if (_estado.value == EstadoConsulta.ELIGIENDO_OPCION) iniciarContador(_segundosRestantes.value)
    }

    fun confirmarAbandono(onTerminado: () -> Unit) {
        val actual = _consulta.value ?: return
        _confirmandoAbandono.value = false
        temporizador?.cancel()
        _estado.value = EstadoConsulta.CARGANDO
        viewModelScope.launch {
            try {
                abandonarConsulta(actual)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // La consulta quedó registrada al cobrar; al reabrir se marca como abandonada (E6.2).
            }
            onTerminado()
        }
    }

    fun enConsulta(): Boolean = _estado.value in setOf(
        EstadoConsulta.DECLARANDO_FE, EstadoConsulta.ELIGIENDO_OPCION, EstadoConsulta.VISION_RESUELTA
    )

    private fun presentarVision() {
        _fe.value = null
        _ultimaRespuesta.value = null
        _segundosRestantes.value = ConsultaVolva.SEGUNDOS_POR_VISION
        _estado.value = EstadoConsulta.DECLARANDO_FE
    }

    private fun iniciarContador(desde: Int) {
        temporizador?.cancel()
        _segundosRestantes.value = desde
        temporizador = viewModelScope.launch {
            while (_segundosRestantes.value > 0) {
                delay(1000)
                _segundosRestantes.value -= 1
            }
            agotarTiempo()
        }
    }

    /** A9.1: se acabaron los 15 segundos. */
    private fun agotarTiempo() {
        if (_estado.value == EstadoConsulta.ELIGIENDO_OPCION) resolver(null)
    }

    private fun resolver(opcion: Int?) {
        val actual = _consulta.value ?: return
        val fe = _fe.value ?: return
        temporizador?.cancel()
        _estado.value = EstadoConsulta.CARGANDO
        viewModelScope.launch {
            try {
                val respuesta = resolverVision(actual, fe, opcion)
                _consulta.value = actual.registrar(respuesta)
                _ultimaRespuesta.value = respuesta
                _estado.value = EstadoConsulta.VISION_RESUELTA
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aviso.value = e.message ?: "No se pudo validar la visión"
                _estado.value = EstadoConsulta.ERROR
            }
        }
    }

    /** Pasos 12 a 15. */
    private fun terminarConsulta() {
        val actual = _consulta.value ?: return
        _estado.value = EstadoConsulta.CARGANDO
        viewModelScope.launch {
            try {
                _resultado.value = finalizarConsulta(actual)
                _estado.value = EstadoConsulta.LECTURA
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aviso.value = e.message ?: "No se pudo leer el caldero"
                _estado.value = EstadoConsulta.ERROR
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ConsultaVolvaViewModel(
                        validarAcceso = container.validarAccesoVolvaUseCase,
                        apostar = container.apostarVolvaUseCase,
                        armarConsulta = container.armarConsultaVolvaUseCase,
                        resolverVision = container.resolverVisionUseCase,
                        finalizarConsulta = container.finalizarConsultaVolvaUseCase,
                        abandonarConsulta = container.abandonarConsultaVolvaUseCase,
                        saldoHidromiel = container.moduloHidromiel.observarSaldo(USUARIO_LOCAL)
                    ).apply { cargarCaldero() }
                }
            }
    }
}
