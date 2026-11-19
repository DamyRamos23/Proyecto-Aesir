package com.aesir.odin.ui.moneda

import androidx.compose.ui.graphics.Color

/**
 * Caras de la moneda: el escudo de Mundos es la primera cara y, al girarlo,
 * salen en orden los cuatro casos de uso nuevos; después vuelve el escudo.
 *
 * Para cambiar textos, colores u orden basta con editar esta lista; el dibujo
 * de cada emblema está en DibujoMoneda.kt. La cara MUNDOS no se dibuja aquí:
 * es el escudo de MundosScreen tal cual.
 */
enum class CaraMoneda(
    val titulo: String,
    val subtitulo: String,
    val descripcion: String,
    /** Runas grabadas en el aro de esta cara. */
    val runas: String,
    val acento: Color,
    val metal: Color,
    val metalClaro: Color,
    val metalOscuro: Color,
    val campoClaro: Color,
    val campoOscuro: Color,
    /** false = el modo todavía no está implementado y abre la pantalla de «en la forja». */
    val disponible: Boolean
) {
    MUNDOS(
        titulo = "Mundos",
        subtitulo = "Los mundos de Yggdrasil",
        descripcion = "Cada mundo es un tema de Ingeniería de Software. Supera su Ragnarök para abrir el siguiente.",
        runas = "ᚠᚢᚦᚨᚱᚲᚷᚹᚺᚾᛁᛃ",
        acento = Color(0xFFF0B85A),
        metal = Color(0xFFC9A35B),
        metalClaro = Color(0xFFF3D894),
        metalOscuro = Color(0xFF6E5427),
        campoClaro = Color(0xFF3A3126),
        campoOscuro = Color(0xFF14110D),
        disponible = true
    ),
    HASTA_EL_ALBA(
        titulo = "Hasta el Alba",
        subtitulo = "El banquete eterno del Valhalla",
        descripcion = "Una noche por día: apuesta hidromiel, resiste siete oleadas y decide entre cobrar o seguir peleando.",
        runas = "ᛞᚨᚷᚨᛉᛖᛁᚾᚺᛖᚱᛁ",
        acento = Color(0xFFE8743B),
        metal = Color(0xFFC9A35B),
        metalClaro = Color(0xFFF6CF8A),
        metalOscuro = Color(0xFF6B3A1C),
        campoClaro = Color(0xFF7A2E1A),
        campoOscuro = Color(0xFF220B06),
        disponible = true
    ),
    APUESTA_BROKKR(
        titulo = "La Apuesta de Brokkr",
        subtitulo = "La forja de los enanos",
        descripcion = "Calienta, martilla y templa un artefacto de los dioses sin que la mosca de Loki te arruine el golpe.",
        runas = "ᛒᚱᛟᚲᚲᚱᛊᛁᚾᛞᚱᛁ",
        acento = Color(0xFFFF9A3C),
        metal = Color(0xFFB87333),
        metalClaro = Color(0xFFE8A86A),
        metalOscuro = Color(0xFF5A3518),
        campoClaro = Color(0xFF4A2C17),
        campoOscuro = Color(0xFF170D06),
        disponible = true
    ),
    VOLVA(
        titulo = "La Völva",
        subtitulo = "La vidente del seiðr",
        descripcion = "La bruja nórdica espera junto al fuego con sus runas y su báculo.",
        runas = "ᚹᛟᛚᚹᚨᛊᛖᛁᚦᚱᚢᚾ",
        acento = Color(0xFFB9A3E8),
        metal = Color(0xFFC9CED6),
        metalClaro = Color(0xFFF2F4F7),
        metalOscuro = Color(0xFF5D6470),
        campoClaro = Color(0xFF3B2E5A),
        campoOscuro = Color(0xFF120D1F),
        disponible = true
    ),
    POR_REVELAR(
        // La cuarta cara es el duelo de enigmas (CU-13); el nombre se conserva para no cambiar el orden ni las pruebas.
        titulo = "Duelo de enigmas",
        subtitulo = "El reto del gigante",
        descripcion = "Adivina el saber que el gigante esconde entre doce tarjetas: pregúntale y solo dirá sí o no.",
        runas = "ᛈᛖᚱᚦᚱᛟᚾᛟᚱᚾᚨ",
        acento = Color(0xFF9DB3C4),
        metal = Color(0xFF8A8070),
        metalClaro = Color(0xFFC4B9A6),
        metalOscuro = Color(0xFF3A3229),
        campoClaro = Color(0xFF2A2622),
        campoOscuro = Color(0xFF0E0C0A),
        disponible = true
    );

    companion object {
        /** Cara por nombre (para la ruta de navegación); si no existe, la de mundos. */
        fun porNombre(nombre: String?): CaraMoneda = entries.firstOrNull { it.name == nombre } ?: MUNDOS
    }
}

/**
 * Estado que vive mientras la app está abierta: la caída del cielo solo se ve
 * al abrir la app, no cada vez que se vuelve a Mundos.
 */
object EstadoMoneda {
    var entradaVista: Boolean = false
}
