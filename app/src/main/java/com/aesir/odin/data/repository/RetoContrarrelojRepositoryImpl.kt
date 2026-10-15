package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.ResultadoRetoDao
import com.aesir.odin.data.local.entity.ResultadoRetoEntity
import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.repository.RetoContrarrelojRepository

class RetoContrarrelojRepositoryImpl(
    private val dao: ResultadoRetoDao
) : RetoContrarrelojRepository {

    override suspend fun guardarResultado(resultado: ResultadoReto) {
        dao.insertar(
            ResultadoRetoEntity(
                id = resultado.id,
                puntaje = resultado.puntaje,
                penalizacion = resultado.penalizacion,
                aciertos = resultado.aciertos,
                respondidas = resultado.respondidas,
                abandonado = resultado.abandonado,
                fecha = resultado.fecha
            )
        )
    }
}
