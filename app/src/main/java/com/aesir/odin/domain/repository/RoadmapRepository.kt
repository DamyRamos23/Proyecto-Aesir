package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.Mundo
import com.aesir.odin.domain.model.Tema

/**
 * Contrato de acceso al roadmap. Coincide con `RoadmapRepository` de la
 * capa Interfaces del diagrama de clases.
 *
 * Se añadió `obtenerMundo(mundoId)`, que no aparece dibujado en el diagrama
 * de clases pero sí es necesaria para el flujo del diagrama de secuencia
 * (validarAccesoNivel necesita poder consultar un solo mundo por id, igual
 * que ya existe obtenerTema(temaId) para los temas).
 */
interface RoadmapRepository {
    suspend fun obtenerTemas(): List<Tema>
    suspend fun obtenerTema(temaId: String): Tema
    suspend fun obtenerMundos(): List<Mundo>
    suspend fun obtenerMundo(mundoId: String): Mundo
    suspend fun obtenerTemasPorMundo(mundoId: String): List<Tema>
    suspend fun marcarLeccionCompletada(temaId: String)
    suspend fun marcarIntroduccionVista(temaId: String)
    suspend fun desbloquearTema(temaId: String)

    // CU-04 Resolver memorama de conceptos
    suspend fun mundoDesbloqueado(mundoId: String): Boolean
    /** Marca el nivel de memorama [nivelId] como completado (RF-17). */
    suspend fun marcarMemoramaCompletado(nivelId: String)

    // CU-10 Realizar evaluación diagnóstico
    /** Registra como conocidos los temas demostrados en el diagnóstico (RF-87). */
    suspend fun marcarTemasConocidos(mundoId: String, temaIds: List<String>)
}
