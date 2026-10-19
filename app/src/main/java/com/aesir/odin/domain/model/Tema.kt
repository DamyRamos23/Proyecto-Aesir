package com.aesir.odin.domain.model

/**
 * Representa un tema dentro de un mundo del roadmap (un nodo hijo del árbol
 * de habilidades). Corresponde al modelo `Tema` de la capa Negocio en el
 * diagrama de clases.
 *
 * [desbloqueado] y [completado] se conservan por compatibilidad, pero quien
 * decide si un tema se puede jugar es el MOD-01 (ModuloHidromiel).
 * [esRagnarok] marca el nivel de fin de mundo, que va en el centro del escudo.
 * [anillo]: 1 = lecciones, 2 = minijuegos, 3 = retos (ver ArbolHabilidades).
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
    val conocido: Boolean = false
) {
    val esCrucigrama: Boolean get() = actividad == Actividad.CRUCIGRAMA
    val esMemorama: Boolean get() = actividad == Actividad.MEMORAMA
    val esFlashcards: Boolean get() = actividad == Actividad.FLASHCARDS
    val esArrastrar: Boolean get() = actividad == Actividad.ARRASTRAR

    /** Cualquier nivel que no es una lección: minijuegos (anillo 2) y retos (anillo 3). */
    val esMinijuego: Boolean get() = anillo >= ArbolHabilidades.ANILLO_MINIJUEGOS

    /** Nivel del tercer anillo (arrastrar y soltar y los que se definan después). */
    val esReto: Boolean get() = anillo == ArbolHabilidades.ANILLO_RETOS
}
