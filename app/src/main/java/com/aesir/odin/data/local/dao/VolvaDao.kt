package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.ConsultaVolvaEntity
import com.aesir.odin.data.local.entity.RunaObtenidaEntity
import com.aesir.odin.data.local.entity.VisionEntity

/** CU-12 Retar a la bruja: visiones, runas del salón y consultas. */
@Dao
interface VolvaDao {
    @Query("SELECT * FROM visiones_volva WHERE temaId IN (:temaIds)")
    suspend fun getVisionesPorTemas(temaIds: List<String>): List<VisionEntity>

    @Query("SELECT * FROM runas_obtenidas ORDER BY orden")
    suspend fun getRunas(): List<RunaObtenidaEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarRuna(entity: RunaObtenidaEntity)

    @Query("SELECT COUNT(*) FROM consultas_volva WHERE fecha >= :inicio AND fecha < :fin")
    suspend fun contarConsultasEntre(inicio: Long, fin: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarConsulta(entity: ConsultaVolvaEntity)

    @Query("SELECT * FROM consultas_volva WHERE terminada = 0 ORDER BY fecha LIMIT 1")
    suspend fun getSinTerminar(): ConsultaVolvaEntity?
}
