package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.ResultadoContrarreloj

interface RetoContrarrelojRepository {
    suspend fun guardarResultado(resultado: ResultadoContrarreloj)
}
