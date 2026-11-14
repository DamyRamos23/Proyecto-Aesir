package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.RachaDao
import com.aesir.odin.data.local.entity.HitoRachaEntity
import com.aesir.odin.data.local.entity.RachaEntity
import com.aesir.odin.domain.racha.AlmacenRacha
import com.aesir.odin.domain.racha.Hito
import com.aesir.odin.domain.racha.Racha
import java.time.LocalDate

/** Almacén de la racha (MOD-05) sobre Room. */
class AlmacenRachaImpl(
    private val dao: RachaDao,
    private val reloj: () -> Long = System::currentTimeMillis
) : AlmacenRacha {

    override suspend fun leer(idUsuario: String): Racha? = dao.leer(idUsuario)?.let {
        Racha(
            idUsuario = it.idUsuario,
            actual = it.actual,
            maxima = it.maxima,
            ultimaActividad = it.ultimaActividad?.let(LocalDate::parse),
            ofrendas = it.ofrendas,
            titulos = it.titulos
        )
    }

    override suspend fun guardar(racha: Racha): Boolean {
        dao.guardar(
            RachaEntity(
                idUsuario = racha.idUsuario,
                actual = racha.actual,
                maxima = racha.maxima,
                ultimaActividad = racha.ultimaActividad?.toString(),
                ofrendas = racha.ofrendas,
                titulos = racha.titulos
            )
        )
        return true
    }

    override suspend fun hitoCobrado(idUsuario: String, dias: Int): Boolean = dao.hitoCobrado(idUsuario, dias)

    override suspend fun registrarHito(idUsuario: String, hito: Hito): Boolean =
        dao.registrarHito(HitoRachaEntity(idUsuario, hito.dias, hito.bono, hito.titulo, reloj())) != -1L

    override suspend fun hitosCobrados(idUsuario: String): List<Int> = dao.hitosCobrados(idUsuario)
}
