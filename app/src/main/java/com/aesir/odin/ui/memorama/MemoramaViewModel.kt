package com.aesir.odin.ui.memorama

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.EstadoTarjeta
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.ResultadoMemorama
import com.aesir.odin.domain.model.TarjetaMemorama
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.memorama.FinalizarMemoramaUseCase
import com.aesir.odin.domain.usecase.memorama.GenerarMemoramaUseCase
import com.aesir.odin.domain.usecase.memorama.VerificarParejaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * CU-04 Resolver memorama de conceptos (MemoramaViewModel del diagrama de clases).
 *
 * - iniciar: genera el tablero de 6 pares y registra el inicio (RF-06, RF-07).
 * - seleccionarTarjeta: voltea la primera y la segunda tarjeta (RF-08, RF-09),
 *   verifica si son pareja (RF-10) y las deja descubiertas (RF-11) o las
 *   vuelve a ocultar tras [TIEMPO_VISTA_MS] (A7.1, RF-12).
 * - Al encontrar todos los pares registra el fin y finaliza (RF-13 a RF-17).
 * - abandonar: descarta el intento sin puntos ni registro (A9.1, RF-18).
 *
 * Además del diagrama: [nivelId] (el memorama es un nivel del árbol), el
 * contexto para el encabezado y el aviso de abandono al MOD-01.
 */
class MemoramaViewModel(
    private val generarMemoramaUseCase: GenerarMemoramaUseCase,
    private val verificarParejaUseCase: VerificarParejaUseCase,
    private val finalizarUseCase: FinalizarMemoramaUseCase,
    private val moduloHidromiel: ModuloHidromiel? = null,
    private val obtenerTemaUseCase: ObtenerIntroduccionTemaUseCase? = null,
    private val obtenerMundoUseCase: ObtenerMundoUseCase? = null,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) : ViewModel() {

    /** Tema y mundo del nivel, solo para el encabezado y el estilo visual. */
    data class Contexto(val tema: Tema?, val mundo: Mundo?)

    /** Par que no coincidió; [numero] cambia en cada fallo (la pantalla lo hace temblar). */
    data class Fallo(val primera: Int, val segunda: Int, val numero: Int)

    /** Indicación que se muestra bajo el tablero. */
    enum class Aviso { ELIGE_UNA, ELIGE_SEGUNDA, PAREJA, NO_SON_PAREJA, COMPLETO }

    private val _fallo = MutableStateFlow<Fallo?>(null)
    val fallo: StateFlow<Fallo?> = _fallo.asStateFlow()

    private val _aviso = MutableStateFlow(Aviso.ELIGE_UNA)
    val aviso: StateFlow<Aviso> = _aviso.asStateFlow()

    private var contadorFallos = 0

    val saldo: StateFlow<Int> = (moduloHidromiel?.observarSaldo(idUsuario) ?: kotlinx.coroutines.flow.flowOf(0))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _tarjetas = MutableStateFlow<List<TarjetaMemorama>>(emptyList())
    val tarjetas: StateFlow<List<TarjetaMemorama>> = _tarjetas.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoMemorama?>(null)
    val resultado: StateFlow<ResultadoMemorama?> = _resultado.asStateFlow()

    private val _paresEncontrados = MutableStateFlow(0)
    val paresEncontrados: StateFlow<Int> = _paresEncontrados.asStateFlow()

    private val _contexto = MutableStateFlow<Contexto?>(null)
    val contexto: StateFlow<Contexto?> = _contexto.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private var mundoId: String = ""
    private var nivelId: String = ""
    private var primeraSeleccion: Int? = null
    private var bloqueado = false
    private var finalizando = false
    private var abandonado = false

    /** Momento de inicio (RF-07). Lo usa la pantalla para mostrar el cronómetro. */
    var tiempoInicio: Long = 0L
        private set

    val enCurso: Boolean
        get() = _tarjetas.value.isNotEmpty() && _resultado.value == null && !abandonado

    fun iniciar(mundoId: String, nivelId: String) {
        if (_tarjetas.value.isNotEmpty() || _resultado.value != null) return // ya iniciado (p. ej. al rotar)
        this.mundoId = mundoId
        this.nivelId = nivelId
        viewModelScope.launch {
            cargarContexto()
            val tarjetas = generarMemoramaUseCase(mundoId)
            if (tarjetas.isEmpty()) {
                _mensajeError.value = "Este mundo todavía no tiene suficientes conceptos para armar el memorama."
                return@launch
            }
            _tarjetas.value = tarjetas
            _paresEncontrados.value = 0
            primeraSeleccion = null
            bloqueado = false
            tiempoInicio = reloj()
        }
    }

    fun seleccionarTarjeta(indice: Int) {
        if (bloqueado || !enCurso) return
        val tarjeta = _tarjetas.value.getOrNull(indice) ?: return
        if (tarjeta.estado != EstadoTarjeta.OCULTA) return

        val primera = primeraSeleccion
        if (primera == null) {
            // Primera tarjeta (RF-08)
            primeraSeleccion = indice
            cambiarEstado(EstadoTarjeta.VISIBLE, indice)
            _aviso.value = Aviso.ELIGE_SEGUNDA
            return
        }

        // Segunda tarjeta (RF-09) y verificación (RF-10)
        bloqueado = true
        cambiarEstado(EstadoTarjeta.VISIBLE, indice)
        val esPar = verificarParejaUseCase(_tarjetas.value[primera], _tarjetas.value[indice])

        viewModelScope.launch {
            if (esPar) {
                // Se deja terminar el giro antes de marcarlas como encontradas
                delay(360)
                if (abandonado) return@launch
                cambiarEstado(EstadoTarjeta.ENCONTRADA, primera, indice) // RF-11
                _paresEncontrados.value += 1
                val completo = _paresEncontrados.value == _tarjetas.value.size / 2
                _aviso.value = if (completo) Aviso.COMPLETO else Aviso.PAREJA
            } else {
                delay(380)
                if (abandonado) return@launch
                contadorFallos++
                _fallo.value = Fallo(primera, indice, contadorFallos)
                _aviso.value = Aviso.NO_SON_PAREJA
                delay(TIEMPO_VISTA_MS) // A7.1: el alumno las ve un momento
                if (abandonado) return@launch
                cambiarEstado(EstadoTarjeta.OCULTA, primera, indice) // RF-12
                _aviso.value = Aviso.ELIGE_UNA
            }
            primeraSeleccion = null
            bloqueado = false

            if (_paresEncontrados.value == _tarjetas.value.size / 2) finalizar()
        }
    }

    /**
     * A9.1 / RF-18: descarta el intento (tarjetas, selección, pares y tiempo),
     * no guarda resultado ni da puntos. Para el MOD-01 el nivel queda
     * abandonado, igual que en el crucigrama: si se pagó entrada, se pierde.
     */
    fun abandonar(alTerminar: () -> Unit) {
        viewModelScope.launch {
            val estabaEnCurso = enCurso && !finalizando
            abandonado = true
            _tarjetas.value = emptyList()
            _paresEncontrados.value = 0
            primeraSeleccion = null
            bloqueado = false
            tiempoInicio = 0L
            if (estabaEnCurso && nivelId.isNotEmpty()) {
                moduloHidromiel?.alFinalizarNivel(NivelFinalizado(idUsuario, nivelId, 0, ResultadoNivel.ABANDONADO))
            }
            alTerminar()
        }
    }

    // -----------------------------------------------------------------------
    // Internos
    // -----------------------------------------------------------------------

    private suspend fun finalizar() {
        if (finalizando) return
        finalizando = true
        val tiempoFin = reloj() // RF-13
        _resultado.value = finalizarUseCase(mundoId, nivelId, tiempoInicio, tiempoFin)
    }

    private fun cambiarEstado(estado: EstadoTarjeta, vararg indices: Int) {
        _tarjetas.value = _tarjetas.value.mapIndexed { i, t -> if (i in indices) t.copy(estado = estado) else t }
    }

    private suspend fun cargarContexto() {
        if (_contexto.value != null) return
        val tema = runCatching { obtenerTemaUseCase?.invoke(nivelId) }.getOrNull()
        val mundo = runCatching { obtenerMundoUseCase?.invoke(mundoId) }.getOrNull()
        _contexto.value = Contexto(tema, mundo)
    }

    companion object {
        const val TIEMPO_VISTA_MS = 1_100L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MemoramaViewModel(
                    generarMemoramaUseCase = container.generarMemoramaUseCase,
                    verificarParejaUseCase = container.verificarParejaUseCase,
                    finalizarUseCase = container.finalizarMemoramaUseCase,
                    moduloHidromiel = container.moduloHidromiel,
                    obtenerTemaUseCase = container.obtenerIntroduccionTemaUseCase,
                    obtenerMundoUseCase = container.obtenerMundoUseCase
                )
            }
        }
    }
}
