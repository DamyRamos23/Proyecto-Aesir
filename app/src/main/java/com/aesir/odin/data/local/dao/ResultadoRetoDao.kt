package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.aesir.odin.data.local.entity.ResultadoRetoEntity

@Dao
interface ResultadoRetoDao {
    @Insert
    suspend fun insertar(entity: ResultadoRetoEntity)
}
