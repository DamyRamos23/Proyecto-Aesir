package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.LeccionEntity
import com.aesir.odin.data.local.entity.ResultadoLeccionEntity

@Dao
interface LeccionDao {
    @Query("SELECT * FROM lecciones WHERE id = :id")
    suspend fun getById(id: String): LeccionEntity?

    @Query("SELECT * FROM lecciones WHERE temaId = :temaId ORDER BY orden")
    suspend fun getByTemaId(temaId: String): List<LeccionEntity>

    @Query("SELECT * FROM lecciones ORDER BY temaId, orden")
    suspend fun getAll(): List<LeccionEntity>

    @Query("UPDATE lecciones SET completada = :completada WHERE id = :id")
    suspend fun actualizarCompletada(id: String, completada: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(lecciones: List<LeccionEntity>)

    @Insert
    suspend fun insertarResultado(entity: ResultadoLeccionEntity)
}
