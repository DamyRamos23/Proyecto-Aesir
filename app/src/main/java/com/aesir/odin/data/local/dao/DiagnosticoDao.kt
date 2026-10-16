package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aesir.odin.data.local.entity.PreguntaDiagnosticaEntity
import com.aesir.odin.data.local.entity.ResultadoDiagnosticoEntity

/** Acceso a datos del CU-10 (DiagnosticoDao del diagrama de clases). */
@Dao
interface DiagnosticoDao {

    /** Preguntas del mundo agrupadas por tema, para que la evaluación sea estable. */
    @Query("SELECT * FROM preguntas_diagnostico WHERE mundoId = :mundoId ORDER BY temaId ASC, id ASC LIMIT :limite")
    suspend fun getPreguntasByMundoId(mundoId: String, limite: Int): List<PreguntaDiagnosticaEntity>

    @Query("SELECT * FROM resultados_diagnostico WHERE mundoId = :mundoId")
    suspend fun getResultado(mundoId: String): ResultadoDiagnosticoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarResultado(entity: ResultadoDiagnosticoEntity)

    /** Utilidad (no aparece en el diagrama) para poblar el banco. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPreguntas(preguntas: List<PreguntaDiagnosticaEntity>)
}
