package com.aesir.odin.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.EstadoTarjeta
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.PilaFlashcards
import com.aesir.odin.domain.model.RespuestaFlashcard
import com.aesir.odin.domain.model.ResultadoFlashcards
import com.aesir.odin.domain.model.TarjetaFlashcard
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.flashcards.FinalizarFlashcardsUseCase
import com.aesir.odin.domain.usecase.flashcards.GenerarFlashcardsUseCase
import com.aesir.odin.domain.usecase.flashcards.RegistrarErrorFlashcardUseCase
import com.aesir.odin.domain.usecase.flashcards.RegistrarFlashcardsSinSuperarUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerTemasPorMundoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Foto de la pila para dibujar la pantalla; se vuelve a publicar tras cada cambio. */
data class EstadoFlashcards(
    val tarjeta: TarjetaFlashcard?,
    /** Número de la carta que se está resolviendo ("Carta 5 de 12"). */
    val numero: Int,
    val total: Int,
    /** Cartas que siguen en el mazo, sin contar la que está en juego. */
    val enMazo: Int,
    val dominadas: Int,
    val errores: Int,
    val puntos: List<EstadoTarjeta>,
    /** Posición de la tarjeta actual dentro de [puntos]. */
    val posicionActual: Int,
    /** Retroalimentación de la última respuesta; null mientras no se responde. */
    val respuesta: RespuestaFlashcard?,
    /**
     * Cuenta las cartas puestas en juego; identifica cada aparición de una
     * carta (una fallada vuelve a salir con otro turno).
     */
    val turno: Int = 0
)

/** Resumen final (paso 9). */
data class ResumenFlashcards(
    val resultado: ResultadoFlashcards,
    val precision: Int,
    val masDificiles: List<Pair<String, Int>>
)

/**
 * CU-03 Repasar conceptos con flashcards.
 *
 * Fase 1: [cargar] arma la pila (o publica [mensajeError] si el nivel no tiene tarjetas).
 * Fase 2: [responder] valida la opción elegida, da retroalimentación inmediata y,
 *   si es un error, lo registra en el historial (A5.1). [continuar] pasa a la
 *   siguiente tarjeta: la acertada sale de la pila y la fallada regresa al final.
 *   A la tercera falla de una misma tarjeta se sugiere repasar la lección (A5.2).
 * Fase 3: al vaciarse la pila se guarda el resultado y se avisa al MOD-01.
 *
 * [abandonar] (A4.1) y aceptar el repaso (A5.2) terminan sin puntaje y le
 * avisan al MOD-01 para que la entrada pagada se pierda.
 */
class FlashcardsViewModel(
    private val generarUseCase: GenerarFlashcardsUseCase,
    private val registrarErrorUseCase: RegistrarErrorFlashcardUseCase,
    private val finalizarUseCase: FinalizarFlashcardsUseCase,
    private val sinSuperarUseCase: RegistrarFlashcardsSinSuperarUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val obtenerTemaUseCase: ObtenerIntroduccionTemaUseCase? = null,
    private val obtenerMundoUseCase: ObtenerMundoUseCase? = null,
    private val obtenerTemasPorMundoUseCase: ObtenerTemasPorMundoUseCase? = null,
    private val idUsuario: String = USUARIO_LOCAL
) : ViewModel() {

    /** Tema y mundo del nivel, para el encabezado y el estilo visual. */
    data class Contexto(val tema: Tema, val mundo: Mundo)

    private var pila: PilaFlashcards? = null
    private var temaId: String? = null
    private var temasIncluidos: List<String> = emptyList()
    private var respuestaActual: RespuestaFlashcard? = null
    private var finalizando = false
    private var nivelReportado = false
    private var turno = 0

    private val _estado = MutableStateFlow<EstadoFlashcards?>(null)
    private val _resumen = MutableStateFlow<ResumenFlashcards?>(null)
    private val _mensajeError = MutableStateFlow<String?>(null)
    private val _contexto = MutableStateFlow<Contexto?>(null)
    private val _sugerenciaRepaso = MutableStateFlow<TarjetaFlashcard?>(null)
    private val _avisoRepaso = MutableStateFlow<String?>(null)
    private val _nombresTemas = MutableStateFlow<Map<String, String>>(emptyMap())

    val estado: StateFlow<EstadoFlashcards?> = _estado.asStateFlow()
    val resumen: StateFlow<ResumenFlashcards?> = _resumen.asStateFlow()
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()
    val contexto: StateFlow<Contexto?> = _contexto.asStateFlow()

    /** A5.2: tarjeta que se falló 3 veces; mientras no sea null se muestra la sugerencia. */
    val sugerenciaRepaso: StateFlow<TarjetaFlashcard?> = _sugerenciaRepaso.asStateFlow()

    /** Motivo por el que no se pudo abrir la lección sugerida (MOD-01). */
    val avisoRepaso: StateFlow<String?> = _avisoRepaso.asStateFlow()

    /** Nombre de cada tema del mundo, para mostrar de qué lección es la carta. */
    val nombresTemas: StateFlow<Map<String, String>> = _nombresTemas.asStateFlow()

    val saldo: StateFlow<Int> = moduloHidromiel.observarSaldo(idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // -----------------------------------------------------------------------
    // Fase 1 · Armar la pila
    // -----------------------------------------------------------------------

    /** Arma la pila solo la primera vez: al rotar la pantalla se conserva la sesión en curso. */
    fun iniciar(temaId: String, temasIncluidos: List<String>) {
        if (pila != null && this.temaId == temaId) return
        cargar(temaId, temasIncluidos)
    }

    fun cargar(temaId: String, temasIncluidos: List<String>) {
        this.temaId = temaId
        this.temasIncluidos = temasIncluidos
        viewModelScope.launch {
            _mensajeError.value = null
            if (_contexto.value == null) cargarContexto(temaId)
            generarUseCase(temaId, temasIncluidos)
                .onSuccess { tarjetas ->
                    pila = PilaFlashcards(tarjetas)
                    respuestaActual = null
                    finalizando = false
                    nivelReportado = false
                    _resumen.value = null
                    _sugerenciaRepaso.value = null
                    publicar()
                }
                .onFailure {
                    pila = null
                    _estado.value = null
                    _mensajeError.value = it.message ?: "No se pudieron armar las tarjetas."
                }
        }
    }

    fun reintentarCarga() {
        val tema = temaId ?: return
        cargar(tema, temasIncluidos)
    }

    private suspend fun cargarContexto(temaId: String) {
        val obtenerTema = obtenerTemaUseCase ?: return
        val obtenerMundo = obtenerMundoUseCase ?: return
        _contexto.value = runCatching {
            val tema = obtenerTema(temaId) ?: return
            Contexto(tema, obtenerMundo(tema.mundoId))
        }.getOrNull()
        val mundoId = _contexto.value?.mundo?.id ?: return
        obtenerTemasPorMundoUseCase?.let { obtener ->
            _nombresTemas.value = runCatching { obtener(mundoId).associate { it.id to it.nombre } }.getOrDefault(emptyMap())
        }
    }

    // -----------------------------------------------------------------------
    // Fase 2 · Responder y avanzar
    // -----------------------------------------------------------------------

    fun responder(indice: Int) {
        val pila = pila ?: return
        val tarjeta = pila.actual ?: return
        val respuesta = pila.responder(indice) ?: return
        respuestaActual = respuesta
        if (!respuesta.correcta) {
            viewModelScope.launch { registrarErrorUseCase(tarjeta, tarjeta.opciones[indice]) }
            if (respuesta.sugerirRepaso) _sugerenciaRepaso.value = tarjeta
        }
        publicar()
    }

    fun continuar() {
        val pila = pila ?: return
        if (respuestaActual == null || _sugerenciaRepaso.value != null) return
        pila.continuar()
        respuestaActual = null
        turno++
        if (pila.terminada) finalizar(pila) else publicar()
    }

    /** A5.2: el alumno decide seguir con las tarjetas. */
    fun seguirConTarjetas() {
        _sugerenciaRepaso.value = null
    }

    /**
     * A5.2: el alumno acepta repasar la lección. La lección se abre con las
     * reglas del MOD-01 (entrada gratis si ya se superó); si no se puede abrir,
     * las flashcards continúan y se muestra el motivo.
     * @param alRepasar recibe el id del tema de la lección.
     */
    fun repasarLeccion(alRepasar: (String) -> Unit) {
        val tarjeta = _sugerenciaRepaso.value ?: return
        val tema = temaId ?: return
        viewModelScope.launch {
            val acceso = moduloHidromiel.cobrarEntrada(idUsuario, tarjeta.temaId)
            if (!acceso.autorizado) {
                _sugerenciaRepaso.value = null
                _avisoRepaso.value = acceso.motivo
                return@launch
            }
            reportarSinSuperar(tema, ResultadoNivel.NO_SUPERADO)
            _sugerenciaRepaso.value = null
            alRepasar(tarjeta.temaId)
        }
    }

    fun limpiarAvisoRepaso() {
        _avisoRepaso.value = null
    }

    /**
     * A4.1: el alumno sale antes de vaciar la pila. Se espera a que el módulo
     * registre el abandono antes de [alTerminar], para que el roadmap ya
     * muestre el saldo correcto.
     */
    fun abandonar(alTerminar: () -> Unit) {
        viewModelScope.launch {
            val tema = temaId
            if (tema != null && pila != null && _resumen.value == null) reportarSinSuperar(tema, ResultadoNivel.ABANDONADO)
            alTerminar()
        }
    }

    // -----------------------------------------------------------------------
    // Fase 3 · Cierre
    // -----------------------------------------------------------------------

    private fun finalizar(pila: PilaFlashcards) {
        val tema = temaId ?: return
        if (finalizando) return
        finalizando = true
        nivelReportado = true
        viewModelScope.launch {
            val resultado = finalizarUseCase(tema, pila)
            _resumen.value = ResumenFlashcards(
                resultado = resultado,
                precision = pila.precision,
                masDificiles = pila.masDificiles().map { (t, fallos) -> t.concepto to fallos }
            )
            publicar()
        }
    }

    private suspend fun reportarSinSuperar(tema: String, resultado: ResultadoNivel) {
        if (nivelReportado) return
        nivelReportado = true
        sinSuperarUseCase(tema, resultado)
    }

    private fun publicar() {
        val pila = pila ?: return
        val actual = pila.actual
        val total = pila.total
        _estado.value = EstadoFlashcards(
            tarjeta = actual,
            numero = (pila.dominadasCount + 1).coerceAtMost(total),
            total = total,
            enMazo = (pila.restantes - 1).coerceAtLeast(0),
            dominadas = pila.dominadasCount,
            errores = pila.errores,
            puntos = pila.tarjetas.map { pila.estadoDe(it) },
            posicionActual = pila.tarjetas.indexOfFirst { it.id == actual?.id },
            respuesta = respuestaActual,
            turno = turno
        )
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FlashcardsViewModel(
                    generarUseCase = container.generarFlashcardsUseCase,
                    registrarErrorUseCase = container.registrarErrorFlashcardUseCase,
                    finalizarUseCase = container.finalizarFlashcardsUseCase,
                    sinSuperarUseCase = container.registrarFlashcardsSinSuperarUseCase,
                    moduloHidromiel = container.moduloHidromiel,
                    obtenerTemaUseCase = container.obtenerIntroduccionTemaUseCase,
                    obtenerMundoUseCase = container.obtenerMundoUseCase,
                    obtenerTemasPorMundoUseCase = container.obtenerTemasPorMundoUseCase
                )
            }
        }
    }
}
