package com.aesir.odin.domain.usecase.flashcards

import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.TarjetaFlashcard
import com.aesir.odin.domain.repository.FlashcardsRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import kotlin.random.Random

/**
 * Arma la pila de tarjetas del nivel (RF-02): conceptos de los temas
 * incluidos, en orden aleatorio.
 *
 * Cada tarjeta muestra la descripción del concepto y [OPCIONES] conceptos
 * para elegir: el correcto y distractores tomados de los demás conceptos del
 * mundo, para que las opciones se parezcan entre sí.
 *
 * Si el nivel no tiene tarjetas registradas regresa Result.failure.
 */
class GenerarFlashcardsUseCase(
    private val repository: FlashcardsRepository,
    private val roadmapRepository: RoadmapRepository,
    private val random: Random = Random.Default
) {

    suspend operator fun invoke(temaId: String, temasIncluidos: List<String>): Result<List<TarjetaFlashcard>> = runCatching {
        val tema = roadmapRepository.obtenerTema(temaId)
        val incluidos = temasIncluidos.ifEmpty { listOf(temaId) }
        val banco = repository.obtenerConceptosPorTemas(incluidos).distinctBy { it.respuesta.lowercase() }
        check(banco.isNotEmpty()) { "Este nivel todavía no tiene tarjetas registradas." }

        val temasDelMundo = roadmapRepository.obtenerTemasPorMundo(tema.mundoId)
            .filter { !it.esRagnarok && !it.esMinijuego }
            .map { it.id }
        val bancoMundo = repository.obtenerConceptosPorTemas(temasDelMundo)
        construir(banco, bancoMundo)
    }

    fun construir(banco: List<ConceptoCrucigrama>, bancoMundo: List<ConceptoCrucigrama>): List<TarjetaFlashcard> =
        banco.shuffled(random).take(MAX_TARJETAS).map { concepto ->
            val distractores = bancoMundo
                .filter { it.id != concepto.id && !it.respuesta.equals(concepto.respuesta, ignoreCase = true) }
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
                indiceCorrecto = opciones.indexOf(concepto.respuesta)
            )
        }

    companion object {
        const val MAX_TARJETAS = 12
        const val OPCIONES = 4
    }
}
