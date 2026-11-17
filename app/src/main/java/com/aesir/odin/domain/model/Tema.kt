package com.aesir.odin.domain.model

/**
 * Representa un nivel del roadmap (un nodo del árbol de habilidades) dentro
 * de un mundo. Corresponde al modelo `Tema` de la capa Negocio en el
 * diagrama de clases. Las lecciones son los temas del mundo; los minijuegos
 * y los retos pertenecen a uno de esos temas ([temaBaseId]).
 *
 * [desbloqueado] y [completado] se conservan por compatibilidad, pero quien
 * decide si un tema se puede jugar es el MOD-01 (ModuloHidromiel).
 * [esRagnarok] marca el nivel de fin de mundo, que va en el centro del escudo.
 * [anillo]: 1 = lecciones de los temas 1 a 4, 2 = minijuegos, 3 = retos de
 * arrastrar y soltar, 4 = lecciones de los temas 5 a 10 (ver ArbolHabilidades).
 * [actividad]: lo que se juega en el nivel.
 */
data class Tema(
    val id: String,
    val mundoId: String,
    val nombre: String,
    val descripcion: String,
    val orden: Int,
    val desbloqueado: Boolean,
    val completado: Boolean,
    val introduccionVista: Boolean,
    val esRagnarok: Boolean = false,
    val anillo: Int = ArbolHabilidades.ANILLO_LECCIONES,
    val actividad: Actividad = Actividad.LECCION,
    /** CU-10: reconocido como conocido por la evaluación diagnóstica. */
    val conocido: Boolean = false,
    /** Lección del tema al que pertenece el nivel; vacío en las lecciones (su tema son ellas). */
    val temaBaseId: String = ""
) {
    val esCrucigrama: Boolean get() = actividad == Actividad.CRUCIGRAMA
    val esMemorama: Boolean get() = actividad == Actividad.MEMORAMA
    val esFlashcards: Boolean get() = actividad == Actividad.FLASHCARDS
    val esArrastrar: Boolean get() = actividad == Actividad.ARRASTRAR

    /** Niveles que no son lecciones: minijuegos (anillo 2) y retos (anillo 3). */
    val esMinijuego: Boolean
        get() = anillo == ArbolHabilidades.ANILLO_MINIJUEGOS || anillo == ArbolHabilidades.ANILLO_RETOS

    /** Nivel del tercer anillo (arrastrar y soltar). */
    val esReto: Boolean get() = anillo == ArbolHabilidades.ANILLO_RETOS

    /** Lección de un tema del mundo (anillo 1 o 4). */
    val esLeccion: Boolean get() = !esRagnarok && !esMinijuego

    /** Id del tema (lección) al que pertenece el nivel; una lección es su propio tema. */
    val idTema: String get() = temaBaseId.ifEmpty { id }
}
