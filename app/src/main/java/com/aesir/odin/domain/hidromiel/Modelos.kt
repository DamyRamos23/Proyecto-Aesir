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
    AJUSTE_ADMIN
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
    val esPrimero: Boolean
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

data class ContrarrelojFinalizado(val idUsuario: String, val aciertos: Int, val fallos: Int = 0)

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
