package com.aesir.odin.ui.retoarrastrar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.EstadoNivel
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.model.RetoArrastrar
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.retoarrastrar.AbandonarRetoUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.CalcularPuntajeRetoUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.FinalizarRetoUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.ObtenerRetoUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.RegistrarErrorRetoArrastrarUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.ValidarColocacionUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerTemasPorMundoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel del CU-09 Resolver reto de arrastrar y soltar (diagrama de clases
 * CU-09). Además de lo que marca el diagrama guarda:
 * - [contexto]: tema y mundo, para el estilo de la pantalla.
 * - [desbloqueados]: nombres de los niveles que se abrieron al aprobar (paso 9).
 * - [aviso]: motivo por el que no se pudo abrir la lección sugerida (A7.2).
 */
class RetoArrastrarViewModel(
    private val obtenerRetoUseCase: ObtenerRetoUseCase,
    private val validarColocacionUseCase: ValidarColocacionUseCase,
    private val puntajeUseCase: CalcularPuntajeRetoUseCase,
    private val registrarErrorUseCase: RegistrarErrorRetoArrastrarUseCase,
    private val finalizarRetoUseCase: FinalizarRetoUseCase,
    private val abandonarRetoUseCase: AbandonarRetoUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val obtenerTemaUseCase: ObtenerIntroduccionTemaUseCase? = null,
    private val obtenerMundoUseCase: ObtenerMundoUseCase? = null,
    private val obtenerTemasPorMundoUseCase: ObtenerTemasPorMundoUseCase? = null,
    private val idUsuario: String = USUARIO_LOCAL
) : ViewModel() {

    data class Contexto(val tema: Tema, val mundo: Mundo)

    private val _reto = MutableStateFlow<RetoArrastrar?>(null)
    private val _colocaciones = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _puntaje = MutableStateFlow(CalcularPuntajeRetoUseCase.PUNTAJE_INICIAL)
    private val _intentosFallidos = MutableStateFlow(0)
    private val _elementosIncorrectos = MutableStateFlow<List<String>>(emptyList())
    private val _resultadoFinal = MutableStateFlow<ResultadoReto?>(null)
    private val _mostrarConfirmacionSalida = MutableStateFlow(false)
    private val _contexto = MutableStateFlow<Contexto?>(null)
    private val _mensajeError = MutableStateFlow<String?>(null)
    private val _desbloqueados = MutableStateFlow<List<String>>(emptyList())
    private val _comprobando = MutableStateFlow(false)
    private val _aviso = MutableStateFlow<String?>(null)

    val reto: StateFlow<RetoArrastrar?> = _reto.asStateFlow()

    /** elementoId → posicionId de lo que el alumno ya colocó. */
    val colocaciones: StateFlow<Map<String, String>> = _colocaciones.asStateFlow()
    val puntaje: StateFlow<Int> = _puntaje.asStateFlow()
    val intentosFallidos: StateFlow<Int> = _intentosFallidos.asStateFlow()

    /** Ids de los elementos mal colocados en la última confirmación (A7.1). */
    val elementosIncorrectos: StateFlow<List<String>> = _elementosIncorrectos.asStateFlow()
    val resultadoFinal: StateFlow<ResultadoReto?> = _resultadoFinal.asStateFlow()
    val mostrarConfirmacionSalida: StateFlow<Boolean> = _mostrarConfirmacionSalida.asStateFlow()
    val contexto: StateFlow<Contexto?> = _contexto.asStateFlow()
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()
    val desbloqueados: StateFlow<List<String>> = _desbloqueados.asStateFlow()
    val comprobando: StateFlow<Boolean> = _comprobando.asStateFlow()
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    val saldo: StateFlow<Int> = moduloHidromiel.observarSaldo(idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private var temaId: String? = null
    private var abandonado = false

    // -----------------------------------------------------------------------
    // Fase 1 · Carga del reto
    // -----------------------------------------------------------------------

    /** Carga el reto solo la primera vez: al rotar la pantalla se conserva el intento en curso. */
    fun cargarReto(temaId: String) {
        if (this.temaId == temaId && (_reto.value != null || _mensajeError.value != null)) return
        this.temaId = temaId
        viewModelScope.launch {
            _mensajeError.value = null
            cargarContexto(temaId)
            runCatching { obtenerRetoUseCase(temaId) }
                .onSuccess { reto ->
                    _reto.value = reto
                    _colocaciones.value = emptyMap()
                    _puntaje.value = CalcularPuntajeRetoUseCase.PUNTAJE_INICIAL
                    _intentosFallidos.value = 0
                    _elementosIncorrectos.value = emptyList()
                    _resultadoFinal.value = null
                }
                .onFailure { _mensajeError.value = it.message ?: "No se pudo cargar el reto." }
        }
    }

    private suspend fun cargarContexto(temaId: String) {
        val obtenerTema = obtenerTemaUseCase ?: return
        val obtenerMundo = obtenerMundoUseCase ?: return
        _contexto.value = runCatching {
            val tema = obtenerTema(temaId) ?: return
            Contexto(tema, obtenerMundo(tema.mundoId))
        }.getOrNull()
    }

    // -----------------------------------------------------------------------
    // Fase 2 · Resolución
    // -----------------------------------------------------------------------

    /**
     * Coloca un elemento en una posición (RF-72, RF-73). Si la posición ya
     * tenía otro elemento, se intercambian; con [posicionId] = null el
     * elemento regresa al banco.
     */
    fun colocarElemento(elementoId: String, posicionId: String?) {
        if (_resultadoFinal.value != null || _comprobando.value) return
        val actuales = _colocaciones.value.toMutableMap()
        val origen = actuales[elementoId]
        var ocupante: String? = null
        if (posicionId == null) {
            actuales.remove(elementoId)
        } else {
            if (origen == posicionId) return
            ocupante = actuales.entries.firstOrNull { it.value == posicionId && it.key != elementoId }?.key
            actuales[elementoId] = posicionId
            ocupante?.let { if (origen != null) actuales[it] = origen else actuales.remove(it) }
        }
        _colocaciones.value = actuales
        // Lo que se movió deja de estar señalado
        _elementosIncorrectos.value = _elementosIncorrectos.value - setOfNotNull(elementoId, ocupante)
    }

    /** Paso 5: confirma la respuesta cuando todos los elementos están colocados. */
    fun confirmarRespuesta() {
        val reto = _reto.value ?: return
        if (_resultadoFinal.value != null || _comprobando.value) return
        if (reto.elementos.any { it.id !in _colocaciones.value }) return
        _comprobando.value = true
        viewModelScope.launch {
            try {
                val incorrectos = validarColocacionUseCase(reto, _colocaciones.value)
                if (incorrectos.isEmpty()) {
                    _elementosIncorrectos.value = emptyList()
                    finalizar(reto, aprobado = true)
                } else {
                    // A7.1: se señalan, se descuentan 20 puntos y se registra el error
                    val intentos = _intentosFallidos.value + 1
                    _intentosFallidos.value = intentos
                    _puntaje.value = puntajeUseCase(intentos)
                    _elementosIncorrectos.value = incorrectos
                    val textos = reto.elementos.filter { it.id in incorrectos }.map { it.texto }
                    val limiteAlcanzado = registrarErrorUseCase(reto.id, textos, intentos)
                    if (limiteAlcanzado) {
                        // A7.2: termina sin puntaje y se muestra la solución
                        _puntaje.value = 0
                        finalizar(reto, aprobado = false)
                        _colocaciones.value = reto.solucion
                    }
                }
            } finally {
                _comprobando.value = false
            }
        }
    }

    // -----------------------------------------------------------------------
    // Fase 3 · Puntaje y resumen
    // -----------------------------------------------------------------------

    private suspend fun finalizar(reto: RetoArrastrar, aprobado: Boolean) {
        val mundoId = _contexto.value?.tema?.mundoId
        val antes = mundoId?.let { estados(it) }.orEmpty()
        val resultado = finalizarRetoUseCase(reto, _intentosFallidos.value, aprobado)
        if (aprobado && mundoId != null) {
            val despues = estados(mundoId)
            val abiertos = despues.filter { (id, estado) ->
                antes[id] == EstadoNivel.BLOQUEADO && estado != EstadoNivel.BLOQUEADO
            }.keys
            val nombres = runCatching { obtenerTemasPorMundoUseCase?.invoke(mundoId).orEmpty() }.getOrDefault(emptyList())
            _desbloqueados.value = nombres
                .filter { it.id in abiertos }
                .sortedWith(compareBy({ it.anillo }, { it.orden }))
                .map { if (it.esRagnarok) "Ragnarök" else it.nombre }
        }
        _resultadoFinal.value = resultado
    }

    private suspend fun estados(mundoId: String): Map<String, EstadoNivel> =
        runCatching { moduloHidromiel.obtenerEstadoRoadmap(idUsuario, mundoId).associate { it.idNivel to it.estado } }
            .getOrDefault(emptyMap())

    /**
     * A7.2: el alumno acepta repasar la lección del tema. La lección se abre
     * con las reglas del MOD-01; si no se puede abrir, se muestra el motivo.
     */
    fun repasarLeccion(temaLeccionId: String, alRepasar: (String) -> Unit) {
        viewModelScope.launch {
            val acceso = moduloHidromiel.cobrarEntrada(idUsuario, temaLeccionId)
            if (acceso.autorizado) alRepasar(temaLeccionId) else _aviso.value = acceso.motivo
        }
    }

    fun limpiarAviso() {
        _aviso.value = null
    }

    // -----------------------------------------------------------------------
    // Flujo alterno · Abandonar (A4.1)
    // -----------------------------------------------------------------------

    fun solicitarSalida() {
        _mostrarConfirmacionSalida.value = true
    }

    fun cancelarSalida() {
        _mostrarConfirmacionSalida.value = false
    }

    /**
     * Descarta las colocaciones sin guardar resultado y avisa al MOD-01. Se
     * espera a que el módulo registre el abandono antes de [alTerminar], para
     * que el roadmap ya muestre el saldo correcto.
     */
    fun confirmarSalida(alTerminar: () -> Unit) {
        _mostrarConfirmacionSalida.value = false
        viewModelScope.launch {
            val tema = temaId
            if (tema != null && _reto.value != null && _resultadoFinal.value == null && !abandonado) {
                abandonado = true
                runCatching { abandonarRetoUseCase(tema) }
            }
            _colocaciones.value = emptyMap()
            alTerminar()
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RetoArrastrarViewModel(
                    obtenerRetoUseCase = container.obtenerRetoUseCase,
                    validarColocacionUseCase = container.validarColocacionUseCase,
                    puntajeUseCase = container.calcularPuntajeRetoUseCase,
                    registrarErrorUseCase = container.registrarErrorRetoArrastrarUseCase,
                    finalizarRetoUseCase = container.finalizarRetoUseCase,
                    abandonarRetoUseCase = container.abandonarRetoUseCase,
                    moduloHidromiel = container.moduloHidromiel,
                    obtenerTemaUseCase = container.obtenerIntroduccionTemaUseCase,
                    obtenerMundoUseCase = container.obtenerMundoUseCase,
                    obtenerTemasPorMundoUseCase = container.obtenerTemasPorMundoUseCase
                )
            }
        }
    }
}
