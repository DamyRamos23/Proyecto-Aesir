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
                fecha = resultado.fecha,
                hidromiel = resultado.hidromiel,
                terminado = true
            )
        )
    }

    override suspend fun contarRetosDelDia(inicioDia: Long): Int = dao.contarDesde(inicioDia)

    override suspend fun registrarInicio(id: String, fecha: Long) {
        dao.insertar(
            ResultadoContrarrelojEntity(
                id = id, puntaje = 0, penalizacion = 0, aciertos = 0, respondidas = 0,
                abandonado = false, fecha = fecha, hidromiel = 0, terminado = false
            )
        )
    }

    override suspend fun retosSinTerminar(): List<String> = dao.sinTerminar()
}
