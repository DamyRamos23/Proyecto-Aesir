package com.aesir.odin.domain.usecase.memorama

import com.aesir.odin.domain.model.TarjetaMemorama
import com.aesir.odin.domain.model.TipoTarjeta
import com.aesir.odin.domain.repository.MemoramaRepository
import kotlin.random.Random

/**
 * RF-06: elige [NUM_PARES] pares al azar del banco del mundo y crea una
 * tarjeta de concepto y una de definición por par, todas ocultas y mezcladas.
 *
 * Devuelve una lista vacía si el banco no alcanza (no debería pasar, porque
 * ValidarAccesoMemoramaUseCase lo revisa antes de entrar).
 */
class GenerarMemoramaUseCase(
    private val repository: MemoramaRepository,
    private val random: Random = Random.Default
) {
    suspend operator fun invoke(mundoId: String): List<TarjetaMemorama> {
        val pares = repository.obtenerParesPorMundo(mundoId)
        if (pares.size < NUM_PARES) return emptyList()

        return pares.shuffled(random).take(NUM_PARES)
            .flatMap { par ->
                listOf(
                    TarjetaMemorama("${par.id}_c", par.id, TipoTarjeta.CONCEPTO, par.concepto),
                    TarjetaMemorama("${par.id}_d", par.id, TipoTarjeta.DEFINICION, par.definicion)
                )
            }
            .shuffled(random)
    }

    companion object {
        const val NUM_PARES = 6
    }
}
