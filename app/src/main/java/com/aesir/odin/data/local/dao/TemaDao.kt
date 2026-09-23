package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.aesir.odin.data.local.entity.TemaEntity

@Dao
interface TemaDao {

    // Paso 6: getById(id) -> Paso 7: TemaEntity
    @Query("SELECT * FROM temas WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TemaEntity?

    // Paso 16: actualizarIntroduccionVista(id, true) -> Paso 17: Unit
    @Query("UPDATE temas SET introduccionVista = :vista WHERE id = :id")
    suspend fun actualizarIntroduccionVista(id: String, vista: Boolean)
}
