package com.aesir.odin.domain.usecase.duelo

import com.aesir.odin.domain.hidromiel.DueloFinalizado
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.ResultadoApuesta
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.AccesoDuelo
import com.aesir.odin.domain.model.CierreDuelo
import com.aesir.odin.domain.model.DueloEnigmas
import com.aesir.odin.domain.model.EstadoFinalDuelo
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.MotivoSinAcceso
import com.aesir.odin.domain.model.RespuestaGigante
import com.aesir.odin.domain.model.ResultadoAdivinanza
import com.aesir.odin.domain.model.ResultadoDuelo
import com.aesir.odin.domain.model.TarjetaConcepto
import com.aesir.odin.domain.repository.DueloRepository
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlin.random.Random

/** Milisegundos del inicio del día real (hora local) que contiene [ms]. RN-10. */
fun inicioDelDia(ms: Long, zona: ZoneId = ZoneId.systemDefault()): Long =
    Instant.ofEpochMilli(ms).atZone(zona).toLocalDate().atStartOfDay(zona).toInstant().toEpochMilli()

/**
 * Pasos 1 y 2, RF-181 a RF-184 y E4.1: Ragnarök superado, duelos restantes
 * del día y si el hidromiel alcanza la apuesta sin bajar del piso (RN-08).
 */
class ValidarAccesoDueloUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val dueloRepository: DueloRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(): AccesoDuelo {
        val apuesta = PoliticaCosto.APUESTA_DUELO
        // Modo admin: todo abierto, también el duelo (la apuesta se cobra igual).
        val ragnarok = ModoAdmin.estaActivo || roadmapRepository.haSuperadoAlgunRagnarok()
        val jugados = dueloRepository.contarDuelosDelDia(inicioDelDia(reloj()))
        val restantes = (PoliticaCosto.MAX_DUELOS_DIA - jugados).coerceAtLeast(0)
        val piso = moduloHidromiel.calcularPisoApuesta(idUsuario)
        val saldo = moduloHidromiel.observarSaldo(idUsuario).first()
        val faltante = (apuesta + piso - saldo).coerceAtLeast(0)
        val motivo = when {
            !ragnarok -> MotivoSinAcceso.SIN_RAGNAROK_SUPERADO
            restantes == 0 -> MotivoSinAcceso.LIMITE_DIARIO_ALCANZADO
            faltante > 0 -> MotivoSinAcceso.HIDROMIEL_INSUFICIENTE
            else -> null
        }
        return AccesoDuelo(motivo == null, motivo, apuesta, saldo, faltante, restantes)
    }
}

/**
 * Paso 5, RF-186 a RF-188: 12 tarjetas de conceptos distintos de temas ya
 * superados (completados o reconocidos por el diagnóstico, RN-06) y el
 * concepto verdadero elegido al azar. Se arma ANTES de cobrar: si no hay 12
 * conceptos regresa failure y no se cobra nada.
 *
 * Solo se ofrecen preguntas que dividen el tablero (alguna tarjeta dice sí y
 * otra no): una que respondería igual para las 12 gastaría un sorbo en nada.
 */
class ArmarDueloUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val dueloRepository: DueloRepository,
    private val random: Random = Random.Default
) {
    suspend operator fun invoke(): Result<DueloEnigmas> {
        val temaIds = roadmapRepository.obtenerTemas().filter { it.completado || it.conocido }.map { it.id }
        val banco = dueloRepository.obtenerConceptos(temaIds).distinctBy { it.nombre.lowercase() }
        if (banco.size < DueloEnigmas.TOTAL_TARJETAS) {
            return Result.failure(ConceptosInsuficientes(banco.size))
        }
        val tarjetas = elegirTarjetas(banco.map { TarjetaConcepto(it) })
        val ids = tarjetas.map { it.concepto.id }
        val preguntas = dueloRepository.obtenerPreguntas()
            .filter { p -> ids.any(p::respuestaPara) && !ids.all(p::respuestaPara) }
            .shuffled(random)
            .take(MAX_PREGUNTAS)
        return Result.success(
            DueloEnigmas(UUID.randomUUID().toString(), tarjetas, preguntas, elegirConceptoVerdadero(tarjetas))
        )
    }

    private fun elegirTarjetas(banco: List<TarjetaConcepto>) = banco.shuffled(random).take(DueloEnigmas.TOTAL_TARJETAS)

    private fun elegirConceptoVerdadero(tarjetas: List<TarjetaConcepto>) = tarjetas[random.nextInt(tarjetas.size)].concepto.id

    class ConceptosInsuficientes(val hay: Int) : IllegalStateException(
        "Los temas que has superado tienen $hay conceptos; el gigante necesita ${DueloEnigmas.TOTAL_TARJETAS}."
    )

    companion object {
        const val MAX_PREGUNTAS = 12
    }
}

/** Paso 4, RF-185: cobra la apuesta (MOD-01) y desde ese momento el duelo cuenta en el límite del día. */
class ApostarDueloUseCase(
    private val moduloHidromiel: ModuloHidromiel,
    private val dueloRepository: DueloRepository,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(duelo: DueloEnigmas): ResultadoApuesta {
        val resultado = moduloHidromiel.cobrarApuestaDuelo(idUsuario)
        if (resultado.autorizado) dueloRepository.registrarDueloIniciado(duelo, reloj())
        return resultado
    }
}

/** Pasos 7 y 8, RF-188 y RF-189: 1 sorbo por pregunta, respuesta sí o no según el concepto verdadero. */
class PreguntarGiganteUseCase {
    operator fun invoke(duelo: DueloEnigmas, preguntaId: String): RespuestaGigante {
        check(duelo.puedePreguntar()) { "El cuerno está vacío o no quedan preguntas" }
        val pregunta = duelo.preguntasDisponibles().first { it.id == preguntaId }
        duelo.cuerno.beber(COSTO_PREGUNTA)
        val esSi = pregunta.respuestaPara(duelo.conceptoVerdadero().id)
        duelo.preguntasRealizadas += pregunta.id
        // A10.1: con el cuerno vacío solo queda una adivinanza.
        if (duelo.cuerno.estaVacio()) duelo.ultimaAdivinanza = true
        return RespuestaGigante(pregunta.id, esSi, duelo.cuerno.sorbos)
    }

    companion object {
        const val COSTO_PREGUNTA = 1
    }
}

/** Pasos 11 y 12, A12.1, RF-191 y RF-192: una adivinanza fallida cuesta 2 sorbos sin bajar de 0. */
class RevelarConceptoUseCase {
    operator fun invoke(duelo: DueloEnigmas, conceptoId: String): ResultadoAdivinanza {
        if (duelo.esConceptoVerdadero(conceptoId)) {
            return ResultadoAdivinanza(correcta = true, sorbosRestantes = duelo.cuerno.sorbos, dueloTerminado = true)
        }
        duelo.cuerno.beber(COSTO_FALLO)
        duelo.descartar(conceptoId)
        duelo.adivinanzasFallidas++
        return ResultadoAdivinanza(
            correcta = false,
            sorbosRestantes = duelo.cuerno.sorbos,
            dueloTerminado = duelo.cuerno.estaVacio() || duelo.ultimaAdivinanza
        )
    }

    companion object {
        const val COSTO_FALLO = 2
    }
}

/**
 * Pasos 12 y 13, E12.1, RF-194 a RF-196: el MOD-01 paga si ganó; si perdió,
 * el concepto va a la bitácora de errores (MOD-06) y se revela (excepción de
 * RN-34). Se guarda el resultado con todos los datos del RF-196.
 */
class FinalizarDueloUseCase(
    private val repository: DueloRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val historialRepository: HistorialRepository,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val idUsuario: String = USUARIO_LOCAL
) {
    suspend operator fun invoke(duelo: DueloEnigmas, ganado: Boolean, tarjetaElegida: String? = null): CierreDuelo {
        val sorbos = duelo.cuerno.sorbos
        val aviso = moduloHidromiel.alFinalizarDuelo(DueloFinalizado(idUsuario, ganado, sorbos))
        val concepto = duelo.conceptoVerdadero()
        if (!ganado) registrarEnBitacora(duelo, tarjetaElegida)
        val resultado = ResultadoDuelo(
            id = duelo.id,
            conceptoId = concepto.id,
            resultado = if (ganado) EstadoFinalDuelo.GANADO else EstadoFinalDuelo.PERDIDO,
            preguntasRealizadas = duelo.preguntasRealizadas.size,
            adivinanzasFallidas = duelo.adivinanzasFallidas,
            sorbosSobrantes = sorbos,
            hidromielObtenido = aviso?.cantidad ?: 0,
            fecha = reloj()
        )
        repository.guardarResultado(resultado)
        return CierreDuelo(resultado, concepto, aviso)
    }

    /** MOD-06 E1: error sin opciones, guardado con una descripción. E2: si falla, el duelo sigue. */
    private suspend fun registrarEnBitacora(duelo: DueloEnigmas, tarjetaElegida: String?) {
        val concepto = duelo.conceptoVerdadero()
        val elegida = tarjetaElegida?.let { id -> duelo.tarjetas.firstOrNull { it.concepto.id == id }?.concepto?.nombre }
        runCatching {
            historialRepository.registrarError(
                HistorialError(
                    id = UUID.randomUUID().toString(),
                    tipoDesafio = TIPO_BITACORA,
                    actividadId = concepto.id,
                    descripcion = "Duelo de enigmas: el gigante escondía «${concepto.nombre}»" +
                        (elegida?.let { " y elegiste «$it»" } ?: "") + ". ${concepto.explicacion}",
                    fecha = reloj()
                )
            )
        }
    }

    companion object {
        const val TIPO_BITACORA = "DUELO_ENIGMAS"
    }
}

/** E7.1 y E7.2, RF-197: el duelo se registra como abandonado; la apuesta no se devuelve. */
class AbandonarDueloUseCase(
    private val repository: DueloRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(duelo: DueloEnigmas): ResultadoDuelo {
        val resultado = ResultadoDuelo(
            duelo.id, duelo.conceptoVerdadero().id, EstadoFinalDuelo.ABANDONADO,
            duelo.preguntasRealizadas.size, duelo.adivinanzasFallidas, duelo.cuerno.sorbos, 0, reloj()
        )
        repository.guardarResultado(resultado)
        return resultado
    }

    /** E7.2: al abrir la app, un duelo que quedó en curso se registra como abandonado. */
    suspend fun cerrarDueloSinTerminar(): ResultadoDuelo? =
        repository.obtenerDueloSinTerminar()
            ?.copy(resultado = EstadoFinalDuelo.ABANDONADO)
            ?.also { repository.guardarResultado(it) }
}
