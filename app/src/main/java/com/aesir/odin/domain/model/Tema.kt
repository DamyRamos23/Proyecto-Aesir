package com.aesir.odin.domain.model

/**
 * Representa un tema dentro de un mundo del roadmap (un nodo hijo del árbol
 * de habilidades). Corresponde al modelo `Tema` de la capa Negocio en el
 * diagrama de clases.
 *
 * [desbloqueado] y [completado] se conservan por compatibilidad, pero quien
 * decide si un tema se puede jugar es el MOD-01 (ModuloHidromiel).
 * [esRagnarok] marca el nivel de fin de mundo, que va en el centro del escudo.
 * [anillo]: 1 = lecciones, 2 = minijuegos (ver ArbolHabilidades).
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
    val actividad: Actividad = Actividad.LECCION
) {
    val esCrucigrama: Boolean get() = actividad == Actividad.CRUCIGRAMA
    val esMinijuego: Boolean get() = anillo == ArbolHabilidades.ANILLO_MINIJUEGOS
}
