package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ArtefactoForjado
import com.aesir.odin.domain.model.NocheAlba
import com.aesir.odin.domain.model.ResultadoNoche

/** Persistencia del CU-01 Sobrevivir oleadas (Hasta el Alba). */
interface AlbaRepository {
    /** RF-02: true si ya hay una noche (terminada o en curso) entre [inicio] y [fin]. */
    suspend fun jugoNocheEnElDia(inicio: Long, fin: Long): Boolean

    /** A2.2: el resultado de la noche que ya se jugó en ese día. */
    suspend fun resultadoDelDia(inicio: Long, fin: Long): ResultadoNoche?

    /** Pasos 7 a 16: guarda cómo va la noche para recuperarla si se cierra la app (E9.2, E15.1). */
    suspend fun guardarNocheEnCurso(noche: NocheAlba)

    suspend fun obtenerNocheSinTerminar(): NocheAlba?

    /** RF-29 */
    suspend fun guardarResultado(resultado: ResultadoNoche)

    /** E20.1: noches cobradas cuyo premio no se pudo acreditar. */
    suspend fun obtenerPremiosPendientes(): List<ResultadoNoche>

    /** Vitrina del salón: artefactos forjados en la Apuesta de Brokkr (CU-02). */
    suspend fun obtenerArtefactos(): List<ArtefactoForjado>

    /** RF-18, RF-19, A21.1: logro «Hasta el Alba» y título de Einherjar. */
    suspend fun tieneEinherjar(): Boolean

    suspend fun otorgarEinherjar(fecha: Long)
}
