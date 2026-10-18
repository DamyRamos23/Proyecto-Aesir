package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.ElementoArrastrableDao
import com.aesir.odin.data.local.dao.PosicionDestinoDao
import com.aesir.odin.data.local.dao.RetoDao
import com.aesir.odin.data.local.entity.ResultadoRetoEntity
import com.aesir.odin.domain.model.ElementoArrastrable
import com.aesir.odin.domain.model.ModalidadReto
import com.aesir.odin.domain.model.PosicionDestino
import com.aesir.odin.domain.model.ResultadoReto
import com.aesir.odin.domain.model.RetoArrastrar
import com.aesir.odin.domain.repository.RetoRepository
import java.util.UUID

class RetoRepositoryImpl(
    private val retoDao: RetoDao,
    private val elementoDao: ElementoArrastrableDao,
    private val posicionDao: PosicionDestinoDao
) : RetoRepository {

    override suspend fun obtenerRetoPorTema(temaId: String): RetoArrastrar {
        val reto = checkNotNull(retoDao.getByTemaId(temaId)) { "Este nivel todavía no tiene un reto registrado." }
        val elementos = elementoDao.getByRetoId(reto.id)
        val posiciones = posicionDao.getByRetoId(reto.id)
        return RetoArrastrar(
            id = reto.id,
            temaId = reto.temaId,
            modalidad = ModalidadReto.desde(reto.modalidad),
            instrucciones = reto.instrucciones,
            imagenDiagrama = reto.imagenDiagrama,
            elementos = elementos.map { ElementoArrastrable(it.id, it.retoId, it.texto, it.posicionCorrectaId) },
            posiciones = posiciones.map { PosicionDestino(it.id, it.retoId, it.orden, it.etiqueta, it.x, it.y) },
            completado = reto.completado
        )
    }

    override suspend fun guardarResultado(resultado: ResultadoReto) {
        retoDao.insertarResultado(
            ResultadoRetoEntity(
                id = UUID.randomUUID().toString(),
                retoId = resultado.retoId,
                temaId = resultado.temaId,
                puntaje = resultado.puntaje,
                intentos = resultado.intentos,
                aprobado = resultado.aprobado,
                fecha = resultado.fecha
            )
        )
        if (resultado.aprobado) retoDao.actualizarCompletado(resultado.retoId, true)
    }
}
