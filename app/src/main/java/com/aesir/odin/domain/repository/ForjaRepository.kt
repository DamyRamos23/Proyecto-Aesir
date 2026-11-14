package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.Artefacto
import com.aesir.odin.domain.model.ArtefactoVitrina
import com.aesir.odin.domain.model.CalidadArtefacto
import com.aesir.odin.domain.model.EtapaMartillar
import com.aesir.odin.domain.model.EtapaTemplar
import com.aesir.odin.domain.model.Forja
import com.aesir.odin.domain.model.ResultadoForja

/** Contrato de acceso a los datos de la forja de Brokkr (CU-02, capa Interfaces). */
interface ForjaRepository {
    /** Artefactos asociados a los mundos indicados. */
    suspend fun obtenerArtefactos(mundoIds: List<String>): List<Artefacto>

    /** Artefactos que el alumno ya tiene, con su calidad. */
    suspend fun obtenerVitrina(): List<ArtefactoVitrina>

    /** Forjas iniciadas entre [inicio] y [fin] (milisegundos), sin importar cómo terminaron (RF-34). */
    suspend fun contarForjasDelDia(inicio: Long, fin: Long): Int

    /** Registra la forja EN_CURSO; desde aquí ya cuenta dentro del límite del día. */
    suspend fun registrarForja(forja: Forja, fecha: Long)

    /** Un problema de diseño del mundo, al azar. */
    suspend fun obtenerProblemaMartillar(mundoId: String): EtapaMartillar

    /** Una definición o diagrama incompleto del mundo, al azar. */
    suspend fun obtenerTemplado(mundoId: String): EtapaTemplar

    /** Guarda el artefacto en la vitrina; el perfecto reemplaza al que tenía con defecto. */
    suspend fun guardarEnVitrina(artefactoId: String, calidad: CalidadArtefacto, fecha: Long)

    /** Actualiza la forja con su resultado final (PERFECTO o ROTO). */
    suspend fun guardarResultado(resultado: ResultadoForja)

    /** RF-45: marca como rotas las forjas que quedaron EN_CURSO (la app se cerró). */
    suspend fun marcarForjasEnCursoComoRotas(fecha: Long): Int
}
