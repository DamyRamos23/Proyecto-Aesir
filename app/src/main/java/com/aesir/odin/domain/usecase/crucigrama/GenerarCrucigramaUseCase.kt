package com.aesir.odin.domain.usecase.crucigrama

import com.aesir.odin.domain.model.Celda
import com.aesir.odin.domain.model.ConceptoCrucigrama
import com.aesir.odin.domain.model.Crucigrama
import com.aesir.odin.domain.model.Direccion
import com.aesir.odin.domain.model.PalabraCrucigrama
import com.aesir.odin.domain.repository.CrucigramaRepository
import java.util.UUID
import kotlin.random.Random

/**
 * Genera un crucigrama con conceptos de los temas incluidos (RF-47).
 *
 * 1. Pide el banco de conceptos al repositorio.
 * 2. Elige [CANTIDAD_PALABRAS] conceptos al azar (seleccionarAleatorios).
 * 3. Los acomoda en la cuadrícula cruzándolos por letras en común
 *    (acomodarPalabras).
 *
 * Si no logra acomodarlos todos, vuelve a intentar con otra selección. Si el
 * banco no alcanza o no se pudo armar ni el mínimo, regresa
 * Result.failure (flujo alternativo A2.1).
 */
class GenerarCrucigramaUseCase(
    private val repository: CrucigramaRepository,
    private val random: Random = Random.Default
) {

    suspend operator fun invoke(temaId: String, temasIncluidos: List<String>): Result<Crucigrama> = runCatching {
        val temas = temasIncluidos.ifEmpty { listOf(temaId) }
        val banco = repository.obtenerBancoPorTemas(temas)
            .map { it.copy(respuesta = normalizarPalabra(it.respuesta)) }
            .filter { it.respuesta.length in LONGITUD_MINIMA..MAX_LADO }
            .distinctBy { it.respuesta }
        check(banco.size >= CANTIDAD_PALABRAS) {
            "No hay suficientes conceptos para armar el crucigrama (${banco.size} de $CANTIDAD_PALABRAS)."
        }

        var mejor: List<PalabraCrucigrama> = emptyList()
        for (intento in 1..INTENTOS) {
            val palabras = acomodarPalabras(seleccionarAleatorios(banco))
            if (palabras.size > mejor.size) mejor = palabras
            if (mejor.size == CANTIDAD_PALABRAS) break
        }
        check(mejor.size >= MINIMO_PALABRAS) { "No se pudieron acomodar los conceptos en la cuadrícula." }

        Crucigrama(
            id = UUID.randomUUID().toString(),
            temaId = temaId,
            temasIncluidos = temas,
            filas = mejor.maxOf { it.celdas.last().fila } + 1,
            columnas = mejor.maxOf { it.celdas.last().columna } + 1,
            palabras = mejor
        )
    }

    fun seleccionarAleatorios(banco: List<ConceptoCrucigrama>): List<ConceptoCrucigrama> =
        banco.shuffled(random).take(CANTIDAD_PALABRAS)

    /**
     * Coloca la palabra más larga en horizontal y cada una de las demás
     * cruzando a una ya colocada, en la dirección contraria. Una palabra
     * solo se coloca si no choca con otra letra, no queda pegada a otra
     * palabra en paralelo y la cuadrícula no pasa de [MAX_LADO] × [MAX_LADO].
     * Las que no caben se descartan. Las coordenadas se devuelven desde (0, 0).
     */
    fun acomodarPalabras(seleccion: List<ConceptoCrucigrama>): List<PalabraCrucigrama> {
        if (seleccion.isEmpty()) return emptyList()
        val tablero = Tablero()
        val ordenados = seleccion.sortedByDescending { it.respuesta.length }
        tablero.colocar(ordenados.first(), 0, 0, Direccion.HORIZONTAL)

        for (concepto in ordenados.drop(1)) {
            val candidatos = tablero.posicionesPara(concepto.respuesta)
            if (candidatos.isEmpty()) continue
            val mayorCruce = candidatos.maxOf { it.cruces }
            val elegido = candidatos.filter { it.cruces == mayorCruce }.random(random)
            tablero.colocar(concepto, elegido.fila, elegido.columna, elegido.direccion)
        }
        return tablero.normalizadas()
    }

    /** Cuadrícula de trabajo del acomodo; las coordenadas pueden ser negativas. */
    private class Tablero {
        private val letras = mutableMapOf<Celda, Char>()
        private val direcciones = mutableMapOf<Celda, MutableSet<Direccion>>()
        private val colocadas = mutableListOf<PalabraCrucigrama>()

        data class Posicion(val fila: Int, val columna: Int, val direccion: Direccion, val cruces: Int)

        fun colocar(concepto: ConceptoCrucigrama, fila: Int, columna: Int, direccion: Direccion) {
            val palabra = PalabraCrucigrama(
                id = concepto.id,
                conceptoId = concepto.id,
                temaId = concepto.temaId,
                respuesta = concepto.respuesta,
                pista = concepto.descripcion,
                fila = fila,
                columna = columna,
                direccion = direccion
            )
            palabra.celdas.forEachIndexed { i, c ->
                letras[c] = palabra.respuesta[i]
                direcciones.getOrPut(c) { mutableSetOf() } += direccion
            }
            colocadas += palabra
        }

        fun posicionesPara(texto: String): List<Posicion> {
            val posiciones = mutableListOf<Posicion>()
            for (otra in colocadas) {
                val direccion = if (otra.direccion == Direccion.HORIZONTAL) Direccion.VERTICAL else Direccion.HORIZONTAL
                otra.celdas.forEachIndexed { j, celdaOtra ->
                    texto.forEachIndexed { i, letra ->
                        if (letra != otra.respuesta[j]) return@forEachIndexed
                        val fila = if (direccion == Direccion.VERTICAL) celdaOtra.fila - i else celdaOtra.fila
                        val columna = if (direccion == Direccion.HORIZONTAL) celdaOtra.columna - i else celdaOtra.columna
                        val cruces = cabe(texto, fila, columna, direccion)
                        if (cruces > 0) posiciones += Posicion(fila, columna, direccion, cruces)
                    }
                }
            }
            return posiciones.distinct()
        }

        /** @return cuántas letras cruza la palabra, o 0 si no se puede colocar ahí. */
        private fun cabe(texto: String, fila: Int, columna: Int, direccion: Direccion): Int {
            val (df, dc) = if (direccion == Direccion.HORIZONTAL) 0 to 1 else 1 to 0
            // Antes del inicio y después del final debe haber una casilla vacía
            if (Celda(fila - df, columna - dc) in letras) return 0
            if (Celda(fila + df * texto.length, columna + dc * texto.length) in letras) return 0

            var cruces = 0
            for (i in texto.indices) {
                val celda = Celda(fila + df * i, columna + dc * i)
                val existente = letras[celda]
                if (existente != null) {
                    if (existente != texto[i] || direccion in direcciones.getValue(celda)) return 0
                    cruces++
                } else {
                    // Sin vecinos a los lados: evita palabras pegadas en paralelo
                    val lado1 = Celda(celda.fila + dc, celda.columna + df)
                    val lado2 = Celda(celda.fila - dc, celda.columna - df)
                    if (lado1 in letras || lado2 in letras) return 0
                }
            }
            val todas = letras.keys + List(texto.length) { Celda(fila + df * it, columna + dc * it) }
            val alto = todas.maxOf { it.fila } - todas.minOf { it.fila } + 1
            val ancho = todas.maxOf { it.columna } - todas.minOf { it.columna } + 1
            return if (alto > MAX_LADO || ancho > MAX_LADO) 0 else cruces
        }

        fun normalizadas(): List<PalabraCrucigrama> {
            val minFila = letras.keys.minOf { it.fila }
            val minColumna = letras.keys.minOf { it.columna }
            return colocadas.map { it.copy(fila = it.fila - minFila, columna = it.columna - minColumna) }
        }
    }

    companion object {
        const val CANTIDAD_PALABRAS = 6
        const val MINIMO_PALABRAS = 4
        const val LONGITUD_MINIMA = 3
        /** Lado máximo de la cuadrícula, para que quepa en la pantalla del teléfono. */
        const val MAX_LADO = 12
        private const val INTENTOS = 40
    }
}
