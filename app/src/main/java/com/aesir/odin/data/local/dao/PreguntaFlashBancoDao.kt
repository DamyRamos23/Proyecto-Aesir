package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.PreguntaFlashEntity

/** Acceso al banco de preguntas compartido (ver `BancoPreguntas`). */
@Dao
interface PreguntaFlashBancoDao {
    @Query("SELECT * FROM preguntas_flash WHERE temaId = :temaId ORDER BY RANDOM() LIMIT 1")
    suspend fun getAleatoriaPorTema(temaId: String): PreguntaFlashEntity?

    /** Varias preguntas distintas y al azar de los temas dados (lo usa CU-11 Reto Contrarreloj). */
    @Query("SELECT * FROM preguntas_flash WHERE temaId IN (:temaIds) ORDER BY RANDOM() LIMIT :limite")
    suspend fun getAleatoriasPorTemas(temaIds: List<String>, limite: Int): List<PreguntaFlashEntity>

    /** Utilidad (no está en el diagrama): para poblar el banco desde el código. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(preguntas: List<PreguntaFlashEntity>)
}
