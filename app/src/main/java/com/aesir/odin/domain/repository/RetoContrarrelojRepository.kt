package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ResultadoContrarreloj

interface RetoContrarrelojRepository {
    /** Guarda (o completa) el resultado de un reto. */
    suspend fun guardarResultado(resultado: ResultadoContrarreloj)

    /** RF-147: retos iniciados desde [inicioDia] (cuentan aunque se hayan abandonado). */
    suspend fun contarRetosDelDia(inicioDia: Long): Int = 0

    /** Paso 4: el reto empezó (la entrada ya se cobró) y queda registrado como en curso. */
    suspend fun registrarInicio(id: String, fecha: Long) = Unit

    /** E7.2: ids de los retos que quedaron en curso al cerrarse la aplicación. */
    suspend fun retosSinTerminar(): List<String> = emptyList()
}
