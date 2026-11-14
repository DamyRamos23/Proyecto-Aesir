package com.aesir.odin.domain.racha

import java.time.LocalDate

/** Tipo de actividad que se avisa a la racha (MOD-05). */
enum class TipoActividad { LECCION, MINIJUEGO, RETO, MODO_VALHALLA }

data class ActividadTerminada(
    val tipo: TipoActividad,
    val abandonada: Boolean,
    val fechaFin: LocalDate
)

data class EstadoRacha(val diasSeguidos: Int = 0, val actualizadaHoy: Boolean = false)

/**
 * MOD-05 La carrera del sol (racha), solo lo que usa la forja del CU-02.
 *
 * El MOD-05 todavía no está implementado en el proyecto: por ahora
 * [Pendiente] recibe el aviso y no hace nada. Cuando exista, basta con
 * implementar esta interfaz y pasarla en AppContainer.
 */
interface ModuloCarreraSol {
    suspend fun alTerminarActividad(e: ActividadTerminada): EstadoRacha

    object Pendiente : ModuloCarreraSol {
        override suspend fun alTerminarActividad(e: ActividadTerminada): EstadoRacha = EstadoRacha()
    }
}
