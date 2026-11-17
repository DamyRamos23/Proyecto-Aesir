package com.aesir.odin.data.local

import com.aesir.odin.data.local.entity.ElementoArrastrableEntity
import com.aesir.odin.data.local.entity.MundoEntity
import com.aesir.odin.data.local.entity.PosicionDestinoEntity
import com.aesir.odin.data.local.entity.RetoArrastrarEntity
import com.aesir.odin.data.local.entity.TemaEntity

/**
 * Tercer anillo del árbol (2 niveles por mundo) y contenido del CU-09.
 *
 * Por mundo: 2 retos de arrastrar y soltar, uno de ordenar un proceso y otro
 * de completar un diagrama. Van arriba y abajo del anillo, cada uno entre los
 * dos minijuegos del anillo 2 que tiene más cerca (ArbolHabilidades). Cada
 * reto pertenece al tema de su contenido ([Contenido.temaOrdenar] y
 * [Contenido.temaDiagrama]), que es la lección que se sugiere repasar.
 *
 * Los ids conservan el número de piedra que tenían cuando el anillo era de 8
 * (1 y 5) para no perder el progreso de quien ya los había superado.
 */
object DatosArrastrar {

    const val NIVELES_POR_MUNDO = 2

    /** (número del id, orden en el anillo, modalidad) de cada reto del tercer anillo. */
    private val POSICIONES_ARRASTRAR = listOf(
        Triple(1, 1, Modalidad.ORDENAR),
        Triple(5, 2, Modalidad.DIAGRAMA)
    )

    private enum class Modalidad { ORDENAR, DIAGRAMA }

    fun idNivel(mundoId: String, numero: Int) = "rt_${mundoId}_$numero"

    fun niveles(mundos: List<MundoEntity>): List<TemaEntity> = mundos.flatMap { mundo ->
        val contenido = CONTENIDO.getValue(mundo.id)
        POSICIONES_ARRASTRAR.map { (numero, orden, modalidad) ->
            val (nombre, descripcion, tema) = when (modalidad) {
                Modalidad.ORDENAR -> Triple("Ordenar el proceso", contenido.ordenar.descripcionNivel, contenido.temaOrdenar)
                Modalidad.DIAGRAMA -> Triple("Completar el diagrama", contenido.diagrama.descripcionNivel, contenido.temaDiagrama)
            }
            TemaEntity(
                id = idNivel(mundo.id, numero),
                mundoId = mundo.id,
                nombre = nombre,
                descripcion = descripcion,
                orden = orden,
                desbloqueado = false,
                introduccionVista = false,
                completado = false,
                tipo = "NORMAL",
                actividad = "ARRASTRAR",
                anillo = 3,
                temaBaseId = DatosIniciales.idTema(mundo.id, tema)
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

    /** [temaOrdenar] y [temaDiagrama]: número de tema (1 a 10) al que pertenece cada reto. */
    private class Contenido(val ordenar: Proceso, val diagrama: Diagrama, val temaOrdenar: Int, val temaDiagrama: Int)

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
            ),
            temaOrdenar = 1,
            temaDiagrama = 1
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
            ),
            temaOrdenar = 3,
            temaDiagrama = 2
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
            ),
            temaOrdenar = 1,
            temaDiagrama = 4
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
            ),
            temaOrdenar = 1,
            temaDiagrama = 3
        )
    )

    // ---------------------------------------------------------------------
    // Entidades
    // ---------------------------------------------------------------------

    private fun idReto(mundoId: String, numero: Int) = "reto_${mundoId}_$numero"

    val retos: List<RetoArrastrarEntity>
    val elementos: List<ElementoArrastrableEntity>
    val posiciones: List<PosicionDestinoEntity>

    init {
        val retos = mutableListOf<RetoArrastrarEntity>()
        val elementos = mutableListOf<ElementoArrastrableEntity>()
        val posiciones = mutableListOf<PosicionDestinoEntity>()
        for ((mundoId, contenido) in CONTENIDO) {
            for ((numero, _, modalidad) in POSICIONES_ARRASTRAR) {
                val retoId = idReto(mundoId, numero)
                val temaId = idNivel(mundoId, numero)
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
