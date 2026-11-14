package com.aesir.odin.domain.racha

import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

/**
 * MOD-05 Sistema de racha · La carrera de Sól (fachada).
 *
 * Es la única entrada al módulo. Lo avisan tres disparadores: abrir la
 * aplicación ([alAbrirAplicacion]), terminar una actividad que cuenta
 * ([alTerminarActividad], lo llaman los Finalizar*UseCase de CU-01 a CU-05 y
 * CU-07 a CU-13) y comprar una ofrenda ([validarOfrenda] y [comprarOfrenda]).
 * Al llegar a un hito pide al MOD-01 el bono y al MOD-02 el sonido.
 *
 * RN-30: no modifica tiempos, puntajes ni la validación de ninguna actividad.
 *
 * Además de regresar cada [EstadoRacha], lo deja en [estado] y publica en
 * [avisos] lo que el alumno debe ver (racha encendida, protegida, en riesgo o
 * perdida) sin importar en qué pantalla esté.
 */
class ModuloCarreraSol(
    private val almacen: AlmacenRacha,
    private val moduloHidromiel: ModuloHidromiel,
    private val reproductor: ReproductorSonido = ReproductorSonido.Silencio,
    private val calculadora: CalculadoraRacha = CalculadoraRacha(),
    private val reglas: ReglasRacha = ReglasRacha()
) : ConsultaRacha {

    private val candado = Mutex()

    private val _estado = MutableStateFlow<EstadoRacha?>(null)
    /** Último estado conocido de la racha (para el contador de la pantalla de mundos). */
    val estado: StateFlow<EstadoRacha?> = _estado.asStateFlow()

    private val _avisos = MutableSharedFlow<AvisoRacha>(extraBufferCapacity = 8)
    val avisos: SharedFlow<AvisoRacha> = _avisos.asSharedFlow()

    // -----------------------------------------------------------------------
    // Disparador 2: abrir la aplicación en un nuevo día (Funcionamiento 1 y 2)
    // -----------------------------------------------------------------------

    suspend fun alAbrirAplicacion(e: AplicacionAbierta): EstadoRacha = candado.withLock {
        val racha = leerOCrear(e.idUsuario)
        val resultado = calculadora.alAbrir(racha, e.fecha)
        almacen.guardar(racha)
        _estado.value = resultado
        when {
            resultado.estado == EstadoRachaDia.PROTEGIDA -> publicar(
                resultado,
                "Sól está a salvo: ${plural(resultado.ofrendasUsadas, "ofrenda protegió", "ofrendas protegieron")} tu racha de ${dias(resultado.actual)}."
            )
            resultado.rachaPerdida > 0 -> publicar(
                resultado,
                "Sköll alcanzó a Sól. Perdiste tu racha de ${dias(resultado.rachaPerdida)}; tu mejor racha sigue siendo de ${dias(resultado.maxima)}."
            )
            resultado.estado == EstadoRachaDia.EN_RIESGO -> publicar(
                resultado,
                "Sköll se acerca: termina una actividad hoy para mantener tu racha de ${dias(resultado.actual)}."
            )
        }
        resultado
    }

    // -----------------------------------------------------------------------
    // Disparadores 1 y 4: terminar una actividad y alcanzar un hito (3 a 5)
    // -----------------------------------------------------------------------

    suspend fun alTerminarActividad(e: ActividadTerminada): EstadoRacha = candado.withLock {
        val racha = leerOCrear(e.idUsuario)
        val contabaHoy = racha.contoHoy(e.fechaFin)
        var resultado = calculadora.alTerminar(racha, e)
        val sumo = !contabaHoy && racha.contoHoy(e.fechaFin)

        var hito: Hito? = null
        if (sumo) {
            val alcanzado = reglas.hitoPara(racha.actual)
            // RN-28 / E7: cada bono se entrega una sola vez por alumno
            if (alcanzado != null && !almacen.hitoCobrado(e.idUsuario, alcanzado.dias)) {
                moduloHidromiel.abonarBonoRacha(e.idUsuario, alcanzado.bono)
                alcanzado.titulo?.let(racha::otorgarTitulo)
                almacen.registrarHito(e.idUsuario, alcanzado)
                hito = alcanzado
            }
        }
        almacen.guardar(racha)
        resultado = resultado.copy(hito = hito)
        _estado.value = resultado

        if (sumo) {
            reproductor.reproducirProgreso(SonidoRacha.RACHA_ENCENDIDA)
            if (hito != null) reproductor.reproducirProgreso(SonidoRacha.HITO_RACHA)
            val extra = when {
                hito?.titulo != null -> " ¡Hito de ${hito.dias} días! +${hito.bono} de hidromiel y el título «${hito.titulo}»."
                hito != null -> " ¡Hito de ${hito.dias} días! +${hito.bono} de hidromiel."
                else -> ""
            }
            publicar(resultado, "Sól brilla: tu racha es de ${dias(resultado.actual)}.$extra")
        }
        resultado
    }

    // -----------------------------------------------------------------------
    // Disparador 3: ofrenda a Sól (Funcionamiento 6)
    // -----------------------------------------------------------------------

    /** Regresa el costo y si se puede comprar, sin cobrar nada (E3, E4). */
    suspend fun validarOfrenda(idUsuario: String): ResultadoOfrenda = candado.withLock {
        val racha = leerOCrear(idUsuario)
        val motivo = reglas.validarCompra(racha, moduloHidromiel.saldo(idUsuario))
        ResultadoOfrenda(
            costo = ReglasRacha.COSTO_OFRENDA,
            puedeComprar = motivo == null,
            comprada = false,
            motivo = motivo,
            ofrendas = racha.ofrendas
        )
    }

    /** Solo se llama si el alumno confirmó: el MOD-01 cobra OFRENDA_SOL y se guarda la ofrenda. */
    suspend fun comprarOfrenda(idUsuario: String): ResultadoOfrenda = candado.withLock {
        val racha = leerOCrear(idUsuario)
        val costo = ReglasRacha.COSTO_OFRENDA
        if (racha.ofrendas >= ReglasRacha.MAX_OFRENDAS) {
            return@withLock ResultadoOfrenda(costo, false, false, MotivoRechazo.LIMITE_OFRENDAS, racha.ofrendas)
        }
        val cobro = moduloHidromiel.cobrarOfrendaSol(idUsuario, costo)
        if (!cobro.autorizado) {
            return@withLock ResultadoOfrenda(costo, false, false, MotivoRechazo.HIDROMIEL_INSUFICIENTE, racha.ofrendas)
        }
        racha.agregarOfrenda()
        almacen.guardar(racha)
        _estado.value = _estado.value?.copy(ofrendas = racha.ofrendas)
        ResultadoOfrenda(costo, puedeComprar = racha.ofrendas < ReglasRacha.MAX_OFRENDAS, comprada = true, motivo = null, ofrendas = racha.ofrendas)
    }

    // -----------------------------------------------------------------------
    // Consultas (sin usar ofrendas ni guardar nada)
    // -----------------------------------------------------------------------

    /** MOD-04 consulta el estado de la racha del día mediante [ConsultaRacha]. */
    override suspend fun estadoDelDia(idUsuario: String, hoy: LocalDate): EstadoRachaDia =
        calculadora.estadoPara(leerOCrear(idUsuario), hoy)

    /** Lo que muestra la pantalla de la racha: estado, títulos e hitos cobrados. */
    suspend fun resumen(idUsuario: String, hoy: LocalDate): ResumenRacha {
        val racha = leerOCrear(idUsuario)
        val estado = EstadoRacha(
            estado = calculadora.estadoPara(racha, hoy),
            actual = racha.actual,
            maxima = racha.maxima,
            ofrendas = racha.ofrendas
        )
        if (_estado.value == null) _estado.value = estado
        return ResumenRacha(estado, racha.titulos, almacen.hitosCobrados(idUsuario), reglas.hitos)
    }

    private suspend fun leerOCrear(idUsuario: String): Racha = almacen.leer(idUsuario) ?: Racha(idUsuario)

    private fun publicar(estado: EstadoRacha, mensaje: String) {
        _avisos.tryEmit(AvisoRacha(estado, mensaje))
    }

    private fun dias(n: Int) = if (n == 1) "1 día" else "$n días"

    private fun plural(n: Int, uno: String, varios: String) = if (n == 1) "1 $uno" else "$n $varios"
}
