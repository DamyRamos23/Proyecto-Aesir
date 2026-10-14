package com.aesir.odin.domain.model

/**
 * Modelos del CU-07 Resolver crucigrama de conceptos (diagrama de clases
 * CU-07, paquete Models).
 */

enum class Direccion { HORIZONTAL, VERTICAL }

/** Casilla de la cuadrícula. */
data class Celda(val fila: Int, val columna: Int)

/** Concepto del banco: la respuesta y la descripción que sirve de pista. */
data class ConceptoCrucigrama(
    val id: String,
    val temaId: String,
    val respuesta: String,
    val descripcion: String
)

/**
 * Palabra colocada en la cuadrícula. [respuesta] ya está normalizada
 * (mayúsculas, sin acentos, sin espacios). [revelada] indica que se resolvió
 * con la ayuda "Revelar palabra", así que no cuenta como acierto.
 */
data class PalabraCrucigrama(
    val id: String,
    val conceptoId: String,
    val temaId: String,
    val respuesta: String,
    val pista: String,
    val fila: Int,
    val columna: Int,
    val direccion: Direccion,
    val resuelta: Boolean = false,
    val revelada: Boolean = false
) {
    val longitud: Int get() = respuesta.length

    /** Casillas que ocupa, en orden de lectura. */
    val celdas: List<Celda>
        get() = List(longitud) { i ->
            if (direccion == Direccion.HORIZONTAL) Celda(fila, columna + i) else Celda(fila + i, columna)
        }

    fun letraEn(celda: Celda): Char? = celdas.indexOf(celda).takeIf { it >= 0 }?.let { respuesta[it] }
}

data class Crucigrama(
    val id: String,
    val temaId: String,
    val temasIncluidos: List<String>,
    val filas: Int,
    val columnas: Int,
    val palabras: List<PalabraCrucigrama>,
    val puntajeInicial: Int = 0,
    val completado: Boolean = false
) {
    val resueltas: Int get() = palabras.count { it.resuelta }

    /** Número de pista de cada palabra: se numeran las casillas de inicio en orden de lectura. */
    val numeros: Map<String, Int>
        get() {
            val inicios = palabras.map { Celda(it.fila, it.columna) }.distinct()
                .sortedWith(compareBy({ it.fila }, { it.columna }))
            return palabras.associate { it.id to inicios.indexOf(Celda(it.fila, it.columna)) + 1 }
        }

    /** Letra correcta de cada casilla ocupada. */
    val solucion: Map<Celda, Char>
        get() = buildMap { palabras.forEach { p -> p.celdas.forEachIndexed { i, c -> put(c, p.respuesta[i]) } } }
}

/** Resultado de validar una palabra (diagrama de secuencia, fase 2). */
data class ResultadoPalabra(
    val palabraId: String,
    val estado: EstadoRespuesta,
    val puntajeActual: Int
)

data class ResultadoCrucigrama(
    val temaId: String,
    val puntajeFinal: Int,
    val aciertos: Int,
    val erroresCometidos: Int,
    val fecha: Long
)
