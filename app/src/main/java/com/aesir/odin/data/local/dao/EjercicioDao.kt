package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.EjercicioEntity

@Dao
interface EjercicioDao {
    @Query("SELECT * FROM ejercicios WHERE leccionId = :leccionId ORDER BY orden")
    suspend fun getByLeccionId(leccionId: String): List<EjercicioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(ejercicios: List<EjercicioEntity>)
}
