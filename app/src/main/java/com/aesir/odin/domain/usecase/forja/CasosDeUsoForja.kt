package com.aesir.odin.domain.usecase.forja

import com.aesir.odin.domain.bitacora.FalloBitacora
import com.aesir.odin.domain.bitacora.ModuloBitacora
import com.aesir.odin.domain.bitacora.OrigenFallo
import com.aesir.odin.domain.hidromiel.ForjaFinalizada
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.ResultadoApuesta
import com.aesir.odin.domain.model.ArtefactoVitrina
import com.aesir.odin.domain.model.AsignacionForja
import com.aesir.odin.domain.model.CalidadArtefacto
import com.aesir.odin.domain.model.EstadoFinalForja
import com.aesir.odin.domain.model.EtapaCalentar
import com.aesir.odin.domain.model.Forja
import com.aesir.odin.domain.model.MotivoBloqueoForja
import com.aesir.odin.domain.model.RangoApuesta
import com.aesir.odin.domain.model.RespuestaEtapa
import com.aesir.odin.domain.model.ResultadoAccesoForja
import com.aesir.odin.domain.model.ResultadoEtapa
import com.aesir.odin.domain.model.ResultadoForja
import com.aesir.odin.domain.model.TipoEtapa
import com.aesir.odin.domain.racha.ActividadTerminada
import com.aesir.odin.domain.racha.ModuloCarreraSol
import com.aesir.odin.domain.racha.TipoActividad
import com.aesir.odin.domain.repository.ForjaRepository
import com.aesir.odin.domain.repository.RetoRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.random.Random

/**
 * Casos de uso del CU-02 Forjar artefactos en la apuesta de Brokkr
 * (diagrama de clases CU-02 v1.2, capa Negocio · UseCases).
 */

/** Inicio y fin (exclusivo) del día real que contiene [ahora], en milisegundos. */
internal fun limitesDelDia(ahora: Long, zona: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val dia = Instant.ofEpochMilli(ahora).atZone(zona).toLocalDate()
    val inicio = dia.atStartOfDay(zona).toInstant().toEpochMilli()
    val fin = dia.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli()
    return inicio to fin
}

/** Mundos con Ragnarök superado. En modo admin cuentan todos los mundos, para poder probar la forja. */
internal suspend fun mundosParaForja(roadmap: RoadmapRepository): List<String> =
    if (ModoAdmin.estaActivo) roadmap.obtenerMundos().sortedBy { it.orden }.map { it.id }
    else roadmap.obtenerMundosSuperados()

/**
 * Precondiciones (pasos 1.2 a 1.7): Ragnarök superado, menos de 2 forjas en
 * el día real (RF-34) y saldo − piso de apuesta ≥ 20 (RN-08).
 * A2.1 → FORJAS_AGOTADAS; A2.2 → HIDROMIEL_INSUFICIENTE con lo que falta.
 */
class ValidarAccesoForjaUseCase(
    private val forjaRepository: ForjaRepository,
    private val roadmapRepository: RoadmapRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(): ResultadoAccesoForja {
        val (inicio, fin) = limitesDelDia(reloj())
        val forjasHoy = forjaRepository.contarForjasDelDia(inicio, fin)
        val restantes = if (ModoAdmin.estaActivo) FORJAS_POR_DIA else (FORJAS_POR_DIA - forjasHoy).coerceAtLeast(0)

        if (mundosParaForja(roadmapRepository).isEmpty()) {
            return ResultadoAccesoForja(false, MotivoBloqueoForja.SIN_RAGNAROK_SUPERADO, forjasRestantes = restantes)
        }
        if (restantes == 0) {
            return ResultadoAccesoForja(false, MotivoBloqueoForja.FORJAS_AGOTADAS, forjasRestantes = 0)
        }
        val disponible = moduloHidromiel.saldo() - moduloHidromiel.calcularPisoApuesta()
        if (disponible < APUESTA_MINIMA) {
            return ResultadoAccesoForja(
                false, MotivoBloqueoForja.HIDROMIEL_INSUFICIENTE,
                hidromielFaltante = APUESTA_MINIMA - disponible, forjasRestantes = restantes
            )
        }
        return ResultadoAccesoForja(true, forjasRestantes = restantes)
    }

    companion object {
        const val FORJAS_POR_DIA = 2
        const val APUESTA_MINIMA = RangoApuesta.MINIMO
    }
}

/** Paso 2: artefactos que el alumno ya tiene y su calidad. */
class ObtenerVitrinaUseCase(private val forjaRepository: ForjaRepository) {
    suspend operator fun invoke(): List<ArtefactoVitrina> = forjaRepository.obtenerVitrina()
}

/**
 * Paso 3 (RF-30): asigna al azar un artefacto de los mundos superados que el
 * alumno no tiene o tiene con defecto. Si ya los tiene todos perfectos, la
 * forja es solo por hidromiel (A3.1, RF-31).
 */
class AsignarArtefactoUseCase(
    private val forjaRepository: ForjaRepository,
    private val roadmapRepository: RoadmapRepository,
    private val azar: Random = Random.Default
) {
    suspend operator fun invoke(vitrina: List<ArtefactoVitrina>): AsignacionForja {
        val mundos = mundosParaForja(roadmapRepository)
        check(mundos.isNotEmpty()) { "No hay mundos superados para forjar." }
        val perfectos = vitrina.filter { it.esPerfecto() }.map { it.artefacto.id }.toSet()
        val candidatos = forjaRepository.obtenerArtefactos(mundos).filter { it.id !in perfectos }
        if (candidatos.isEmpty()) {
            return AsignacionForja(artefacto = null, mundoId = mundos.random(azar), soloHidromiel = true)
        }
        val elegido = candidatos.random(azar)
        return AsignacionForja(elegido, elegido.mundoId, soloHidromiel = false)
    }
}

/** Paso 4 (RF-32): de 20 al menor entre 80 y el saldo menos el piso de apuesta. */
class CalcularRangoApuestaUseCase(private val moduloHidromiel: ModuloHidromiel) {
    suspend operator fun invoke(saldo: Int): RangoApuesta {
        val piso = moduloHidromiel.calcularPisoApuesta()
        return RangoApuesta(pisoApuesta = piso, maximoPermitido = minOf(RangoApuesta.MAXIMO, saldo - piso))
    }
}

/** Pasos 5.3 a 5.7: arma la forja con las tres etapas del mundo del artefacto. */
class ArmarForjaUseCase(
    private val forjaRepository: ForjaRepository,
    private val retoRepository: RetoRepository,
    private val azar: Random = Random.Default
) {
    suspend operator fun invoke(asignacion: AsignacionForja, apuesta: Int): Forja {
        val mundoId = asignacion.mundoId
        val calentar = EtapaCalentar(mundoId, retoRepository.obtenerRetoOrdenar(mundoId), azar)
        val martillar = forjaRepository.obtenerProblemaMartillar(mundoId)
        val templar = forjaRepository.obtenerTemplado(mundoId)
        return Forja(UUID.randomUUID().toString(), asignacion, apuesta, listOf(calentar, martillar, templar))
    }
}

/**
 * Paso 6 (RF-33, RF-34): el MOD-01 descuenta la apuesta (APUESTA_FORJA) y la
 * forja queda registrada EN_CURSO, contada en el límite del día.
 */
class ApostarForjaUseCase(
    private val moduloHidromiel: ModuloHidromiel,
    private val forjaRepository: ForjaRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(forja: Forja, rango: RangoApuesta): ResultadoApuesta {
        require(rango.contiene(forja.apuesta)) { "La apuesta está fuera del rango permitido." }
        val resultado = moduloHidromiel.cobrarApuestaForja(forja.apuesta)
        if (resultado.cobrada) forjaRepository.registrarForja(forja, reloj())
        return resultado
    }
}

/** Anota un error de la forja en la bitácora (MOD-06), sin la respuesta correcta. */
private suspend fun ModuloBitacora.anotarErrorForja(forja: Forja, tipo: TipoEtapa, detalle: String, fecha: Long) =
    registrarFallo(
        FalloBitacora(
            origen = OrigenFallo.FORJA,
            actividadId = forja.id,
            descripcion = "Forja de Brokkr · «${tipo.nombre}»: $detalle",
            fecha = fecha
        )
    )

/**
 * Pasos 9, 12 y 15 (RF-39): valida la etapa en curso. Si es incorrecta suma un
 * error y lo avisa a la bitácora (A9.1). En ambos casos avanza a la siguiente.
 */
class ValidarEtapaForjaUseCase(
    private val moduloBitacora: ModuloBitacora,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(forja: Forja, respuesta: RespuestaEtapa): ResultadoEtapa {
        check(!forja.terminada()) { "La forja ya terminó." }
        val etapa = forja.etapaEnCurso()
        require(etapa.tipo == respuesta.tipo) { "La respuesta no es de la etapa en curso." }
        val correcta = etapa.validar(respuesta)
        if (!correcta) {
            forja.registrarError()
            moduloBitacora.anotarErrorForja(forja, etapa.tipo, "respuesta incorrecta. ${etapa.enunciado}", reloj())
        }
        forja.avanzar()
        return ResultadoEtapa(etapa.tipo, correcta, forja.errores, forja.terminada())
    }
}

/** A14.1 (RF-38): se acabaron los 60 s del temple; la etapa cuenta como incorrecta. */
class EnfriarMetalUseCase(
    private val moduloBitacora: ModuloBitacora,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(forja: Forja): ResultadoEtapa {
        check(!forja.terminada()) { "La forja ya terminó." }
        val etapa = forja.etapaEnCurso()
        check(etapa.tipo == TipoEtapa.TEMPLAR) { "Solo el temple tiene límite de tiempo." }
        forja.registrarError()
        moduloBitacora.anotarErrorForja(forja, etapa.tipo, "el metal se enfrió sin respuesta. ${etapa.enunciado}", reloj())
        forja.avanzar()
        return ResultadoEtapa(etapa.tipo, correcta = false, erroresAcumulados = forja.errores, forjaTerminada = forja.terminada(), metalFrio = true)
    }
}

/**
 * Pasos 16 a 18 (RF-40, RF-41, RF-43): determina el resultado, avisa el premio
 * o la pérdida al MOD-01, guarda el artefacto perfecto en la vitrina, guarda
 * el resultado y avisa a la racha (MOD-05).
 */
class FinalizarForjaUseCase(
    private val forjaRepository: ForjaRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val moduloCarreraSol: ModuloCarreraSol,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(forja: Forja): ResultadoForja {
        val resultado = determinarResultado(forja.errores)
        val aviso = moduloHidromiel.alFinalizarForja(ForjaFinalizada(forja.id, forja.apuesta, resultado))
        val fecha = reloj()
        val artefacto = forja.asignacion.artefacto
        if (resultado == EstadoFinalForja.PERFECTO && artefacto != null) {
            forjaRepository.guardarEnVitrina(artefacto.id, CalidadArtefacto.PERFECTO, fecha)
        }
        val perfecto = resultado == EstadoFinalForja.PERFECTO
        val final = ResultadoForja(
            id = forja.id,
            artefacto = artefacto,
            resultado = resultado,
            errores = forja.errores,
            apuesta = forja.apuesta,
            hidromielGanado = if (perfecto) aviso.cantidad else 0,
            hidromielPerdido = if (perfecto) 0 else forja.apuesta,
            abandonada = false,
            aviso = aviso,
            fecha = fecha
        )
        forjaRepository.guardarResultado(final)
        moduloCarreraSol.alTerminarActividad(
            ActividadTerminada(TipoActividad.MODO_VALHALLA, abandonada = false, fechaFin = LocalDate.now())
        )
        return final
    }

    private fun determinarResultado(errores: Int): EstadoFinalForja =
        if (errores == 0) EstadoFinalForja.PERFECTO else EstadoFinalForja.ROTO
}

/** A7.1 (RF-45): abandonar la forja la rompe; la apuesta se pierde y la forja ya contó en el día. */
class AbandonarForjaUseCase(
    private val forjaRepository: ForjaRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(forja: Forja): ResultadoForja {
        val resultado = ResultadoForja(
            id = forja.id,
            artefacto = forja.asignacion.artefacto,
            resultado = EstadoFinalForja.ROTO,
            errores = forja.errores,
            apuesta = forja.apuesta,
            hidromielGanado = 0,
            hidromielPerdido = forja.apuesta,
            abandonada = true,
            aviso = null,
            fecha = reloj()
        )
        forjaRepository.guardarResultado(resultado)
        return resultado
    }
}

/** RF-45: al abrir la app, las forjas que quedaron EN_CURSO (la app se cerró) se marcan como rotas. */
class CerrarForjasInconclusasUseCase(
    private val forjaRepository: ForjaRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(): Int = forjaRepository.marcarForjasEnCursoComoRotas(reloj())
}
