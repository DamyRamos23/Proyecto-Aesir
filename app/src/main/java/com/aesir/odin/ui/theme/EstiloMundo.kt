package com.aesir.odin.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/** Material del escudo, de los paneles y de la piedra rúnica de cada mundo. */
enum class MaterialEscudo { MADERA, PIEDRA, HIERRO, OBSIDIANA }

/** Símbolo nórdico que identifica a cada mundo. */
enum class Emblema { VEGVISIR, OJO, YGGDRASIL, MJOLNIR }

/** Paisaje que se dibuja detrás de las pantallas del mundo. */
enum class Escena { AMANECER, TORMENTA, AURORA, VOLCAN }

/**
 * Todo lo que distingue visualmente a un mundo: color, material, runas,
 * emblema y paisaje. Se elige por el `orden` del mundo, así que funciona con
 * los mundos que vengan de la base de datos sin tocar el dominio.
 */
@Immutable
data class EstiloMundo(
    val acento: Color,
    val metal: Color,
    val metalClaro: Color,
    val metalOscuro: Color,
    val caraClara: Color,
    val caraOscura: Color,
    val fondo: Color,
    val particula: Color,
    val material: MaterialEscudo,
    val emblema: Emblema,
    val escena: Escena,
    /** Runas del mundo; el tema N usa runas[N - 1]. */
    val runas: List<String>
) {
    fun runaDeTema(orden: Int): String = runas[((orden - 1).coerceAtLeast(0)) % runas.size]
}

object EstilosMundo {

    /** Mundo 1: madera y oro, amanecer en el fiordo, vegvísir. */
    val Madera = EstiloMundo(
        acento = Color(0xFFF0B85A),
        metal = Color(0xFFC9A35B),
        metalClaro = Color(0xFFE0C07A),
        metalOscuro = Color(0xFF6E5228),
        caraClara = Color(0xFF6A4428),
        caraOscura = Color(0xFF2B1A0F),
        fondo = Color(0xFF130E0A),
        particula = Color(0xFFFFB050),
        material = MaterialEscudo.MADERA,
        emblema = Emblema.VEGVISIR,
        escena = Escena.AMANECER,
        runas = listOf("ᚠ", "ᚢ", "ᚦ", "ᚨ", "ᚱ", "ᚲ", "ᚷ", "ᚹ")
    )

    /** Mundo 2: piedra y plata, costa en tormenta, ojo de Odín. */
    val Piedra = EstiloMundo(
        acento = Color(0xFF5FD3C6),
        metal = Color(0xFFA9B8BC),
        metalClaro = Color(0xFFDDE6E8),
        metalOscuro = Color(0xFF56666A),
        caraClara = Color(0xFF5B6A6B),
        caraOscura = Color(0xFF1D2627),
        fondo = Color(0xFF0C1214),
        particula = Color(0xFFBFE0E4),
        material = MaterialEscudo.PIEDRA,
        emblema = Emblema.OJO,
        escena = Escena.TORMENTA,
        runas = listOf("ᚺ", "ᚾ", "ᛃ", "ᛇ", "ᛈ", "ᛉ", "ᛊ", "ᛁ")
    )

    /** Mundo 3: hierro y aurora, noche polar, Yggdrasil. */
    val Hierro = EstiloMundo(
        acento = Color(0xFF5CF2B0),
        metal = Color(0xFF8F9CC8),
        metalClaro = Color(0xFFC9D2F2),
        metalOscuro = Color(0xFF3C4670),
        caraClara = Color(0xFF2C3B5E),
        caraOscura = Color(0xFF0B1222),
        fondo = Color(0xFF070B16),
        particula = Color(0xFFCFE9FF),
        material = MaterialEscudo.HIERRO,
        emblema = Emblema.YGGDRASIL,
        escena = Escena.AURORA,
        runas = listOf("ᛏ", "ᛒ", "ᛖ", "ᛗ", "ᛞ", "ᛟ", "ᛚ", "ᛜ")
    )

    /** Mundo 4: obsidiana y lava, reino volcánico, Mjölnir. */
    val Obsidiana = EstiloMundo(
        acento = Color(0xFFFF7A26),
        metal = Color(0xFFB5652E),
        metalClaro = Color(0xFFE09058),
        metalOscuro = Color(0xFF4A2312),
        caraClara = Color(0xFF2E1512),
        caraOscura = Color(0xFF070303),
        fondo = Color(0xFF0D0605),
        particula = Color(0xFFFF8A3A),
        material = MaterialEscudo.OBSIDIANA,
        emblema = Emblema.MJOLNIR,
        escena = Escena.VOLCAN,
        runas = listOf("ᚼ", "ᛘ", "ᛦ", "ᚬ", "ᛅ", "ᛐ", "ᛋ", "ᚭ")
    )

    private val enOrden = listOf(Madera, Piedra, Hierro, Obsidiana)

    /** Estilo del mundo según su `orden` (1, 2, 3, 4...). Si hay más de 4 mundos se repiten. */
    fun porOrden(orden: Int): EstiloMundo = enOrden[((orden - 1).coerceAtLeast(0)) % enOrden.size]
}

/** Colores compartidos por todas las pantallas nórdicas. */
object OdinPaleta {
    val Hollin = Color(0xFF14110D)
    val HollinAlto = Color(0xFF1E1A15)
    val Hierro = Color(0xFF3A3229)
    val Oro = Color(0xFFC9A35B)
    val Brasa = Color(0xFFF0B85A)
    val Hueso = Color(0xFFE9E1D0)
    val HuesoTenue = Color(0xFFB3AA98)
    val Escarcha = Color(0xFF9DB3C4)
    val EscarchaProfunda = Color(0xFF233240)
    val Tinta = Color(0xFF33220F)
    val TintaOscura = Color(0xFF24170B)
    val TintaRoja = Color(0xFF7A2E1A)
    val Correcto = Color(0xFF7FD18B)
    val Incorrecto = Color(0xFFE2614B)
    val Incompleto = Color(0xFFE0A33A)
}

/**
 * Tipografías. Con FontFamily.Serif la app compila sin archivos extra.
 * Para el look final: descarga Cinzel y Alegreya de Google Fonts, cópialas a
 * res/font (cinzel_semibold.ttf, alegreya_regular.ttf) y cambia estas dos líneas por:
 *   FontFamily(Font(R.font.cinzel_semibold, FontWeight.SemiBold))
 *   FontFamily(Font(R.font.alegreya_regular))
 */
val FuenteTitulo: FontFamily = FontFamily.Serif
val FuenteTexto: FontFamily = FontFamily.Serif
