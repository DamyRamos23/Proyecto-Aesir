package com.aesir.odin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.aesir.odin.data.local.entity.MovimientoHidromielEntity
import com.aesir.odin.data.local.entity.MundoDesbloqueadoEntity
import com.aesir.odin.data.local.entity.NivelSuperadoEntity
import com.aesir.odin.data.local.entity.ReservaHidromielEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HidromielDao {

    @Query("SELECT * FROM reservas_hidromiel WHERE idUsuario = :idUsuario")
    suspend fun getReserva(idUsuario: String): ReservaHidromielEntity?

    @Query("SELECT saldo FROM reservas_hidromiel WHERE idUsuario = :idUsuario")
    fun observarSaldo(idUsuario: String): Flow<Int?>

    @Upsert
    suspend fun guardarReserva(reserva: ReservaHidromielEntity)

    @Insert
    suspend fun insertarMovimientos(movimientos: List<MovimientoHidromielEntity>)

    @Query("SELECT * FROM movimientos_hidromiel WHERE idUsuario = :idUsuario ORDER BY id DESC")
    suspend fun getMovimientos(idUsuario: String): List<MovimientoHidromielEntity>

    @Query("SELECT * FROM niveles_superados WHERE idUsuario = :idUsuario AND idNivel = :idNivel")
    suspend fun getNivelSuperado(idUsuario: String, idNivel: String): NivelSuperadoEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarNivelSuperado(nivel: NivelSuperadoEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarMundoDesbloqueado(mundo: MundoDesbloqueadoEntity)
}
