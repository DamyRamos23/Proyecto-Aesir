package com.aesir.odin.ui.leccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.EstadoNivel
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.ResultadoAcceso
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.leccion.ObtenerLeccionUseCase
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
 * Resumen de una lección completada (RF-21, RF-22). Los números (puntaje,
 * errores, total) llegan por navegación; este ViewModel solo carga el tema,
 * el mundo y los temas del mundo para la animación de la piedra rúnica, y
 * pide al MOD-01 el estado de los niveles para ofrecer el siguiente nivel
 * disponible con su costo de entrada.
 */
class ResumenLeccionViewModel(
    private val obtenerLeccionUseCase: ObtenerLeccionUseCase,
    private val obtenerTemaUseCase: ObtenerIntroduccionTemaUseCase,
    private val obtenerMundoUseCase: ObtenerMundoUseCase,
    private val obtenerTemasPorMundoUseCase: ObtenerTemasPorMundoUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL
) : ViewModel() {

    /**
     * @param temas temas normales del mundo (sin el Ragnarök).
     * @param accesos estado y costo de cada nivel según el MOD-01.
     */
    data class ContextoResumen(
        val tema: Tema,
        val mundo: Mundo,
        val temas: List<Tema>,
        val accesos: Map<String, ResultadoAcceso>,
        val idRagnarok: String? = null
    ) {
        /**
         * Siguiente nivel que ya se puede jugar y aún no se supera: se busca a
         * partir del actual, primero en las lecciones y luego en los minijuegos.
         */
        val siguiente: Tema?
            get() {
                val ordenados = temas.sortedWith(compareBy({ it.anillo }, { it.orden }))
                val i = ordenados.indexOfFirst { it.id == tema.id }
                val desdeActual = ordenados.drop(i + 1) + ordenados.take(i + 1)
                return desdeActual.firstOrNull {
                    it.id != tema.id &&
                        (accesos[it.id]?.estado == EstadoNivel.DISPONIBLE || accesos[it.id]?.estado == EstadoNivel.GRATUITO)
                }
            }

        val accesoSiguiente: ResultadoAcceso? get() = siguiente?.let { accesos[it.id] }

        val ragnarokDisponible: Boolean
            get() = idRagnarok != null && accesos[idRagnarok]?.estado == EstadoNivel.DISPONIBLE
    }

    val saldo: StateFlow<Int> = moduloHidromiel.observarSaldo(idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _mensaje = MutableStateFlow<String?>(null)
    /** Motivo por el que no se pudo entrar al siguiente tema (E1). */
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _contexto = MutableStateFlow<ContextoResumen?>(null)
    val contexto: StateFlow<ContextoResumen?> = _contexto.asStateFlow()

    private val _fallo = MutableStateFlow(false)
    val fallo: StateFlow<Boolean> = _fallo.asStateFlow()

    fun cargar(leccionId: String) {
        viewModelScope.launch {
            val resultado = runCatching {
                val leccion = obtenerLeccionUseCase(leccionId) ?: throw IllegalStateException("Lección no encontrada")
                val tema = obtenerTemaUseCase(leccion.temaId) ?: throw IllegalStateException("Tema no encontrado")
                val todos = obtenerTemasPorMundoUseCase(tema.mundoId)
                ContextoResumen(
                    tema = tema,
                    mundo = obtenerMundoUseCase(tema.mundoId),
                    temas = todos.filterNot { it.esRagnarok },
                    accesos = moduloHidromiel.obtenerEstadoRoadmap(idUsuario, tema.mundoId).associateBy { it.idNivel },
                    idRagnarok = todos.firstOrNull { it.esRagnarok }?.id
                )
            }
            _contexto.value = resultado.getOrNull()
            _fallo.value = resultado.isFailure
        }
    }

    /** Cobra la entrada del tema con el MOD-01 y solo navega si se autorizó. */
    fun abrirTema(temaId: String, alAutorizar: (String) -> Unit) {
        viewModelScope.launch {
            val resultado = moduloHidromiel.cobrarEntrada(idUsuario, temaId)
            if (resultado.autorizado) alAutorizar(temaId) else _mensaje.value = resultado.motivo
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    companion object {
        fun factory(container: AppContainer, leccionId: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ResumenLeccionViewModel(
                        obtenerLeccionUseCase = container.obtenerLeccionUseCase,
                        obtenerTemaUseCase = container.obtenerIntroduccionTemaUseCase,
                        obtenerMundoUseCase = container.obtenerMundoUseCase,
                        obtenerTemasPorMundoUseCase = container.obtenerTemasPorMundoUseCase,
                        moduloHidromiel = container.moduloHidromiel
                    ).apply { cargar(leccionId) }
                }
            }
    }
}
