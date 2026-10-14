package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aesir.odin.data.local.entity.ResultadoCrucigramaEntity

@Dao
interface ResultadoCrucigramaDao {
    @Insert
    suspend fun insertar(entity: ResultadoCrucigramaEntity)

    @Query("SELECT * FROM resultados_crucigrama WHERE temaId = :temaId ORDER BY puntajeFinal DESC, fecha ASC LIMIT 1")
    suspend fun getMejorPorTema(temaId: String): ResultadoCrucigramaEntity?
}
