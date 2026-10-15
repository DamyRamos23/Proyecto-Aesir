package com.aesir.odin.domain.usecase.reto

import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.RetoContrarrelojRepository

class FakePreguntaFlashRepository(private val banco: List<PreguntaFlash>) : PreguntaFlashRepository {
    var ultimosTemas: List<String> = emptyList()
    var ultimaCantidad: Int = 0

    override suspend fun obtenerPreguntasParaReto(temaIds: List<String>, cantidad: Int): List<PreguntaFlash> {
        ultimosTemas = temaIds
        ultimaCantidad = cantidad
        return banco.filter { it.temaId in temaIds }.take(cantidad)
    }
}

class FakeRetoContrarrelojRepository : RetoContrarrelojRepository {
    val resultados = mutableListOf<ResultadoReto>()

    override suspend fun guardarResultado(resultado: ResultadoReto) {
        resultados += resultado
    }
}

fun preguntaFlash(id: String, temaId: String) = PreguntaFlash(
    id = id,
    temaId = temaId,
    enunciado = "Pregunta $id",
    opciones = listOf("A", "B", "C"),
    respuestasCorrectas = listOf(0)
)
