package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.ResultadoContrarrelojEntity

@Dao
interface ResultadoContrarrelojDao {
    /** El registro del inicio se completa con el resultado (mismo id). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entity: ResultadoContrarrelojEntity)

    @Query("SELECT COUNT(*) FROM resultados_contrarreloj WHERE fecha >= :desde")
    suspend fun contarDesde(desde: Long): Int

    @Query("SELECT id FROM resultados_contrarreloj WHERE terminado = 0")
    suspend fun sinTerminar(): List<String>
}
