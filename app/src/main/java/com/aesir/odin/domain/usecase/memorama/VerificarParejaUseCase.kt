package com.aesir.odin.domain.usecase.memorama

import com.aesir.odin.domain.sonido.EventoSonido
import com.aesir.odin.domain.sonido.ModuloSonido
import com.aesir.odin.domain.model.TarjetaMemorama

/**
 * RF-10: dos tarjetas son pareja si pertenecen al mismo par y una es el
 * concepto y la otra la definición.
 */
class VerificarParejaUseCase {
    operator fun invoke(primera: TarjetaMemorama, segunda: TarjetaMemorama): Boolean {
        val pareja = primera.id != segunda.id &&
            primera.parId == segunda.parId &&
            primera.tipo != segunda.tipo
        ModuloSonido.avisar(if (pareja) EventoSonido.ACIERTO else EventoSonido.ERROR) // MOD-02
        return pareja
    }
}
