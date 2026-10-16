package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.ParConceptoEntity
import com.aesir.odin.data.local.entity.ResultadoMemoramaEntity

/** Acceso a datos del CU-04 (MemoramaDao del diagrama de clases). */
@Dao
interface MemoramaDao {

    @Query("SELECT * FROM pares_concepto WHERE mundoId = :mundoId")
    suspend fun getParesByMundoId(mundoId: String): List<ParConceptoEntity>

    @Query("SELECT COUNT(*) FROM pares_concepto WHERE mundoId = :mundoId")
    suspend fun contarPares(mundoId: String): Int

    @Insert
    suspend fun insertarResultado(entity: ResultadoMemoramaEntity)

    /** Utilidad (no aparece en el diagrama) para poblar el banco. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPares(pares: List<ParConceptoEntity>)
}
