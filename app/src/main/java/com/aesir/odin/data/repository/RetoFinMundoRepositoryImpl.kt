package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.ResultadoRetoFinMundoDao
import com.aesir.odin.data.local.entity.ResultadoRetoFinMundoEntity
import com.aesir.odin.domain.model.IntentoRetoFinMundo
import com.aesir.odin.domain.repository.RetoFinMundoRepository
import java.util.UUID

class RetoFinMundoRepositoryImpl(
    private val dao: ResultadoRetoFinMundoDao
) : RetoFinMundoRepository {

    override suspend fun guardarIntento(intento: IntentoRetoFinMundo) {
        dao.insertar(
            ResultadoRetoFinMundoEntity(
                id = UUID.randomUUID().toString(),
                mundoId = intento.mundoId,
                puntaje = intento.puntaje,
                aciertos = intento.aciertos,
                errores = intento.errores,
                superado = intento.superado,
                abandonado = intento.abandonado,
                fecha = intento.fecha
            )
        )
    }

    override suspend fun obtenerIntentos(mundoId: String): List<IntentoRetoFinMundo> =
        dao.getPorMundo(mundoId).map {
            IntentoRetoFinMundo(it.mundoId, it.puntaje, it.aciertos, it.errores, it.superado, it.abandonado, it.fecha)
        }
}
