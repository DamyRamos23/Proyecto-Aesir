package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.TemaEntity

@Dao
interface TemaDao {
    @Query("SELECT * FROM temas ORDER BY mundoId, orden")
    suspend fun getAll(): List<TemaEntity>

    @Query("SELECT * FROM temas WHERE id = :id")
    suspend fun getById(id: String): TemaEntity?

    @Query("UPDATE temas SET leccionCompletada = 1 WHERE id = :id")
    suspend fun marcarLeccionCompletada(id: String)

    @Query("UPDATE temas SET desbloqueado = 1 WHERE id = :id")
    suspend fun desbloquear(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(temas: List<TemaEntity>)
}
