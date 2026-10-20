package com.aesir.odin.domain.usecase.retoarrastrar

import com.aesir.odin.domain.model.ElementoArrastrable
import com.aesir.odin.domain.model.PosicionDestino
import com.aesir.odin.domain.model.RetoArrastrar
import com.aesir.odin.domain.repository.RetoRepository
import kotlin.random.Random

/**
 * Fase 1 del CU-09: obtiene el reto registrado para el tema y revuelve sus
 * elementos (RF-70: las etapas se muestran en orden aleatorio y nunca ya
 * ordenadas).
 */
class ObtenerRetoUseCase(
    private val repository: RetoRepository,
    private val random: Random = Random.Default
) {
    suspend operator fun invoke(temaId: String): RetoArrastrar {
        val reto = repository.obtenerRetoPorTema(temaId)
        check(reto.elementos.isNotEmpty() && reto.posiciones.isNotEmpty()) { "El reto de este nivel no tiene elementos." }
        return reto.copy(elementos = mezclarElementos(reto.elementos, reto.posiciones))
    }

    /**
     * Revuelve los elementos cuidando que no queden en el orden de la solución.
     *
     * La solución se define por el [PosicionDestino.orden] de la posición
     * correcta de cada elemento, no por el orden en que llegan de la base de
     * datos (la consulta de elementos_arrastrables no tiene ORDER BY, así que
     * ese orden depende de cómo se insertaron).
     */
    fun mezclarElementos(
        elementos: List<ElementoArrastrable>,
        posiciones: List<PosicionDestino>
    ): List<ElementoArrastrable> {
        if (elementos.size < 2) return elementos
        val ordenDe = posiciones.associate { it.id to it.orden }
        fun orden(e: ElementoArrastrable) = ordenDe[e.posicionCorrectaId] ?: Int.MAX_VALUE
        val solucion = elementos.sortedBy(::orden)
        val ordenSolucion = solucion.map(::orden)
        // Si todas las posiciones tienen el mismo orden, cualquier mezcla "está ordenada"
        if (ordenSolucion.distinct().size < 2) return elementos.shuffled(random)

        fun yaOrdenado(lista: List<ElementoArrastrable>) = lista.map(::orden) == ordenSolucion

        repeat(10) {
            val mezclados = elementos.shuffled(random)
            if (!yaOrdenado(mezclados)) return mezclados
        }
        // Rotar la solución nunca la deja ordenada cuando hay al menos dos órdenes distintos
        return solucion.drop(1) + solucion.first()
    }
}
