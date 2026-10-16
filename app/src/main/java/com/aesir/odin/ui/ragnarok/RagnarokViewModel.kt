package com.aesir.odin.ui.ragnarok

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.DisponibilidadReto
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.PreguntaReto
import com.aesir.odin.domain.model.ResultadoRespuesta
import com.aesir.odin.domain.model.ResultadoRetoFinMundo
import com.aesir.odin.domain.model.RetoFinMundo
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.ragnarok.AbandonarRetoFinMundoUseCase
import com.aesir.odin.domain.usecase.ragnarok.FinalizarRetoFinMundoUseCase
import com.aesir.odin.domain.usecase.ragnarok.ObtenerRetoFinMundoUseCase
import com.aesir.odin.domain.usecase.ragnarok.RegistrarErrorRetoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Pantallas del reto: entrada, batalla y resultado (más los casos en que no se puede abrir). */
sealed interface RagnarokUi {
    data object Cargando : RagnarokUi

    /** A2.1: faltan temas por completar. */
    data class Bloqueado(val pendientes: List<String>) : RagnarokUi
    data class Error(val mensaje: String) : RagnarokUi

    /** Paso 4: instrucciones y puntaje mínimo. */
    data class Entrada(val reto: RetoFinMundo) : RagnarokUi

    /** Pasos 5 a 8: una pregunta a la vez. */
    data object Batalla : RagnarokUi
    data class Resultado(val resultado: ResultadoRetoFinMundo) : RagnarokUi
}

/** Lo que la pantalla de batalla necesita saber de la pregunta en curso. */
data class EstadoBatalla(
    val pregunta: PreguntaReto,
    val numero: Int,
    val total: Int,
    val aciertos: Int,
    val errores: Int,
    /** Resultado por pregunta (null = todavía sin contestar), para los símbolos de progreso. */
    val progreso: List<EstadoRespuesta?>,
    /** Retroalimentación de la pregunta actual; null mientras no se confirma. */
    val respuesta: ResultadoRespuesta?,
    /** Opciones que el alumno marcó al confirmar. */
    val seleccionadas: List<Int>,
    val esUltima: Boolean
)

/**
 * CU-08 Resolver reto de fin de mundo (Ragnarök).
 *
 * [iniciar] verifica que el reto esté disponible y lo arma (pasos 2 y 3, A2.1).
 * [iniciarBatalla] pasa de las instrucciones a las preguntas. [responder]
 * valida con retroalimentación inmediata y registra los errores con su tema
 * (A7.1); [siguiente] avanza y, tras la última pregunta, calcula el puntaje.
 * Con 70 o más se supera el reto; si no (A10.1), [reintentar] arma otro
 * conjunto de preguntas y cobra de nuevo la entrada al MOD-01.
 * [abandonar] (A6.1) registra el intento como no superado.
 */
class RagnarokViewModel(
    private val obtenerRetoUseCase: ObtenerRetoFinMundoUseCase,
    private val registrarErrorUseCase: RegistrarErrorRetoUseCase,
    private val finalizarUseCase: FinalizarRetoFinMundoUseCase,
    private val abandonarUseCase: AbandonarRetoFinMundoUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val obtenerTemaUseCase: ObtenerIntroduccionTemaUseCase,
    private val obtenerMundoUseCase: ObtenerMundoUseCase,
    private val idUsuario: String = USUARIO_LOCAL
) : ViewModel() {

    /** Tema (el Ragnarök) y mundo del reto, para el encabezado y el estilo visual. */
    data class Contexto(val tema: Tema, val mundo: Mundo)

    private val validarRespuestaUseCase = ValidarRespuestaUseCase()

    private val _ui = MutableStateFlow<RagnarokUi>(RagnarokUi.Cargando)
    private val _batalla = MutableStateFlow<EstadoBatalla?>(null)
    private val _contexto = MutableStateFlow<Contexto?>(null)
    private val _aviso = MutableStateFlow<String?>(null)

    val ui: StateFlow<RagnarokUi> = _ui.asStateFlow()
    val batalla: StateFlow<EstadoBatalla?> = _batalla.asStateFlow()
    val contexto: StateFlow<Contexto?> = _contexto.asStateFlow()

    /** Mensaje del MOD-01 cuando no se puede reintentar (p. ej. falta hidromiel). */
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private var temaId: String? = null
    private var reto: RetoFinMundo? = null
    private var indice = 0
    private val respuestas = mutableListOf<Pair<PreguntaReto, EstadoRespuesta>>()
    private var ultimaRespuesta: ResultadoRespuesta? = null
    private var seleccionadas: List<Int> = emptyList()
    private var terminado = false
    private var finalizando = false

    /** El reto sigue en curso (entrada o batalla): salir cuenta como abandonar. */
    val enCurso: Boolean get() = _ui.value is RagnarokUi.Entrada || _ui.value is RagnarokUi.Batalla

    // Fase 1 · Disponibilidad y armado del reto

    /** Arma el reto solo la primera vez: al volver a la pantalla se conserva el intento en curso. */
    fun iniciar(temaId: String) {
        if (reto != null && this.temaId == temaId) return
        cargar(temaId)
    }

    fun cargar(temaId: String) {
        this.temaId = temaId
        _ui.value = RagnarokUi.Cargando
        viewModelScope.launch {
            val tema = runCatching { obtenerTemaUseCase(temaId) }.getOrNull()
            if (tema == null) {
                _ui.value = RagnarokUi.Error("No se encontró el reto de fin de mundo.")
                return@launch
            }
            runCatching { obtenerMundoUseCase(tema.mundoId) }.getOrNull()?.let { _contexto.value = Contexto(tema, it) }
            armar(tema.mundoId, emptySet())
        }
    }

    private suspend fun armar(mundoId: String, excluir: Set<String>) {
        when (val d = obtenerRetoUseCase(mundoId, excluir)) {
            is DisponibilidadReto.Disponible -> {
                reto = d.reto
                reiniciar()
                _ui.value = RagnarokUi.Entrada(d.reto)
            }
            is DisponibilidadReto.Bloqueado -> _ui.value = RagnarokUi.Bloqueado(d.temasPendientes)
            DisponibilidadReto.SinPreguntas -> _ui.value = RagnarokUi.Error("Este mundo todavía no tiene preguntas para el reto.")
        }
    }

    private fun reiniciar() {
        indice = 0
        respuestas.clear()
        ultimaRespuesta = null
        seleccionadas = emptyList()
        terminado = false
        finalizando = false
        _batalla.value = null
    }

    // Fase 2 · Preguntas

    fun iniciarBatalla() {
        if (_ui.value !is RagnarokUi.Entrada) return
        _ui.value = RagnarokUi.Batalla
        publicarBatalla()
    }

    fun responder(opciones: List<Int>) {
        val reto = reto ?: return
        if (_ui.value != RagnarokUi.Batalla || ultimaRespuesta != null || indice >= reto.preguntas.size || opciones.isEmpty()) return
        val pregunta = reto.preguntas[indice]
        val resultado = validarRespuestaUseCase(opciones, pregunta.respuestasCorrectas)
        ultimaRespuesta = resultado
        seleccionadas = opciones
        respuestas += pregunta to resultado.estado
        if (resultado.estado != EstadoRespuesta.CORRECTA) {
            viewModelScope.launch { runCatching { registrarErrorUseCase(pregunta, resultado.estado) } }
        }
        publicarBatalla()
    }

    fun siguiente() {
        val reto = reto ?: return
        if (ultimaRespuesta == null) return
        if (indice + 1 < reto.preguntas.size) {
            indice++
            ultimaRespuesta = null
            seleccionadas = emptyList()
            publicarBatalla()
        } else {
            finalizar()
        }
    }

    // Fase 3 · Resultado, reintento y abandono

    private fun finalizar() {
        val reto = reto ?: return
        if (finalizando) return
        finalizando = true
        viewModelScope.launch {
            val resultado = finalizarUseCase(reto, respuestas.toList())
            terminado = true
            _ui.value = RagnarokUi.Resultado(resultado)
        }
    }

    /**
     * A10.1 paso 4: el alumno reintenta. Cada intento cuesta la entrada del
     * Ragnarök (MOD-01); si no se puede pagar se deja el motivo en [aviso].
     */
    fun reintentar() {
        val tema = temaId ?: return
        val actual = reto ?: return
        if (_ui.value !is RagnarokUi.Resultado) return
        viewModelScope.launch {
            val acceso = moduloHidromiel.cobrarEntrada(idUsuario, tema)
            if (!acceso.autorizado) {
                _aviso.value = acceso.motivo
                return@launch
            }
            armar(actual.mundoId, actual.preguntas.map { it.id }.toSet())
        }
    }

    fun limpiarAviso() {
        _aviso.value = null
    }

    /**
     * A6.1: el alumno sale antes de terminar. Si ya se había terminado no pasa
     * nada con el MOD-01. El módulo registra el abandono antes de [alTerminar],
     * para que el roadmap ya muestre el saldo correcto.
     */
    fun abandonar(alTerminar: () -> Unit) {
        val reto = reto
        if (reto == null || terminado) {
            alTerminar()
            return
        }
        terminado = true
        viewModelScope.launch {
            runCatching { abandonarUseCase(reto, respuestas.count { it.second == EstadoRespuesta.CORRECTA }) }
            alTerminar()
        }
    }

    private fun publicarBatalla() {
        val reto = reto ?: return
        if (indice >= reto.preguntas.size) return
        val aciertos = respuestas.count { it.second == EstadoRespuesta.CORRECTA }
        _batalla.value = EstadoBatalla(
            pregunta = reto.preguntas[indice],
            numero = indice + 1,
            total = reto.preguntas.size,
            aciertos = aciertos,
            errores = respuestas.size - aciertos,
            progreso = reto.preguntas.indices.map { i -> respuestas.getOrNull(i)?.second },
            respuesta = ultimaRespuesta,
            seleccionadas = seleccionadas,
            esUltima = indice == reto.preguntas.size - 1
        )
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RagnarokViewModel(
                    obtenerRetoUseCase = container.obtenerRetoFinMundoUseCase,
                    registrarErrorUseCase = container.registrarErrorRetoUseCase,
                    finalizarUseCase = container.finalizarRetoFinMundoUseCase,
                    abandonarUseCase = container.abandonarRetoFinMundoUseCase,
                    moduloHidromiel = container.moduloHidromiel,
                    obtenerTemaUseCase = container.obtenerIntroduccionTemaUseCase,
                    obtenerMundoUseCase = container.obtenerMundoUseCase
                )
            }
        }
    }
}
