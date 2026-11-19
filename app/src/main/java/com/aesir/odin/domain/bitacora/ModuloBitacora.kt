package com.aesir.odin.domain.bitacora

import com.aesir.odin.domain.model.EstadoError
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.repository.HistorialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** Actividad donde se cometió la última falla (MOD-06, «Origen»). */
enum class OrigenError {
    HASTA_EL_ALBA, FORJA, FLASHCARDS, LECCION, PREGUNTA_FLASH, RETO_FIN_MUNDO,
    ARRASTRAR_SOLTAR, RETO_CONTRARRELOJ, RETAR_BRUJA, DUELO_ENIGMAS
}

/**
 * Datos de la pregunta fallada que la actividad le pasa a la bitácora.
 * [preguntaId] es el id de la pregunta o concepto del banco (RN-32);
 * [respuestaAlumno] es null si se acabó el tiempo.
 */
data class FalloBitacora(
    val origen: OrigenError,
    val preguntaId: String,
    val temaId: String,
    val enunciado: String,
    val opciones: List<String> = emptyList(),
    val respuestaCorrecta: String = "",
    val respuestaAlumno: String?,
    val pista: String = "",
    val explicacion: String? = null,
    val fecha: Long
) {
    /** E1: sin opciones, [enunciado] guarda la descripción del error. */
    fun tieneOpciones(): Boolean = opciones.isNotEmpty()
}

/** Texto de las opciones elegidas; null si no respondió (se acabó el tiempo). */
fun opcionesElegidas(opciones: List<String>, indices: List<Int>?): String? =
    indices?.takeIf { it.isNotEmpty() }?.joinToString(", ") { opciones.getOrNull(it) ?: "?" }

/** Fallo de una pregunta del banco de Pregunta Flash: CU-01, CU-06 y CU-11 la comparten (RN-32). */
fun PreguntaFlash.comoFallo(origen: OrigenError, seleccion: List<Int>?, fecha: Long) = FalloBitacora(
    origen = origen,
    preguntaId = id,
    temaId = temaId,
    enunciado = enunciado,
    opciones = opciones,
    respuestaCorrecta = respuestasCorrectas.joinToString(", ") { opciones[it] },
    respuestaAlumno = opcionesElegidas(opciones, seleccion),
    pista = temaId,
    fecha = fecha
)

/**
 * MOD-06 Bitácora de errores (los hilos de Urd). Las actividades avisan cada
 * respuesta incorrecta, incompleta o sin tiempo y el módulo la guarda sin
 * detenerlas. No toca el hidromiel, la racha ni el roadmap (RN-33).
 */
class ModuloBitacora(
    private val repositorio: HistorialRepository,
    private val alcance: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    // Dos fallos seguidos de la misma pregunta no deben insertar dos filas (índice único, RN-32).
    private val candado = Mutex()

    /** Paso 5: regresa de inmediato; el guardado corre en la corrutina del módulo. */
    fun registrarFallo(fallo: FalloBitacora) {
        alcance.launch { guardar(fallo) }
    }

    /** Pasos 2 a 4. E2: si la base falla, devuelve failure y el registro se descarta. */
    internal suspend fun guardar(fallo: FalloBitacora): Result<Unit> = runCatching {
        candado.withLock {
            val previo = repositorio.buscarPorPregunta(fallo.preguntaId)
            if (previo != null) repositorio.actualizar(previo.conOtroFallo(fallo))
            else repositorio.registrar(
                HistorialError(
                    id = UUID.randomUUID().toString(),
                    preguntaId = fallo.preguntaId,
                    origen = fallo.origen,
                    temaId = fallo.temaId,
                    enunciado = fallo.enunciado,
                    opciones = fallo.opciones,
                    respuestaCorrecta = fallo.respuestaCorrecta,
                    respuestaAlumno = fallo.respuestaAlumno,
                    pista = fallo.pista,
                    explicacion = fallo.explicacion,
                    estado = EstadoError.PENDIENTE,
                    fallos = 1,
                    fecha = fallo.fecha
                )
            )
        }
    }
}
