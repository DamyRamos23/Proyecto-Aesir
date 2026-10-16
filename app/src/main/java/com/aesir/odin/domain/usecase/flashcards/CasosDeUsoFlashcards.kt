package com.aesir.odin.domain.usecase.flashcards

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NivelFinalizado
import com.aesir.odin.domain.hidromiel.ResultadoNivel
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.PilaFlashcards
import com.aesir.odin.domain.model.ResultadoFlashcards
import com.aesir.odin.domain.model.TarjetaFlashcard
import com.aesir.odin.domain.repository.FlashcardsRepository
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase
import java.util.UUID
import kotlin.random.Random

/**
 * Pasos 1 y 2 del CU-03: arma la pila de tarjetas del nivel con los conceptos
 * de los temas que repasa. Cada tarjeta muestra la descripción y cuatro
 * conceptos: el correcto y tres distractores del mismo mundo.
 */
class GenerarFlashcardsUseCase(
    private val repository: FlashcardsRepository,
    private val roadmapRepository: RoadmapRepository,
    private val random: Random = Random.Default
) {
    companion object {
        const val MAX_TARJETAS = 12
        const val OPCIONES = 4
    }

    suspend operator fun invoke(temaId: String, temasIncluidos: List<String>): Result<List<TarjetaFlashcard>> {
        val tema = runCatching { roadmapRepository.obtenerTema(temaId) }.getOrNull()
            ?: return Result.failure(IllegalStateException("No se encontró el nivel de flashcards."))
        val incluidos = temasIncluidos.ifEmpty { listOf(temaId) }
        val banco = repository.obtenerConceptosPorTemas(incluidos).distinctBy { it.respuesta.lowercase() }
        if (banco.isEmpty()) return Result.failure(IllegalStateException("Este nivel todavía no tiene tarjetas registradas."))
        val temasDelMundo = roadmapRepository.obtenerTemasPorMundo(tema.mundoId)
            .filter { !it.esRagnarok && !it.esMinijuego }
            .map { it.id }
        val bancoMundo = repository.obtenerConceptosPorTemas(temasDelMundo)
        return Result.success(construir(banco, bancoMundo))
    }

    fun construir(banco: List<ConceptoCrucigrama>, bancoMundo: List<ConceptoCrucigrama>): List<TarjetaFlashcard> =
        banco.shuffled(random).take(MAX_TARJETAS).map { concepto ->
            val distractores = bancoMundo
                .filter { it.id != concepto.id && it.respuesta.lowercase() != concepto.respuesta.lowercase() }
                .distinctBy { it.respuesta.lowercase() }
                .shuffled(random)
                .take(OPCIONES - 1)
                .map { it.respuesta }
            val opciones = (distractores + concepto.respuesta).shuffled(random)
            TarjetaFlashcard(
                id = concepto.id,
                temaId = concepto.temaId,
                descripcion = concepto.descripcion,
                concepto = concepto.respuesta,
                opciones = opciones,
                indiceCorrecto = opciones.indexOf(concepto.respuesta).coerceAtLeast(0)
            )
        }
}

/** A5.1: registra en el historial de errores la tarjeta que el alumno falló. */
class RegistrarErrorFlashcardUseCase(
    private val repository: HistorialRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    companion object {
        const val TIPO_FLASHCARD = "FLASHCARD"
    }

    suspend operator fun invoke(tarjeta: TarjetaFlashcard, opcionElegida: String) {
        repository.registrarError(
            HistorialError(
                id = UUID.randomUUID().toString(),
                tipoDesafio = TIPO_FLASHCARD,
                actividadId = tarjeta.id,
                descripcion = "Flashcard: «${tarjeta.concepto}» confundido con «$opcionElegida»",
                fecha = reloj()
            )
        )
    }
}

/**
 * Cierra las flashcards cuando la pila queda vacía (pasos 8 y 9):
 * 1. Guarda el resultado con el puntaje (aciertos al primer intento, 0 a 100).
 * 2. Registra la actividad del tema como completada (el mismo caso de uso de CU-05 y CU-07).
 * 3. Avisa al MOD-01 que el nivel se superó: él otorga la recompensa y abre
 *    los niveles conectados.
 */
class FinalizarFlashcardsUseCase(
    private val repository: FlashcardsRepository,
    private val registrarCompletadaUseCase: RegistrarLeccionCompletadaUseCase,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(temaId: String, pila: PilaFlashcards): Result<ResultadoFlashcards> {
        if (!pila.terminada) return Result.failure(IllegalStateException("La pila todavía tiene tarjetas"))
        val resultado = ResultadoFlashcards(
            temaId = temaId,
            puntaje = pila.puntaje,
            aciertosPrimerIntento = pila.aciertosPrimerIntento,
            errores = pila.errores,
            totalTarjetas = pila.total,
            fecha = reloj()
        )
        repository.guardarResultado(resultado)
        registrarCompletadaUseCase(temaId)
        moduloHidromiel.alFinalizarNivel(NivelFinalizado(idUsuario, temaId, resultado.puntaje, ResultadoNivel.SUPERADO))
        return Result.success(resultado)
    }
}

/**
 * A4.1 y A5.2: la actividad termina sin puntaje ni registro de completada. Se
 * avisa al MOD-01 para que la entrada pagada se pierda.
 */
class RegistrarFlashcardsSinSuperarUseCase(
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(temaId: String, resultado: ResultadoNivel) {
        require(resultado != ResultadoNivel.SUPERADO) { "Para un nivel superado usa FinalizarFlashcardsUseCase" }
        moduloHidromiel.alFinalizarNivel(NivelFinalizado(idUsuario, temaId, 0, resultado))
    }
}
