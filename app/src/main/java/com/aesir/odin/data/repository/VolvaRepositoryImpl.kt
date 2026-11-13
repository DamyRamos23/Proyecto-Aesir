package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.VolvaDao
import com.aesir.odin.data.local.entity.ConsultaVolvaEntity
import com.aesir.odin.data.local.entity.RunaObtenidaEntity
import com.aesir.odin.data.local.entity.VisionEntity
import com.aesir.odin.domain.model.ConsultaVolva
import com.aesir.odin.domain.model.Lectura
import com.aesir.odin.domain.model.Norna
import com.aesir.odin.domain.model.ResultadoConsulta
import com.aesir.odin.domain.model.RunaFuthark
import com.aesir.odin.domain.model.SalonRunas
import com.aesir.odin.domain.model.Vision
import com.aesir.odin.domain.repository.VolvaRepository

class VolvaRepositoryImpl(private val dao: VolvaDao) : VolvaRepository {

    override suspend fun obtenerVisiones(temaIds: List<String>): List<Vision> =
        if (temaIds.isEmpty()) emptyList() else dao.getVisionesPorTemas(temaIds).mapNotNull { it.toDomain() }

    override suspend fun obtenerSalon(): SalonRunas {
        val ordenes = dao.getRunas().map { it.orden }.toSet()
        return SalonRunas(RunaFuthark.TODAS.filter { it.orden in ordenes })
    }

    override suspend fun guardarRunaObtenida(runa: RunaFuthark, fecha: Long) =
        dao.insertarRuna(RunaObtenidaEntity(runa.orden, fecha))

    override suspend fun consultoEnElDia(inicio: Long, fin: Long): Boolean =
        dao.contarConsultasEntre(inicio, fin) > 0

    override suspend fun registrarConsultaIniciada(consulta: ConsultaVolva) =
        dao.guardarConsulta(
            ConsultaVolvaEntity(
                id = consulta.id, apuesta = consulta.apuesta, puntosDestino = 0,
                visionesCumplidas = 0, visionesErradas = 0, lectura = null, pago = 0,
                runaOrden = null, abandonada = false, terminada = false, fecha = consulta.fecha
            )
        )

    override suspend fun guardarConsulta(resultado: ResultadoConsulta) =
        dao.guardarConsulta(
            ConsultaVolvaEntity(
                id = resultado.id,
                apuesta = resultado.apuesta,
                puntosDestino = resultado.puntosDestino,
                visionesCumplidas = resultado.visionesCumplidas,
                visionesErradas = resultado.visionesErradas,
                lectura = resultado.lectura?.name,
                pago = resultado.pago,
                runaOrden = resultado.runaObtenida?.orden,
                abandonada = resultado.abandonada,
                terminada = true,
                fecha = resultado.fecha
            )
        )

    override suspend fun obtenerConsultaSinTerminar(): ResultadoConsulta? =
        dao.getSinTerminar()?.let {
            ResultadoConsulta(
                id = it.id, apuesta = it.apuesta, puntosDestino = it.puntosDestino,
                visionesCumplidas = it.visionesCumplidas, visionesErradas = it.visionesErradas,
                lectura = it.lectura?.let(Lectura::valueOf), premioBruto = 0, diezmo = 0, pago = it.pago,
                runaObtenida = null, abandonada = it.abandonada, fecha = it.fecha
            )
        }
}

private fun VisionEntity.toDomain(): Vision? {
    val norna = runCatching { Norna.valueOf(norna) }.getOrNull() ?: return null
    if (opciones.size != 3 || indiceCorrecta !in opciones.indices) return null
    return Vision(id, temaId, norna, caso, opciones, indiceCorrecta, pista, explicacion)
}
