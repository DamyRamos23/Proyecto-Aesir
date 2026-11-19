package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.ArtefactoForjadoEntity
import com.aesir.odin.data.local.entity.LogroEntity
import com.aesir.odin.data.local.entity.NocheAlbaEntity

/** CU-01 Hasta el Alba: noches, vitrina de artefactos y logros del salón. */
@Dao
interface AlbaDao {
    @Query("SELECT COUNT(*) FROM noches_alba WHERE fecha >= :inicio AND fecha < :fin")
    suspend fun contarNochesEntre(inicio: Long, fin: Long): Int

    @Query("SELECT * FROM noches_alba WHERE fecha >= :inicio AND fecha < :fin AND terminada = 1 ORDER BY fecha DESC LIMIT 1")
    suspend fun getTerminadaEntre(inicio: Long, fin: Long): NocheAlbaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarNoche(entity: NocheAlbaEntity)

    @Query("SELECT * FROM noches_alba WHERE terminada = 0 ORDER BY fecha LIMIT 1")
    suspend fun getSinTerminar(): NocheAlbaEntity?

    @Query("SELECT * FROM noches_alba WHERE terminada = 1 AND premioPendiente = 1 ORDER BY fecha")
    suspend fun getPremiosPendientes(): List<NocheAlbaEntity>

    @Query("SELECT * FROM artefactos_forjados")
    suspend fun getArtefactos(): List<ArtefactoForjadoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarArtefacto(entity: ArtefactoForjadoEntity)

    @Query("SELECT COUNT(*) FROM logros WHERE id = :id")
    suspend fun contarLogro(id: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarLogro(entity: LogroEntity)
}
