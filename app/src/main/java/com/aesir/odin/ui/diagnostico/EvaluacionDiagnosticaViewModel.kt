package com.aesir.odin.ui.diagnostico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aesir.odin.di.AppContainer
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.PreguntaDiagnostica
import com.aesir.odin.domain.model.RespuestaDiagnostica
import com.aesir.odin.domain.model.ResultadoDiagnostico
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.usecase.diagnostico.EvaluarDiagnosticoUseCase
import com.aesir.odin.domain.usecase.diagnostico.FinalizarDiagnosticoUseCase
import com.aesir.odin.domain.usecase.diagnostico.ObtenerPreguntasDiagnosticoUseCase
import com.aesir.odin.domain.usecase.diagnostico.ObtenerPreguntasRepasoUseCase
import com.aesir.odin.domain.usecase.diagnostico.OmitirDiagnosticoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerTemasPorMundoUseCase
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * CU-10 Realizar evaluación diagnóstico, con las tres fases del mockup
 * «El pozo de Mímir»:
 *
 * - [Fase.LLAMADO]: presentación y reglas; se puede omitir (A1.1 / A1.2).
 * - [Fase.PRUEBA]: «Prueba de sabiduría», una pregunta a la vez. El alumno
 *   elige, revela la respuesta (el pozo dice si el eco coincide) y pasa a la
 *   siguiente. Al revelar la última se registra el resultado (RF-86 a RF-91).
 * - [Fase.REVELACION]: lo que el pozo reconoció, tema por tema.
 *
 * Salir antes de terminar descarta todo sin registrar nada (A6.1, RF-96, RF-97).
 *
 * Con [repaso] es el repaso libre del mundo en el pozo: preguntas al azar del
 * banco y no se registra nada (ni omisión, ni resultado, ni temas conocidos).
 */
class EvaluacionDiagnosticaViewModel(
    private val obtenerPreguntasUseCase: ObtenerPreguntasDiagnosticoUseCase,
    private val obtenerRepasoUseCase: ObtenerPreguntasRepasoUseCase,
    private val finalizarUseCase: FinalizarDiagnosticoUseCase,
    private val omitirUseCase: OmitirDiagnosticoUseCase,
    private val obtenerMundoUseCase: ObtenerMundoUseCase,
    private val obtenerTemasPorMundoUseCase: ObtenerTemasPorMundoUseCase,
    private val validador: ValidarRespuestaUseCase,
    val repaso: Boolean = false
) : ViewModel() {

    enum class Fase { LLAMADO, PRUEBA, REVELACION }

    /** Resultado de un tema del mundo para la revelación. */
    data class DetalleTema(
        val id: String,
        val nombre: String,
        val orden: Int,
        val aciertos: Int,
        val total: Int,
        val conocido: Boolean
    )

    /** Foto de la evaluación para dibujar la pantalla. */
    data class Estado(
        val fase: Fase = Fase.LLAMADO,
        val preguntas: List<PreguntaDiagnostica> = emptyList(),
        val indiceActual: Int = 0,
        /** Opciones elegidas en la pregunta actual. */
        val seleccion: Set<Int> = emptySet(),
        /** La pregunta actual ya se reveló. */
        val revelada: Boolean = false,
        /** Si cada pregunta respondida fue correcta (enciende su runa). */
        val aciertos: List<Boolean> = emptyList(),
        val resultado: ResultadoDiagnostico? = null,
        val confirmandoOmision: Boolean = false,
        /** true cuando la omisión quedó registrada; la pantalla navega al roadmap. */
        val omitida: Boolean = false,
        val mundo: Mundo? = null,
        val temas: List<Tema> = emptyList(),
        val mensajeError: String? = null
    ) {
        val preguntaActual: PreguntaDiagnostica? get() = preguntas.getOrNull(indiceActual)
        val esUltima: Boolean get() = indiceActual >= preguntas.size - 1

        /** Si la respuesta revelada de la pregunta actual fue correcta. */
        val actualCorrecta: Boolean? get() = if (revelada) aciertos.getOrNull(indiceActual) else null

        fun nombreTema(temaId: String): String = temas.firstOrNull { it.id == temaId }?.nombre.orEmpty()

        /** Temas del mundo (lecciones) con sus aciertos, en orden. */
        val detalleTemas: List<DetalleTema>
            get() {
                val conocidos = resultado?.temasConocidos.orEmpty().toSet()
                val aciertosPorTema = mutableMapOf<String, Int>()
                val totalPorTema = mutableMapOf<String, Int>()
                preguntas.forEachIndexed { i, p ->
                    totalPorTema[p.temaId] = (totalPorTema[p.temaId] ?: 0) + 1
                    if (aciertos.getOrNull(i) == true) aciertosPorTema[p.temaId] = (aciertosPorTema[p.temaId] ?: 0) + 1
                }
                return temas.filter { !it.esRagnarok && !it.esMinijuego }.sortedBy { it.orden }.map {
                    DetalleTema(it.id, it.nombre, it.orden, aciertosPorTema[it.id] ?: 0, totalPorTema[it.id] ?: 0, it.id in conocidos)
                }
            }
    }

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    private var mundoId = ""
    private val respuestas = mutableListOf<RespuestaDiagnostica>()
    private var finalizando = false

    // Llamado

    fun iniciar(mundoId: String) {
        val e = _estado.value
        if (this.mundoId == mundoId && (e.preguntas.isNotEmpty() || e.mensajeError != null)) return // ya iniciado
        this.mundoId = mundoId
        reiniciar()
        viewModelScope.launch {
            val mundo = runCatching { obtenerMundoUseCase(mundoId) }.getOrNull()
            val temas = runCatching { obtenerTemasPorMundoUseCase(mundoId) }.getOrDefault(emptyList())
            val cargadas = runCatching { if (repaso) obtenerRepasoUseCase(mundoId) else obtenerPreguntasUseCase(mundoId) }
                .getOrDefault(emptyList())
            _estado.update {
                it.copy(
                    mundo = mundo,
                    temas = temas,
                    preguntas = cargadas,
                    mensajeError = if (cargadas.isEmpty()) "Este mundo todavía no tiene preguntas de diagnóstico." else null
                )
            }
        }
    }

    /** «Consultar el pozo»: empieza la prueba. */
    fun consultar() {
        if (_estado.value.preguntas.isEmpty()) return
        _estado.update { it.copy(fase = Fase.PRUEBA) }
    }

    fun solicitarOmision() = _estado.update { it.copy(confirmandoOmision = true) }

    /** A1.1: registra la omisión; los niveles se quedan en su estado inicial. */
    fun confirmarOmision() {
        if (repaso) return
        viewModelScope.launch {
            runCatching { omitirUseCase(mundoId) }
            _estado.update { it.copy(confirmandoOmision = false, omitida = true) }
        }
    }

    /** A1.2: la evaluación sigue disponible. */
    fun cancelarOmision() = _estado.update { it.copy(confirmandoOmision = false) }

    // Prueba

    fun alternarOpcion(indice: Int) {
        val e = _estado.value
        val pregunta = e.preguntaActual ?: return
        if (e.fase != Fase.PRUEBA || e.revelada || indice !in pregunta.opciones.indices) return
        val nueva = when {
            !pregunta.esSeleccionMultiple -> setOf(indice)
            indice in e.seleccion -> e.seleccion - indice
            else -> e.seleccion + indice
        }
        _estado.update { it.copy(seleccion = nueva) }
    }

    /**
     * «Revelar respuesta» (RF-83, RF-84): guarda la elección y muestra si el
     * eco coincide. Con la última pregunta se registra el resultado.
     */
    fun revelar() {
        val e = _estado.value
        val pregunta = e.preguntaActual ?: return
        if (e.fase != Fase.PRUEBA || e.revelada || finalizando || e.seleccion.isEmpty()) return
        val elegidas = e.seleccion.sorted()
        respuestas += RespuestaDiagnostica(pregunta.id, elegidas)
        val correcta = validador(elegidas, pregunta.respuestasCorrectas).estado == EstadoRespuesta.CORRECTA
        _estado.update { it.copy(aciertos = it.aciertos + correcta, revelada = true) }
        if (respuestas.size >= e.preguntas.size) {
            finalizando = true
            val preguntas = e.preguntas
            val lista = respuestas.toList()
            viewModelScope.launch {
                val resultado = if (repaso) {
                    // Solo se evalúa para mostrar la revelación; no se guarda nada
                    EvaluarDiagnosticoUseCase(validador)(mundoId, preguntas, lista)
                } else {
                    finalizarUseCase(mundoId, preguntas, lista)
                }
                _estado.update { it.copy(resultado = resultado) }
            }
        }
    }

    /** «Siguiente»: pasa a la siguiente pregunta o, tras la última, a la revelación. */
    fun siguiente() {
        val e = _estado.value
        if (e.fase != Fase.PRUEBA || !e.revelada) return
        if (e.indiceActual + 1 < e.preguntas.size) {
            _estado.update { it.copy(indiceActual = it.indiceActual + 1, seleccion = emptySet(), revelada = false) }
        } else if (e.resultado != null) {
            _estado.update { it.copy(fase = Fase.REVELACION) }
        }
    }

    /**
     * A6.1: descarta las respuestas y el avance. No se registra nada, así que
     * al volver a entrar la evaluación empieza desde la primera pregunta (RF-97).
     */
    fun abandonar() {
        if (_estado.value.resultado != null) return
        reiniciar()
        _estado.update { it.copy(preguntas = emptyList()) }
        mundoId = ""
    }

    private fun reiniciar() {
        respuestas.clear()
        finalizando = false
        _estado.update {
            it.copy(
                fase = Fase.LLAMADO, aciertos = emptyList(), seleccion = emptySet(),
                revelada = false, indiceActual = 0, resultado = null, omitida = false
            )
        }
    }

    companion object {
        fun factory(container: AppContainer, repaso: Boolean = false): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EvaluacionDiagnosticaViewModel(
                    obtenerPreguntasUseCase = container.obtenerPreguntasDiagnosticoUseCase,
                    obtenerRepasoUseCase = container.obtenerPreguntasRepasoUseCase,
                    finalizarUseCase = container.finalizarDiagnosticoUseCase,
                    omitirUseCase = container.omitirDiagnosticoUseCase,
                    obtenerMundoUseCase = container.obtenerMundoUseCase,
                    obtenerTemasPorMundoUseCase = container.obtenerTemasPorMundoUseCase,
                    validador = container.validarRespuestaUseCase,
                    repaso = repaso
                )
            }
        }
    }
}
