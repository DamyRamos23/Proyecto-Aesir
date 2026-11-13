package com.aesir.odin.domain.usecase.volva

import com.aesir.odin.domain.hidromiel.ConsultaVolvaTerminada
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.ResultadoAcceso
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.AccesoVolva
import com.aesir.odin.domain.model.ConsultaVolva
import com.aesir.odin.domain.model.Fe
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.Lectura
import com.aesir.odin.domain.model.MotivoBloqueoVolva
import com.aesir.odin.domain.model.Norna
import com.aesir.odin.domain.model.ResultadoConsulta
import com.aesir.odin.domain.model.Tema
import com.aesir.odin.domain.model.Vision
import com.aesir.odin.domain.model.VisionRespondida
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import com.aesir.odin.domain.repository.VolvaRepository
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlin.random.Random

/*
 * CU-12 Retar a la bruja (La Völva). Casos de uso del diagrama de clases CU-12,
 * ajustados al SRS v3.2 (RF-160 a RF-180).
 */

/** Temas que el alumno ya superó (lecciones completadas). En modo admin cuentan todas las lecciones. */
internal fun temasSuperados(temas: List<Tema>): List<String> =
    (if (ModoAdmin.estaActivo) temas.filter { !it.esRagnarok && !it.esMinijuego }
    else temas.filter { it.completado && !it.esRagnarok && !it.esMinijuego }).map { it.id }

/** Inicio y fin (en milisegundos) del día real que contiene [momento]. */
internal fun limitesDelDia(momento: Long, zona: ZoneId): Pair<Long, Long> {
    val dia = Instant.ofEpochMilli(momento).atZone(zona).toLocalDate()
    return dia.atStartOfDay(zona).toInstant().toEpochMilli() to dia.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli()
}

internal suspend fun mundosDesbloqueados(roadmap: RoadmapRepository): List<String> =
    roadmap.obtenerMundos().filter { ModoAdmin.estaActivo || it.desbloqueado }.map { it.id }

/**
 * Pasos 1 y 2, E2.1 a E2.4: decide si el alumno puede apostar y arma lo que
 * muestra «El caldero del destino». Antes, si la aplicación se cerró a la mitad
 * de una consulta, la registra como abandonada (E6.2).
 */
class ValidarAccesoVolvaUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val volvaRepository: VolvaRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val zona: ZoneId = ZoneId.systemDefault()
) {
    suspend operator fun invoke(): AccesoVolva {
        volvaRepository.obtenerConsultaSinTerminar()?.let { pendiente ->
            volvaRepository.guardarConsulta(pendiente.copy(abandonada = true))
        }
        val temas = roadmapRepository.obtenerTemas()
        val salon = volvaRepository.obtenerSalon()
        val rango = moduloHidromiel.rangoApuestaVolva(idUsuario, mundosDesbloqueados(roadmapRepository))
        val (inicio, fin) = limitesDelDia(reloj(), zona)

        val ragnarokSuperado = ModoAdmin.estaActivo || temas.any { it.esRagnarok && it.completado }
        val motivo = when {
            !ragnarokSuperado -> MotivoBloqueoVolva.VALHALLA_CERRADO                                    // E2.2
            !ModoAdmin.estaActivo && volvaRepository.consultoEnElDia(inicio, fin) -> MotivoBloqueoVolva.CALDERO_APAGADO // E2.1
            !rango.alcanza -> MotivoBloqueoVolva.HIDROMIEL_INSUFICIENTE                                 // E2.3
            !hayVisionesSuficientes(temasSuperados(temas)) -> MotivoBloqueoVolva.VISIONES_INSUFICIENTES  // E2.4
            else -> null
        }
        val faltante = (PoliticaCosto.APUESTA_MIN_VOLVA + rango.piso - rango.saldo).coerceAtLeast(0)
        return AccesoVolva(motivo, rango, salon, if (motivo == MotivoBloqueoVolva.HIDROMIEL_INSUFICIENTE) faltante else 0)
    }

    private suspend fun hayVisionesSuficientes(temaIds: List<String>): Boolean {
        val porNorna = volvaRepository.obtenerVisiones(temaIds).groupingBy { it.norna }.eachCount()
        return Norna.entries.all { (porNorna[it] ?: 0) >= ConsultaVolva.VISIONES_POR_NORNA }
    }
}

/** Resultado de [ApostarVolvaUseCase]: si se cobró, trae el id de la consulta ya contada en el día. */
data class ApuestaVolva(val acceso: ResultadoAcceso, val consultaId: String?, val fecha: Long)

/** Pasos 4 y 5 (RF-163, RF-164): cobra la apuesta y cuenta la consulta dentro del límite del día. */
class ApostarVolvaUseCase(
    private val moduloHidromiel: ModuloHidromiel,
    private val volvaRepository: VolvaRepository,
    private val roadmapRepository: RoadmapRepository,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(apuesta: Int): ApuestaVolva {
        val acceso = moduloHidromiel.cobrarApuestaVolva(idUsuario, apuesta, mundosDesbloqueados(roadmapRepository))
        if (!acceso.autorizado) return ApuestaVolva(acceso, null, reloj())
        val fecha = reloj()
        val consulta = ConsultaVolva(UUID.randomUUID().toString(), if (ModoAdmin.estaActivo) 0 else apuesta, emptyList(), fecha = fecha)
        volvaRepository.registrarConsultaIniciada(consulta)
        return ApuestaVolva(acceso, consulta.id, fecha)
    }
}

/** No hay dos visiones de cada Norna en los temas superados (E2.4). */
class VisionesInsuficientesException : Exception("No hay suficientes visiones en los temas superados")

/** Paso 6 (RF-165): elige dos visiones de cada Norna de los temas superados, en orden Urd, Verdandi, Skuld. */
class ArmarConsultaVolvaUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val volvaRepository: VolvaRepository,
    private val azar: Random = Random.Default
) {
    suspend operator fun invoke(consultaId: String, apuesta: Int, fecha: Long): Result<ConsultaVolva> {
        val banco = volvaRepository.obtenerVisiones(temasSuperados(roadmapRepository.obtenerTemas()))
        val elegidas = elegirVisiones(banco) ?: return Result.failure(VisionesInsuficientesException())
        return Result.success(ConsultaVolva(consultaId, apuesta, elegidas, fecha = fecha))
    }

    private fun elegirVisiones(banco: List<Vision>): List<Vision>? {
        val porNorna = Norna.entries.associateWith { norna ->
            banco.filter { it.norna == norna }.shuffled(azar).take(ConsultaVolva.VISIONES_POR_NORNA)
        }
        if (porNorna.values.any { it.size < ConsultaVolva.VISIONES_POR_NORNA }) return null
        // Ronda 1: Urd, Verdandi, Skuld; ronda 2: Urd, Verdandi, Skuld.
        return (0 until ConsultaVolva.VISIONES_POR_NORNA).flatMap { ronda ->
            Norna.entries.map { porNorna.getValue(it)[ronda].barajada(azar) }
        }
    }
}

/**
 * Pasos 9 y 10, A9.1 y A10.1 (RF-168 a RF-171): valida la opción, calcula los
 * puntos según la fe y, si la visión es errada, la avisa a la bitácora de
 * errores. [opcion] es null cuando se acabaron los 15 segundos.
 */
class ResolverVisionUseCase(
    private val historialRepository: HistorialRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(consulta: ConsultaVolva, fe: Fe, opcion: Int?): VisionRespondida {
        val vision = requireNotNull(consulta.visionActual()) { "La consulta ya terminó" }
        val cumplida = vision.esCorrecta(opcion)
        if (!cumplida) registrarEnBitacora(vision, opcion)
        return VisionRespondida(
            visionId = vision.id,
            norna = vision.norna,
            fe = fe,
            opcionElegida = opcion,
            cumplida = cumplida,
            puntos = if (cumplida) fe.puntosAcierto else fe.puntosFallo
        )
    }

    /** MOD-06, E2: si no se puede guardar, la consulta continúa sin interrupción. */
    private suspend fun registrarEnBitacora(vision: Vision, opcion: Int?) {
        val elegida = opcion?.let { vision.opciones.getOrNull(it) } ?: "sin respuesta (se acabó el tiempo)"
        runCatching {
            historialRepository.registrarError(
                HistorialError(
                    id = UUID.randomUUID().toString(),
                    tipoDesafio = TIPO_RETAR_BRUJA,
                    actividadId = vision.id,
                    descripcion = "La Völva (${vision.norna.nombre}): ${vision.caso} " +
                        "Opciones: ${vision.opciones.joinToString(" | ")}. " +
                        "Correcta: ${vision.opcionCorrecta()}. Elegida: $elegida.",
                    fecha = reloj()
                )
            )
        }
    }

    companion object {
        const val TIPO_RETAR_BRUJA = "RETAR_BRUJA"
    }
}

/**
 * Pasos 12 a 15, A12.1 a A12.3, A14.1 (RF-172 a RF-180): determina la lectura,
 * notifica el pago al MOD-01, entrega la runa si fue Profecía cumplida y
 * registra la consulta.
 */
class FinalizarConsultaVolvaUseCase(
    private val volvaRepository: VolvaRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val politica: PoliticaCosto = PoliticaCosto(),
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(consulta: ConsultaVolva): ResultadoConsulta {
        val puntos = consulta.puntosDestino()
        val lectura = Lectura.dePuntos(puntos)
        val pago = politica.pagoVolva(consulta.apuesta, lectura)
        moduloHidromiel.alTerminarConsultaVolva(ConsultaVolvaTerminada(idUsuario, consulta.apuesta, lectura))

        val runa = if (lectura == Lectura.PROFECIA_CUMPLIDA) {
            volvaRepository.obtenerSalon().siguienteRuna()?.also { volvaRepository.guardarRunaObtenida(it, reloj()) }
        } else null

        val resultado = ResultadoConsulta(
            id = consulta.id,
            apuesta = consulta.apuesta,
            puntosDestino = puntos,
            visionesCumplidas = consulta.respondidas.count { it.cumplida },
            visionesErradas = consulta.respondidas.count { !it.cumplida },
            lectura = lectura,
            premioBruto = pago.bruto,
            diezmo = pago.diezmo,
            pago = pago.neto,
            runaObtenida = runa,
            abandonada = false,
            fecha = consulta.fecha,
            respondidas = consulta.respondidas
        )
        volvaRepository.guardarConsulta(resultado)
        return resultado
    }
}

/** E6.1 (RF-178): la consulta abandonada pierde la apuesta y sigue contando en el día. */
class AbandonarConsultaVolvaUseCase(private val volvaRepository: VolvaRepository) {
    suspend operator fun invoke(consulta: ConsultaVolva): ResultadoConsulta {
        val resultado = ResultadoConsulta(
            id = consulta.id,
            apuesta = consulta.apuesta,
            puntosDestino = consulta.puntosDestino(),
            visionesCumplidas = consulta.respondidas.count { it.cumplida },
            visionesErradas = consulta.respondidas.count { !it.cumplida },
            lectura = null,
            premioBruto = 0,
            diezmo = 0,
            pago = 0,
            runaObtenida = null,
            abandonada = true,
            fecha = consulta.fecha,
            respondidas = consulta.respondidas
        )
        volvaRepository.guardarConsulta(resultado)
        return resultado
    }
}
