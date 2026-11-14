package com.aesir.odin.ui.forja

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.ArtefactoVitrina
import com.aesir.odin.domain.model.AsignacionForja
import com.aesir.odin.domain.model.EtapaTemplar
import com.aesir.odin.domain.model.Forja
import com.aesir.odin.domain.model.MotivoBloqueoForja
import com.aesir.odin.domain.model.RangoApuesta
import com.aesir.odin.domain.model.RespuestaEtapa
import com.aesir.odin.domain.model.ResultadoAccesoForja
import com.aesir.odin.domain.model.ResultadoEtapa
import com.aesir.odin.domain.model.ResultadoForja
import com.aesir.odin.domain.model.TipoEtapa
import com.aesir.odin.domain.usecase.forja.AbandonarForjaUseCase
import com.aesir.odin.domain.usecase.forja.ApostarForjaUseCase
import com.aesir.odin.domain.usecase.forja.ArmarForjaUseCase
import com.aesir.odin.domain.usecase.forja.AsignarArtefactoUseCase
import com.aesir.odin.domain.usecase.forja.CalcularRangoApuestaUseCase
import com.aesir.odin.domain.usecase.forja.EnfriarMetalUseCase
import com.aesir.odin.domain.usecase.forja.FinalizarForjaUseCase
import com.aesir.odin.domain.usecase.forja.ObtenerVitrinaUseCase
import com.aesir.odin.domain.usecase.forja.ValidarAccesoForjaUseCase
import com.aesir.odin.domain.usecase.forja.ValidarEtapaForjaUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estados de la pantalla de la forja (diagrama de clases CU-02, capa ViewModel). */
enum class EstadoForja {
    CARGANDO,
    SIN_ACCESO,
    FORJA_APAGADA,
    HIDROMIEL_INSUFICIENTE,
    ELIGIENDO_APUESTA,
    CALENTANDO,
    MARTILLANDO,
    TEMPLANDO,
    CONFIRMANDO_ABANDONO,
    TERMINADO,
    ERROR
}

/**
 * CU-02 Forjar artefactos en la apuesta de Brokkr (diagrama de secuencia
 * CU-02 v1.2).
 *
 * [iniciarForja] valida el acceso, carga la vitrina, asigna el artefacto y
 * calcula el rango de apuesta. Tras [apostarALoki] presenta las tres etapas;
 * después de revelar cada una deja el resultado en [ultimaRespuesta] (sin la
 * respuesta correcta) hasta que el alumno elige [continuar]. Al terminar la
 * tercera, [terminarForja] determina el resultado y lo deja en [resultado].
 */
class ForjaBrokkrViewModel(
    private val validarAcceso: ValidarAccesoForjaUseCase,
    private val obtenerVitrina: ObtenerVitrinaUseCase,
    private val asignarArtefacto: AsignarArtefactoUseCase,
    private val calcularRango: CalcularRangoApuestaUseCase,
    private val armarForja: ArmarForjaUseCase,
    private val apostar: ApostarForjaUseCase,
    private val validarEtapa: ValidarEtapaForjaUseCase,
    private val enfriarMetal: EnfriarMetalUseCase,
    private val finalizarForja: FinalizarForjaUseCase,
    private val abandonarForja: AbandonarForjaUseCase,
    private val observarHidromiel: () -> Flow<Int>
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoForja.CARGANDO)
    private val _forja = MutableStateFlow<Forja?>(null)
    private val _vitrina = MutableStateFlow<List<ArtefactoVitrina>>(emptyList())
    private val _asignacion = MutableStateFlow<AsignacionForja?>(null)
    private val _rango = MutableStateFlow<RangoApuesta?>(null)
    private val _ultimaRespuesta = MutableStateFlow<ResultadoEtapa?>(null)
    private val _segundosTemple = MutableStateFlow(EtapaTemplar.SEGUNDOS_ENFRIADO)
    private val _resultado = MutableStateFlow<ResultadoForja?>(null)
    private val _aviso = MutableStateFlow<String?>(null)
    private val _acceso = MutableStateFlow<ResultadoAccesoForja?>(null)
    private val _procesando = MutableStateFlow(false)

    val estado: StateFlow<EstadoForja> = _estado.asStateFlow()
    val forja: StateFlow<Forja?> = _forja.asStateFlow()
    val vitrina: StateFlow<List<ArtefactoVitrina>> = _vitrina.asStateFlow()
    val asignacion: StateFlow<AsignacionForja?> = _asignacion.asStateFlow()
    val rango: StateFlow<RangoApuesta?> = _rango.asStateFlow()
    val ultimaRespuesta: StateFlow<ResultadoEtapa?> = _ultimaRespuesta.asStateFlow()
    val segundosTemple: StateFlow<Int> = _segundosTemple.asStateFlow()
    val resultado: StateFlow<ResultadoForja?> = _resultado.asStateFlow()
    /** Mensaje para el alumno: apuesta fuera de rango, hidromiel insuficiente, error, etc. */
    val aviso: StateFlow<String?> = _aviso.asStateFlow()
    /** Resultado de validar las precondiciones: forjas restantes e hidromiel faltante. */
    val acceso: StateFlow<ResultadoAccesoForja?> = _acceso.asStateFlow()
    /** true mientras se cobra la apuesta o se valida una etapa (evita dobles toques). */
    val procesando: StateFlow<Boolean> = _procesando.asStateFlow()
    val saldo: StateFlow<Int> = observarHidromiel().stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private var temporizador: Job? = null
    private var estadoPrevio: EstadoForja? = null

    // -----------------------------------------------------------------------
    // Pasos 1 a 4
    // -----------------------------------------------------------------------

    fun iniciarForja() {
        temporizador?.cancel()
        _forja.value = null
        _asignacion.value = null
        _rango.value = null
        _ultimaRespuesta.value = null
        _resultado.value = null
        _aviso.value = null
        _estado.value = EstadoForja.CARGANDO
        ejecutar {
            val acceso = validarAcceso()
            _acceso.value = acceso
            _vitrina.value = obtenerVitrina()
            if (!acceso.autorizado) {
                _estado.value = when (acceso.motivo) {
                    MotivoBloqueoForja.FORJAS_AGOTADAS -> EstadoForja.FORJA_APAGADA
                    MotivoBloqueoForja.HIDROMIEL_INSUFICIENTE -> EstadoForja.HIDROMIEL_INSUFICIENTE
                    else -> EstadoForja.SIN_ACCESO
                }
                return@ejecutar
            }
            _asignacion.value = asignarArtefacto(_vitrina.value)
            _rango.value = calcularRango(observarHidromiel().first())
            _estado.value = EstadoForja.ELIGIENDO_APUESTA
        }
    }

    // -----------------------------------------------------------------------
    // Pasos 5 y 6
    // -----------------------------------------------------------------------

    fun apostarALoki(cantidad: Int) {
        val rango = _rango.value ?: return
        val asignacion = _asignacion.value ?: return
        if (_estado.value != EstadoForja.ELIGIENDO_APUESTA || _procesando.value) return
        if (!rango.contiene(cantidad)) {
            // A5.1: no se continúa hasta que la apuesta esté dentro del rango
            _aviso.value = "La apuesta debe estar entre ${rango.minimo} y ${rango.maximoPermitido} de hidromiel."
            return
        }
        _procesando.value = true
        ejecutar {
            try {
                val nueva = armarForja(asignacion, cantidad)
                val cobro = apostar(nueva, rango)
                if (!cobro.cobrada) {
                    _aviso.value = "Te faltan ${cobro.faltante} de hidromiel para esta apuesta."
                    return@ejecutar
                }
                _aviso.value = null
                _forja.value = nueva
                _estado.value = EstadoForja.CALENTANDO
            } finally {
                _procesando.value = false
            }
        }
    }

    fun limpiarAviso() {
        _aviso.value = null
    }

    // -----------------------------------------------------------------------
    // Pasos 7 a 15
    // -----------------------------------------------------------------------

    fun revelarCalentar(orden: List<String>) = revelar(RespuestaEtapa(TipoEtapa.CALENTAR, ordenPasos = orden))

    fun revelarMartillar(piezaId: String) = revelar(RespuestaEtapa(TipoEtapa.MARTILLAR, piezaId = piezaId))

    fun revelarTemplar(huecos: Map<String, String>) {
        if (_forja.value?.etapaEnCurso()?.tipo != TipoEtapa.TEMPLAR || _ultimaRespuesta.value != null) return
        temporizador?.cancel()
        revelar(RespuestaEtapa(TipoEtapa.TEMPLAR, huecos = huecos))
    }

    /** Tras ver si la respuesta fue correcta, pasa a la siguiente etapa o al resultado. */
    fun continuar() {
        val ultima = _ultimaRespuesta.value ?: return
        if (_estado.value == EstadoForja.CONFIRMANDO_ABANDONO) return
        _ultimaRespuesta.value = null
        when {
            ultima.forjaTerminada -> terminarForja()
            ultima.tipo == TipoEtapa.CALENTAR -> _estado.value = EstadoForja.MARTILLANDO
            ultima.tipo == TipoEtapa.MARTILLAR -> {
                _estado.value = EstadoForja.TEMPLANDO
                iniciarEnfriamiento()
            }
            else -> terminarForja()
        }
    }

    private fun revelar(respuesta: RespuestaEtapa) {
        val actual = _forja.value ?: return
        if (_procesando.value || _ultimaRespuesta.value != null || actual.terminada()) return
        if (actual.etapaEnCurso().tipo != respuesta.tipo) return
        _procesando.value = true
        ejecutar {
            try {
                _ultimaRespuesta.value = validarEtapa(actual, respuesta)
            } finally {
                _procesando.value = false
            }
        }
    }

    /** Paso 13: el metal tarda 60 segundos en enfriarse. */
    private fun iniciarEnfriamiento() {
        temporizador?.cancel()
        _segundosTemple.value = EtapaTemplar.SEGUNDOS_ENFRIADO
        temporizador = viewModelScope.launch {
            while (_segundosTemple.value > 0) {
                delay(1_000)
                _segundosTemple.value = _segundosTemple.value - 1
            }
            alEnfriarseMetal()
        }
    }

    /** A14.1: se acabó el tiempo antes de revelar la respuesta; la etapa cuenta como incorrecta. */
    private fun alEnfriarseMetal() {
        val actual = _forja.value ?: return
        if (_ultimaRespuesta.value != null || actual.terminada() || actual.etapaEnCurso().tipo != TipoEtapa.TEMPLAR) return
        if (_procesando.value) return
        _procesando.value = true
        ejecutar {
            try {
                _ultimaRespuesta.value = enfriarMetal(actual)
            } finally {
                _procesando.value = false
            }
        }
    }

    // -----------------------------------------------------------------------
    // Pasos 16 a 19
    // -----------------------------------------------------------------------

    private fun terminarForja() {
        val actual = _forja.value ?: return
        _estado.value = EstadoForja.CARGANDO
        ejecutar {
            val final = finalizarForja(actual)
            _resultado.value = final
            _aviso.value = final.aviso?.mensaje
            _forja.value = null
            _estado.value = EstadoForja.TERMINADO
        }
    }

    // -----------------------------------------------------------------------
    // A7.1 Abandonar la forja
    // -----------------------------------------------------------------------

    /** Solo pide confirmación entre los pasos 7 y 15, cuando ya hay una apuesta en juego. */
    fun solicitarAbandono() {
        val actual = _forja.value ?: return
        if (actual.terminada()) return
        val estadoActual = _estado.value
        if (estadoActual !in ETAPAS) return
        estadoPrevio = estadoActual
        _estado.value = EstadoForja.CONFIRMANDO_ABANDONO
    }

    fun cancelarAbandono() {
        if (_estado.value != EstadoForja.CONFIRMANDO_ABANDONO) return
        _estado.value = estadoPrevio ?: EstadoForja.CALENTANDO
        estadoPrevio = null
    }

    fun confirmarAbandono() {
        val actual = _forja.value ?: return
        if (_estado.value != EstadoForja.CONFIRMANDO_ABANDONO) return
        temporizador?.cancel()
        _estado.value = EstadoForja.CARGANDO
        ejecutar {
            _resultado.value = abandonarForja(actual)
            _forja.value = null
            _ultimaRespuesta.value = null
            _estado.value = EstadoForja.TERMINADO
        }
    }

    private fun ejecutar(bloque: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                bloque()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aviso.value = e.message ?: "Algo salió mal en la forja."
                _estado.value = EstadoForja.ERROR
            }
        }
    }

    companion object {
        private val ETAPAS = setOf(EstadoForja.CALENTANDO, EstadoForja.MARTILLANDO, EstadoForja.TEMPLANDO)

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ForjaBrokkrViewModel(
                        validarAcceso = container.validarAccesoForjaUseCase,
                        obtenerVitrina = container.obtenerVitrinaUseCase,
                        asignarArtefacto = container.asignarArtefactoUseCase,
                        calcularRango = container.calcularRangoApuestaUseCase,
                        armarForja = container.armarForjaUseCase,
                        apostar = container.apostarForjaUseCase,
                        validarEtapa = container.validarEtapaForjaUseCase,
                        enfriarMetal = container.enfriarMetalUseCase,
                        finalizarForja = container.finalizarForjaUseCase,
                        abandonarForja = container.abandonarForjaUseCase,
                        observarHidromiel = { container.moduloHidromiel.observarSaldo(USUARIO_LOCAL) }
                    ).apply { iniciarForja() }
                }
            }
    }
}
