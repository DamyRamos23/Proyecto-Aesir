package com.aesir.odin.ui.roadmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.domain.hidromiel.EstadoNivel
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.ResultadoAcceso
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.memorama.ValidarAccesoMemoramaUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerTemasPorMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ValidarAccesoNivelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel de RoadmapScreen (CU-01) con el MOD-01 Sistema de Hidromiel:
 *
 * 1. validarAccesoNivel(mundoId): comprueba si el mundo está desbloqueado.
 * 2. cargarRoadmap(mundoId): carga los temas y pide al módulo el estado y
 *    el costo de cada nivel (fase 1 del diagrama de secuencia MOD-01).
 * 3. intentarAbrirTema(temaId): cobra la entrada con el módulo (fase 2). Si
 *    el nivel está bloqueado o falta hidromiel, deja el motivo en
 *    mensajeBloqueo en vez de navegar.
 *
 * El Ragnarök se separa de [temas] porque no va en el anillo del escudo,
 * sino oculto en el centro.
 */
class RoadmapViewModel(
    private val obtenerTemasUseCase: ObtenerTemasPorMundoUseCase,
    private val validarAccesoNivelUseCase: ValidarAccesoNivelUseCase,
    private val obtenerMundoUseCase: ObtenerMundoUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val validarAccesoMemoramaUseCase: ValidarAccesoMemoramaUseCase? = null,
    private val idUsuario: String = USUARIO_LOCAL
) : ViewModel() {

    private val _temas = MutableStateFlow<List<Tema>>(emptyList())
    val temas: StateFlow<List<Tema>> = _temas.asStateFlow()

    private val _ragnarok = MutableStateFlow<Tema?>(null)
    val ragnarok: StateFlow<Tema?> = _ragnarok.asStateFlow()

    private val _mundo = MutableStateFlow<Mundo?>(null)
    val mundo: StateFlow<Mundo?> = _mundo.asStateFlow()

    /** Estado y costo de cada nivel, por id de tema. */
    private val _accesos = MutableStateFlow<Map<String, ResultadoAcceso>>(emptyMap())
    val accesos: StateFlow<Map<String, ResultadoAcceso>> = _accesos.asStateFlow()

    val saldo: StateFlow<Int> = moduloHidromiel.observarSaldo(idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _mensajeBloqueo = MutableStateFlow<String?>(null)
    val mensajeBloqueo: StateFlow<String?> = _mensajeBloqueo.asStateFlow()

    private var mundoIdActual: String? = null

    suspend fun validarAccesoNivel(nivelId: String): Boolean {
        val desbloqueado = validarAccesoNivelUseCase(nivelId)
        if (!desbloqueado) {
            _mensajeBloqueo.value = "Este mundo todavía está bloqueado. Supera el Ragnarök del mundo anterior para abrirlo."
        }
        return desbloqueado
    }

    suspend fun cargarRoadmap(mundoId: String) {
        mundoIdActual = mundoId
        _mundo.value = obtenerMundoUseCase(mundoId)
        actualizarAccesos()
    }

    /** @return true si se puede entrar (se cobró la entrada o era gratis). */
    suspend fun intentarAbrirTema(temaId: String): Boolean {
        val resultado = moduloHidromiel.cobrarEntrada(idUsuario, temaId)
        if (!resultado.autorizado) _mensajeBloqueo.value = resultado.motivo
        actualizarAccesos()
        return resultado.autorizado
    }

    /**
     * Entrada a cualquier nivel. Los memoramas pasan primero por
     * intentarAbrirMemorama (CU-04); el resto, directo al MOD-01.
     */
    suspend fun intentarAbrirNivel(tema: Tema): Boolean =
        if (tema.esMemorama) intentarAbrirMemorama(tema.mundoId, tema.id) else intentarAbrirTema(tema.id)

    /**
     * CU-04, RF-05: valida que el mundo esté desbloqueado y tenga memorama y,
     * si es así, pide al MOD-01 que autorice (y cobre) la entrada al nivel.
     */
    suspend fun intentarAbrirMemorama(mundoId: String, nivelId: String): Boolean {
        val validar = validarAccesoMemoramaUseCase
        if (validar != null && !validar(mundoId)) {
            _mensajeBloqueo.value = "Este memorama todavía no está disponible."
            return false
        }
        return intentarAbrirTema(nivelId)
    }

    fun requierePago(temaId: String): Boolean =
        _accesos.value[temaId]?.let { it.estado == EstadoNivel.DISPONIBLE && it.costo > 0 } == true

    fun limpiarMensajeBloqueo() {
        _mensajeBloqueo.value = null
    }

    private suspend fun actualizarAccesos() {
        val mundoId = mundoIdActual ?: return
        _accesos.value = moduloHidromiel.obtenerEstadoRoadmap(idUsuario, mundoId).associateBy { it.idNivel }
        // Las columnas completado/desbloqueado las actualiza el módulo; se recargan
        // para que el progreso de la cabecera coincida.
        val todos = obtenerTemasUseCase(mundoId)
        // Lecciones (anillo interior) primero y luego minijuegos (anillo exterior)
        _temas.value = todos.filterNot { it.esRagnarok }.sortedWith(compareBy({ it.anillo }, { it.orden }))
        _ragnarok.value = todos.firstOrNull { it.esRagnarok }
    }

    companion object {
        fun factory(
            obtenerTemasUseCase: ObtenerTemasPorMundoUseCase,
            validarAccesoNivelUseCase: ValidarAccesoNivelUseCase,
            obtenerMundoUseCase: ObtenerMundoUseCase,
            moduloHidromiel: ModuloHidromiel,
            validarAccesoMemoramaUseCase: ValidarAccesoMemoramaUseCase? = null
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RoadmapViewModel(
                    obtenerTemasUseCase,
                    validarAccesoNivelUseCase,
                    obtenerMundoUseCase,
                    moduloHidromiel,
                    validarAccesoMemoramaUseCase
                )
            }
        }
    }
}
