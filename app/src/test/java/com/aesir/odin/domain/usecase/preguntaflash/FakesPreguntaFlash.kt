package com.aesir.odin.domain.usecase.preguntaflash

import com.aesir.odin.domain.model.EstadoPreguntaFlash
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.PreguntaFlashAttempt
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.CambioHidromiel
import com.aesir.odin.domain.repository.HidromielFlashRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreguntaFlashRepository(preguntas: List<PreguntaFlash> = emptyList()) : PreguntaFlashRepository {
    val preguntas = preguntas.toMutableList()
    val intentos = mutableListOf<PreguntaFlashAttempt>()

    override suspend fun obtenerPreguntaAleatoriaDeTema(temaId: String): PreguntaFlash? =
        preguntas.filter { it.temaId == temaId }.randomOrNull()

    override suspend fun obtenerPreguntasParaReto(temaIds: List<String>, cantidad: Int): List<PreguntaFlash> =
        preguntas.filter { it.temaId in temaIds }.shuffled().take(cantidad)

    override suspend fun guardarIntento(intento: PreguntaFlashAttempt) {
        intentos += intento
    }
}

/** Imita las reglas de MOD-01 para la pregunta flash: correcta +5, fallar −2, nunca baja de 0. */
class FakeHidromielFlashRepository(inicial: Int = 0) : HidromielFlashRepository {
    val saldo = MutableStateFlow(inicial)
    val aplicados = mutableListOf<EstadoPreguntaFlash>()

    override fun observarSaldo(): Flow<Int> = saldo

    override suspend fun aplicarResultado(estado: EstadoPreguntaFlash): CambioHidromiel {
        aplicados += estado
        val antes = saldo.value
        saldo.value = if (estado == EstadoPreguntaFlash.CORRECTA) antes + 5 else (antes - 2).coerceAtLeast(0)
        return CambioHidromiel(saldo.value - antes, saldo.value)
    }
}

fun preguntaFlash(
    id: String = "p1",
    temaId: String = "t1",
    correctas: List<Int> = listOf(1),
    opciones: List<String> = listOf("A", "B", "C", "D")
) = PreguntaFlash(id, temaId, "Enunciado $id", opciones, correctas)
