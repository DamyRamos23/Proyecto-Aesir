package com.aesir.odin.data.repository

import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.data.local.dao.AlbaDao
import com.aesir.odin.data.local.dao.VitrinaDao
import com.aesir.odin.data.local.entity.LogroEntity
import com.aesir.odin.data.local.entity.NocheAlbaEntity
import com.aesir.odin.domain.model.Artefacto
import com.aesir.odin.domain.model.ArtefactoForjado
import com.aesir.odin.domain.model.Desenlace
import com.aesir.odin.domain.model.Enemigo
import com.aesir.odin.domain.model.FaseNoche
import com.aesir.odin.domain.model.NocheAlba
import com.aesir.odin.domain.model.ResultadoNoche
import com.aesir.odin.domain.repository.AlbaRepository

/**
 * [vitrinaDao]: la vitrina del salón que llena la forja de Brokkr (CU-02). Si se
 * pasa, los artefactos de la noche salen de ahí; si no, de la tabla propia del CU-01.
 */
class AlbaRepositoryImpl(
    private val dao: AlbaDao,
    private val vitrinaDao: VitrinaDao? = null
) : AlbaRepository {

    override suspend fun jugoNocheEnElDia(inicio: Long, fin: Long): Boolean = dao.contarNochesEntre(inicio, fin) >= PoliticaCosto.NOCHES_ALBA_DIA

    override suspend fun resultadoDelDia(inicio: Long, fin: Long): ResultadoNoche? =
        dao.getTerminadaEntre(inicio, fin)?.aResultado()

    override suspend fun guardarNocheEnCurso(noche: NocheAlba) = dao.guardarNoche(
        NocheAlbaEntity(
            id = noche.id, fecha = noche.fecha, apuesta = noche.apuesta, oleada = noche.enemigo.numero,
            golpesRestantes = noche.golpesRestantes, escudos = noche.escudos,
            multiplicador = noche.multiplicadorAlcanzado, bonoDraupnir = noche.bonoDraupnir,
            aciertos = noche.aciertos, errores = noche.errores,
            artefactosUsados = noche.artefactosUsados.map { it.name },
            preguntasMostradas = noche.preguntasMostradas, preguntasFalladas = noche.preguntasFalladas,
            temasConError = noche.temasConError.toList(), fase = noche.fase.name,
            desenlace = null, premioBruto = 0, diezmo = 0, premioNeto = 0, premioPendiente = false, terminada = false
        )
    )

    override suspend fun obtenerNocheSinTerminar(): NocheAlba? = dao.getSinTerminar()?.let { e ->
        NocheAlba(
            id = e.id, apuesta = e.apuesta, fecha = e.fecha, enemigo = Enemigo.deNumero(e.oleada),
            golpesRestantes = e.golpesRestantes, escudos = e.escudos, multiplicadorAlcanzado = e.multiplicador,
            bonoDraupnir = e.bonoDraupnir, aciertos = e.aciertos, errores = e.errores,
            artefactos = obtenerArtefactos(),
            artefactosUsados = e.artefactosUsados.mapNotNull { a -> runCatching { Artefacto.valueOf(a) }.getOrNull() }.toSet(),
            preguntasMostradas = e.preguntasMostradas, preguntasFalladas = e.preguntasFalladas,
            temasConError = e.temasConError.toSet(),
            fase = runCatching { FaseNoche.valueOf(e.fase) }.getOrDefault(FaseNoche.EN_OLEADA)
        )
    }

    override suspend fun guardarResultado(resultado: ResultadoNoche) = dao.guardarNoche(
        NocheAlbaEntity(
            id = resultado.id, fecha = resultado.fecha, apuesta = resultado.apuesta, oleada = resultado.oleadaAlcanzada,
            golpesRestantes = 0, escudos = resultado.escudosRestantes, multiplicador = resultado.multiplicador,
            bonoDraupnir = 0, aciertos = resultado.aciertos, errores = resultado.errores,
            artefactosUsados = resultado.artefactosUsados.map { it.name },
            preguntasMostradas = emptyList(), preguntasFalladas = emptyList(),
            temasConError = resultado.temasConError, fase = FaseNoche.TERMINADA.name,
            desenlace = resultado.desenlace.name, premioBruto = resultado.premioBruto, diezmo = resultado.diezmo,
            premioNeto = resultado.premioNeto, premioPendiente = resultado.premioPendiente, terminada = true
        )
    )

    override suspend fun obtenerPremiosPendientes(): List<ResultadoNoche> = dao.getPremiosPendientes().map { it.aResultado() }

    override suspend fun obtenerArtefactos(): List<ArtefactoForjado> {
        val vitrina = vitrinaDao ?: return artefactosPropios()
        return vitrina.getAll().mapNotNull { e ->
            val artefacto = Artefacto.deIdForja(e.artefactoId) ?: return@mapNotNull null
            ArtefactoForjado(artefacto)
        }.sortedBy { it.artefacto.ordinal }
    }

    private suspend fun artefactosPropios(): List<ArtefactoForjado> = dao.getArtefactos().mapNotNull { e ->
        val artefacto = runCatching { Artefacto.valueOf(e.artefacto) }.getOrNull() ?: return@mapNotNull null
        ArtefactoForjado(artefacto)
    }.sortedBy { it.artefacto.ordinal }

    override suspend fun tieneEinherjar(): Boolean = dao.contarLogro(LOGRO_HASTA_EL_ALBA) > 0

    override suspend fun otorgarEinherjar(fecha: Long) {
        dao.insertarLogro(LogroEntity(LOGRO_HASTA_EL_ALBA, fecha))
        dao.insertarLogro(LogroEntity(TITULO_EINHERJAR, fecha))
    }

    private fun NocheAlbaEntity.aResultado() = ResultadoNoche(
        id = id, fecha = fecha, apuesta = apuesta, oleadaAlcanzada = oleada,
        oleadasSuperadas = when (desenlace) {
            Desenlace.ALBA.name -> NocheAlba.OLEADAS
            else -> Enemigo.entries.firstOrNull { it.multiplicador == multiplicador }?.numero ?: 0
        },
        desenlace = desenlace?.let { runCatching { Desenlace.valueOf(it) }.getOrNull() } ?: Desenlace.CAIDA,
        multiplicador = multiplicador, aciertos = aciertos, errores = errores, escudosRestantes = escudos,
        artefactosUsados = artefactosUsados.mapNotNull { runCatching { Artefacto.valueOf(it) }.getOrNull() },
        premioBruto = premioBruto, diezmo = diezmo, premioNeto = premioNeto,
        temasConError = temasConError, premioPendiente = premioPendiente
    )

    companion object {
        const val LOGRO_HASTA_EL_ALBA = "LOGRO_HASTA_EL_ALBA"
        const val TITULO_EINHERJAR = "TITULO_EINHERJAR"
    }
}
