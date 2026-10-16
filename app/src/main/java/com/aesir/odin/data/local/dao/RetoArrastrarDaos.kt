package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aesir.odin.data.local.entity.ElementoArrastrableEntity
import com.aesir.odin.data.local.entity.PosicionDestinoEntity
import com.aesir.odin.data.local.entity.ResultadoRetoEntity
import com.aesir.odin.data.local.entity.RetoArrastrarEntity

@Dao
interface RetoDao {
    @Query("SELECT * FROM retos_arrastrar WHERE temaId = :temaId LIMIT 1")
    suspend fun getByTemaId(temaId: String): RetoArrastrarEntity?

    @Query("UPDATE retos_arrastrar SET completado = :completado WHERE id = :id")
    suspend fun actualizarCompletado(id: String, completado: Boolean)

    @Insert
    suspend fun insertarResultado(entity: ResultadoRetoEntity)
}

@Dao
interface ElementoArrastrableDao {
    @Query("SELECT * FROM elementos_arrastrables WHERE retoId = :retoId")
    suspend fun getByRetoId(retoId: String): List<ElementoArrastrableEntity>
}

@Dao
interface PosicionDestinoDao {
    @Query("SELECT * FROM posiciones_destino WHERE retoId = :retoId ORDER BY orden")
    suspend fun getByRetoId(retoId: String): List<PosicionDestinoEntity>
}
