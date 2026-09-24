package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.MundoEntity

/**
 * Acceso a datos de "mundos". Métodos alineados con el diagrama de clases:
 * getAll, getById y actualizarDesbloqueado. insertAll es un método de
 * utilidad (no aparece en el diagrama) usado únicamente para poblar la
 * base de datos de ejemplo la primera vez que se crea.
 */
@Dao
interface MundoDao {

    @Query("SELECT * FROM mundos ORDER BY orden ASC")
    suspend fun getAll(): List<MundoEntity>

    @Query("SELECT * FROM mundos WHERE id = :id")
    suspend fun getById(id: String): MundoEntity

    @Query("UPDATE mundos SET desbloqueado = :desbloqueado WHERE id = :id")
    suspend fun actualizarDesbloqueado(id: String, desbloqueado: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mundos: List<MundoEntity>)
}
