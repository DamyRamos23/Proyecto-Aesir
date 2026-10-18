package com.aesir.odin.domain.repository

import com.aesir.odin.domain.model.EstadoPreguntaFlash
import kotlinx.coroutines.flow.Flow

/**
 * Puente entre CU-06 Pregunta Flash y MOD-01 Sistema de Hidromiel: los puntos
 * de la pregunta flash SON hidromiel (correcta +5, fallar −2, nunca baja de 0).
 */
interface HidromielFlashRepository {
    /** Saldo de hidromiel del alumno, actualizado en vivo. */
    fun observarSaldo(): Flow<Int>

    /** Abona o descuenta hidromiel según cómo se resolvió la pregunta. */
    suspend fun aplicarResultado(estado: EstadoPreguntaFlash): CambioHidromiel
}

/**
 * @property cantidad lo que cambió el saldo (+5, −2, o menos si no alcanzaba).
 * @property saldo hidromiel total después del cambio.
 */
data class CambioHidromiel(val cantidad: Int, val saldo: Int)
