package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aesir.odin.data.local.entity.ResultadoFlashcardsEntity
import com.aesir.odin.data.local.entity.ResultadoRetoFinMundoEntity

@Dao
interface ResultadoFlashcardsDao {
    @Insert
    suspend fun insertar(entity: ResultadoFlashcardsEntity)

    @Query("SELECT * FROM resultados_flashcards WHERE temaId = :temaId ORDER BY puntaje DESC, fecha ASC LIMIT 1")
    suspend fun getMejorPorTema(temaId: String): ResultadoFlashcardsEntity?
}

@Dao
interface RetoFinMundoDao {
    @Insert
    suspend fun insertar(entity: ResultadoRetoFinMundoEntity)

    @Query("SELECT * FROM intentos_reto_fin_mundo WHERE mundoId = :mundoId ORDER BY fecha DESC")
    suspend fun getPorMundo(mundoId: String): List<ResultadoRetoFinMundoEntity>
}
