package com.aesir.odin.ui.duelo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.AccesoDuelo
import com.aesir.odin.domain.model.CierreDuelo
import com.aesir.odin.domain.model.DueloEnigmas
import com.aesir.odin.domain.model.MotivoSinAcceso
import com.aesir.odin.domain.model.PreguntaEnigma
import com.aesir.odin.domain.model.RespuestaGigante
import com.aesir.odin.domain.model.TarjetaConcepto
import com.aesir.odin.domain.usecase.duelo.AbandonarDueloUseCase
import com.aesir.odin.domain.usecase.duelo.ApostarDueloUseCase
import com.aesir.odin.domain.usecase.duelo.ArmarDueloUseCase
import com.aesir.odin.domain.usecase.duelo.FinalizarDueloUseCase
import com.aesir.odin.domain.usecase.duelo.PreguntarGiganteUseCase
import com.aesir.odin.domain.usecase.duelo.RevelarConceptoUseCase
import com.aesir.odin.domain.usecase.duelo.ValidarAccesoDueloUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EstadoDuelo {
    CARGANDO,
    SIN_RAGNAROK_SUPERADO,
    LIMITE_DIARIO_ALCANZADO,
    HIDROMIEL_INSUFICIENTE,
    CONCEPTOS_INSUFICIENTES,
    CONFIRMANDO_APUESTA,
    EN_CURSO,
    ULTIMA_ADIVINANZA,
    PREGUNTAS_AGOTADAS,
    CONFIRMANDO_ABANDONO,
    TERMINADO,
    ERROR
}

/** Foto inmutable del duelo para la pantalla (el DueloEnigmas cambia en cada jugada). */
data class TableroDuelo(
    val tarjetas: List<TarjetaConcepto>,
    val sorbos: Int,
    val preguntas: List<PreguntaEnigma>,
    /** Nombre de la tarjeta de la última adivinanza fallida (A12.1), para avisarlo. */
    val ultimoFallo: String?
)

/** ViewModel del CU-13: sigue el diagrama de secuencia (cargarDuelo → confirmarApuesta → preguntar/voltear → revelar). */
class DueloEnigmasViewModel(
    private val validarAcceso: ValidarAccesoDueloUseCase,
    private val armarDuelo: ArmarDueloUseCase,
    private val apostarDuelo: ApostarDueloUseCase,
    private val preguntarGigante: PreguntarGiganteUseCase,
    private val revelarConcepto: RevelarConceptoUseCase,
    private val finalizarDuelo: FinalizarDueloUseCase,
    private val abandonarDuelo: AbandonarDueloUseCase,
    val saldo: Flow<Int>
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoDuelo.CARGANDO)
    val estado: StateFlow<EstadoDuelo> = _estado.asStateFlow()

    private val _acceso = MutableStateFlow<AccesoDuelo?>(null)
    val acceso: StateFlow<AccesoDuelo?> = _acceso.asStateFlow()

    private val _tablero = MutableStateFlow<TableroDuelo?>(null)
    val tablero: StateFlow<TableroDuelo?> = _tablero.asStateFlow()

    private val _ultimaRespuesta = MutableStateFlow<Pair<String, RespuestaGigante>?>(null)
    /** Texto de la pregunta y la respuesta del gigante. */
    val ultimaRespuesta: StateFlow<Pair<String, RespuestaGigante>?> = _ultimaRespuesta.asStateFlow()

    private val _cierre = MutableStateFlow<CierreDuelo?>(null)
    val cierre: StateFlow<CierreDuelo?> = _cierre.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    /** Motivo de E4.1 o de un error al armar o cobrar. */
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _abandonado = MutableStateFlow(false)
    val abandonado: StateFlow<Boolean> = _abandonado.asStateFlow()

    private var duelo: DueloEnigmas? = null
    private var estadoAntesDeAbandonar = EstadoDuelo.EN_CURSO
    private var ultimoFallo: String? = null

    /** Pasos 1 y 2. */
    fun cargarDuelo() {
        _estado.value = EstadoDuelo.CARGANDO
        viewModelScope.launch {
            val acceso = runCatching { validarAcceso() }.getOrElse { return@launch error(it) }
            _acceso.value = acceso
            _estado.value = when (acceso.motivo) {
                null -> EstadoDuelo.CONFIRMANDO_APUESTA
                MotivoSinAcceso.SIN_RAGNAROK_SUPERADO -> EstadoDuelo.SIN_RAGNAROK_SUPERADO
                MotivoSinAcceso.LIMITE_DIARIO_ALCANZADO -> EstadoDuelo.LIMITE_DIARIO_ALCANZADO
                MotivoSinAcceso.HIDROMIEL_INSUFICIENTE -> EstadoDuelo.HIDROMIEL_INSUFICIENTE
            }
        }
    }

    /** Paso 3: el tablero se arma antes de cobrar (si no hay 12 conceptos no se cobra nada). */
    fun confirmarApuesta() {
        if (_estado.value != EstadoDuelo.CONFIRMANDO_APUESTA) return
        _estado.value = EstadoDuelo.CARGANDO
        viewModelScope.launch {
            val nuevo = armarDuelo().getOrElse {
                _mensaje.value = it.message
                _estado.value = EstadoDuelo.CONCEPTOS_INSUFICIENTES
                return@launch
            }
            val apuesta = runCatching { apostarDuelo(nuevo) }.getOrElse { return@launch error(it) }
            if (!apuesta.autorizado) {
                // El saldo cambió entre los pasos 2 y 4
                _mensaje.value = apuesta.motivo
                _acceso.value = _acceso.value?.copy(permitido = false, faltante = apuesta.faltante, motivo = MotivoSinAcceso.HIDROMIEL_INSUFICIENTE)
                _estado.value = EstadoDuelo.HIDROMIEL_INSUFICIENTE
                return@launch
            }
            duelo = nuevo
            ultimoFallo = null
            _ultimaRespuesta.value = null
            publicar()
            _estado.value = EstadoDuelo.EN_CURSO
        }
    }

    /** Pasos 7 y 8. */
    fun preguntar(preguntaId: String) {
        val d = duelo ?: return
        if (!d.puedePreguntar()) return
        val texto = d.preguntasDisponibles().firstOrNull { it.id == preguntaId }?.texto ?: return
        _ultimaRespuesta.value = texto to preguntarGigante(d, preguntaId)
        ultimoFallo = null
        publicar()
        _estado.value = estadoDeJuego(d)
    }

    /** Paso 9 (A9.1 y A9.2). */
    fun voltearTarjeta(conceptoId: String) {
        val d = duelo ?: return
        d.voltear(conceptoId)
        publicar()
    }

    /** Pasos 11 y 12 (A12.1 si falla). */
    fun revelar(conceptoId: String) {
        val d = duelo ?: return
        val r = revelarConcepto(d, conceptoId)
        when {
            r.correcta -> terminarDuelo(ganado = true)
            r.dueloTerminado -> terminarDuelo(ganado = false, tarjetaElegida = conceptoId)
            else -> {
                ultimoFallo = d.tarjetas.firstOrNull { it.concepto.id == conceptoId }?.concepto?.nombre
                publicar()
                _estado.value = estadoDeJuego(d)
            }
        }
    }

    /** E7.1 */
    fun solicitarAbandono() {
        if (duelo == null || _estado.value == EstadoDuelo.TERMINADO || _estado.value == EstadoDuelo.CONFIRMANDO_ABANDONO) return
        estadoAntesDeAbandonar = _estado.value
        _estado.value = EstadoDuelo.CONFIRMANDO_ABANDONO
    }

    fun confirmarAbandono() {
        val d = duelo ?: return
        viewModelScope.launch {
            runCatching { abandonarDuelo(d) }
            duelo = null
            _abandonado.value = true
        }
    }

    fun cancelarAbandono() {
        if (_estado.value == EstadoDuelo.CONFIRMANDO_ABANDONO) _estado.value = estadoAntesDeAbandonar
    }

    private fun terminarDuelo(ganado: Boolean, tarjetaElegida: String? = null) {
        val d = duelo ?: return
        _estado.value = EstadoDuelo.CARGANDO
        viewModelScope.launch {
            val cierre = runCatching { finalizarDuelo(d, ganado, tarjetaElegida) }.getOrElse { return@launch error(it) }
            duelo = null
            _cierre.value = cierre
            _estado.value = EstadoDuelo.TERMINADO
        }
    }

    private fun estadoDeJuego(d: DueloEnigmas) = when {
        d.ultimaAdivinanza -> EstadoDuelo.ULTIMA_ADIVINANZA
        d.preguntasDisponibles().isEmpty() -> EstadoDuelo.PREGUNTAS_AGOTADAS
        else -> EstadoDuelo.EN_CURSO
    }

    private fun publicar() {
        val d = duelo ?: return
        _tablero.value = TableroDuelo(d.tarjetas, d.cuerno.sorbos, d.preguntasDisponibles(), ultimoFallo)
    }

    private fun error(e: Throwable) {
        _mensaje.value = e.message ?: "No se pudo completar la acción."
        _estado.value = EstadoDuelo.ERROR
    }

    companion object {
        fun factory(c: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DueloEnigmasViewModel(
                    c.validarAccesoDueloUseCase, c.armarDueloUseCase, c.apostarDueloUseCase,
                    PreguntarGiganteUseCase(), RevelarConceptoUseCase(), c.finalizarDueloUseCase,
                    c.abandonarDueloUseCase, c.moduloHidromiel.observarSaldo(USUARIO_LOCAL)
                )
            }
        }
    }
}
