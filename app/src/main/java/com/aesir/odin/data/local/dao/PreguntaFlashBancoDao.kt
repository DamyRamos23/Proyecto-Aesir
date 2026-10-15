package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.aesir.odin.data.local.entity.PreguntaFlashEntity

@Dao
interface PreguntaFlashBancoDao {
    @Query("SELECT * FROM preguntas_flash WHERE temaId IN (:temaIds) ORDER BY RANDOM() LIMIT :limite")
    suspend fun getAleatoriasPorTemas(temaIds: List<String>, limite: Int): List<PreguntaFlashEntity>
}
