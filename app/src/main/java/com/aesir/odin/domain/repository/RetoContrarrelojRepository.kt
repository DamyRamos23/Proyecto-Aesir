package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ResultadoReto

interface RetoContrarrelojRepository {
    suspend fun guardarResultado(resultado: ResultadoReto)
}
