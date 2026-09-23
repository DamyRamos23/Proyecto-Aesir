package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.TemaEntity

/**
 * Acceso a datos de "temas". Métodos alineados con el diagrama de clases:
 * getAll, getById, getByMundoId, actualizarCompletado,
 * actualizarIntroduccionVista y actualizarDesbloqueado. insertAll es un
 * método de utilidad (no aparece en el diagrama) usado únicamente para
 * poblar la base de datos de ejemplo la primera vez que se crea.
 */
@Dao
interface TemaDao {

    @Query("SELECT * FROM temas ORDER BY orden ASC")
    suspend fun getAll(): List<TemaEntity>

    @Query("SELECT * FROM temas WHERE id = :id")
    suspend fun getById(id: String): TemaEntity

    @Query("SELECT * FROM temas WHERE mundoId = :mundoId ORDER BY orden ASC")
    suspend fun getByMundoId(mundoId: String): List<TemaEntity>

    @Query("UPDATE temas SET completado = :completado WHERE id = :id")
    suspend fun actualizarCompletado(id: String, completado: Boolean)

    @Query("UPDATE temas SET introduccionVista = :vista WHERE id = :id")
    suspend fun actualizarIntroduccionVista(id: String, vista: Boolean)

    @Query("UPDATE temas SET desbloqueado = :desbloqueado WHERE id = :id")
    suspend fun actualizarDesbloqueado(id: String, desbloqueado: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(temas: List<TemaEntity>)
}
