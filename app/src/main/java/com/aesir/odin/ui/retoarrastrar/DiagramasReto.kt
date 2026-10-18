package com.aesir.odin.ui.retoarrastrar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/** Texto fijo del diagrama (lo que ya viene dado); centro en (x, y) de 0 a 1. */
internal data class EtiquetaDiagrama(
    val texto: String,
    val x: Float,
    val y: Float,
    val ancho: Float = 0.3f,
    val negrita: Boolean = false,
    /** Se dibuja como piedra fija (lado izquierdo del modelo en V). */
    val piedra: Boolean = false
)

/**
 * Diagramas incompletos de la modalidad COMPLETAR_DIAGRAMA (RF-71). Cada
 * diagrama se dibuja en un lienzo cuadrado con coordenadas de 0 a 1; los
 * huecos vienen de PosicionDestino.x / y en la base de datos.
 */
internal object DiagramasReto {

    /** Ancho de un hueco (y de la piedra que se suelta en él), en fracción del lienzo. */
    const val ANCHO_HUECO = 0.3f

    fun etiquetas(nombre: String?): List<EtiquetaDiagrama> = when (nombre) {
        "diagrama_clases" -> listOf(
            EtiquetaDiagrama("Animal", 0.5f, 0.07f, ancho = 0.36f, negrita = true),
            EtiquetaDiagrama("− nombre: String", 0.5f, 0.155f, ancho = 0.38f),
            EtiquetaDiagrama("+ comer(): void", 0.5f, 0.245f, ancho = 0.38f)
        )
        "casos_uso" -> listOf(
            EtiquetaDiagrama("Validar PIN", 0.83f, 0.24f, ancho = 0.26f),
            EtiquetaDiagrama("Imprimir recibo", 0.83f, 0.8f, ancho = 0.26f),
            EtiquetaDiagrama("Cajero automático", 0.66f, 0.93f, ancho = 0.5f, negrita = true)
        )
        "piramide_pruebas" -> listOf(
            EtiquetaDiagrama("más amplias y lentas", 0.15f, 0.2f, ancho = 0.28f),
            EtiquetaDiagrama("más numerosas y rápidas", 0.85f, 0.2f, ancho = 0.28f)
        )
        "modelo_v" -> listOf(
            EtiquetaDiagrama("Requisitos", 0.21f, 0.14f, piedra = true),
            EtiquetaDiagrama("Diseño del sistema", 0.25f, 0.34f, piedra = true),
            EtiquetaDiagrama("Diseño de arquitectura", 0.29f, 0.54f, piedra = true),
            EtiquetaDiagrama("Diseño de módulos", 0.33f, 0.74f, piedra = true),
            EtiquetaDiagrama("Codificación", 0.5f, 0.91f, piedra = true)
        )
        else -> emptyList()
    }

    /**
     * Dibuja las líneas y formas del diagrama.
     * @param alto alto de un hueco en px, para dejar espacio a las flechas.
     */
    fun DrawScope.dibujarDiagrama(nombre: String?, tinta: Color, acento: Color, alto: Float) {
        val w = size.width
        val h = size.height
        fun p(x: Float, y: Float) = Offset(x * w, y * h)
        val grueso = 1.6f * density
        val fino = 1.1f * density
        val guiones = PathEffect.dashPathEffect(floatArrayOf(5f * density, 4f * density))
        when (nombre) {
            "diagrama_clases" -> {
                // Clase padre
                caja(p(0.3f, 0.03f), p(0.7f, 0.29f), tinta, grueso, listOf(0.11f * h, 0.2f * h))
                // Clase hija con sus tres compartimentos vacíos
                caja(p(0.2f, 0.52f), p(0.8f, 0.96f), tinta, grueso, listOf(0.65f * h, 0.8f * h))
                // Herencia: línea con triángulo hueco hacia la clase padre
                drawLine(tinta, p(0.5f, 0.52f), p(0.5f, 0.335f), grueso)
                val punta = Path().apply {
                    moveTo(0.5f * w, 0.29f * h)
                    lineTo(0.47f * w, 0.335f * h)
                    lineTo(0.53f * w, 0.335f * h)
                    close()
                }
                drawPath(punta, Color(0xFF14110D))
                drawPath(punta, tinta, style = Stroke(grueso))
                drawLine(tinta.copy(alpha = .5f), p(0.5f, 0.41f), p(0.61f, 0.41f), fino, pathEffect = guiones)
            }
            "casos_uso" -> {
                // Actor
                val cabeza = p(0.16f, 0.33f)
                drawCircle(tinta, 0.035f * w, cabeza, style = Stroke(grueso))
                drawLine(tinta, p(0.16f, 0.365f), p(0.16f, 0.49f), grueso)
                drawLine(tinta, p(0.1f, 0.41f), p(0.22f, 0.41f), grueso)
                drawLine(tinta, p(0.16f, 0.49f), p(0.11f, 0.58f), grueso, cap = StrokeCap.Round)
                drawLine(tinta, p(0.16f, 0.49f), p(0.21f, 0.58f), grueso, cap = StrokeCap.Round)
                // Límite del sistema
                drawRect(tinta.copy(alpha = .08f), p(0.34f, 0.04f), Size(0.64f * w, 0.92f * h))
                drawRect(tinta, p(0.34f, 0.04f), Size(0.64f * w, 0.92f * h), style = Stroke(grueso))
                // Asociación actor – caso de uso
                drawLine(tinta, p(0.22f, 0.44f), p(0.37f, 0.5f), fino)
                // Casos de uso
                ovalo(p(0.53f, 0.5f), 0.175f * w, 0.075f * h, tinta, grueso)
                ovalo(p(0.83f, 0.24f), 0.14f * w, 0.06f * h, tinta, grueso)
                ovalo(p(0.83f, 0.8f), 0.14f * w, 0.06f * h, tinta, grueso)
                // «include»: del caso base al incluido; «extend»: de la extensión al caso base
                flecha(p(0.62f, 0.435f), p(0.75f, 0.29f), tinta, fino, guiones)
                flecha(p(0.75f, 0.75f), p(0.62f, 0.565f), tinta, fino, guiones)
            }
            "piramide_pruebas" -> {
                val apice = p(0.5f, 0.04f)
                val izq = p(0.04f, 0.96f)
                val der = p(0.96f, 0.96f)
                fun borde(y: Float, lado: Int): Offset {
                    val t = (y - 0.04f) / 0.92f
                    val x = 0.5f + lado * 0.46f * t
                    return p(x, y)
                }
                val cortes = listOf(0.04f, 0.4f, 0.6f, 0.78f, 0.96f)
                for (i in 0 until cortes.size - 1) {
                    val banda = Path().apply {
                        val a = borde(cortes[i], -1)
                        moveTo(a.x, a.y)
                        borde(cortes[i], 1).let { lineTo(it.x, it.y) }
                        borde(cortes[i + 1], 1).let { lineTo(it.x, it.y) }
                        borde(cortes[i + 1], -1).let { lineTo(it.x, it.y) }
                        close()
                    }
                    drawPath(banda, acento.copy(alpha = 0.05f + 0.05f * i))
                }
                for (y in cortes.subList(1, cortes.size - 1)) drawLine(tinta, borde(y, -1), borde(y, 1), fino)
                val triangulo = Path().apply {
                    moveTo(apice.x, apice.y); lineTo(der.x, der.y); lineTo(izq.x, izq.y); close()
                }
                drawPath(triangulo, tinta, style = Stroke(grueso))
                // Flechas laterales: hacia arriba más amplias, hacia abajo más numerosas
                flecha(p(0.1f, 0.6f), p(0.1f, 0.3f), tinta.copy(alpha = .7f), fino, null)
                flecha(p(0.9f, 0.3f), p(0.9f, 0.6f), tinta.copy(alpha = .7f), fino, null)
            }
            "modelo_v" -> {
                drawLine(tinta.copy(alpha = .55f), p(0.05f, 0.05f), p(0.5f, 0.96f), 6f * density, cap = StrokeCap.Round)
                drawLine(tinta.copy(alpha = .55f), p(0.5f, 0.96f), p(0.95f, 0.05f), 6f * density, cap = StrokeCap.Round)
                // Cada fase con su prueba
                val filas = listOf(0.14f to (0.21f to 0.79f), 0.34f to (0.25f to 0.75f), 0.54f to (0.29f to 0.71f), 0.74f to (0.33f to 0.67f))
                for ((y, xs) in filas) {
                    drawLine(
                        acento.copy(alpha = .55f),
                        p(xs.first + ANCHO_HUECO / 2f, y), p(xs.second - ANCHO_HUECO / 2f, y),
                        fino, pathEffect = guiones
                    )
                }
            }
        }
    }

    private fun DrawScope.caja(a: Offset, b: Offset, tinta: Color, grosor: Float, divisiones: List<Float>) {
        drawRect(Color(0xFF14110D), a, Size(b.x - a.x, b.y - a.y))
        drawRect(tinta.copy(alpha = .06f), a, Size(b.x - a.x, b.y - a.y))
        drawRect(tinta, a, Size(b.x - a.x, b.y - a.y), style = Stroke(grosor))
        for (y in divisiones) drawLine(tinta, Offset(a.x, y), Offset(b.x, y), grosor * .8f)
    }

    private fun DrawScope.ovalo(c: Offset, rx: Float, ry: Float, tinta: Color, grosor: Float) {
        val r = Rect(c.x - rx, c.y - ry, c.x + rx, c.y + ry)
        drawOval(Color(0xFF14110D), r.topLeft, r.size)
        drawOval(tinta.copy(alpha = .08f), r.topLeft, r.size)
        drawOval(tinta, r.topLeft, r.size, style = Stroke(grosor))
    }

    private fun DrawScope.flecha(desde: Offset, hasta: Offset, tinta: Color, grosor: Float, efecto: PathEffect?) {
        drawLine(tinta, desde, hasta, grosor, pathEffect = efecto)
        val d = hasta - desde
        val largo = d.getDistance().coerceAtLeast(1f)
        val u = d / largo
        val n = Offset(-u.y, u.x)
        val t = 9f * density
        val punta = Path().apply {
            moveTo(hasta.x, hasta.y)
            lineTo(hasta.x - u.x * t + n.x * t * .5f, hasta.y - u.y * t + n.y * t * .5f)
            moveTo(hasta.x, hasta.y)
            lineTo(hasta.x - u.x * t - n.x * t * .5f, hasta.y - u.y * t - n.y * t * .5f)
        }
        drawPath(punta, tinta, style = Stroke(grosor * 1.2f, cap = StrokeCap.Round))
    }
}
