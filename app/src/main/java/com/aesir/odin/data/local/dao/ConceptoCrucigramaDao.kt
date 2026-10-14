package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.ConceptoCrucigramaEntity

@Dao
interface ConceptoCrucigramaDao {
    @Query("SELECT * FROM conceptos_crucigrama WHERE temaId IN (:temaIds)")
    suspend fun getByTemaIds(temaIds: List<String>): List<ConceptoCrucigramaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(entities: List<ConceptoCrucigramaEntity>)
}
