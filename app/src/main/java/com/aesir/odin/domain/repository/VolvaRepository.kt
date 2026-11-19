package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ConsultaVolva
import com.aesir.odin.domain.model.ResultadoConsulta
import com.aesir.odin.domain.model.RunaFuthark
import com.aesir.odin.domain.model.SalonRunas
import com.aesir.odin.domain.model.Vision

/** Persistencia del CU-12 Retar a la bruja (interfaz VolvaRepository del diagrama de clases). */
interface VolvaRepository {
    suspend fun obtenerVisiones(temaIds: List<String>): List<Vision>
    suspend fun obtenerSalon(): SalonRunas
    suspend fun guardarRunaObtenida(runa: RunaFuthark, fecha: Long)
    /** RF-161: true si ya hay una consulta (terminada o abandonada) entre [inicio] y [fin]. */
    suspend fun consultoEnElDia(inicio: Long, fin: Long): Boolean
    /** Paso 5: la consulta queda contada en el día desde que se cobra la apuesta. */
    suspend fun registrarConsultaIniciada(consulta: ConsultaVolva)
    /** RF-180 */
    suspend fun guardarConsulta(resultado: ResultadoConsulta)
    /** E6.2: consulta que se quedó a la mitad al cerrarse la aplicación. */
    suspend fun obtenerConsultaSinTerminar(): ResultadoConsulta?
}
