package com.aesir.odin.domain.usecase.memorama

import com.aesir.odin.domain.model.TarjetaMemorama
import com.aesir.odin.domain.model.TipoTarjeta
import com.aesir.odin.domain.repository.MemoramaRepository
import kotlin.random.Random

/**
 * RF-58: elige [NUM_PARES] pares al azar del tema del nivel y crea una
 * tarjeta de concepto y una de definición por par, todas ocultas y mezcladas.
 * Si el tema no alcanza (no debería pasar: cada tema tiene al menos 6
 * conceptos) usa el banco del mundo.
 *
 * Devuelve una lista vacía si el banco no alcanza (no debería pasar, porque
 * ValidarAccesoMemoramaUseCase lo revisa antes de entrar).
 */
class GenerarMemoramaUseCase(
    private val repository: MemoramaRepository,
    private val random: Random = Random.Default
) {
    suspend operator fun invoke(mundoId: String, nivelId: String? = null): List<TarjetaMemorama> {
        val pares = nivelId?.let { repository.obtenerParesPorNivel(it) }?.takeIf { it.size >= NUM_PARES }
            ?: repository.obtenerParesPorMundo(mundoId)
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
