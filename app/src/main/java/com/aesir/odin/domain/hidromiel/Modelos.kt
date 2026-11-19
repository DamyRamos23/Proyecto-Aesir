package com.aesir.odin.domain.hidromiel

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tipos del MOD-01 Sistema de Hidromiel (diagrama de clases MOD-01).
 * Todo es Kotlin puro: no depende de Android ni de Room.
 */

/** La app aún no tiene cuentas: todo el progreso es del usuario local. */
const val USUARIO_LOCAL = "local"

// ---------------------------------------------------------------------------
// Enumeraciones
// ---------------------------------------------------------------------------

/** Estados de un nivel (tabla "Estados de un nivel" del MOD-01). */
enum class EstadoNivel { BLOQUEADO, DISPONIBLE, GRATUITO, SUPERADO }

enum class TipoNivel { NORMAL, RAGNAROK }

enum class ResultadoNivel { SUPERADO, NO_SUPERADO, ABANDONADO }

enum class ResultadoFlash { CORRECTA, INCORRECTA, INCOMPLETA, SIN_RESPUESTA }

enum class TipoAyuda { REVELAR_LETRA, REVELAR_PALABRA }

enum class OrigenSuperado { JUGADO, DIAGNOSTICO }

enum class TipoMovimiento {
    SALDO_INICIAL,
    ENTRADA_NIVEL,
    RECOMPENSA_NIVEL,
    RECOMPENSA_DIARIA,
    AYUDA_CRUCIGRAMA,
    PREGUNTA_FLASH,
    ABANDONO_CONTRARRELOJ,
    ENTRADA_CONTRARRELOJ,
    RECOMPENSA_CONTRARRELOJ,
    BONO_DIAGNOSTICO,
    /** Juegos ocultos (easter eggs) de la pantalla de Mundos. */
    APUESTA_OCULTA,
    /** CU-13 duelo de enigmas. */
    APUESTA_DUELO,
    PREMIO_DUELO,
    PREMIO_OCULTO,
    /** CU-12: apuesta de la consulta a la Völva (RF-164). */
    APUESTA_VOLVA,
    /** CU-12: premio o devolución de la consulta a la Völva (RF-173 a RF-175). */
    PREMIO_VOLVA,
    /** CU-01: apuesta de una noche de Hasta el Alba (RF-05). */
    APUESTA_ALBA,
    /** CU-01: premio neto al volver al banquete o sobrevivir hasta el alba (RF-17). */
    PREMIO_ALBA,
    /** CU-02: apuesta de la forja de Brokkr (se descuenta al apostar a Loki). */
    APUESTA_FORJA,
    /** CU-02: premio de la forja perfecta (apuesta ×3 menos el diezmo de Odín). */
    PREMIO_FORJA,
    /** MOD-05: bono por alcanzar un hito de racha. */
    BONO_RACHA,
    /** MOD-05: compra de una ofrenda a Sól. */
    OFRENDA_SOL,
    /** CU-07 A3.1: el nivel no se pudo generar y el alumno regresó al roadmap. */
    DEVOLUCION_ENTRADA
}

// ---------------------------------------------------------------------------
// DTOs
// ---------------------------------------------------------------------------

/**
 * Lo que el módulo necesita saber de un nivel del roadmap.
 * [nombre] no aparece en el diagrama; se agregó para poder armar el motivo
 * de bloqueo (E4: "indica qué nivel conectado debe superar").
 */
data class NivelInfo(
    val idNivel: String,
    val idMundo: String,
    val nombre: String,
    val tipo: TipoNivel,
    val esPrimero: Boolean,
    /** Anillo del árbol (1 y 4 lecciones, 2 minijuegos, 3 retos). */
    val anillo: Int = 1,
    /** Tema (lección) al que pertenece el nivel: el diagnóstico reconoce todos los niveles de un tema conocido. */
    val idTema: String = idNivel
)

/**
 * Respuesta del módulo sobre un nivel.
 * - [faltante]: hidromiel que falta (E1) o, si es el Ragnarök bloqueado,
 *   cuántos niveles faltan por superar (E4). 0 si no falta nada.
 * - [motivo]: texto listo para mostrar al alumno.
 */
data class ResultadoAcceso(
    val idNivel: String,
    val estado: EstadoNivel,
    val autorizado: Boolean,
    val costo: Int,
    val faltante: Int,
    val motivo: String
)

/**
 * Aviso de lo ganado o perdido. [cantidad] es el cambio de saldo que produjo
 * este evento: positiva si se ganó, negativa si se descontó, 0 si no cambió
 * (por ejemplo, al no superar un nivel la entrada ya se había cobrado antes).
 */
data class AvisoHidromiel(
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val saldoActual: Int,
    val mensaje: String
)

// ---------------------------------------------------------------------------
// Eventos que recibe el módulo
// ---------------------------------------------------------------------------

data class NivelFinalizado(
    val idUsuario: String,
    val idNivel: String,
    val puntaje: Int,
    val resultado: ResultadoNivel
)

data class DiagnosticoCompletado(
    val idUsuario: String,
    val idMundo: String,
    val nivelesConocidos: List<String>
)

data class PreguntaFlashResuelta(
    val idUsuario: String,
    val resultado: ResultadoFlash
)

/** [saldoReto]: puntaje acumulado en el reto al abandonarlo; si es negativo se suma a la penalización. */
data class ContrarrelojAbandonado(val idUsuario: String, val saldoReto: Int = 0)

/** CU-02: la forja de Brokkr terminó con [resultado]; si fue perfecta se abona el premio (RF-41). */
data class ForjaFinalizada(
    val forjaId: String,
    val apuesta: Int,
    val resultado: com.aesir.odin.domain.model.EstadoFinalForja
)

/** CU-02: resultado de cobrar la apuesta de la forja (RF-33). [faltante]: hidromiel que faltó si no se cobró. */
data class ResultadoApuestaForja(
    val cobrada: Boolean,
    val saldoActual: Int,
    val faltante: Int = 0,
    /** E8: por qué no se cobró cuando no es por falta de hidromiel. */
    val motivo: String? = null
)

/**
 * CU-11: el reto terminó. [acumulado] es el hidromiel que se juntó durante el
 * reto, sin bajar de 0 (RF-154); si no se da, se calcula con aciertos y fallos.
 */
data class ContrarrelojFinalizado(val idUsuario: String, val aciertos: Int, val fallos: Int = 0, val acumulado: Int? = null)

/** CU-13: el duelo terminó (ganado o perdido) con [sorbosSobrantes] en el cuerno. */
data class DueloFinalizado(val idUsuario: String, val ganado: Boolean, val sorbosSobrantes: Int)

/** CU-13: respuesta a una apuesta. Reemplaza a ResultadoAcceso porque el duelo no es un nivel. */
data class ResultadoApuesta(val autorizado: Boolean, val faltante: Int, val motivo: String)

/** CU-12: la consulta a la Völva terminó con [lectura]; el módulo paga lo que corresponda (pasos 12 y 13). */
data class ConsultaVolvaTerminada(val idUsuario: String, val apuesta: Int, val lectura: com.aesir.odin.domain.model.Lectura)

/** CU-01: la noche terminó con cobro (retirada o alba); el módulo abona [premioNeto] como PREMIO_ALBA. */
data class NocheAlbaCobrada(val idUsuario: String, val premioNeto: Int, val diezmo: Int)

/** Pago de un modo del Valhalla: premio bruto, diezmo de Odín y lo que se abona. */
data class PagoValhalla(val bruto: Int, val diezmo: Int, val neto: Int)

// ---------------------------------------------------------------------------
// Registros
// ---------------------------------------------------------------------------

data class MovimientoHidromiel(
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val fecha: LocalDateTime,
    val saldoResultante: Int
)

/** RN-02 / RN-06: un nivel superado jugando o reconocido por el diagnóstico. */
data class NivelSuperado(
    val idUsuario: String,
    val idNivel: String,
    val origen: OrigenSuperado,
    val recompensaOtorgada: Int,
    val fecha: LocalDate
)

/** Se crea al superar el Ragnarök del mundo anterior. */
data class MundoDesbloqueado(
    val idUsuario: String,
    val idMundo: String,
    val fecha: LocalDate
)
