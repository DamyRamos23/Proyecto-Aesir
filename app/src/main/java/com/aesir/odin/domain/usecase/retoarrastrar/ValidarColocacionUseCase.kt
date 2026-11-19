package com.aesir.odin.domain.usecase.retoarrastrar

import com.aesir.odin.domain.sonido.EventoSonido
import com.aesir.odin.domain.sonido.ModuloSonido
import com.aesir.odin.domain.model.RetoArrastrar

/**
 * Paso 6 del CU-09 (RF-74): compara cada colocación con la posición correcta.
 * Regresa los ids de los elementos mal colocados (o sin colocar); si la lista
 * está vacía, el reto está resuelto.
 *
 * @param colocaciones elementoId → posicionId.
 */
class ValidarColocacionUseCase {
    operator fun invoke(reto: RetoArrastrar, colocaciones: Map<String, String>): List<String> {
        val incorrectos = reto.elementos
            .filter { colocaciones[it.id] != it.posicionCorrectaId }
            .map { it.id }
        ModuloSonido.avisar(if (incorrectos.isEmpty()) EventoSonido.ACIERTO else EventoSonido.ERROR) // MOD-02
        return incorrectos
    }
}
