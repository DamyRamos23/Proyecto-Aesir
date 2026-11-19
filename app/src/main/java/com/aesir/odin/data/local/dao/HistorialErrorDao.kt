package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aesir.odin.data.local.entity.HistorialErrorEntity

/** MOD-06: sin operación de borrado (RN-35). */
@Dao
interface HistorialErrorDao {
    @Query("SELECT * FROM historial_errores WHERE preguntaId = :preguntaId LIMIT 1")
    suspend fun getPorPregunta(preguntaId: String): HistorialErrorEntity?

    @Insert
    suspend fun insertar(entity: HistorialErrorEntity)

    @Update
    suspend fun actualizar(entity: HistorialErrorEntity)

    @Query("SELECT * FROM historial_errores WHERE fecha >= :desde ORDER BY fecha DESC")
    suspend fun getDesde(desde: Long): List<HistorialErrorEntity>
}
