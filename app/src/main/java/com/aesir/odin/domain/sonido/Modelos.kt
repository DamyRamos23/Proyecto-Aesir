package com.aesir.odin.domain.sonido

/**
 * MOD-02: categorías de efectos. [prioridad]: mayor gana cuando hay que callar
 * uno (Progreso, Respuesta, Valhalla, Hidromiel, Tiempo, Interfaz).
 */
enum class CategoriaSonido(val prioridad: Int, val duracionMaximaMs: Long) {
    PROGRESO(6, 5_000),
    RESPUESTA(5, 3_000),
    VALHALLA(4, 3_000),
    HIDROMIEL(3, 3_000),
    TIEMPO(2, 3_000),
    INTERFAZ(1, 3_000)
}

/**
 * Catálogo de efectos (RN-12: un evento siempre suena igual). [archivo] es el
 * nombre del recurso (res/raw en Android, Recursos/Sonidos en iOS).
 * [duracionMs] es lo que dura el archivo; nunca pasa de la duración máxima de
 * su categoría (el cuerno de Odín puede durar hasta 5 s).
 */
enum class EventoSonido(val categoria: CategoriaSonido, val archivo: String, val duracionMs: Long) {
    // Interfaz
    TOQUE(CategoriaSonido.INTERFAZ, "toque", 600),
    VOLTEO_CARTA(CategoriaSonido.INTERFAZ, "volteo_carta", 700),
    SOLTAR_PIEZA(CategoriaSonido.INTERFAZ, "soltar_pieza", 532),
    GIRO_MONEDA(CategoriaSonido.INTERFAZ, "giro_moneda", 1_300),
    CAIDA_MONEDA(CategoriaSonido.INTERFAZ, "caida_moneda", 859),

    // Respuesta (RN-14: el error es el mismo para cualquier respuesta incorrecta)
    ACIERTO(CategoriaSonido.RESPUESTA, "acierto", 1_600),
    ERROR(CategoriaSonido.RESPUESTA, "error", 950),
    INCOMPLETA(CategoriaSonido.RESPUESTA, "incompleta", 1_100),

    // Tiempo: un golpe de tambor por segundo en los últimos 5 s
    TAMBOR(CategoriaSonido.TIEMPO, "tambor", 750),

    // Hidromiel (MOD-01)
    HIDROMIEL_GANA(CategoriaSonido.HIDROMIEL, "cuerno_llena", 1_900),
    HIDROMIEL_PIERDE(CategoriaSonido.HIDROMIEL, "cuerno_vacia", 1_700),

    // Progreso
    DESBLOQUEO(CategoriaSonido.PROGRESO, "cuerno_guerra", 4_127),
    LOGRO(CategoriaSonido.PROGRESO, "coro_salon", 4_900),

    // Valhalla
    GOLPE_ENEMIGO(CategoriaSonido.VALHALLA, "golpe_enemigo", 1_300),
    ESCUDO_ROTO(CategoriaSonido.VALHALLA, "escudo_roto", 1_399),
    CUERNO_ODIN(CategoriaSonido.VALHALLA, "cuerno_odin", 4_900),
    MARTILLO_FORJA(CategoriaSonido.VALHALLA, "martillo", 1_509),
    MOSCA_LOKI(CategoriaSonido.VALHALLA, "mosca_loki", 1_900),
    CALDERO(CategoriaSonido.VALHALLA, "caldero", 2_950);

    val prioridad: Int get() = categoria.prioridad
}

/** Efecto de un resultado de respuesta (Respuesta: acierto, error o incompleta). */
fun eventoDeRespuesta(estado: com.aesir.odin.domain.model.EstadoRespuesta?): EventoSonido = when (estado) {
    com.aesir.odin.domain.model.EstadoRespuesta.CORRECTA -> EventoSonido.ACIERTO
    com.aesir.odin.domain.model.EstadoRespuesta.INCOMPLETA -> EventoSonido.INCOMPLETA
    else -> EventoSonido.ERROR
}

/** Preferencias de sonido del alumno (se cambian en Ajustes, RN-15). */
data class PreferenciasSonido(
    val activado: Boolean = true,
    /** Volumen de efectos, de 0 a 1 (80 % por defecto). */
    val volumen: Float = VOLUMEN_PREDETERMINADO,
    /** Tema principal de fondo (no está en el SRS: se agrega junto al MOD-02). */
    val musica: Boolean = true,
    val volumenMusica: Float = VOLUMEN_MUSICA_PREDETERMINADO
) {
    companion object {
        const val VOLUMEN_PREDETERMINADO = 0.8f
        const val VOLUMEN_MUSICA_PREDETERMINADO = 0.45f
    }
}

/** Dónde se guardan las preferencias de sonido. */
interface AlmacenPreferenciasSonido {
    fun leer(): PreferenciasSonido?
    fun guardar(preferencias: PreferenciasSonido): Boolean
}

/**
 * Pistas de música de fondo, una por tipo de pantalla. Cada una suena en bucle
 * (archivo «musica_<archivo>» en res/raw y en Recursos/Sonidos de iOS).
 */
enum class PistaMusica(val archivo: String) {
    /** Menú de selección: mundos, moneda, roadmap, ajustes, racha. */
    PRINCIPAL("principal"),
    /** Combate: Ragnarök y Hasta el Alba. */
    BATALLA("batalla"),
    /** Reto contrarreloj: más rápido, con reloj de madera. */
    CONTRARRELOJ("contrarreloj"),
    /** Lecciones, minijuegos, diagnóstico, repaso y duelo: tranquila, sin tambores. */
    LECCION("leccion"),
    /** La Völva: aura mística (cristal frotado, campanas). */
    VOLVA("volva")
}

/** Música de fondo en bucle (MediaPlayer en Android, AVAudioPlayer en iOS). */
interface ReproductorMusica {
    /** Cambia a [pista] con un fundido si ya sonaba otra; si es la misma, solo sigue. */
    fun reproducir(pista: PistaMusica, volumen: Float)
    fun pausar()
    fun cambiarVolumen(volumen: Float)
}

/**
 * Lo que sabe hacer el reproductor de la plataforma (SoundPool en Android,
 * AVAudioPlayer en iOS). [reproducir] devuelve un identificador de lo que está
 * sonando, o null si el efecto no se encontró o no se pudo reproducir (E2).
 */
interface ReproductorEfectos {
    fun cargar(catalogo: List<EventoSonido>)
    fun reproducir(evento: EventoSonido, volumen: Float, velocidad: Float = 1f): Int?
    fun detener(id: Int)
    fun detenerTodos()
}
