package com.aesir.odin.domain.hidromiel

import com.aesir.odin.domain.hidromiel.regla.ReglaConexionArbol
import com.aesir.odin.domain.model.ArbolHabilidades
import com.aesir.odin.domain.hidromiel.regla.ReglaNivelSuperado
import com.aesir.odin.domain.hidromiel.regla.ReglaPrimerNivel
import com.aesir.odin.domain.hidromiel.regla.ReglaRagnarok
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * MOD-01 Sistema de Hidromiel y desbloqueo de niveles (fachada).
 *
 * Es la única entrada al módulo: las pantallas y los casos de uso le avisan
 * lo que pasó (abrir el roadmap, iniciar o terminar un nivel, diagnóstico,
 * pregunta flash, contrarreloj, nuevo día) y el módulo cobra, otorga y
 * decide qué niveles están abiertos. Sustituye cualquier otra regla de
 * desbloqueo de niveles.
 *
 * Además de devolver cada [AvisoHidromiel], lo publica en [avisos] para que
 * la interfaz lo muestre sin importar en qué pantalla esté el alumno.
 */
class ModuloHidromiel(
    private val catalogo: CatalogoNiveles,
    private val repositorio: HidromielRepository,
    private val politica: PoliticaCosto = PoliticaCosto(),
    private val calculadora: CalculadoraRecompensa = CalculadoraRecompensa(politica),
    private val controlAcceso: ControlAcceso = ControlAcceso(
        listOf(
            ReglaNivelSuperado(repositorio),
            ReglaPrimerNivel(),
            ReglaRagnarok(catalogo, repositorio),
            ReglaConexionArbol(catalogo, repositorio)
        )
    ),
    private val hoy: () -> LocalDate = LocalDate::now,
    private val ahora: () -> LocalDateTime = LocalDateTime::now
) {
    private val candado = Mutex()

    /**
     * Nivel cuya entrada ya se pagó y que aún no termina, por usuario. Así,
     * si el alumno sale de la introducción y vuelve a entrar, no se le cobra
     * dos veces. Se olvida al terminar el nivel o al cerrar la app.
     */
    private val entradasPagadas = mutableMapOf<String, String>()

    private val _avisos = MutableSharedFlow<AvisoHidromiel>(extraBufferCapacity = 8)
    val avisos: SharedFlow<AvisoHidromiel> = _avisos.asSharedFlow()

    /** Saldo actual; antes de la primera partida muestra la cantidad inicial. */
    fun observarSaldo(idUsuario: String): Flow<Int> =
        repositorio.observarSaldo(idUsuario).map { it ?: politica.cantidadInicial() }

    // -----------------------------------------------------------------------
    // Fase 1 · Abrir el roadmap de un mundo (Funcionamiento, paso 1)
    // -----------------------------------------------------------------------

    suspend fun obtenerEstadoRoadmap(idUsuario: String, idMundo: String): List<ResultadoAcceso> =
        candado.withLock {
            repositorio.enTransaccion {
                val saldo = obtenerOCrearReserva(idUsuario).saldo
                catalogo.nivelesDelMundo(idMundo).map { nivel ->
                    val estado = evaluar(idUsuario, nivel)
                    armarResultado(idUsuario, nivel, estado, saldo)
                }
            }
        }

    // -----------------------------------------------------------------------
    // Fase 2 · Seleccionar un nivel y pagar la entrada (pasos 2 y 3)
    // -----------------------------------------------------------------------

    suspend fun cobrarEntrada(idUsuario: String, idNivel: String): ResultadoAcceso {
        var aviso: AvisoHidromiel? = null
        val resultado = candado.withLock {
            repositorio.enTransaccion {
                val nivel = catalogo.obtener(idNivel)
                val reserva = obtenerOCrearReserva(idUsuario)
                val estado = evaluar(idUsuario, nivel)
                val previo = armarResultado(idUsuario, nivel, estado, reserva.saldo)

                if (estado != EstadoNivel.DISPONIBLE || !previo.autorizado || previo.costo == 0) {
                    // BLOQUEADO (E4), GRATUITO/SUPERADO (E2), hidromiel insuficiente (E1)
                    // o entrada ya pagada
                    return@enTransaccion previo
                }
                reserva.descontar(previo.costo, TipoMovimiento.ENTRADA_NIVEL)
                repositorio.guardarReserva(reserva)
                entradasPagadas[idUsuario] = nivel.idNivel
                aviso = AvisoHidromiel(
                    tipo = TipoMovimiento.ENTRADA_NIVEL,
                    cantidad = -previo.costo,
                    saldoActual = reserva.saldo,
                    mensaje = "Pagaste ${previo.costo} de hidromiel para entrar a «${nivel.nombre}»."
                )
                previo.copy(motivo = "Entrada pagada.")
            }
        }
        aviso?.let(::publicar)
        return resultado
    }

    /** E5: ayudas del crucigrama. Si no alcanza, se aplica E1 y no se entrega. */
    suspend fun cobrarAyuda(idUsuario: String, tipo: TipoAyuda): ResultadoAcceso {
        if (ModoAdmin.estaActivo) {
            return ResultadoAcceso(tipo.name, EstadoNivel.DISPONIBLE, true, 0, 0, "Modo admin: ayuda gratuita.")
        }
        var aviso: AvisoHidromiel? = null
        val resultado = candado.withLock {
            repositorio.enTransaccion {
                val costo = politica.costoAyuda(tipo)
                val reserva = obtenerOCrearReserva(idUsuario)
                if (!reserva.descontar(costo, TipoMovimiento.AYUDA_CRUCIGRAMA)) {
                    val falta = costo - reserva.saldo
                    return@enTransaccion ResultadoAcceso(
                        idNivel = tipo.name, estado = EstadoNivel.DISPONIBLE, autorizado = false,
                        costo = costo, faltante = falta,
                        motivo = "Te faltan $falta de hidromiel para esta ayuda."
                    )
                }
                repositorio.guardarReserva(reserva)
                aviso = AvisoHidromiel(
                    TipoMovimiento.AYUDA_CRUCIGRAMA, -costo, reserva.saldo,
                    "Usaste $costo de hidromiel en una ayuda."
                )
                ResultadoAcceso(tipo.name, EstadoNivel.DISPONIBLE, true, costo, 0, "Ayuda entregada.")
            }
        }
        aviso?.let(::publicar)
        return resultado
    }

    // -----------------------------------------------------------------------
    // Fase 3 · Terminar o abandonar el nivel (pasos 4 a 8)
    // -----------------------------------------------------------------------

    suspend fun alFinalizarNivel(e: NivelFinalizado): AvisoHidromiel {
        val aviso = candado.withLock {
            repositorio.enTransaccion {
                val nivel = catalogo.obtener(e.idNivel)
                val reserva = obtenerOCrearReserva(e.idUsuario)
                val estado = evaluar(e.idUsuario, nivel)
                if (entradasPagadas[e.idUsuario] == nivel.idNivel) entradasPagadas.remove(e.idUsuario)

                if (e.resultado != ResultadoNivel.SUPERADO) {
                    // La entrada pagada se pierde; no se otorga hidromiel (paso 6)
                    val pagado = if (estado == EstadoNivel.DISPONIBLE) politica.costoDe(nivel) else 0
                    val verbo = if (e.resultado == ResultadoNivel.ABANDONADO) "Abandonaste" else "No superaste"
                    return@enTransaccion AvisoHidromiel(
                        tipo = TipoMovimiento.ENTRADA_NIVEL,
                        cantidad = 0, // la entrada ya se descontó al iniciar el nivel
                        saldoActual = reserva.saldo,
                        mensaje = if (pagado > 0) "$verbo «${nivel.nombre}». La entrada de $pagado de hidromiel se perdió."
                        else "$verbo «${nivel.nombre}». La entrada era gratuita, no perdiste hidromiel."
                    )
                }

                if (estado == EstadoNivel.SUPERADO) {
                    // RN-02: repetir un nivel superado no da hidromiel
                    return@enTransaccion AvisoHidromiel(
                        TipoMovimiento.RECOMPENSA_NIVEL, 0, reserva.saldo,
                        "Repasaste «${nivel.nombre}». Repetir un nivel superado no da hidromiel."
                    )
                }

                // Primera vez que se supera (paso 5)
                val completaMundo = nivel.tipo == TipoNivel.NORMAL &&
                    catalogo.nivelesDelMundo(nivel.idMundo)
                        .filter { it.tipo == TipoNivel.NORMAL && it.idNivel != nivel.idNivel }
                        .all { repositorio.obtenerNivelSuperado(e.idUsuario, it.idNivel) != null }

                val recompensa = calculadora.calcularRecompensa(e.puntaje, completaMundo)
                reserva.abonar(recompensa, TipoMovimiento.RECOMPENSA_NIVEL)
                repositorio.guardarReserva(reserva)
                repositorio.registrarNivelSuperado(
                    NivelSuperado(e.idUsuario, nivel.idNivel, OrigenSuperado.JUGADO, recompensa, hoy())
                )

                var extra = if (completaMundo) " El Ragnarök despertó: ya puedes enfrentarlo." else ""
                if (nivel.tipo == TipoNivel.RAGNAROK) {
                    // Paso 8: superar el Ragnarök abre el siguiente mundo
                    val siguiente = catalogo.siguienteMundo(nivel.idMundo)
                    extra = if (siguiente != null) {
                        repositorio.registrarMundoDesbloqueado(MundoDesbloqueado(e.idUsuario, siguiente, hoy()))
                        " Se abrió un nuevo mundo."
                    } else {
                        " Conquistaste todos los mundos."
                    }
                }
                AvisoHidromiel(
                    TipoMovimiento.RECOMPENSA_NIVEL, recompensa, reserva.saldo,
                    "¡Superaste «${nivel.nombre}»! Ganaste $recompensa de hidromiel.$extra"
                )
            }
        }
        publicar(aviso)
        return aviso
    }

    // -----------------------------------------------------------------------
    // Otros disparadores
    // -----------------------------------------------------------------------

    /**
     * Marca como superados los niveles conocidos, sin cobrar ni otorgar (RN-06).
     * El Ragnarök no se puede reconocer por diagnóstico: siempre se juega.
     *
     * El diagnóstico pregunta por las lecciones; los «niveles de esos temas»
     * (CU-10, paso 9) son además los minijuegos y retos que dependen solo de
     * ellas: un minijuego cuenta si todas sus lecciones conectadas son
     * conocidas, y un reto si todos sus minijuegos conectados lo son.
     *
     * A9.1: si con eso quedan superados todos los niveles del mundo, se otorga
     * una vez 3C como BONO_DIAGNOSTICO para cubrir la entrada al Ragnarök.
     */
    suspend fun alCompletarDiagnostico(e: DiagnosticoCompletado) {
        val aviso = candado.withLock {
            repositorio.enTransaccion {
                val reserva = obtenerOCrearReserva(e.idUsuario)
                val normales = catalogo.nivelesDelMundo(e.idMundo)
                    .filter { it.tipo == TipoNivel.NORMAL }
                    .associateBy { it.idNivel }
                val conocidos = nivelesReconocidos(e.nivelesConocidos, normales)
                conocidos
                    .filter { repositorio.obtenerNivelSuperado(e.idUsuario, it) == null }
                    .forEach {
                        repositorio.registrarNivelSuperado(
                            NivelSuperado(e.idUsuario, it, OrigenSuperado.DIAGNOSTICO, 0, hoy())
                        )
                    }

                val soloQuedaRagnarok = conocidos.isNotEmpty() &&
                    normales.keys.all { repositorio.obtenerNivelSuperado(e.idUsuario, it) != null }
                if (!soloQuedaRagnarok) return@enTransaccion null
                val bono = PoliticaCosto.C * PoliticaCosto.MULT_RAGNAROK
                reserva.abonar(bono, TipoMovimiento.BONO_DIAGNOSTICO)
                repositorio.guardarReserva(reserva)
                AvisoHidromiel(
                    TipoMovimiento.BONO_DIAGNOSTICO, bono, reserva.saldo,
                    "El pozo reconoce todo tu saber: el Ragnarök despertó y recibes $bono de hidromiel para enfrentarlo."
                )
            }
        }
        aviso?.let(::publicar)
    }

    /** Lecciones conocidas más los minijuegos y retos que quedan cubiertos por ellas, anillo por anillo. */
    private suspend fun nivelesReconocidos(lecciones: List<String>, normales: Map<String, NivelInfo>): Set<String> {
        val conocidos = lecciones.filter { it in normales }.toMutableSet()
        for (anillo in ArbolHabilidades.ANILLO_MINIJUEGOS..ArbolHabilidades.ANILLO_RETOS) {
            normales.values
                .filter { it.anillo == anillo && it.idNivel !in conocidos }
                .forEach { nivel ->
                    val base = catalogo.conectados(nivel.idNivel).filter { normales[it]?.anillo == anillo - 1 }
                    if (base.isNotEmpty() && base.all { it in conocidos }) conocidos += nivel.idNivel
                }
        }
        return conocidos
    }

    // -----------------------------------------------------------------------
    // Apuestas: piso (RN-08) y juegos ocultos
    // -----------------------------------------------------------------------

    /**
     * RN-08: costo de entrada más bajo entre los niveles Disponibles de los
     * mundos abiertos, o 0 si no hay. Ninguna apuesta puede dejar el saldo debajo.
     */
    suspend fun calcularPisoApuesta(idUsuario: String): Int = candado.withLock {
        repositorio.enTransaccion { piso(idUsuario) }
    }

    private suspend fun piso(idUsuario: String): Int =
        catalogo.mundosDesbloqueados()
            .flatMap { catalogo.nivelesDelMundo(it) }
            .filter { evaluar(idUsuario, it) == EstadoNivel.DISPONIBLE }
            .minOfOrNull { politica.costoDe(it) } ?: 0

    /** Cobra la apuesta de un juego oculto si no deja el saldo debajo del piso (RN-01, RN-08). */
    suspend fun apostarJuegoOculto(idUsuario: String, cantidad: Int): ResultadoAcceso =
        candado.withLock {
            repositorio.enTransaccion {
                val reserva = obtenerOCrearReserva(idUsuario)
                val piso = piso(idUsuario)
                val falta = (cantidad + piso - reserva.saldo).coerceAtLeast(0)
                if (falta > 0) {
                    val motivo = if (piso > 0) "Debes conservar $piso de hidromiel para tu siguiente nivel. Te faltan $falta."
                    else "Te faltan $falta de hidromiel para apostar."
                    return@enTransaccion ResultadoAcceso(ID_JUEGO_OCULTO, EstadoNivel.DISPONIBLE, false, cantidad, falta, motivo)
                }
                reserva.descontar(cantidad, TipoMovimiento.APUESTA_OCULTA)
                repositorio.guardarReserva(reserva)
                ResultadoAcceso(ID_JUEGO_OCULTO, EstadoNivel.DISPONIBLE, true, cantidad, 0, "Apuesta aceptada.")
            }
        }

    /** CU-13 paso 4: descuenta 24 como APUESTA_DUELO si el saldo no queda debajo del piso (RN-08). */
    suspend fun cobrarApuestaDuelo(idUsuario: String): ResultadoApuesta {
        var aviso: AvisoHidromiel? = null
        val resultado = candado.withLock {
            repositorio.enTransaccion {
                val reserva = obtenerOCrearReserva(idUsuario)
                val piso = piso(idUsuario)
                val apuesta = PoliticaCosto.APUESTA_DUELO
                val falta = (apuesta + piso - reserva.saldo).coerceAtLeast(0)
                if (falta > 0) {
                    return@enTransaccion ResultadoApuesta(
                        false, falta,
                        if (piso > 0) "Te faltan $falta de hidromiel: debes conservar $piso para tu siguiente nivel."
                        else "Te faltan $falta de hidromiel para la apuesta."
                    )
                }
                reserva.descontar(apuesta, TipoMovimiento.APUESTA_DUELO)
                repositorio.guardarReserva(reserva)
                aviso = AvisoHidromiel(TipoMovimiento.APUESTA_DUELO, -apuesta, reserva.saldo, "Apostaste $apuesta de hidromiel contra el gigante.")
                ResultadoApuesta(true, 0, "Apuesta aceptada.")
            }
        }
        aviso?.let(::publicar)
        return resultado
    }

    /** CU-13 paso 12: si ganó, abona 24 + 4 × sorbos sobrantes como PREMIO_DUELO, sin diezmo. Si perdió, nada. */
    suspend fun alFinalizarDuelo(e: DueloFinalizado): AvisoHidromiel? {
        if (!e.ganado) return null
        val premio = PoliticaCosto.premioDuelo(e.sorbosSobrantes)
        return moverSaldo(e.idUsuario) { reserva ->
            reserva.abonar(premio, TipoMovimiento.PREMIO_DUELO)
            AvisoHidromiel(TipoMovimiento.PREMIO_DUELO, premio, reserva.saldo, "¡Venciste al gigante! Recibes $premio de hidromiel.")
        }
    }

    /** Paga el premio bruto de un juego oculto (si perdió no hay nada que pagar). */
    suspend fun pagarJuegoOculto(idUsuario: String, premio: Int) {
        if (premio <= 0) return
        candado.withLock {
            repositorio.enTransaccion {
                val reserva = obtenerOCrearReserva(idUsuario)
                reserva.abonar(premio, TipoMovimiento.PREMIO_OCULTO)
                repositorio.guardarReserva(reserva)
            }
        }
    }

    /** E6: correcta +5; incorrecta, incompleta o sin respuesta −2 (RN-01: nunca baja de 0). */
    suspend fun alResolverPreguntaFlash(e: PreguntaFlashResuelta): AvisoHidromiel =
        moverSaldo(e.idUsuario) { reserva ->
            when (e.resultado) {
                ResultadoFlash.CORRECTA -> {
                    reserva.abonar(PoliticaCosto.FLASH_CORRECTA, TipoMovimiento.PREGUNTA_FLASH)
                    AvisoHidromiel(
                        TipoMovimiento.PREGUNTA_FLASH, PoliticaCosto.FLASH_CORRECTA, reserva.saldo,
                        "¡Respuesta correcta! Ganaste ${PoliticaCosto.FLASH_CORRECTA} de hidromiel."
                    )
                }
                ResultadoFlash.INCORRECTA, ResultadoFlash.INCOMPLETA, ResultadoFlash.SIN_RESPUESTA -> {
                    val real = reserva.descontarHastaCero(PoliticaCosto.FLASH_FALLO, TipoMovimiento.PREGUNTA_FLASH)
                    // A7.2 / RF-37: el aviso distingue la respuesta incompleta de la incorrecta
                    val motivo = when (e.resultado) {
                        ResultadoFlash.SIN_RESPUESTA -> "Se agotó el tiempo."
                        ResultadoFlash.INCOMPLETA -> "Respuesta incompleta: te faltó alguna opción."
                        else -> "Respuesta incorrecta."
                    }
                    AvisoHidromiel(
                        TipoMovimiento.PREGUNTA_FLASH, -real, reserva.saldo,
                        "$motivo Perdiste $real de hidromiel."
                    )
                }
            }
        }

    /** Entrada al reto contrarreloj (10). Si no alcanza, se aplica E1 y no se cobra. En modo admin es gratis. */
    suspend fun cobrarEntradaContrarreloj(idUsuario: String): ResultadoAcceso {
        if (ModoAdmin.estaActivo) {
            return ResultadoAcceso(ID_CONTRARRELOJ, EstadoNivel.DISPONIBLE, true, 0, 0, "Modo admin: entrada gratuita.")
        }
        var aviso: AvisoHidromiel? = null
        val resultado = candado.withLock {
            repositorio.enTransaccion {
                val costo = PoliticaCosto.ENTRADA_CONTRARRELOJ
                val reserva = obtenerOCrearReserva(idUsuario)
                if (!reserva.descontar(costo, TipoMovimiento.ENTRADA_CONTRARRELOJ)) {
                    val falta = costo - reserva.saldo
                    return@enTransaccion ResultadoAcceso(
                        idNivel = ID_CONTRARRELOJ, estado = EstadoNivel.DISPONIBLE, autorizado = false,
                        costo = costo, faltante = falta,
                        motivo = "Te faltan $falta de hidromiel para entrar al reto."
                    )
                }
                repositorio.guardarReserva(reserva)
                aviso = AvisoHidromiel(
                    TipoMovimiento.ENTRADA_CONTRARRELOJ, -costo, reserva.saldo,
                    "Pagaste $costo de hidromiel para entrar al reto contrarreloj."
                )
                ResultadoAcceso(ID_CONTRARRELOJ, EstadoNivel.DISPONIBLE, true, costo, 0, "Entrada pagada.")
            }
        }
        aviso?.let(::publicar)
        return resultado
    }

    /** Al terminar el reto (sin abandonarlo): +2 por cada correcta y -1 por cada incorrecta (RN-01: nunca baja de 0). */
    suspend fun alFinalizarContrarreloj(e: ContrarrelojFinalizado): AvisoHidromiel =
        moverSaldo(e.idUsuario) { reserva ->
            val neto = PoliticaCosto.CONTRARRELOJ_CORRECTA * e.aciertos.coerceAtLeast(0) -
                PoliticaCosto.CONTRARRELOJ_FALLO * e.fallos.coerceAtLeast(0)
            val cambio = if (neto >= 0) {
                if (neto > 0) reserva.abonar(neto, TipoMovimiento.RECOMPENSA_CONTRARRELOJ)
                neto
            } else {
                -reserva.descontarHastaCero(-neto, TipoMovimiento.RECOMPENSA_CONTRARRELOJ)
            }
            AvisoHidromiel(
                TipoMovimiento.RECOMPENSA_CONTRARRELOJ, cambio, reserva.saldo,
                when {
                    cambio > 0 -> "¡Reto terminado! Ganaste $cambio de hidromiel."
                    cambio < 0 -> "Reto terminado. Por tus errores perdiste ${-cambio} de hidromiel."
                    else -> "Reto terminado. Tu hidromiel no cambió."
                }
            )
        }

    /**
     * E6: abandonar un reto contrarreloj resta 20. Lo ganado en el reto se descarta,
     * pero si el saldo del reto es negativo se suma a la penalización (-10 → resta 30).
     * RN-01: nunca baja de 0.
     */
    suspend fun alAbandonarContrarreloj(e: ContrarrelojAbandonado): AvisoHidromiel =
        moverSaldo(e.idUsuario) { reserva ->
            val penalizacion = PoliticaCosto.ABANDONO_CONTRARRELOJ + (-e.saldoReto).coerceAtLeast(0)
            val real = reserva.descontarHastaCero(penalizacion, TipoMovimiento.ABANDONO_CONTRARRELOJ)
            AvisoHidromiel(
                TipoMovimiento.ABANDONO_CONTRARRELOJ, -real, reserva.saldo,
                "Abandonaste el contrarreloj. Perdiste $real de hidromiel."
            )
        }

    /** Modo admin: fija el saldo a [cantidad] (mínimo 0). Fuera del modo admin no hace nada. */
    suspend fun establecerSaldoAdmin(idUsuario: String, cantidad: Int): AvisoHidromiel? {
        if (!ModoAdmin.estaActivo) return null
        return moverSaldo(idUsuario) { reserva ->
            val diferencia = cantidad.coerceAtLeast(0) - reserva.saldo
            if (diferencia > 0) reserva.abonar(diferencia, TipoMovimiento.AJUSTE_ADMIN)
            else if (diferencia < 0) reserva.descontar(-diferencia, TipoMovimiento.AJUSTE_ADMIN)
            AvisoHidromiel(TipoMovimiento.AJUSTE_ADMIN, diferencia, reserva.saldo, "Modo admin: hidromiel ajustado a ${reserva.saldo}.")
        }
    }

    /**
     * E3 / RN-03: 2C una vez al día, sin acumular días anteriores.
     * @return null si hoy ya se reclamó.
     */
    suspend fun reclamarRecompensaDiaria(idUsuario: String, fecha: LocalDate = hoy()): AvisoHidromiel? {
        val aviso = candado.withLock {
            repositorio.enTransaccion {
                val reserva = obtenerOCrearReserva(idUsuario)
                if (!reserva.puedeReclamarDiaria(fecha)) return@enTransaccion null
                val cantidad = politica.recompensaDiaria()
                reserva.abonar(cantidad, TipoMovimiento.RECOMPENSA_DIARIA)
                reserva.marcarDiariaReclamada(fecha)
                repositorio.guardarReserva(reserva)
                AvisoHidromiel(
                    TipoMovimiento.RECOMPENSA_DIARIA, cantidad, reserva.saldo,
                    "Recompensa del día: $cantidad de hidromiel."
                )
            }
        }
        aviso?.let(::publicar)
        return aviso
    }

    // -----------------------------------------------------------------------
    // Internos
    // -----------------------------------------------------------------------

    /**
     * Precondición del MOD-01: la primera vez se asigna la cantidad inicial.
     * La diaria de ese mismo día ya va incluida en la inicial, así que la
     * primera recompensa diaria llega al día siguiente.
     */
    private suspend fun evaluar(idUsuario: String, nivel: NivelInfo): EstadoNivel {
        val estado = controlAcceso.evaluar(idUsuario, nivel)
        // Modo admin: todo lo que no está superado queda abierto y gratis (incluido el Ragnarök)
        return if (ModoAdmin.estaActivo && (estado == EstadoNivel.BLOQUEADO || estado == EstadoNivel.DISPONIBLE)) EstadoNivel.GRATUITO
        else estado
    }

    private suspend fun obtenerOCrearReserva(idUsuario: String): ReservaHidromiel {
        repositorio.obtenerReserva(idUsuario)?.let { return it }
        val nueva = ReservaHidromiel(idUsuario, saldo = 0, fechaUltimaDiaria = hoy(), ahora = ahora)
        nueva.abonar(politica.cantidadInicial(), TipoMovimiento.SALDO_INICIAL)
        repositorio.guardarReserva(nueva)
        return nueva
    }

    private suspend fun armarResultado(
        idUsuario: String,
        nivel: NivelInfo,
        estado: EstadoNivel,
        saldo: Int
    ): ResultadoAcceso = when (estado) {
        EstadoNivel.BLOQUEADO -> {
            if (nivel.tipo == TipoNivel.RAGNAROK) {
                val faltan = ReglaRagnarok(catalogo, repositorio).nivelesFaltantes(idUsuario, nivel.idMundo)
                ResultadoAcceso(
                    nivel.idNivel, estado, false, 0, faltan,
                    "Faltan $faltan ${if (faltan == 1) "nivel" else "niveles"} del mundo para despertar el Ragnarök."
                )
            } else {
                val nombres = catalogo.conectados(nivel.idNivel).map { catalogo.obtener(it).nombre }
                val motivo = if (nombres.isEmpty()) "Este nivel todavía está bloqueado."
                else "Supera ${nombres.joinToString(" o ") { "«$it»" }} para desbloquear este nivel."
                ResultadoAcceso(nivel.idNivel, estado, false, 0, 0, motivo)
            }
        }
        EstadoNivel.GRATUITO -> ResultadoAcceso(
            nivel.idNivel, estado, true, 0, 0,
            if (ModoAdmin.estaActivo) "Modo admin: entrada gratuita." else "Primer nivel del mundo: entrada gratuita."
        )
        EstadoNivel.SUPERADO -> ResultadoAcceso(nivel.idNivel, estado, true, 0, 0, "Ya lo superaste: repetirlo es gratis, pero no da hidromiel.")
        EstadoNivel.DISPONIBLE -> if (entradasPagadas[idUsuario] == nivel.idNivel) {
            ResultadoAcceso(nivel.idNivel, estado, true, 0, 0, "Ya pagaste la entrada de este nivel.")
        } else {
            val costo = politica.costoDe(nivel)
            val falta = (costo - saldo).coerceAtLeast(0)
            ResultadoAcceso(
                nivel.idNivel, estado, falta == 0, costo, falta,
                if (falta == 0) "La entrada cuesta $costo de hidromiel."
                else "La entrada cuesta $costo de hidromiel. Te faltan $falta."
            )
        }
    }

    private suspend fun moverSaldo(
        idUsuario: String,
        operacion: (ReservaHidromiel) -> AvisoHidromiel
    ): AvisoHidromiel {
        val aviso = candado.withLock {
            repositorio.enTransaccion {
                val reserva = obtenerOCrearReserva(idUsuario)
                val aviso = operacion(reserva)
                if (reserva.movimientosNuevos.isNotEmpty()) repositorio.guardarReserva(reserva)
                aviso
            }
        }
        publicar(aviso)
        return aviso
    }

    private fun publicar(aviso: AvisoHidromiel) {
        _avisos.tryEmit(aviso)
    }

    companion object {
        /** Identificador del reto contrarreloj en los [ResultadoAcceso]. */
        const val ID_CONTRARRELOJ = "CONTRARRELOJ"
        const val ID_JUEGO_OCULTO = "JUEGO_OCULTO"
    }
}
