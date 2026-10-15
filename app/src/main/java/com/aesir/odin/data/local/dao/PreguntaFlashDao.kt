package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aesir.odin.data.local.entity.PreguntaFlashAttemptEntity

@Dao
interface PreguntaFlashDao {
    @Insert
    suspend fun insertar(entity: PreguntaFlashAttemptEntity)

    @Query("SELECT * FROM preguntas_flash_intentos ORDER BY fecha DESC")
    suspend fun getHistorial(): List<PreguntaFlashAttemptEntity>
}
