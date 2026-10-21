package com.aesir.odin.domain.model

/**
 * Modelos del CU-12 Retar a la bruja (La Völva), según el diagrama de clases
 * CU-12 y la versión 3.2 del SRS (RF-160 a RF-180).
 */

/** Las tres Nornas: cada una pide un tipo de visión distinto (paso 6). */
enum class Norna(val nombre: String, val tiempo: String, val tipoVision: String) {
    URD("Urd", "el pasado", "Encuentra el error"),
    VERDANDI("Verdandi", "el presente", "Identifica el caso"),
    SKULD("Skuld", "el futuro", "Predice la consecuencia")
}

/** Fe declarada antes de ver las opciones (RF-166) y sus puntos de destino (RF-169). */
enum class Fe(val nombre: String, val puntosAcierto: Int, val puntosFallo: Int) {
    CAUTA("Cauta", 1, -1),
    FIRME("Firme", 2, -2),
    TOTAL("Total", 4, -4)
}

/** Lectura del caldero según la suma de los puntos de destino (RF-172). */
enum class Lectura(val nombre: String) {
    PROFECIA_CUMPLIDA("Profecía cumplida"),
    LECTURA_CLARA("Lectura clara"),
    HUMO_DEL_CALDERO("Humo del caldero"),
    MALDICION("Maldición");

    companion object {
        fun dePuntos(puntos: Int): Lectura = when {
            puntos >= 14 -> PROFECIA_CUMPLIDA
            puntos >= 8 -> LECTURA_CLARA
            puntos >= 1 -> HUMO_DEL_CALDERO
            else -> MALDICION
        }
    }
}

/** Un caso de un tema superado, con tres opciones y una sola correcta. */
data class Vision(
    val id: String,
    val temaId: String,
    val norna: Norna,
    val caso: String,
    val opciones: List<String>,
    private val indiceCorrecta: Int,
    val pista: String,
    val explicacion: String
) {
    fun esCorrecta(indice: Int?): Boolean = indice == indiceCorrecta

    /** Solo para la bitácora (MOD-06): nunca se le muestra al alumno (RF-170). */
    fun opcionCorrecta(): String = opciones[indiceCorrecta]

    /** La misma visión con las opciones en otro orden, para que la posición no delate la correcta. */
    fun barajada(azar: kotlin.random.Random): Vision {
        val orden = opciones.indices.shuffled(azar)
        return copy(opciones = orden.map { opciones[it] }, indiceCorrecta = orden.indexOf(indiceCorrecta))
    }
}

/** [opcionElegida] es null cuando se acabaron los 15 segundos (A9.1). */
data class VisionRespondida(
    val visionId: String,
    val norna: Norna,
    val fe: Fe,
    val opcionElegida: Int?,
    val cumplida: Boolean,
    val puntos: Int
)

/** Consulta en curso: seis visiones (dos por Norna) y lo respondido. */
data class ConsultaVolva(
    val id: String,
    val apuesta: Int,
    val visiones: List<Vision>,
    val respondidas: List<VisionRespondida> = emptyList(),
    val fecha: Long
) {
    val indiceActual: Int get() = respondidas.size
    fun visionActual(): Vision? = visiones.getOrNull(indiceActual)
    fun puntosDestino(): Int = respondidas.sumOf { it.puntos }
    fun registrar(respuesta: VisionRespondida): ConsultaVolva = copy(respondidas = respondidas + respuesta)
    fun terminada(): Boolean = respondidas.size >= visiones.size

    companion object {
        const val TOTAL_VISIONES = 6
        const val VISIONES_POR_NORNA = 2
        const val SEGUNDOS_POR_VISION = 15
    }
}

/** Una de las 24 runas del Futhark antiguo, en el orden del alfabeto. */
data class RunaFuthark(val orden: Int, val simbolo: String, val nombre: String) {
    val id: String get() = "runa_$orden"

    companion object {
        val TODAS: List<RunaFuthark> = listOf(
            "ᚠ" to "Fehu", "ᚢ" to "Uruz", "ᚦ" to "Thurisaz", "ᚨ" to "Ansuz", "ᚱ" to "Raidho", "ᚲ" to "Kenaz",
            "ᚷ" to "Gebo", "ᚹ" to "Wunjo", "ᚺ" to "Hagalaz", "ᚾ" to "Naudiz", "ᛁ" to "Isa", "ᛃ" to "Jera",
            "ᛇ" to "Eihwaz", "ᛈ" to "Perthro", "ᛉ" to "Algiz", "ᛊ" to "Sowilo", "ᛏ" to "Tiwaz", "ᛒ" to "Berkano",
            "ᛖ" to "Ehwaz", "ᛗ" to "Mannaz", "ᛚ" to "Laguz", "ᛜ" to "Ingwaz", "ᛞ" to "Dagaz", "ᛟ" to "Othala"
        ).mapIndexed { i, (simbolo, nombre) -> RunaFuthark(i + 1, simbolo, nombre) }
    }
}

/** Runas del salón del alumno. */
data class SalonRunas(val obtenidas: List<RunaFuthark>) {
    /** Siguiente runa en el orden del alfabeto que aún no tiene (paso 14), o null si ya tiene las 24 (A14.1). */
    fun siguienteRuna(): RunaFuthark? = RunaFuthark.TODAS.firstOrNull { r -> obtenidas.none { it.orden == r.orden } }
    fun completo(): Boolean = obtenidas.size >= TOTAL_RUNAS

    companion object {
        const val TOTAL_RUNAS = 24
    }
}

/** Por qué no se puede apostar al abrir el caldero (E2.1 a E2.4). */
enum class MotivoBloqueoVolva {
    CALDERO_APAGADO,
    VALHALLA_CERRADO,
    HIDROMIEL_INSUFICIENTE,
    VISIONES_INSUFICIENTES
}

/** Rango de apuesta permitido (RF-163). */
data class RangoApuesta(val minimo: Int, val maximo: Int, val saldo: Int, val piso: Int) {
    val alcanza: Boolean get() = maximo >= minimo
    fun contiene(apuesta: Int): Boolean = apuesta in minimo..maximo
}

/** Lo que se muestra en «El caldero del destino» (paso 2). */
data class AccesoVolva(
    val motivo: MotivoBloqueoVolva?,
    val rango: RangoApuesta,
    val salon: SalonRunas,
    /** E2.3: cuánto hidromiel le falta para la apuesta mínima. */
    val faltante: Int = 0
) {
    val permitido: Boolean get() = motivo == null
}

/** Resultado registrado de una consulta (RF-180). */
data class ResultadoConsulta(
    val id: String,
    val apuesta: Int,
    val puntosDestino: Int,
    val visionesCumplidas: Int,
    val visionesErradas: Int,
    val lectura: Lectura?,
    val premioBruto: Int,
    val diezmo: Int,
    val pago: Int,
    val runaObtenida: RunaFuthark?,
    val abandonada: Boolean,
    val fecha: Long,
    val respondidas: List<VisionRespondida> = emptyList()
)
