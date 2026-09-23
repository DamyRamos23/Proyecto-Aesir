package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aesir.odin.data.local.entity.HistorialErrorEntity

@Dao
interface HistorialErrorDao {
    @Insert
    suspend fun insertar(entity: HistorialErrorEntity)

    @Query("SELECT * FROM historial_errores ORDER BY fecha DESC")
    suspend fun getAll(): List<HistorialErrorEntity>

    @Query("SELECT * FROM historial_errores WHERE fecha >= :desde ORDER BY fecha DESC")
    suspend fun getDesde(desde: Long): List<HistorialErrorEntity>
}
