package com.aesir.odin.data.local

import com.aesir.odin.data.local.entity.ElementoArrastrableEntity
import com.aesir.odin.data.local.entity.MundoEntity
import com.aesir.odin.data.local.entity.PosicionDestinoEntity
import com.aesir.odin.data.local.entity.RetoArrastrarEntity
import com.aesir.odin.data.local.entity.TemaEntity

/**
 * Tercer anillo del árbol (8 niveles por mundo) y contenido del CU-09.
 *
 * Por mundo: 2 retos de arrastrar y soltar (uno de ordenar un proceso y otro
 * de completar un diagrama) y 6 niveles "por definir" que se verán en el
 * escudo pero todavía no se pueden jugar. Los de arrastrar van en las
 * posiciones 1 y 5 del anillo (arriba y abajo), donde sus minijuegos
 * cercanos del anillo 2 son jugables (crucigrama y flashcards).
 */
object DatosArrastrar {

    const val NIVELES_POR_MUNDO = 8

    /** Posiciones (orden 1..8) del anillo 3 que son de arrastrar y soltar. */
    private val POSICIONES_ARRASTRAR = mapOf(1 to Modalidad.ORDENAR, 5 to Modalidad.DIAGRAMA)

    private enum class Modalidad { ORDENAR, DIAGRAMA }

    fun idNivel(mundoId: String, orden: Int) = "rt_${mundoId}_$orden"

    fun niveles(mundos: List<MundoEntity>): List<TemaEntity> = mundos.flatMap { mundo ->
        val contenido = CONTENIDO.getValue(mundo.id)
        (1..NIVELES_POR_MUNDO).map { orden ->
            val modalidad = POSICIONES_ARRASTRAR[orden]
            val (nombre, descripcion, actividad) = when (modalidad) {
                Modalidad.ORDENAR -> Triple("Ordenar el proceso", contenido.ordenar.descripcionNivel, "ARRASTRAR")
                Modalidad.DIAGRAMA -> Triple("Completar el diagrama", contenido.diagrama.descripcionNivel, "ARRASTRAR")
                null -> Triple("Reto por definir", "Un nuevo minijuego llegará a esta piedra rúnica.", "POR_DEFINIR")
            }
            TemaEntity(
                id = idNivel(mundo.id, orden),
                mundoId = mundo.id,
                nombre = nombre,
                descripcion = descripcion,
                orden = orden,
                desbloqueado = false,
                introduccionVista = false,
                completado = false,
                tipo = "NORMAL",
                actividad = actividad,
                anillo = 3
            )
        }
    }

    // ---------------------------------------------------------------------
    // Retos
    // ---------------------------------------------------------------------

    private class Proceso(val descripcionNivel: String, val instrucciones: String, val etapas: List<String>)

    /** Hueco del diagrama: texto correcto y centro (0..1). */
    private class Hueco(val texto: String, val x: Float, val y: Float)

    private class Diagrama(
        val descripcionNivel: String,
        val instrucciones: String,
        val imagen: String,
        val huecos: List<Hueco>
    )

    private class Contenido(val ordenar: Proceso, val diagrama: Diagrama)

    private val CONTENIDO: Map<String, Contenido> = mapOf(
        "mundo_uml" to Contenido(
            ordenar = Proceso(
                "Arrastra los pasos para modelar un diagrama de clases en el orden correcto.",
                "Ordena los pasos para modelar un diagrama de clases, del primero al último.",
                listOf(
                    "Identificar las clases del dominio",
                    "Definir los atributos de cada clase",
                    "Definir los métodos de cada clase",
                    "Establecer las relaciones entre clases",
                    "Indicar la multiplicidad de las relaciones"
                )
            ),
            diagrama = Diagrama(
                "Completa las partes de una clase UML y su relación con la clase padre.",
                "Coloca cada pieza en su lugar: los compartimentos de la clase Perro y la relación que la une con Animal.",
                "diagrama_clases",
                listOf(
                    Hueco("Nombre de la clase", 0.5f, 0.585f),
                    Hueco("Atributos", 0.5f, 0.725f),
                    Hueco("Métodos", 0.5f, 0.88f),
                    Hueco("Herencia", 0.76f, 0.41f)
                )
            )
        ),
        "mundo_casos_uso" to Contenido(
            ordenar = Proceso(
                "Arrastra los pasos para especificar un caso de uso en el orden correcto.",
                "Ordena los pasos para especificar un caso de uso, del primero al último.",
                listOf(
                    "Identificar al actor principal",
                    "Definir el objetivo del caso de uso",
                    "Escribir las precondiciones",
                    "Describir el flujo principal",
                    "Agregar los flujos alternativos",
                    "Definir las postcondiciones"
                )
            ),
            diagrama = Diagrama(
                "Completa un diagrama de casos de uso de un cajero automático.",
                "Coloca cada elemento en el diagrama del cajero: quién lo usa, qué hace y cómo se relacionan sus casos de uso.",
                "casos_uso",
                listOf(
                    Hueco("Actor", 0.16f, 0.68f),
                    Hueco("Límite del sistema", 0.66f, 0.1f),
                    Hueco("Caso de uso", 0.53f, 0.5f),
                    Hueco("«include»", 0.69f, 0.36f),
                    Hueco("«extend»", 0.69f, 0.66f)
                )
            )
        ),
        "mundo_ciclo_vida" to Contenido(
            ordenar = Proceso(
                "Arrastra las fases del ciclo de vida del software en el orden correcto.",
                "Ordena las fases del ciclo de vida del software, de la primera a la última.",
                listOf("Análisis de requisitos", "Diseño del sistema", "Implementación", "Pruebas", "Despliegue", "Mantenimiento")
            ),
            diagrama = Diagrama(
                "Completa la pirámide de pruebas, de la base a la punta.",
                "Coloca cada tipo de prueba en su nivel de la pirámide: abajo las más numerosas y rápidas, arriba las más amplias.",
                "piramide_pruebas",
                listOf(
                    Hueco("Pruebas de aceptación", 0.5f, 0.31f),
                    Hueco("Pruebas de sistema", 0.5f, 0.5f),
                    Hueco("Pruebas de integración", 0.5f, 0.69f),
                    Hueco("Pruebas unitarias", 0.5f, 0.87f)
                )
            )
        ),
        "mundo_modelos_trad" to Contenido(
            ordenar = Proceso(
                "Arrastra las fases del modelo en cascada en el orden correcto.",
                "Ordena las fases del modelo en cascada, de la primera a la última.",
                listOf("Requisitos", "Diseño", "Implementación", "Verificación", "Mantenimiento")
            ),
            diagrama = Diagrama(
                "Completa el modelo en V emparejando cada fase con sus pruebas.",
                "Coloca en el lado derecho de la V la prueba que verifica cada fase del lado izquierdo.",
                "modelo_v",
                listOf(
                    Hueco("Pruebas de aceptación", 0.79f, 0.14f),
                    Hueco("Pruebas de sistema", 0.75f, 0.34f),
                    Hueco("Pruebas de integración", 0.71f, 0.54f),
                    Hueco("Pruebas unitarias", 0.67f, 0.74f)
                )
            )
        )
    )

    // ---------------------------------------------------------------------
    // Entidades
    // ---------------------------------------------------------------------

    private fun idReto(mundoId: String, orden: Int) = "reto_${mundoId}_$orden"

    val retos: List<RetoArrastrarEntity>
    val elementos: List<ElementoArrastrableEntity>
    val posiciones: List<PosicionDestinoEntity>

    init {
        val retos = mutableListOf<RetoArrastrarEntity>()
        val elementos = mutableListOf<ElementoArrastrableEntity>()
        val posiciones = mutableListOf<PosicionDestinoEntity>()
        for ((mundoId, contenido) in CONTENIDO) {
            for ((orden, modalidad) in POSICIONES_ARRASTRAR) {
                val retoId = idReto(mundoId, orden)
                val temaId = idNivel(mundoId, orden)
                when (modalidad) {
                    Modalidad.ORDENAR -> {
                        val proceso = contenido.ordenar
                        retos += RetoArrastrarEntity(retoId, temaId, "ORDENAR_PROCESO", proceso.instrucciones, null, completado = false)
                        proceso.etapas.forEachIndexed { i, etapa ->
                            val posicionId = "${retoId}_p${i + 1}"
                            posiciones += PosicionDestinoEntity(posicionId, retoId, i + 1, etiqueta = null, x = null, y = null)
                            elementos += ElementoArrastrableEntity("${retoId}_e${i + 1}", retoId, etapa, posicionId)
                        }
                    }
                    Modalidad.DIAGRAMA -> {
                        val diagrama = contenido.diagrama
                        retos += RetoArrastrarEntity(retoId, temaId, "COMPLETAR_DIAGRAMA", diagrama.instrucciones, diagrama.imagen, completado = false)
                        diagrama.huecos.forEachIndexed { i, hueco ->
                            val posicionId = "${retoId}_p${i + 1}"
                            posiciones += PosicionDestinoEntity(posicionId, retoId, i + 1, etiqueta = null, x = hueco.x, y = hueco.y)
                            elementos += ElementoArrastrableEntity("${retoId}_e${i + 1}", retoId, hueco.texto, posicionId)
                        }
                    }
                }
            }
        }
        this.retos = retos
        this.elementos = elementos
        this.posiciones = posiciones
    }
}
