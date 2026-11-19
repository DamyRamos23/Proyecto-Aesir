package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.ConceptoCrucigramaDao
import com.aesir.odin.data.local.dao.MemoramaDao
import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.local.entity.ResultadoMemoramaEntity
import com.aesir.odin.domain.model.ParConcepto
import com.aesir.odin.domain.model.ResultadoMemorama
import com.aesir.odin.domain.repository.MemoramaRepository
import com.aesir.odin.domain.usecase.memorama.GenerarMemoramaUseCase
import java.util.UUID

/** CU-04: traduce entre las entidades de Room y los modelos de Negocio. */
class MemoramaRepositoryImpl(
    private val dao: MemoramaDao,
    private val temaDao: TemaDao? = null,
    private val conceptoDao: ConceptoCrucigramaDao? = null
) : MemoramaRepository {

    /**
     * RF-58: los pares salen del banco de conceptos del tema del nivel (el
     * mismo del crucigrama y las flashcards): el concepto y su descripción.
     */
    override suspend fun obtenerParesPorNivel(nivelId: String): List<ParConcepto> {
        val nivel = temaDao?.getById(nivelId) ?: return emptyList()
        val temaId = nivel.temaBaseId.ifEmpty { nivel.id }
        return conceptoDao?.getByTemaIds(listOf(temaId)).orEmpty().map {
            ParConcepto(it.id, nivel.mundoId, it.respuesta.lowercase().replaceFirstChar { c -> c.uppercase() }, it.descripcion)
        }
    }

    override suspend fun obtenerParesPorMundo(mundoId: String): List<ParConcepto> =
        dao.getParesByMundoId(mundoId).map { ParConcepto(it.id, it.mundoId, it.concepto, it.definicion) }

    /** Un mundo tiene memorama si su banco alcanza para un tablero completo. */
    override suspend fun mundoTieneMemorama(mundoId: String): Boolean =
        dao.contarPares(mundoId) >= GenerarMemoramaUseCase.NUM_PARES

    override suspend fun guardarResultado(resultado: ResultadoMemorama) {
        dao.insertarResultado(
            ResultadoMemoramaEntity(
                id = UUID.randomUUID().toString(),
                mundoId = resultado.mundoId,
                nivelId = resultado.nivelId,
                tiempoEmpleadoSeg = resultado.tiempoEmpleadoSeg,
                puntos = resultado.puntos,
                fecha = resultado.fecha
            )
        )
    }
}
