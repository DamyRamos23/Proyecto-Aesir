package com.aesir.odin.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aesir.odin.data.local.converter.Converters
import com.aesir.odin.data.local.entity.ArtefactoEntity
import com.aesir.odin.data.local.entity.ProblemaMartillarEntity
import com.aesir.odin.data.local.entity.TempladoEntity

/**
 * Contenido semilla del CU-02 Forjar artefactos en la apuesta de Brokkr.
 *
 * - Un artefacto por mundo, en este orden: mundo 1 Mjölnir, mundo 2 Gungnir,
 *   mundo 3 Draupnir y mundo 4 Skíðblaðnir, con su poder en Hasta el Alba en
 *   cada calidad (CU-01).
 * - Por mundo, problemas de diseño para «Martillar» y definiciones
 *   incompletas para «Templar». «Calentar el metal» reutiliza el reto de
 *   ordenar un proceso del CU-09 (DatosArrastrar).
 *
 * Es contenido fijo: se inserta (o actualiza) cada vez que se abre la base,
 * igual que el banco de preguntas, sin tocar la vitrina ni las forjas.
 */
object DatosForja {

    private const val UML = "mundo_uml"
    private const val CASOS_USO = "mundo_casos_uso"
    private const val CICLO_VIDA = "mundo_ciclo_vida"
    private const val MODELOS = "mundo_modelos_trad"

    val artefactos: List<ArtefactoEntity> = listOf(
        ArtefactoEntity(
            "art_mjolnir", UML, "Mjölnir",
            poderPerfecto = "Derriba a todos los enemigos de una oleada, una vez por noche.",
            poderConDefecto = "Derriba a la mitad de los enemigos de una oleada, una vez por noche."
        ),
        ArtefactoEntity(
            "art_gungnir", CASOS_USO, "Gungnir",
            poderPerfecto = "La lanza nunca falla: responde bien una pregunta, una vez por noche.",
            poderConDefecto = "Descarta dos opciones incorrectas de una pregunta, una vez por noche."
        ),
        ArtefactoEntity(
            "art_draupnir", CICLO_VIDA, "Draupnir",
            poderPerfecto = "Duplica el hidromiel que ganas durante una noche.",
            poderConDefecto = "Aumenta un 50 % el hidromiel que ganas durante una noche."
        ),
        ArtefactoEntity(
            "art_skidbladnir", MODELOS, "Skíðblaðnir",
            poderPerfecto = "Navega hasta el alba: salta la última oleada de la noche.",
            poderConDefecto = "Acorta a la mitad la última oleada de la noche."
        )
    )

    val problemas: List<ProblemaMartillarEntity> = listOf(
        // ---- Mundo 1: UML ----
        ProblemaMartillarEntity(
            "pm_uml_1", UML,
            "Tu sistema debe crear distintos tipos de documento sin que el cliente conozca la clase concreta. ¿Qué patrón ayuda a encapsular la creación de objetos?",
            listOf("Factory Method", "Observer", "Singleton", "Adapter"), "0"
        ),
        ProblemaMartillarEntity(
            "pm_uml_2", UML,
            "Un Pedido está formado por Líneas de pedido que no existen sin él: si se borra el pedido, se borran sus líneas. ¿Qué relación UML usas?",
            listOf("Agregación", "Composición", "Dependencia", "Generalización"), "1"
        ),
        // ---- Mundo 2: Casos de Uso ----
        ProblemaMartillarEntity(
            "pm_cu_1", CASOS_USO,
            "Cada vez que el cliente quiere «Pagar en línea», el sistema siempre debe «Validar tarjeta». ¿Qué relación une ambos casos de uso?",
            listOf("extend", "Generalización", "include", "Asociación"), "2"
        ),
        ProblemaMartillarEntity(
            "pm_cu_2", CASOS_USO,
            "«Aplicar cupón» solo ocurre a veces, cuando el cliente tiene uno, durante «Comprar». ¿Qué relación usas?",
            listOf("include", "extend", "Composición", "Dependencia"), "1"
        ),
        // ---- Mundo 3: Ciclo de Vida ----
        ProblemaMartillarEntity(
            "pm_cv_1", CICLO_VIDA,
            "Ya sabes qué debe hacer el sistema; antes de programar necesitas definir su arquitectura y sus componentes. ¿En qué fase estás?",
            listOf("Análisis de requisitos", "Implementación", "Diseño del sistema", "Mantenimiento"), "2"
        ),
        ProblemaMartillarEntity(
            "pm_cv_2", CICLO_VIDA,
            "Tras la entrega, el cliente reporta un error y pide una función nueva. ¿Qué fase del ciclo de vida atiende esto?",
            listOf("Mantenimiento", "Pruebas", "Diseño del sistema", "Análisis de requisitos"), "0"
        ),
        // ---- Mundo 4: Modelos Tradicionales ----
        ProblemaMartillarEntity(
            "pm_mt_1", MODELOS,
            "Los requisitos están muy claros, son estables y no cambiarán durante el proyecto. ¿Qué modelo conviene?",
            listOf("Espiral", "Prototipos", "Incremental", "Cascada"), "3"
        ),
        ProblemaMartillarEntity(
            "pm_mt_2", MODELOS,
            "El proyecto es grande y tiene riesgos técnicos que hay que evaluar y reducir en cada vuelta. ¿Qué modelo eliges?",
            listOf("Espiral", "Cascada", "Modelo en V", "Incremental"), "0"
        )
    )

    val templados: List<TempladoEntity> = listOf(
        // ---- Mundo 1: UML ----
        TempladoEntity(
            "tp_uml_1", UML, "DEFINICION",
            "Completa la definición del patrón antes de que se enfríe el metal.",
            "Una clase que define una interfaz para crear un objeto, y sus subclases deciden qué clase instanciar: {h1}.",
            listOf("Factory Method"),
            listOf("Factory Method", "Singleton", "Observer", "Composite")
        ),
        TempladoEntity(
            "tp_uml_2", UML, "DEFINICION",
            "Completa la definición del diagrama de secuencia.",
            "Cada participante tiene una {h1} que muestra su existencia en el tiempo, y los mensajes entre participantes se ordenan de {h2}.",
            listOf("línea de vida", "arriba hacia abajo"),
            listOf("línea de vida", "arriba hacia abajo", "izquierda a derecha", "clase abstracta")
        ),
        // ---- Mundo 2: Casos de Uso ----
        TempladoEntity(
            "tp_cu_1", CASOS_USO, "DEFINICION",
            "Completa la especificación del caso de uso.",
            "La {h1} describe lo que debe cumplirse antes de iniciar el caso de uso, y la {h2} lo que queda garantizado al terminar.",
            listOf("precondición", "postcondición"),
            listOf("precondición", "postcondición", "relación include", "interfaz")
        ),
        TempladoEntity(
            "tp_cu_2", CASOS_USO, "DEFINICION",
            "Completa la definición de los elementos del modelo.",
            "Un {h1} es un rol externo que interactúa con el sistema, y el {h2} separa lo que hace el sistema de su entorno.",
            listOf("actor", "límite del sistema"),
            listOf("actor", "límite del sistema", "método", "flujo alterno")
        ),
        // ---- Mundo 3: Ciclo de Vida ----
        TempladoEntity(
            "tp_cv_1", CICLO_VIDA, "DEFINICION",
            "Completa la descripción de las fases.",
            "El {h1} comprende y documenta qué debe hacer el sistema, y la {h2} traduce el diseño a código fuente.",
            listOf("análisis de requisitos", "implementación"),
            listOf("análisis de requisitos", "implementación", "mantenimiento", "retiro")
        ),
        TempladoEntity(
            "tp_cv_2", CICLO_VIDA, "DEFINICION",
            "Completa la definición.",
            "Las {h1} verifican la calidad del software y detectan defectos antes de entregarlo al cliente.",
            listOf("pruebas"),
            listOf("pruebas", "entrevistas", "licencias", "estimaciones")
        ),
        // ---- Mundo 4: Modelos Tradicionales ----
        TempladoEntity(
            "tp_mt_1", MODELOS, "DEFINICION",
            "Completa la definición del modelo en V.",
            "En el modelo en V, las pruebas {h1} verifican el diseño detallado y las pruebas de {h2} validan los requisitos del cliente.",
            listOf("unitarias", "aceptación"),
            listOf("unitarias", "aceptación", "carga", "humo")
        ),
        TempladoEntity(
            "tp_mt_2", MODELOS, "DEFINICION",
            "Completa la definición del modelo.",
            "El modelo {h1} construye el sistema por partes: cada versión agrega funcionalidad a la anterior.",
            listOf("incremental"),
            listOf("incremental", "en cascada", "en espiral", "en V")
        )
    )

    fun insertar(db: SupportSQLiteDatabase) {
        val converters = Converters()
        artefactos.forEach { a ->
            db.insert("artefactos", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", a.id)
                put("mundoId", a.mundoId)
                put("nombre", a.nombre)
                put("poderPerfecto", a.poderPerfecto)
                put("poderConDefecto", a.poderConDefecto)
            })
        }
        problemas.forEach { p ->
            db.insert("problemas_martillar", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", p.id)
                put("mundoId", p.mundoId)
                put("enunciado", p.enunciado)
                put("piezas", converters.fromStringList(p.piezas))
                put("piezaCorrectaId", p.piezaCorrectaId)
            })
        }
        templados.forEach { t ->
            db.insert("templados", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", t.id)
                put("mundoId", t.mundoId)
                put("tipo", t.tipo)
                put("enunciado", t.enunciado)
                put("plantilla", t.plantilla)
                put("respuestas", converters.fromStringList(t.respuestas))
                put("opciones", converters.fromStringList(t.opciones))
            })
        }
    }
}
