package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aesir.odin.data.local.entity.ConceptoEnigmaEntity
import com.aesir.odin.data.local.entity.PreguntaEnigmaEntity
import com.aesir.odin.data.local.entity.ResultadoDueloEntity

@Dao
interface ConceptoEnigmaDao {
    @Query("SELECT * FROM conceptos_enigma WHERE temaId IN (:temaIds)")
    suspend fun getPorTemas(temaIds: List<String>): List<ConceptoEnigmaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conceptos: List<ConceptoEnigmaEntity>)
}

@Dao
interface PreguntaEnigmaDao {
    @Query("SELECT * FROM preguntas_enigma")
    suspend fun getAll(): List<PreguntaEnigmaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(preguntas: List<PreguntaEnigmaEntity>)
}

@Dao
interface ResultadoDueloDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entity: ResultadoDueloEntity)

    @Update
    suspend fun actualizar(entity: ResultadoDueloEntity)

    @Query("SELECT COUNT(*) FROM duelos_enigmas WHERE fecha >= :inicioDia")
    suspend fun contarDesde(inicioDia: Long): Int

    @Query("SELECT * FROM duelos_enigmas WHERE resultado IS NULL ORDER BY fecha DESC LIMIT 1")
    suspend fun getSinTerminar(): ResultadoDueloEntity?
}
