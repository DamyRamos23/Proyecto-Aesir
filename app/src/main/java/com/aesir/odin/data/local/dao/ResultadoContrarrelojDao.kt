package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.aesir.odin.data.local.entity.ResultadoContrarrelojEntity

@Dao
interface ResultadoContrarrelojDao {
    @Insert
    suspend fun insertar(entity: ResultadoContrarrelojEntity)
}
