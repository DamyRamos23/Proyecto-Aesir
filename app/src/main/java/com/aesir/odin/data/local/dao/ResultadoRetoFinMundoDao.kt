package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aesir.odin.data.local.entity.ResultadoRetoFinMundoEntity

@Dao
interface ResultadoRetoFinMundoDao {
    @Insert
    suspend fun insertar(entity: ResultadoRetoFinMundoEntity)

    @Query("SELECT * FROM resultados_reto_fin_mundo WHERE mundoId = :mundoId ORDER BY fecha DESC")
    suspend fun getPorMundo(mundoId: String): List<ResultadoRetoFinMundoEntity>
}
