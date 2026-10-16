package com.aesir.odin.domain.model

/**
 * Modelos del CU-09 Resolver reto de arrastrar y soltar (diagrama de clases
 * CU-09, capa Negocio · Models).
 */

/** Modalidad del reto registrada para el tema (RF-69). */
enum class ModalidadReto {
    /** Ordenar las etapas de un proceso en las posiciones numeradas. */
    ORDENAR_PROCESO,

    /** Completar un diagrama colocando sus elementos en los huecos. */
    COMPLETAR_DIAGRAMA;

    companion object {
        fun desde(texto: String?): ModalidadReto = entries.firstOrNull { it.name == texto } ?: ORDENAR_PROCESO
    }
}

/** Elemento que el alumno arrastra: una etapa del proceso o una pieza del diagrama. */
data class ElementoArrastrable(
    val id: String,
    val retoId: String,
    val texto: String,
    val posicionCorrectaId: String
)

/**
 * Lugar donde se suelta un elemento.
 * - En ORDENAR_PROCESO es una posición numerada ([orden]).
 * - En COMPLETAR_DIAGRAMA es un hueco del diagrama: [x] y [y] son su centro,
 *   de 0 a 1 sobre el ancho y el alto del diagrama; [etiqueta] es una pista opcional.
 */
data class PosicionDestino(
    val id: String,
    val retoId: String,
    val orden: Int,
    val etiqueta: String? = null,
    val x: Float? = null,
    val y: Float? = null
)

/**
 * Reto de arrastrar y soltar de un tema.
 * [imagenDiagrama] nombra el diagrama que se dibuja de fondo en la modalidad
 * COMPLETAR_DIAGRAMA (ver DiagramasReto en la capa de presentación).
 */
data class RetoArrastrar(
    val id: String,
    val temaId: String,
    val modalidad: ModalidadReto,
    val instrucciones: String,
    val imagenDiagrama: String?,
    val elementos: List<ElementoArrastrable>,
    val posiciones: List<PosicionDestino>,
    val completado: Boolean
) {
    /** Colocación correcta de cada elemento (elementoId → posicionId), para mostrar la solución (A7.2). */
    val solucion: Map<String, String> get() = elementos.associate { it.id to it.posicionCorrectaId }
}

/** Resultado de un intento terminado del reto (postcondiciones). */
data class ResultadoReto(
    val retoId: String,
    val temaId: String,
    val puntaje: Int,
    /** Confirmaciones que hizo el alumno, contando la correcta. */
    val intentos: Int,
    val aprobado: Boolean,
    val fecha: Long
)
