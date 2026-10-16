package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.ResultadoContrarrelojDao
import com.aesir.odin.data.local.entity.ResultadoContrarrelojEntity
import com.aesir.odin.domain.model.ResultadoContrarreloj
import com.aesir.odin.domain.repository.RetoContrarrelojRepository

class RetoContrarrelojRepositoryImpl(
    private val dao: ResultadoContrarrelojDao
) : RetoContrarrelojRepository {

    override suspend fun guardarResultado(resultado: ResultadoContrarreloj) {
        dao.insertar(
            ResultadoContrarrelojEntity(
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
