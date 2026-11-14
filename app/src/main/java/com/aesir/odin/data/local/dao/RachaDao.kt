package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.aesir.odin.data.local.entity.HitoRachaEntity
import com.aesir.odin.data.local.entity.RachaEntity

/** DAO del MOD-05 Sistema de racha. */
@Dao
interface RachaDao {
    @Query("SELECT * FROM rachas WHERE idUsuario = :idUsuario")
    suspend fun leer(idUsuario: String): RachaEntity?

    @Upsert
    suspend fun guardar(entity: RachaEntity)

    @Query("SELECT COUNT(*) > 0 FROM hitos_racha WHERE idUsuario = :idUsuario AND dias = :dias")
    suspend fun hitoCobrado(idUsuario: String, dias: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrarHito(entity: HitoRachaEntity): Long

    @Query("SELECT dias FROM hitos_racha WHERE idUsuario = :idUsuario ORDER BY dias")
    suspend fun hitosCobrados(idUsuario: String): List<Int>
}
