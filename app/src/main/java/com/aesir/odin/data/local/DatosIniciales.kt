package com.aesir.odin.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aesir.odin.data.local.converter.Converters
import com.aesir.odin.data.local.entity.EjercicioEntity
import com.aesir.odin.data.local.entity.LeccionEntity
import com.aesir.odin.data.local.entity.MundoEntity
import com.aesir.odin.data.local.entity.TemaEntity

/**
 * Contenido con el que se crea la base de datos la primera vez que se abre.
 * Se inserta dentro de onCreate, así que ya está disponible en la primera consulta.
 */
object DatosIniciales {

    private const val MUNDO_1 = "mundo_1"

    val mundos = listOf(
        MundoEntity(MUNDO_1, "Fundamentos", "Conceptos básicos de la Ingeniería de Software", 1, desbloqueado = true)
    )

    val temas = listOf(
        TemaEntity("tema_1", MUNDO_1, "¿Qué es la Ingeniería de Software?",
            "Introducción a los conceptos fundamentales de la disciplina.", 1,
            desbloqueado = true, introduccionVista = false, completado = false),
        TemaEntity("tema_2", MUNDO_1, "Ciclo de vida del software",
            "Modelos de proceso y metodologías de desarrollo.", 2,
            desbloqueado = false, introduccionVista = false, completado = false),
        TemaEntity("tema_3", MUNDO_1, "Ingeniería de requerimientos",
            "Técnicas para la obtención y especificación de requerimientos.", 3,
            desbloqueado = false, introduccionVista = false, completado = false)
    )

    val lecciones = listOf(
        LeccionEntity("leccion_1_1", "tema_1", "Fundamentos de la Ingeniería de Software", 1, completada = false),
        LeccionEntity("leccion_2_1", "tema_2", "Modelos de proceso", 1, completada = false),
        LeccionEntity("leccion_3_1", "tema_3", "Tipos de requerimientos", 1, completada = false)
    )

    val ejercicios = listOf(
        // Tema 1
        EjercicioEntity("ej_1_1_1", "leccion_1_1",
            "¿Cuál es el objetivo principal de la Ingeniería de Software?",
            listOf(
                "Escribir código lo más rápido posible",
                "Desarrollar software de calidad de forma sistemática, disciplinada y medible",
                "Diseñar el hardware en el que corre el software",
                "Reemplazar a los usuarios en la toma de decisiones"
            ), listOf(1), 1),
        EjercicioEntity("ej_1_1_2", "leccion_1_1",
            "La llamada \"crisis del software\" se refiere a:",
            listOf(
                "La falta de computadoras en los años 60",
                "Proyectos que se entregaban tarde, excedían el presupuesto y tenían baja calidad",
                "La desaparición de los lenguajes de bajo nivel",
                "El aumento del precio de las licencias"
            ), listOf(1), 2),
        EjercicioEntity("ej_1_1_3", "leccion_1_1",
            "Selecciona las características de calidad del software (ISO/IEC 25010):",
            listOf(
                "Mantenibilidad",
                "Color de la interfaz",
                "Seguridad",
                "Usabilidad"
            ), listOf(0, 2, 3), 3),
        EjercicioEntity("ej_1_1_4", "leccion_1_1",
            "¿Qué diferencia a la Ingeniería de Software de la programación?",
            listOf(
                "No hay diferencia, son sinónimos",
                "La programación incluye la gestión del proyecto y la programación no",
                "La Ingeniería de Software abarca todo el ciclo de vida: requerimientos, diseño, construcción, pruebas y mantenimiento",
                "La Ingeniería de Software solo se ocupa de la documentación"
            ), listOf(2), 4),
        EjercicioEntity("ej_1_1_5", "leccion_1_1",
            "¿Cuáles son productos de trabajo (artefactos) de un proyecto de software?",
            listOf(
                "Especificación de requerimientos",
                "Diagramas de diseño",
                "Casos de prueba",
                "El monitor del programador"
            ), listOf(0, 1, 2), 5),

        // Tema 2
        EjercicioEntity("ej_2_1_1", "leccion_2_1",
            "En el modelo en cascada, las fases:",
            listOf(
                "Se ejecutan en paralelo",
                "Se ejecutan de forma secuencial, una después de otra",
                "Se repiten en iteraciones de dos semanas",
                "No tienen un orden definido"
            ), listOf(1), 1),
        EjercicioEntity("ej_2_1_2", "leccion_2_1",
            "¿Qué modelo de proceso entrega el software en incrementos funcionales?",
            listOf("Cascada", "Incremental", "Código y corrección", "Big bang"),
            listOf(1), 2),
        EjercicioEntity("ej_2_1_3", "leccion_2_1",
            "Selecciona los eventos de Scrum:",
            listOf("Sprint Planning", "Daily Scrum", "Diagrama de Gantt", "Sprint Retrospective"),
            listOf(0, 1, 3), 3),

        // Tema 3
        EjercicioEntity("ej_3_1_1", "leccion_3_1",
            "\"El sistema deberá responder en menos de 2 segundos\" es un requerimiento:",
            listOf("Funcional", "No funcional", "De negocio", "De hardware"),
            listOf(1), 1),
        EjercicioEntity("ej_3_1_2", "leccion_3_1",
            "\"El sistema deberá permitir al estudiante iniciar una lección\" es un requerimiento:",
            listOf("Funcional", "No funcional", "De rendimiento", "De portabilidad"),
            listOf(0), 2),
        EjercicioEntity("ej_3_1_3", "leccion_3_1",
            "Selecciona técnicas de obtención de requerimientos:",
            listOf("Entrevistas", "Compilación", "Cuestionarios", "Prototipos"),
            listOf(0, 2, 3), 3)
    )

    fun insertar(db: SupportSQLiteDatabase) {
        val converters = Converters()
        mundos.forEach { mundo ->
            db.insert("mundos", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", mundo.id)
                put("nombre", mundo.nombre)
                put("descripcion", mundo.descripcion)
                put("orden", mundo.orden)
                put("desbloqueado", mundo.desbloqueado)
            })
        }
        temas.forEach { tema ->
            db.insert("temas", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", tema.id)
                put("mundoId", tema.mundoId)
                put("nombre", tema.nombre)
                put("descripcion", tema.descripcion)
                put("orden", tema.orden)
                put("desbloqueado", tema.desbloqueado)
                put("introduccionVista", tema.introduccionVista)
                put("completado", tema.completado)
            })
        }
        lecciones.forEach { leccion ->
            db.insert("lecciones", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", leccion.id)
                put("temaId", leccion.temaId)
                put("titulo", leccion.titulo)
                put("orden", leccion.orden)
                put("completada", leccion.completada)
            })
        }
        ejercicios.forEach { ejercicio ->
            db.insert("ejercicios", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", ejercicio.id)
                put("leccionId", ejercicio.leccionId)
                put("enunciado", ejercicio.enunciado)
                put("opciones", converters.fromStringList(ejercicio.opciones))
                put("respuestasCorrectas", converters.fromIntList(ejercicio.respuestasCorrectas))
                put("orden", ejercicio.orden)
            })
        }
    }
}
