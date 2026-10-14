package com.aesir.odin.domain.hidromiel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Catálogo en memoria: los niveles normales de cada mundo forman un anillo
 * (cada uno conectado a sus dos vecinos y el último con el primero) y el
 * Ragnarök va al final, igual que CatalogoNivelesImpl.
 */
class FakeCatalogo(mundos: Map<String, Int>) : CatalogoNiveles {
    private val ordenMundos = mundos.keys.toList()
    private val niveles: Map<String, List<NivelInfo>> = mundos.mapValues { (idMundo, normales) ->
        (1..normales).map { i ->
            NivelInfo("$idMundo-n$i", idMundo, "Nivel $i", TipoNivel.NORMAL, esPrimero = i == 1)
        } + NivelInfo("$idMundo-ragnarok", idMundo, "Ragnarök", TipoNivel.RAGNAROK, esPrimero = false)
    }
    private val porId = niveles.values.flatten().associateBy { it.idNivel }

    override suspend fun obtener(idNivel: String) = porId.getValue(idNivel)
    override suspend fun nivelesDelMundo(idMundo: String) = niveles.getValue(idMundo)
    override suspend fun conectados(idNivel: String): List<String> {
        val nivel = porId.getValue(idNivel)
        val normales = niveles.getValue(nivel.idMundo).filter { it.tipo == TipoNivel.NORMAL }
        if (nivel.tipo == TipoNivel.RAGNAROK) return normales.map { it.idNivel }
        val i = normales.indexOf(nivel)
        val n = normales.size
        return listOf(normales[(i - 1 + n) % n], normales[(i + 1) % n]).map { it.idNivel }.distinct()
    }
    override suspend fun siguienteMundo(idMundo: String) = ordenMundos.getOrNull(ordenMundos.indexOf(idMundo) + 1)
}

class FakeHidromielRepository : HidromielRepository {
    private val saldos = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val fechasDiaria = mutableMapOf<String, java.time.LocalDate?>()
    val historial = mutableListOf<MovimientoHidromiel>()
    val superados = mutableMapOf<Pair<String, String>, NivelSuperado>()
    val mundosDesbloqueados = mutableListOf<MundoDesbloqueado>()

    override suspend fun obtenerReserva(idUsuario: String): ReservaHidromiel? =
        saldos.value[idUsuario]?.let { ReservaHidromiel(idUsuario, it, fechasDiaria[idUsuario]) }

    override suspend fun guardarReserva(reserva: ReservaHidromiel) {
        saldos.value = saldos.value + (reserva.idUsuario to reserva.saldo)
        fechasDiaria[reserva.idUsuario] = reserva.fechaUltimaDiaria
        historial += reserva.movimientosNuevos
        reserva.marcarMovimientosGuardados()
    }

    override fun observarSaldo(idUsuario: String): Flow<Int?> = saldos.map { it[idUsuario] }

    override suspend fun obtenerNivelSuperado(idUsuario: String, idNivel: String) = superados[idUsuario to idNivel]

    override suspend fun registrarNivelSuperado(nivel: NivelSuperado) {
        superados.putIfAbsent(nivel.idUsuario to nivel.idNivel, nivel)
    }

    override suspend fun registrarMundoDesbloqueado(mundo: MundoDesbloqueado) {
        mundosDesbloqueados += mundo
    }

    override suspend fun <T> enTransaccion(bloque: suspend () -> T): T = bloque()

    fun saldo(idUsuario: String = U) = saldos.value[idUsuario]

    /** Pone el saldo directamente (para preparar escenarios). */
    fun fijarSaldo(saldo: Int, idUsuario: String = U) {
        saldos.value = saldos.value + (idUsuario to saldo)
    }
}

const val U = "alumno"
