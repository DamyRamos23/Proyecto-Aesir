package com.aesir.odin.domain.usecase.retoarrastrar

import com.aesir.odin.domain.model.ElementoArrastrable
import com.aesir.odin.domain.model.RetoArrastrar
import com.aesir.odin.domain.repository.RetoRepository
import kotlin.random.Random

/**
 * Fase 1 del CU-09: obtiene el reto registrado para el tema y revuelve sus
 * elementos (RF-70: las etapas se muestran en orden aleatorio).
 */
class ObtenerRetoUseCase(
    private val repository: RetoRepository,
    private val random: Random = Random.Default
) {
    suspend operator fun invoke(temaId: String): RetoArrastrar {
        val reto = repository.obtenerRetoPorTema(temaId)
        check(reto.elementos.isNotEmpty() && reto.posiciones.isNotEmpty()) { "El reto de este nivel no tiene elementos." }
        return reto.copy(elementos = mezclarElementos(reto.elementos))
    }

    /** Revuelve los elementos cuidando que no queden ya en el orden de la solución. */
    fun mezclarElementos(elementos: List<ElementoArrastrable>): List<ElementoArrastrable> {
        if (elementos.size < 2) return elementos
        repeat(10) {
            val mezclados = elementos.shuffled(random)
            if (mezclados.map { it.id } != elementos.map { it.id }) return mezclados
        }
        return elementos.drop(1) + elementos.first()
    }
}
