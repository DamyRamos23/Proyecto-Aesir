package com.aesir.odin.ui.crucigrama

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.TipoAyuda
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.Celda
import com.aesir.odin.domain.model.Crucigrama
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.PalabraCrucigrama
import com.aesir.odin.domain.model.ResultadoCrucigrama
import com.aesir.odin.domain.model.ResultadoPalabra
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.crucigrama.ActualizarPuntajeUseCase
import com.aesir.odin.domain.usecase.crucigrama.FinalizarCrucigramaUseCase
import com.aesir.odin.domain.usecase.crucigrama.GenerarCrucigramaUseCase
import com.aesir.odin.domain.usecase.crucigrama.ValidarPalabraUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * CU-07 Resolver crucigrama de conceptos (CrucigramaViewModel del diagrama
 * de clases).
 *
 * Fase 1: cargarCrucigrama genera la cuadrícula (o publica mensajeError, A2.1).
 * Fase 2: el alumno selecciona una palabra y la escribe con el teclado; al
 * llenar sus casillas se llama a responder, que valida y actualiza el puntaje
 * (A6.1: una respuesta incorrecta penaliza y borra lo escrito para reintentar).
 * Fase 3: al resolver todas las palabras, finalizar guarda el resultado.
 *
 * Además del diagrama: las ayudas "Revelar letra" y "Revelar palabra", que
 * cobran hidromiel con el MOD-01 (E5), y abandonar, que avisa al módulo.
 */
class CrucigramaViewModel(
    private val generarCrucigramaUseCase: GenerarCrucigramaUseCase,
    private val validarPalabraUseCase: ValidarPalabraUseCase,
    private val actualizarPuntajeUseCase: ActualizarPuntajeUseCase,
    private val finalizarCrucigramaUseCase: FinalizarCrucigramaUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val obtenerTemaUseCase: ObtenerIntroduccionTemaUseCase? = null,
    private val obtenerMundoUseCase: ObtenerMundoUseCase? = null,
    private val idUsuario: String = USUARIO_LOCAL
) : ViewModel() {

    /** Tema y mundo del nivel, solo para el encabezado y el estilo visual. */
    data class Contexto(val tema: Tema, val mundo: Mundo)

    private val _crucigrama = MutableStateFlow<Crucigrama?>(null)
    private val _palabraSeleccionada = MutableStateFlow<String?>(null)
    private val _puntaje = MutableStateFlow(0)
    private val _errores = MutableStateFlow(0)
    private val _ultimoResultado = MutableStateFlow<ResultadoPalabra?>(null)
    private val _resultadoFinal = MutableStateFlow<ResultadoCrucigrama?>(null)
    private val _mensajeError = MutableStateFlow<String?>(null)
    private val _entradas = MutableStateFlow<Map<Celda, Char>>(emptyMap())
    private val _celdaActual = MutableStateFlow<Celda?>(null)
    private val _bloqueadas = MutableStateFlow<Set<Celda>>(emptySet())
    private val _contexto = MutableStateFlow<Contexto?>(null)
    private val _avisoAyuda = MutableStateFlow<String?>(null)

    val crucigrama: StateFlow<Crucigrama?> = _crucigrama.asStateFlow()
    val palabraSeleccionada: StateFlow<String?> = _palabraSeleccionada.asStateFlow()
    val puntaje: StateFlow<Int> = _puntaje.asStateFlow()
    val errores: StateFlow<Int> = _errores.asStateFlow()
    val ultimoResultado: StateFlow<ResultadoPalabra?> = _ultimoResultado.asStateFlow()
    val resultadoFinal: StateFlow<ResultadoCrucigrama?> = _resultadoFinal.asStateFlow()
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    /** Letras escritas por el alumno (o reveladas), por casilla. */
    val entradas: StateFlow<Map<Celda, Char>> = _entradas.asStateFlow()

    /** Casilla donde cae la siguiente letra. */
    val celdaActual: StateFlow<Celda?> = _celdaActual.asStateFlow()

    /** Casillas que ya no se pueden cambiar: palabras resueltas y letras reveladas. */
    val bloqueadas: StateFlow<Set<Celda>> = _bloqueadas.asStateFlow()

    val contexto: StateFlow<Contexto?> = _contexto.asStateFlow()

    /** Motivo por el que no se entregó una ayuda (E1 del MOD-01). */
    val avisoAyuda: StateFlow<String?> = _avisoAyuda.asStateFlow()

    val saldo: StateFlow<Int> = moduloHidromiel.observarSaldo(idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private var temaId: String? = null
    private var temasIncluidos: List<String> = emptyList()
    private var finalizando = false
    private var abandonoReportado = false

    // -----------------------------------------------------------------------
    // Fase 1 · Generación del crucigrama
    // -----------------------------------------------------------------------

    fun cargarCrucigrama(temaId: String, temasIncluidos: List<String>) {
        this.temaId = temaId
        this.temasIncluidos = temasIncluidos
        viewModelScope.launch {
            _mensajeError.value = null
            if (_contexto.value == null) cargarContexto(temaId)
            generarCrucigramaUseCase(temaId, temasIncluidos)
                .onSuccess { crucigrama ->
                    _puntaje.value = crucigrama.puntajeInicial
                    _errores.value = 0
                    _entradas.value = emptyMap()
                    _bloqueadas.value = emptySet()
                    _ultimoResultado.value = null
                    _resultadoFinal.value = null
                    finalizando = false
                    _crucigrama.value = crucigrama
                    crucigrama.palabras
                        .minByOrNull { crucigrama.numeros[it.id] ?: Int.MAX_VALUE }
                        ?.let { seleccionarPalabra(it.id) }
                }
                .onFailure {
                    _crucigrama.value = null
                    _mensajeError.value = "No se pudo generar el crucigrama. ${it.message.orEmpty()}".trim()
                }
        }
    }

    fun reintentarGeneracion() {
        val tema = temaId ?: return
        cargarCrucigrama(tema, temasIncluidos)
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

    fun seleccionarPalabra(palabraId: String) {
        val palabra = palabra(palabraId) ?: return
        _palabraSeleccionada.value = palabraId
        _celdaActual.value = primeraEditable(palabra) ?: palabra.celdas.first()
    }

    /**
     * Tocar una casilla selecciona la palabra que pasa por ahí. Si pasan dos,
     * tocar de nuevo la misma casilla cambia entre la horizontal y la vertical.
     */
    fun seleccionarCelda(celda: Celda) {
        val crucigrama = _crucigrama.value ?: return
        val pasan = crucigrama.palabras.filter { celda in it.celdas }
        if (pasan.isEmpty()) return
        val actual = pasan.firstOrNull { it.id == _palabraSeleccionada.value }
        val elegida = when {
            actual != null && _celdaActual.value == celda && pasan.size > 1 -> pasan.first { it.id != actual.id }
            actual != null -> actual
            else -> pasan.firstOrNull { !it.resuelta } ?: pasan.first()
        }
        _palabraSeleccionada.value = elegida.id
        _celdaActual.value = if (celda in _bloqueadas.value) primeraEditable(elegida) ?: celda else celda
    }

    /** Escribe una letra en la casilla actual y avanza; al llenar la palabra, la valida. */
    fun escribir(letra: Char) {
        val palabra = seleccionada() ?: return
        if (palabra.resuelta || _resultadoFinal.value != null) return
        val celda = _celdaActual.value?.takeIf { it in palabra.celdas && it !in _bloqueadas.value }
            ?: primeraEditable(palabra) ?: return
        _entradas.value = _entradas.value + (celda to letra.uppercaseChar())
        _ultimoResultado.value = null

        val i = palabra.celdas.indexOf(celda)
        _celdaActual.value = palabra.celdas.drop(i + 1).firstOrNull { it !in _bloqueadas.value } ?: celda

        if (palabra.celdas.all { it in _entradas.value }) {
            responder(palabra.id, palabra.celdas.joinToString("") { _entradas.value.getValue(it).toString() })
        }
    }

    /** Borra la letra de la casilla actual o, si está vacía, la anterior. */
    fun borrar() {
        val palabra = seleccionada() ?: return
        if (palabra.resuelta) return
        val actual = _celdaActual.value ?: return
        val i = palabra.celdas.indexOf(actual).coerceAtLeast(0)
        val objetivo = if (actual in _entradas.value && actual !in _bloqueadas.value) actual
        else palabra.celdas.take(i).lastOrNull { it !in _bloqueadas.value && it in _entradas.value } ?: return
        _entradas.value = _entradas.value - objetivo
        _celdaActual.value = objetivo
        _ultimoResultado.value = null
    }

    /** RF-50 a RF-53: valida, actualiza el puntaje y marca la palabra si es correcta. */
    fun responder(palabraId: String, respuesta: String) {
        val crucigrama = _crucigrama.value ?: return
        val palabra = crucigrama.palabras.firstOrNull { it.id == palabraId } ?: return
        if (palabra.resuelta) return

        val estado = validarPalabraUseCase(palabra, respuesta)
        val nuevoPuntaje = actualizarPuntajeUseCase(_puntaje.value, estado)
        _puntaje.value = nuevoPuntaje
        _ultimoResultado.value = ResultadoPalabra(palabraId, estado, nuevoPuntaje)

        if (estado == EstadoRespuesta.CORRECTA) {
            marcarResuelta(palabra, revelada = false)
        } else {
            // A6.1: se penaliza y se borra lo escrito para que lo intente de nuevo
            _errores.value = _errores.value + 1
            _entradas.value = _entradas.value - palabra.celdas.filter { it !in _bloqueadas.value }.toSet()
            _celdaActual.value = primeraEditable(palabra)
        }
    }

    // -----------------------------------------------------------------------
    // Ayudas (MOD-01, E5)
    // -----------------------------------------------------------------------

    fun revelarLetra() {
        val palabra = seleccionada()?.takeIf { !it.resuelta } ?: return
        val celda = palabra.celdas.firstOrNull { it !in _bloqueadas.value && _entradas.value[it] != palabra.letraEn(it) }
            ?: return
        viewModelScope.launch {
            val cobro = moduloHidromiel.cobrarAyuda(idUsuario, TipoAyuda.REVELAR_LETRA)
            if (!cobro.autorizado) {
                _avisoAyuda.value = cobro.motivo
                return@launch
            }
            _entradas.value = _entradas.value + (celda to palabra.letraEn(celda)!!)
            _bloqueadas.value = _bloqueadas.value + celda
            _celdaActual.value = primeraEditable(palabra) ?: celda
            if (palabra.celdas.all { it in _entradas.value }) {
                responder(palabra.id, palabra.celdas.joinToString("") { _entradas.value.getValue(it).toString() })
            }
        }
    }

    fun revelarPalabra() {
        val palabra = seleccionada()?.takeIf { !it.resuelta } ?: return
        viewModelScope.launch {
            val cobro = moduloHidromiel.cobrarAyuda(idUsuario, TipoAyuda.REVELAR_PALABRA)
            if (!cobro.autorizado) {
                _avisoAyuda.value = cobro.motivo
                return@launch
            }
            // La palabra revelada se resuelve sin sumar puntos ni contar como acierto
            marcarResuelta(palabra, revelada = true)
        }
    }

    fun limpiarAvisoAyuda() {
        _avisoAyuda.value = null
    }

    // -----------------------------------------------------------------------
    // Fase 3 · Resultado
    // -----------------------------------------------------------------------

    private fun finalizar() {
        val crucigrama = _crucigrama.value ?: return
        if (finalizando) return
        finalizando = true
        viewModelScope.launch {
            _resultadoFinal.value = finalizarCrucigramaUseCase(crucigrama.copy(completado = true), _puntaje.value, _errores.value)
        }
    }

    /**
     * El alumno sale antes de terminar: el nivel cuenta como abandonado y la
     * entrada se pierde (MOD-01). Si el crucigrama ni siquiera se generó, no
     * se reporta nada: la entrada sigue pagada y puede volver a intentarlo.
     */
    fun abandonar(alTerminar: () -> Unit) {
        viewModelScope.launch {
            val tema = temaId
            if (tema != null && _crucigrama.value != null && _resultadoFinal.value == null && !finalizando && !abandonoReportado) {
                abandonoReportado = true
                moduloHidromiel.alFinalizarNivel(NivelFinalizado(idUsuario, tema, 0, ResultadoNivel.ABANDONADO))
            }
            alTerminar()
        }
    }

    // -----------------------------------------------------------------------
    // Internos
    // -----------------------------------------------------------------------

    private fun palabra(id: String): PalabraCrucigrama? = _crucigrama.value?.palabras?.firstOrNull { it.id == id }

    private fun seleccionada(): PalabraCrucigrama? = _palabraSeleccionada.value?.let(::palabra)

    private fun primeraEditable(palabra: PalabraCrucigrama): Celda? =
        palabra.celdas.firstOrNull { it !in _bloqueadas.value && it !in _entradas.value }
            ?: palabra.celdas.firstOrNull { it !in _bloqueadas.value }

    private fun marcarResuelta(palabra: PalabraCrucigrama, revelada: Boolean) {
        val crucigrama = _crucigrama.value ?: return
        val actualizado = crucigrama.copy(
            palabras = crucigrama.palabras.map {
                if (it.id == palabra.id) it.copy(resuelta = true, revelada = revelada) else it
            }
        )
        _crucigrama.value = actualizado
        _entradas.value = _entradas.value + palabra.celdas.associateWith { palabra.letraEn(it)!! }
        _bloqueadas.value = _bloqueadas.value + palabra.celdas

        // Una palabra que se completó por los cruces también queda resuelta
        val completadasPorCruce = actualizado.palabras.filter { p ->
            !p.resuelta && p.celdas.all { it in _bloqueadas.value }
        }
        if (completadasPorCruce.isNotEmpty()) {
            _crucigrama.value = actualizado.copy(
                palabras = actualizado.palabras.map { p ->
                    if (p in completadasPorCruce) p.copy(resuelta = true, revelada = revelada || p.revelada) else p
                }
            )
        }

        val siguiente = _crucigrama.value!!.let { c ->
            c.palabras.filter { !it.resuelta }.minByOrNull { c.numeros[it.id] ?: Int.MAX_VALUE }
        }
        if (siguiente != null) seleccionarPalabra(siguiente.id) else finalizar()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CrucigramaViewModel(
                    generarCrucigramaUseCase = container.generarCrucigramaUseCase,
                    validarPalabraUseCase = container.validarPalabraUseCase,
                    actualizarPuntajeUseCase = container.actualizarPuntajeUseCase,
                    finalizarCrucigramaUseCase = container.finalizarCrucigramaUseCase,
                    moduloHidromiel = container.moduloHidromiel,
                    obtenerTemaUseCase = container.obtenerIntroduccionTemaUseCase,
                    obtenerMundoUseCase = container.obtenerMundoUseCase
                )
            }
        }
    }
}
