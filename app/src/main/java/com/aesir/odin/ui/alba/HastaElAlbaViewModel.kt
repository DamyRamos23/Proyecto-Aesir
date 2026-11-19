package com.aesir.odin.ui.alba

import com.aesir.odin.domain.sonido.ModuloSonido
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.AccesoAlba
import com.aesir.odin.domain.model.Artefacto
import com.aesir.odin.domain.model.Desenlace
import com.aesir.odin.domain.model.EfectoArtefacto
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.MotivoBloqueoAlba
import com.aesir.odin.domain.model.NocheAlba
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.ResultadoNoche
import com.aesir.odin.domain.usecase.alba.ApostarAlbaUseCase
import com.aesir.odin.domain.usecase.alba.CaerEnBatallaAlbaUseCase
import com.aesir.odin.domain.usecase.alba.CobrarNocheAlbaUseCase
import com.aesir.odin.domain.usecase.alba.ObtenerPreguntaAlbaUseCase
import com.aesir.odin.domain.usecase.alba.ResponderPreguntaAlbaUseCase
import com.aesir.odin.domain.usecase.alba.SeguirPeleandoAlbaUseCase
import com.aesir.odin.domain.usecase.alba.UsarArtefactoAlbaUseCase
import com.aesir.odin.domain.usecase.alba.ValidarAccesoAlbaUseCase
import com.aesir.odin.domain.usecase.alba.premioNetoAlCobrar
import com.aesir.odin.ui.components.odin.PulsoEscena
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

/** Pantallas y momentos de la noche. */
enum class EstadoAlba {
    CARGANDO,
    VALHALLA_CERRADO,
    NOCHE_JUGADA,
    HIDROMIEL_INSUFICIENTE,
    SIN_PREGUNTAS,
    APOSTANDO,
    /** Pasos 8 a 10: la pregunta está en pantalla y corre el contador. */
    PREGUNTA,
    /** Pasos 11 y 12, A11.1: se muestra si fue golpe o escudo roto antes de seguir. */
    RESPUESTA,
    /** Paso 15: «¿Vuelves al banquete o sigues peleando?». */
    DECISION,
    /** A18.1: llegó al alba con Draupnir sin usar. */
    OFRECER_DRAUPNIR,
    /** Pasos 22 y 23, A11.2, A16.1. */
    RESUMEN,
    ERROR
}

/** Lo que se le dice al alumno después de responder (sin revelar la correcta, RF-12). */
enum class Veredicto(val titulo: String, val detalle: String) {
    GOLPE("¡Golpe!", "Tu respuesta fue correcta."),
    INCORRECTA("Escudo roto", "Tu respuesta fue incorrecta."),
    INCOMPLETA("Escudo roto", "Tu respuesta estaba incompleta."),
    TIEMPO("Escudo roto", "Se acabó el tiempo.")
}

/**
 * CU-01 Sobrevivir oleadas (Hasta el Alba). Lleva la pantalla de inicio con la
 * apuesta, las 7 oleadas (pregunta → golpe o escudo roto), la decisión de
 * volver al banquete y el resumen de la noche.
 */
class HastaElAlbaViewModel(
    private val validarAcceso: ValidarAccesoAlbaUseCase,
    private val apostar: ApostarAlbaUseCase,
    private val obtenerPregunta: ObtenerPreguntaAlbaUseCase,
    private val responderPregunta: ResponderPreguntaAlbaUseCase,
    private val usarArtefacto: UsarArtefactoAlbaUseCase,
    private val seguirPeleando: SeguirPeleandoAlbaUseCase,
    private val cobrarNoche: CobrarNocheAlbaUseCase,
    private val caerEnBatalla: CaerEnBatallaAlbaUseCase,
    private val nombresDeTemas: suspend (List<String>) -> List<String>,
    saldoHidromiel: Flow<Int>
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoAlba.CARGANDO)
    private val _acceso = MutableStateFlow<AccesoAlba?>(null)
    private val _apuesta = MutableStateFlow(0)
    private val _noche = MutableStateFlow<NocheAlba?>(null)
    private val _pregunta = MutableStateFlow<PreguntaFlash?>(null)
    private val _seleccion = MutableStateFlow<Set<Int>>(emptySet())
    private val _segundos = MutableStateFlow(0)
    private val _senaladas = MutableStateFlow<List<Int>>(emptyList())
    private val _eliminadas = MutableStateFlow<List<Int>>(emptyList())
    private val _veredicto = MutableStateFlow<Veredicto?>(null)
    private val _pulso = MutableStateFlow<PulsoEscena?>(null)
    private val _resultado = MutableStateFlow<ResultadoNoche?>(null)
    private val _temasConError = MutableStateFlow<List<String>>(emptyList())
    private val _aviso = MutableStateFlow<String?>(null)
    private val _confirmandoHuida = MutableStateFlow(false)

    val estado: StateFlow<EstadoAlba> = _estado.asStateFlow()
    val acceso: StateFlow<AccesoAlba?> = _acceso.asStateFlow()
    val apuesta: StateFlow<Int> = _apuesta.asStateFlow()
    val noche: StateFlow<NocheAlba?> = _noche.asStateFlow()
    val pregunta: StateFlow<PreguntaFlash?> = _pregunta.asStateFlow()
    val seleccion: StateFlow<Set<Int>> = _seleccion.asStateFlow()
    val segundos: StateFlow<Int> = _segundos.asStateFlow()
    /** A9.4: segundos que le quedan a la pausa de Skíðblaðnir con defecto. */
    /** A9.3: opciones que señala Gungnir perfecto. */
    val senaladas: StateFlow<List<Int>> = _senaladas.asStateFlow()
    /** A9.3: opciones que elimina Gungnir con defecto. */
    val eliminadas: StateFlow<List<Int>> = _eliminadas.asStateFlow()
    val veredicto: StateFlow<Veredicto?> = _veredicto.asStateFlow()
    val pulso: StateFlow<PulsoEscena?> = _pulso.asStateFlow()
    val resultado: StateFlow<ResultadoNoche?> = _resultado.asStateFlow()
    val temasConError: StateFlow<List<String>> = _temasConError.asStateFlow()
    val aviso: StateFlow<String?> = _aviso.asStateFlow()
    val confirmandoHuida: StateFlow<Boolean> = _confirmandoHuida.asStateFlow()
    val saldo: StateFlow<Int> = saldoHidromiel.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private var temporizador: Job? = null
    private var contadorPulsos = 0

    // -----------------------------------------------------------------------
    // Pantalla de inicio (pasos 1 a 7)
    // -----------------------------------------------------------------------

    /** Artefactos que el alumno eligió en la vitrina para llevar a la noche (por omisión, todos los que tiene). */
    private val _llevar = MutableStateFlow<Set<Artefacto>>(emptySet())
    val llevar: StateFlow<Set<Artefacto>> = _llevar.asStateFlow()
    private val _eligiendoArtefactos = MutableStateFlow(false)
    /** true mientras se muestra la vitrina en modo Hasta el Alba. */
    val eligiendoArtefactos: StateFlow<Boolean> = _eligiendoArtefactos.asStateFlow()

    fun abrirVitrina() { if (_estado.value == EstadoAlba.APOSTANDO) _eligiendoArtefactos.value = true }
    fun cerrarVitrina() { _eligiendoArtefactos.value = false }
    fun alternarArtefacto(a: Artefacto) {
        if (_acceso.value?.artefactos?.any { it.artefacto == a } != true) return
        _llevar.value = if (a in _llevar.value) _llevar.value - a else _llevar.value + a
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = EstadoAlba.CARGANDO
            val acceso = intentar("No se pudo abrir el Valhalla") { validarAcceso() } ?: return@launch
            _acceso.value = acceso
            _llevar.value = acceso.artefactos.map { it.artefacto }.toSet()
            _apuesta.value = acceso.rango.minimo
            _aviso.value = acceso.avisoRecuperacion
            _estado.value = when (acceso.motivo) {
                MotivoBloqueoAlba.VALHALLA_CERRADO -> EstadoAlba.VALHALLA_CERRADO
                MotivoBloqueoAlba.NOCHE_JUGADA -> EstadoAlba.NOCHE_JUGADA
                MotivoBloqueoAlba.HIDROMIEL_INSUFICIENTE -> EstadoAlba.HIDROMIEL_INSUFICIENTE
                MotivoBloqueoAlba.SIN_PREGUNTAS -> EstadoAlba.SIN_PREGUNTAS
                null -> EstadoAlba.APOSTANDO
            }
        }
    }

    /** Paso 5. Fuera del rango no se habilita la confirmación (A5.2). */
    fun cambiarApuesta(nueva: Int) {
        if (_estado.value != EstadoAlba.APOSTANDO) return
        _apuesta.value = nueva.coerceIn(0, 999)
        _aviso.value = null
    }

    fun apuestaValida(): Boolean = _acceso.value?.rango?.contiene(_apuesta.value) == true

    /** Pasos 5 a 9: cobra la apuesta, registra la noche e inicia la oleada 1. */
    fun bajarAlCampo() {
        if (_estado.value != EstadoAlba.APOSTANDO) return
        val rango = _acceso.value?.rango ?: return
        val cantidad = _apuesta.value
        if (!rango.contiene(cantidad)) {
            _aviso.value = "La apuesta debe ir de ${rango.minimo} a ${rango.maximo} de hidromiel."
            return
        }
        viewModelScope.launch {
            _estado.value = EstadoAlba.CARGANDO
            val pago = intentar("No se pudo cobrar la apuesta") { apostar(cantidad, _llevar.value) } ?: return@launch
            val nueva = pago.noche
            if (nueva == null) { // A6.1
                _aviso.value = pago.acceso.motivo
                _estado.value = EstadoAlba.APOSTANDO
                return@launch
            }
            _noche.value = nueva
            siguientePregunta(null)
        }
    }

    // -----------------------------------------------------------------------
    // Oleadas (pasos 8 a 14)
    // -----------------------------------------------------------------------

    fun alternarOpcion(indice: Int) {
        if (_estado.value != EstadoAlba.PREGUNTA || indice in _eliminadas.value) return
        val actual = _seleccion.value
        _seleccion.value = if (indice in actual) actual - indice else actual + indice
    }

    /** Paso 10: confirma la respuesta. */
    fun confirmar() {
        if (_estado.value != EstadoAlba.PREGUNTA || _confirmandoHuida.value || _seleccion.value.isEmpty()) return
        responder(_seleccion.value.sorted())
    }

    /** A9.2 a A9.4 y A15.1. */
    fun usar(artefacto: Artefacto) {
        val actual = _noche.value ?: return
        if (_estado.value != EstadoAlba.PREGUNTA && _estado.value != EstadoAlba.DECISION) return
        viewModelScope.launch {
            val (nueva, efecto) = intentar("No se pudo usar ${artefacto.nombre}") {
                usarArtefacto(actual, artefacto, _pregunta.value)
            } ?: return@launch
            _noche.value = nueva
            when (efecto) {
                is EfectoArtefacto.NoDisponible -> _aviso.value = efecto.motivo
                is EfectoArtefacto.GolpeDeMjolnir -> {
                    temporizador?.cancel()
                    pulsar("odinGolpe()")
                    if (efecto.derrotado) oleadaSuperada(nueva) else siguientePregunta(_pregunta.value?.id)
                }
                is EfectoArtefacto.Gungnir -> {
                    _senaladas.value = efecto.correctas
                    _eliminadas.value = efecto.eliminadas
                    _seleccion.value = _seleccion.value - efecto.eliminadas.toSet()
                }
                EfectoArtefacto.EscudoRecuperado -> _aviso.value = "Skíðblaðnir te devolvió un escudo."
                is EfectoArtefacto.BonoDraupnir -> _aviso.value = "Draupnir multiplica tu premio."
            }
        }
    }

    // -----------------------------------------------------------------------
    // Decisión (pasos 15 y 16, A16.1, A18.1)
    // -----------------------------------------------------------------------

    fun premioSiCobra(): Int = _noche.value?.let { premioNetoAlCobrar(it) } ?: 0

    fun seguir() {
        val actual = _noche.value ?: return
        if (_estado.value != EstadoAlba.DECISION) return
        viewModelScope.launch {
            _estado.value = EstadoAlba.CARGANDO
            val nueva = intentar("No se pudo seguir peleando") { seguirPeleando(actual) } ?: return@launch
            _noche.value = nueva
            siguientePregunta(null)
        }
    }

    fun volverAlBanquete() {
        val actual = _noche.value ?: return
        if (_estado.value != EstadoAlba.DECISION) return
        terminar { cobrarNoche(actual, Desenlace.RETIRADA) }
    }

    /** A18.1: activa (o no) Draupnir antes de cobrar el alba. */
    fun cobrarAlba(activarDraupnir: Boolean) {
        val actual = _noche.value ?: return
        if (_estado.value != EstadoAlba.OFRECER_DRAUPNIR) return
        viewModelScope.launch {
            var noche = actual
            if (activarDraupnir) {
                noche = intentar("No se pudo usar Draupnir") { usarArtefacto(actual, Artefacto.DRAUPNIR, null).first } ?: return@launch
            }
            _noche.value = noche
            terminar { cobrarNoche(noche, Desenlace.ALBA) }
        }
    }

    // -----------------------------------------------------------------------
    // Huir (E9.1)
    // -----------------------------------------------------------------------

    fun enBatalla(): Boolean = _estado.value in setOf(EstadoAlba.PREGUNTA, EstadoAlba.RESPUESTA, EstadoAlba.DECISION, EstadoAlba.OFRECER_DRAUPNIR)

    /** E9.1: oculta la pregunta y detiene el contador mientras confirma. */
    fun solicitarHuida() {
        if (!enBatalla()) return
        temporizador?.cancel()
        _confirmandoHuida.value = true
    }

    /** E9.1 paso 4: la pregunta oculta se descarta y sale otra distinta. */
    fun cancelarHuida() {
        if (!_confirmandoHuida.value) return
        _confirmandoHuida.value = false
        if (_estado.value == EstadoAlba.PREGUNTA) siguientePregunta(_pregunta.value?.id)
    }

    fun confirmarHuida() {
        val actual = _noche.value ?: return
        _confirmandoHuida.value = false
        temporizador?.cancel()
        terminar { caerEnBatalla(actual) }
    }

    fun limpiarAviso() { _aviso.value = null }

    // -----------------------------------------------------------------------
    // Internos
    // -----------------------------------------------------------------------

    private fun siguientePregunta(excluir: String?) {
        val actual = _noche.value ?: return
        temporizador?.cancel()
        viewModelScope.launch {
            val p = intentar("No hay preguntas para la batalla") { obtenerPregunta(actual, excluir) }
            if (p == null) {
                if (_estado.value != EstadoAlba.ERROR) {
                    _aviso.value = "No hay preguntas de tus temas superados."
                    _estado.value = EstadoAlba.ERROR
                }
                return@launch
            }
            _pregunta.value = p
            _seleccion.value = emptySet()
            _senaladas.value = emptyList()
            _eliminadas.value = emptyList()
            _veredicto.value = null
            _estado.value = EstadoAlba.PREGUNTA
            iniciarContador(actual.enemigo.segundosPorPregunta)
        }
    }

    private fun iniciarContador(desde: Int) {
        temporizador?.cancel()
        _segundos.value = desde
        temporizador = viewModelScope.launch {
            while (_segundos.value > 0) {
                delay(1000)
                _segundos.value -= 1
                ModuloSonido.compartido?.avisarTiempoRestante(_segundos.value) // MOD-02, disparador 3
            }
            if (_estado.value == EstadoAlba.PREGUNTA && !_confirmandoHuida.value) responder(null) // A10.1
        }
    }

    private fun responder(seleccion: List<Int>?) {
        val actual = _noche.value ?: return
        val p = _pregunta.value ?: return
        temporizador?.cancel()
        _estado.value = EstadoAlba.RESPUESTA
        viewModelScope.launch {
            val r = intentar("No se pudo validar la respuesta") { responderPregunta(actual, p, seleccion) } ?: return@launch
            _noche.value = r.noche
            _veredicto.value = when (r.estado) {
                EstadoRespuesta.CORRECTA -> Veredicto.GOLPE
                EstadoRespuesta.INCOMPLETA -> Veredicto.INCOMPLETA
                EstadoRespuesta.INCORRECTA -> Veredicto.INCORRECTA
                null -> Veredicto.TIEMPO
            }
            pulsar(if (r.golpeAcertado) "odinGolpe()" else "odinEscudo()")
            delay(PAUSA_RESPUESTA_MS)
            when {
                r.cayo -> terminar { caerEnBatalla(r.noche) }             // A11.2
                r.oleadaSuperada -> oleadaSuperada(r.noche)               // paso 14
                else -> siguientePregunta(p.id)                            // paso 13 o A11.1
            }
        }
    }

    /** Paso 14 y 17: tras la oleada 7 no hay decisión; se cobra el alba (paso 18). */
    private suspend fun oleadaSuperada(noche: NocheAlba) {
        _estado.value = EstadoAlba.RESPUESTA // bloquea las opciones mientras cae el enemigo
        _veredicto.value = null
        pulsar("odinDerrota()")
        delay(PAUSA_DERROTA_MS)
        _noche.value = noche
        if (noche.enemigo.ultimo) {
            if (noche.disponible(Artefacto.DRAUPNIR) != null) _estado.value = EstadoAlba.OFRECER_DRAUPNIR
            else terminar { cobrarNoche(noche, Desenlace.ALBA) }
        } else {
            _estado.value = EstadoAlba.DECISION
        }
    }

    private fun terminar(accion: suspend () -> ResultadoNoche) {
        temporizador?.cancel()
        _estado.value = EstadoAlba.CARGANDO
        viewModelScope.launch {
            val r = intentar("No se pudo guardar la noche") { accion() } ?: return@launch
            _resultado.value = r
            _temasConError.value = runCatching { nombresDeTemas(r.temasConError) }.getOrDefault(emptyList())
            _estado.value = EstadoAlba.RESUMEN
        }
    }

    private fun pulsar(script: String) {
        contadorPulsos += 1
        _pulso.value = PulsoEscena(contadorPulsos, script)
    }

    private suspend fun <T> intentar(mensaje: String, bloque: suspend () -> T): T? = try {
        bloque()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        _aviso.value = e.message ?: mensaje
        _estado.value = EstadoAlba.ERROR
        null
    }

    override fun onCleared() {
        temporizador?.cancel()
        super.onCleared()
    }

    companion object {
        const val PAUSA_RESPUESTA_MS = 1100L
        const val PAUSA_DERROTA_MS = 1200L

        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    HastaElAlbaViewModel(
                        validarAcceso = container.validarAccesoAlbaUseCase,
                        apostar = container.apostarAlbaUseCase,
                        obtenerPregunta = container.obtenerPreguntaAlbaUseCase,
                        responderPregunta = container.responderPreguntaAlbaUseCase,
                        usarArtefacto = container.usarArtefactoAlbaUseCase,
                        seguirPeleando = container.seguirPeleandoAlbaUseCase,
                        cobrarNoche = container.cobrarNocheAlbaUseCase,
                        caerEnBatalla = container.caerEnBatallaAlbaUseCase,
                        nombresDeTemas = { ids ->
                            val temas = container.roadmapRepository.obtenerTemas().associateBy { it.id }
                            ids.mapNotNull { temas[it]?.nombre }
                        },
                        saldoHidromiel = container.moduloHidromiel.observarSaldo(USUARIO_LOCAL)
                    ).apply { cargar() }
                }
            }
    }
}
