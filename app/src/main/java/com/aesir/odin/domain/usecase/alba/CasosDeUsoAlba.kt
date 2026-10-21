package com.aesir.odin.domain.usecase.alba

import com.aesir.odin.domain.racha.fechaLocal
import com.aesir.odin.domain.racha.TipoActividad
import com.aesir.odin.domain.racha.ActividadTerminada
import com.aesir.odin.domain.racha.ModuloCarreraSol
import com.aesir.odin.domain.sonido.EventoSonido
import com.aesir.odin.domain.sonido.ModuloSonido
import com.aesir.odin.domain.sonido.eventoDeRespuesta
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.hidromiel.NocheAlbaCobrada
import com.aesir.odin.domain.hidromiel.PoliticaCosto
import com.aesir.odin.domain.hidromiel.ResultadoAcceso
import com.aesir.odin.domain.hidromiel.USUARIO_LOCAL
import com.aesir.odin.domain.model.AccesoAlba
import com.aesir.odin.domain.model.Artefacto
import com.aesir.odin.domain.model.ArtefactoForjado
import com.aesir.odin.domain.model.Desenlace
import com.aesir.odin.domain.model.EfectoArtefacto
import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.FaseNoche
import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.model.MotivoBloqueoAlba
import com.aesir.odin.domain.model.NocheAlba
import com.aesir.odin.domain.model.PreguntaFlash
import com.aesir.odin.domain.model.ResultadoNoche
import com.aesir.odin.domain.repository.AlbaRepository
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase
import com.aesir.odin.domain.usecase.volva.limitesDelDia
import com.aesir.odin.domain.usecase.volva.mundosDesbloqueados
import com.aesir.odin.domain.usecase.volva.temasSuperados
import java.time.ZoneId
import java.util.UUID
import kotlin.random.Random

/*
 * CU-01 Sobrevivir oleadas (Hasta el Alba), SRS v3.2 (RF-01 a RF-29).
 * Reutiliza el banco de preguntas de la pregunta flash y la validación de las
 * lecciones (RF-10), y paga a través del MOD-01.
 */

/** Cuántas preguntas se piden al banco para elegir la de cada golpe (todas las de los temas superados). */
private const val TAMANO_BANCO = 1000

/**
 * Pasos 1 a 4, A2.1, A2.2 y A3.1: decide si el alumno puede apostar y arma lo
 * que muestra la pantalla de inicio. Antes resuelve lo pendiente de otra
 * sesión: una noche que se quedó a la mitad (E9.2 y E15.1) y los premios que
 * no se pudieron acreditar (E20.1).
 */
class ValidarAccesoAlbaUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val albaRepository: AlbaRepository,
    private val preguntaRepository: PreguntaFlashRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val cobrarNoche: CobrarNocheAlbaUseCase,
    private val caerEnBatalla: CaerEnBatallaAlbaUseCase,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val zona: ZoneId = ZoneId.systemDefault()
) {
    suspend operator fun invoke(): AccesoAlba {
        val recuperacion = recuperarNocheSinTerminar()
        acreditarPremiosPendientes()

        val temas = roadmapRepository.obtenerTemas()
        val rango = moduloHidromiel.rangoApuestaAlba(idUsuario, mundosDesbloqueados(roadmapRepository))
        val (inicio, fin) = limitesDelDia(reloj(), zona)
        val ragnarokSuperado = ModoAdmin.estaActivo || temas.any { it.esRagnarok && it.completado }
        val jugada = !ModoAdmin.estaActivo && albaRepository.jugoNocheEnElDia(inicio, fin)
        val superados = temasSuperados(temas)

        val motivo = when {
            !ragnarokSuperado -> MotivoBloqueoAlba.VALHALLA_CERRADO                                   // A2.1
            jugada -> MotivoBloqueoAlba.NOCHE_JUGADA                                                  // A2.2
            !rango.alcanza -> MotivoBloqueoAlba.HIDROMIEL_INSUFICIENTE                                // A3.1
            preguntaRepository.obtenerPreguntasParaReto(superados, 1).isEmpty() -> MotivoBloqueoAlba.SIN_PREGUNTAS
            else -> null
        }
        val faltante = (PoliticaCosto.APUESTA_MIN_ALBA + rango.piso - rango.saldo).coerceAtLeast(0)
        return AccesoAlba(
            motivo = motivo,
            rango = rango,
            artefactos = albaRepository.obtenerArtefactos(),
            faltante = if (motivo == MotivoBloqueoAlba.HIDROMIEL_INSUFICIENTE) faltante else 0,
            nocheDeHoy = if (jugada) albaRepository.resultadoDelDia(inicio, fin) else null,
            avisoRecuperacion = recuperacion,
            mundoEnCurso = if (motivo == MotivoBloqueoAlba.VALHALLA_CERRADO) mundoEnCurso(temas) else null
        )
    }

    /** A2.1: el primer mundo abierto (por orden) cuyo Ragnarök aún no se supera. */
    private suspend fun mundoEnCurso(temas: List<com.aesir.odin.domain.model.Tema>): String? {
        val mundos = roadmapRepository.obtenerMundos().sortedBy { it.orden }
        val pendiente = mundos.firstOrNull { m ->
            m.desbloqueado && temas.none { it.mundoId == m.id && it.esRagnarok && it.completado }
        } ?: mundos.firstOrNull()
        return pendiente?.nombre
    }

    /** E9.2: en plena oleada cuenta como caída; E15.1: en la decisión se cobra solo. */
    private suspend fun recuperarNocheSinTerminar(): String? {
        val noche = albaRepository.obtenerNocheSinTerminar() ?: return null
        return if (noche.fase == FaseNoche.EN_DECISION && noche.multiplicadorAlcanzado > 0) {
            val r = cobrarNoche(noche, Desenlace.RETIRADA)
            "La última noche se cerró al decidir: se cobró ${com.aesir.odin.domain.model.textoMultiplicador(r.multiplicador)} y ganaste ${r.premioNeto} de hidromiel."
        } else {
            caerEnBatalla(noche)
            "La última noche se cerró en plena batalla: caíste en la oleada ${noche.enemigo.numero}."
        }
    }

    private suspend fun acreditarPremiosPendientes() {
        albaRepository.obtenerPremiosPendientes().forEach { pendiente ->
            runCatching {
                moduloHidromiel.acreditarPremioAlba(NocheAlbaCobrada(idUsuario, pendiente.premioNeto, pendiente.diezmo))
                albaRepository.guardarResultado(pendiente.copy(premioPendiente = false))
            }
        }
    }
}

/** Resultado de [ApostarAlbaUseCase]: si se cobró, trae la noche ya registrada como jugada. */
data class ApuestaAlba(val acceso: ResultadoAcceso, val noche: NocheAlba?)

/** Pasos 5 a 7 (RF-05, RF-06, A6.1): cobra la apuesta e inicia la noche en la oleada 1 con 3 escudos. */
class ApostarAlbaUseCase(
    private val moduloHidromiel: ModuloHidromiel,
    private val albaRepository: AlbaRepository,
    private val roadmapRepository: RoadmapRepository,
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    /** [llevar]: los artefactos que el alumno eligió en la vitrina para esta noche (null = todos los que tiene). */
    suspend operator fun invoke(apuesta: Int, llevar: Set<Artefacto>? = null): ApuestaAlba {
        val acceso = moduloHidromiel.cobrarApuestaAlba(idUsuario, apuesta, mundosDesbloqueados(roadmapRepository))
        if (!acceso.autorizado) return ApuestaAlba(acceso, null)
        val noche = NocheAlba(
            id = UUID.randomUUID().toString(),
            apuesta = if (ModoAdmin.estaActivo) 0 else apuesta,
            fecha = reloj(),
            artefactos = albaRepository.obtenerArtefactos().filter { llevar == null || it.artefacto in llevar }
        )
        albaRepository.guardarNocheEnCurso(noche)
        return ApuestaAlba(acceso, noche)
    }
}

/**
 * Paso 9 y A9.1 (RF-09): elige al azar una pregunta de los temas superados que
 * no se haya mostrado en la noche. Si ya se mostraron todas, repite las de la
 * noche empezando por las que se respondieron mal. [excluir] evita repetir la
 * pregunta que se acaba de descartar (E9.1, A11.1).
 */
class ObtenerPreguntaAlbaUseCase(
    private val roadmapRepository: RoadmapRepository,
    private val preguntaRepository: PreguntaFlashRepository,
    private val azar: Random = Random.Default
) {
    suspend operator fun invoke(noche: NocheAlba, excluir: String? = null): PreguntaFlash? {
        val banco = preguntaRepository.obtenerPreguntasParaReto(temasSuperados(roadmapRepository.obtenerTemas()), TAMANO_BANCO)
        if (banco.isEmpty()) return null
        banco.filter { it.id !in noche.preguntasMostradas && it.id != excluir }.takeIf { it.isNotEmpty() }
            ?.let { return it.random(azar) }
        // A9.1: se agotaron; primero las falladas, después las demás de la noche.
        val porId = banco.associateBy { it.id }
        val falladas = noche.preguntasFalladas.mapNotNull { porId[it] }.filter { it.id != excluir }
        return falladas.firstOrNull()
            ?: banco.filter { it.id != excluir }.randomOrNull(azar)
            ?: banco.first()
    }
}

/** Lo que pasó al responder una pregunta. */
data class RespuestaAlba(
    val noche: NocheAlba,
    /** null cuando se acabó el tiempo (A10.1). */
    val estado: EstadoRespuesta?,
    val golpeAcertado: Boolean,
    val oleadaSuperada: Boolean,
    val cayo: Boolean
)

/**
 * Pasos 10 a 14, A10.1, A11.1 y A11.2 (RF-10 a RF-14): valida con la misma
 * validación de las lecciones; un acierto golpea al enemigo y un error rompe un
 * escudo y se avisa a la bitácora. [seleccion] es null si se acabó el tiempo.
 */
class ResponderPreguntaAlbaUseCase(
    private val validarRespuesta: ValidarRespuestaUseCase,
    private val historialRepository: HistorialRepository,
    private val albaRepository: AlbaRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(noche: NocheAlba, pregunta: PreguntaFlash, seleccion: List<Int>?): RespuestaAlba {
        val estado = seleccion?.let { validarRespuesta(it, pregunta.respuestasCorrectas, avisarSonido = false).estado }
        val acierto = estado == EstadoRespuesta.CORRECTA
        var nueva = noche.mostrar(pregunta.id)
        nueva = if (acierto) nueva.acierto() else nueva.error(pregunta.id, pregunta.temaId)
        val superada = acierto && nueva.enemigoDerrotado
        if (superada) nueva = nueva.superarOleada()
        if (!acierto) registrarEnBitacora(pregunta, seleccion)
        // MOD-02, paso 6: el resultado y el evento del Valhalla, el de mayor prioridad primero
        ModuloSonido.avisar(
            eventoDeRespuesta(estado),
            if (acierto) EventoSonido.GOLPE_ENEMIGO else EventoSonido.ESCUDO_ROTO
        )
        if (!nueva.cayo) albaRepository.guardarNocheEnCurso(nueva)
        return RespuestaAlba(nueva, estado, acierto, superada, nueva.cayo)
    }

    /** RF-13 y MOD-06, E2: si no se puede guardar, la batalla sigue sin interrupción. */
    private suspend fun registrarEnBitacora(pregunta: PreguntaFlash, seleccion: List<Int>?) {
        val elegidas = seleccion?.takeIf { it.isNotEmpty() }?.joinToString(", ") { pregunta.opciones.getOrNull(it) ?: "?" }
            ?: "sin respuesta (se acabó el tiempo)"
        runCatching {
            historialRepository.registrarError(
                HistorialError(
                    id = UUID.randomUUID().toString(),
                    tipoDesafio = TIPO_HASTA_EL_ALBA,
                    actividadId = pregunta.id,
                    descripcion = "Hasta el Alba (tema ${pregunta.temaId}): ${pregunta.enunciado} " +
                        "Opciones: ${pregunta.opciones.joinToString(" | ")}. " +
                        "Correctas: ${pregunta.respuestasCorrectas.joinToString(", ") { pregunta.opciones[it] }}. " +
                        "Elegidas: $elegidas.",
                    fecha = reloj()
                )
            )
        }
    }

    companion object {
        const val TIPO_HASTA_EL_ALBA = "HASTA_EL_ALBA"
    }
}

/**
 * A9.2 a A9.4 y A15.1 (RF-20 a RF-24): usa un artefacto que el alumno llevó a
 * la noche, una vez por noche.
 */
class UsarArtefactoAlbaUseCase(
    private val albaRepository: AlbaRepository,
    private val azar: Random = Random.Default
) {
    suspend operator fun invoke(noche: NocheAlba, artefacto: Artefacto, pregunta: PreguntaFlash?): Pair<NocheAlba, EfectoArtefacto> {
        val forjado = noche.disponible(artefacto)
            ?: return noche to EfectoArtefacto.NoDisponible(
                if (artefacto in noche.artefactosUsados) "${artefacto.nombre} ya se usó esta noche." else "No llevaste ${artefacto.nombre} a esta noche."
            )
        val (nueva, efecto) = when (artefacto) {
            Artefacto.MJOLNIR -> {
                if (noche.fase != FaseNoche.EN_OLEADA) return noche to EfectoArtefacto.NoDisponible("Mjölnir solo se usa durante una oleada.")
                var n = noche.golpear(noche.golpesRestantes)
                if (n.enemigoDerrotado) n = n.superarOleada()
                n to EfectoArtefacto.GolpeDeMjolnir(noche.golpesRestantes - n.golpesRestantes, n.enemigoDerrotado)
            }
            Artefacto.GUNGNIR -> {
                val p = pregunta ?: return noche to EfectoArtefacto.NoDisponible("Gungnir se usa sobre una pregunta.")
                noche to EfectoArtefacto.Gungnir(p.respuestasCorrectas, emptyList())
            }
            Artefacto.SKIDBLADNIR -> {
                if (noche.fase != FaseNoche.EN_OLEADA) return noche to EfectoArtefacto.NoDisponible("Skíðblaðnir solo se usa durante una oleada.")
                if (noche.escudos >= NocheAlba.ESCUDOS_POR_NOCHE) return noche to EfectoArtefacto.NoDisponible("No tienes escudos rotos.")
                noche.copy(escudos = noche.escudos + 1) to EfectoArtefacto.EscudoRecuperado
            }
            Artefacto.DRAUPNIR -> {
                if (noche.fase != FaseNoche.EN_DECISION) return noche to EfectoArtefacto.NoDisponible("Draupnir se activa al decidir si vuelves al banquete.")
                val bono = NocheAlba.BONO_DRAUPNIR_PERFECTO
                noche.copy(bonoDraupnir = bono) to EfectoArtefacto.BonoDraupnir(bono)
            }
        }
        val usada = nueva.usar(artefacto)
        albaRepository.guardarNocheEnCurso(usada)
        return usada to efecto
    }
}

/** Paso 16: el alumno sigue peleando; la noche pasa al siguiente enemigo y se guarda. */
class SeguirPeleandoAlbaUseCase(private val albaRepository: AlbaRepository) {
    suspend operator fun invoke(noche: NocheAlba): NocheAlba {
        val siguiente = noche.siguienteOleada()
        albaRepository.guardarNocheEnCurso(siguiente)
        return siguiente
    }
}

/**
 * Pasos 18 a 23, A16.1, A21.1, E15.1 y E20.1 (RF-16 a RF-19): calcula el premio
 * con el multiplicador alcanzado (×4 al llegar al alba) más Draupnir, lo
 * acredita como PREMIO_ALBA y guarda el resultado. Si el MOD-01 falla, el
 * premio queda pendiente.
 */
class CobrarNocheAlbaUseCase(
    private val albaRepository: AlbaRepository,
    private val moduloHidromiel: ModuloHidromiel,
    private val politica: PoliticaCosto = PoliticaCosto(),
    private val idUsuario: String = USUARIO_LOCAL,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val moduloCarreraSol: ModuloCarreraSol? = null
) {
    suspend operator fun invoke(noche: NocheAlba, desenlace: Desenlace): ResultadoNoche {
        require(desenlace != Desenlace.CAIDA) { "Una caída no se cobra" }
        val base = if (desenlace == Desenlace.ALBA) com.aesir.odin.domain.model.Enemigo.EL_ALBA.multiplicador else noche.multiplicadorAlcanzado
        val multiplicador = base + noche.bonoDraupnir
        val pago = politica.pagoAlba(noche.apuesta, multiplicador)
        val pendiente = runCatching {
            moduloHidromiel.acreditarPremioAlba(NocheAlbaCobrada(idUsuario, pago.neto, pago.diezmo))
        }.isFailure
        val einherjarNuevo = desenlace == Desenlace.ALBA && !albaRepository.tieneEinherjar()
        if (einherjarNuevo) albaRepository.otorgarEinherjar(reloj())
        // MOD-02: el cuerno de Odín al llegar el alba; el coro si además se gana el título
        if (desenlace == Desenlace.ALBA) ModuloSonido.avisar(EventoSonido.CUERNO_ODIN)
        if (einherjarNuevo) ModuloSonido.avisar(EventoSonido.LOGRO)
        val resultado = ResultadoNoche(
            id = noche.id,
            fecha = noche.fecha,
            apuesta = noche.apuesta,
            oleadaAlcanzada = noche.enemigo.numero,
            oleadasSuperadas = if (desenlace == Desenlace.ALBA) NocheAlba.OLEADAS else noche.oleadasSuperadas,
            desenlace = desenlace,
            multiplicador = multiplicador,
            aciertos = noche.aciertos,
            errores = noche.errores,
            escudosRestantes = noche.escudos,
            artefactosUsados = noche.artefactosUsados.sortedBy { it.ordinal },
            premioBruto = pago.bruto,
            diezmo = pago.diezmo,
            premioNeto = pago.neto,
            temasConError = noche.temasConError.toList(),
            premioPendiente = pendiente,
            einherjarNuevo = einherjarNuevo
        )
        albaRepository.guardarResultado(resultado)
        avisarRacha(TipoActividad.MODO_VALHALLA)
        return resultado
    }

    /** MOD-05: terminar la actividad cuenta para la racha del día (si falla, la actividad sigue). */
    private suspend fun avisarRacha(tipo: TipoActividad) {
        runCatching { moduloCarreraSol?.alTerminarActividad(ActividadTerminada(idUsuario, tipo, abandonada = false, fechaFin = fechaLocal(reloj()))) }
    }
}

/**
 * A11.2, E9.1 y E9.2 (RF-14, RF-25, RF-26): el alumno cae en batalla; la
 * apuesta se pierde, no se paga premio y el bono de Draupnir se pierde.
 */
class CaerEnBatallaAlbaUseCase(private val albaRepository: AlbaRepository) {
    suspend operator fun invoke(noche: NocheAlba): ResultadoNoche {
        val resultado = ResultadoNoche(
            id = noche.id,
            fecha = noche.fecha,
            apuesta = noche.apuesta,
            oleadaAlcanzada = noche.enemigo.numero,
            oleadasSuperadas = noche.oleadasSuperadas,
            desenlace = Desenlace.CAIDA,
            multiplicador = noche.multiplicadorAlcanzado,
            aciertos = noche.aciertos,
            errores = noche.errores,
            escudosRestantes = noche.escudos,
            artefactosUsados = noche.artefactosUsados.sortedBy { it.ordinal },
            premioBruto = 0,
            diezmo = 0,
            premioNeto = 0,
            temasConError = noche.temasConError.toList()
        )
        albaRepository.guardarResultado(resultado)
        return resultado
    }
}

/** Premio neto que se cobraría ahora mismo (paso 15, RF-15). */
fun premioNetoAlCobrar(noche: NocheAlba, politica: PoliticaCosto = PoliticaCosto()): Int =
    politica.pagoAlba(noche.apuesta, noche.multiplicadorDeCobro).neto

/** Artefactos que se pueden usar ahora mismo, según la fase de la noche. */
fun artefactosUsables(noche: NocheAlba): List<ArtefactoForjado> =
    noche.artefactos.filter { it.artefacto !in noche.artefactosUsados }
