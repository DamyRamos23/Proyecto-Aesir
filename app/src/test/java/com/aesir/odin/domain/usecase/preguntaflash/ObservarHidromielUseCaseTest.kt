package com.aesir.odin.domain.usecase.preguntaflash

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservarHidromielUseCaseTest {
    @Test
    fun devuelveElSaldoDeHidromiel() = runBlocking {
        val hidromiel = FakeHidromielFlashRepository(inicial = 35)
        assertEquals(35, ObservarHidromielUseCase(hidromiel)().first())
    }
}
