package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.aesir.odin.data.local.entity.ArtefactoEntity
import com.aesir.odin.data.local.entity.ForjaEntity
import com.aesir.odin.data.local.entity.ProblemaMartillarEntity
import com.aesir.odin.data.local.entity.TempladoEntity
import com.aesir.odin.data.local.entity.VitrinaArtefactoEntity

/** DAOs del CU-02 Forjar artefactos en la apuesta de Brokkr. */

@Dao
interface ArtefactoDao {
    @Query("SELECT * FROM artefactos WHERE mundoId IN (:mundoIds)")
    suspend fun getPorMundos(mundoIds: List<String>): List<ArtefactoEntity>

    @Query("SELECT * FROM artefactos")
    suspend fun getAll(): List<ArtefactoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(artefactos: List<ArtefactoEntity>)
}

@Dao
interface VitrinaDao {
    @Query("SELECT * FROM vitrina")
    suspend fun getAll(): List<VitrinaArtefactoEntity>

    @Query("SELECT * FROM vitrina WHERE artefactoId = :artefactoId")
    suspend fun getById(artefactoId: String): VitrinaArtefactoEntity?

    @Upsert
    suspend fun upsert(entity: VitrinaArtefactoEntity)
}

@Dao
interface ForjaDao {
    @Query("SELECT COUNT(*) FROM forjas WHERE fecha >= :inicio AND fecha < :fin")
    suspend fun contarEntre(inicio: Long, fin: Long): Int

    @Query("SELECT * FROM forjas WHERE id = :id")
    suspend fun getById(id: String): ForjaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entity: ForjaEntity)

    @Update
    suspend fun actualizar(entity: ForjaEntity)

    /** RF-45: las forjas que quedaron en curso se rompen y su apuesta se pierde. */
    @Query("UPDATE forjas SET estado = 'ROTO', abandonada = 1, hidromielNeto = -apuesta WHERE estado = 'EN_CURSO'")
    suspend fun marcarEnCursoComoRotas(): Int
}

@Dao
interface EtapaForjaDao {
    @Query("SELECT * FROM problemas_martillar WHERE mundoId = :mundoId")
    suspend fun getProblemas(mundoId: String): List<ProblemaMartillarEntity>

    @Query("SELECT * FROM templados WHERE mundoId = :mundoId")
    suspend fun getTemplados(mundoId: String): List<TempladoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblemas(lista: List<ProblemaMartillarEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplados(lista: List<TempladoEntity>)
}
