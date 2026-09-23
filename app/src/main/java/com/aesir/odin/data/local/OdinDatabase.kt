package com.aesir.odin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aesir.odin.data.local.dao.MundoDao
import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.local.entity.MundoEntity
import com.aesir.odin.data.local.entity.TemaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos local de ODÍN. No aparece explícitamente en los diagramas
 * (que solo muestran los DAOs), pero es necesaria para que Room pueda
 * generar las implementaciones de MundoDao y TemaDao.
 *
 * La primera vez que se crea, se siembra con un roadmap de ejemplo sobre
 * Ingeniería de Software para poder probar el flujo de CU-01 de punta a punta.
 */
@Database(entities = [MundoEntity::class, TemaEntity::class], version = 1, exportSchema = false)
abstract class OdinDatabase : RoomDatabase() {

    abstract fun mundoDao(): MundoDao
    abstract fun temaDao(): TemaDao

    companion object {
        @Volatile
        private var INSTANCE: OdinDatabase? = null

        fun getInstance(context: Context): OdinDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OdinDatabase::class.java,
                    "odin.db"
                )
                    .addCallback(seedCallback)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val seedCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        sembrarDatosDeEjemplo(database)
                    }
                }
            }
        }

        private suspend fun sembrarDatosDeEjemplo(database: OdinDatabase) {
            val mundos = listOf(
                MundoEntity(
                    id = "mundo-fundamentos",
                    nombre = "Fundamentos",
                    descripcion = "Los cimientos de la Ingeniería de Software: qué es, por qué importa y cómo se organiza el trabajo.",
                    orden = 1,
                    desbloqueado = true
                ),
                MundoEntity(
                    id = "mundo-requerimientos",
                    nombre = "Requerimientos",
                    descripcion = "Cómo se descubren, documentan y priorizan las necesidades de un proyecto de software.",
                    orden = 2,
                    desbloqueado = false
                ),
                MundoEntity(
                    id = "mundo-diseno",
                    nombre = "Diseño y Arquitectura",
                    descripcion = "Principios y patrones para estructurar sistemas de software mantenibles.",
                    orden = 3,
                    desbloqueado = false
                ),
                MundoEntity(
                    id = "mundo-calidad",
                    nombre = "Pruebas y Calidad",
                    descripcion = "Técnicas para verificar que el software funciona como se espera.",
                    orden = 4,
                    desbloqueado = false
                )
            )

            val temas = listOf(
                // Fundamentos
                TemaEntity(
                    id = "tema-que-es-is",
                    mundoId = "mundo-fundamentos",
                    nombre = "¿Qué es la Ingeniería de Software?",
                    descripcion = "Definición, objetivos y diferencias con la programación tradicional.",
                    orden = 1,
                    desbloqueado = true,
                    completado = false,
                    introduccionVista = false
                ),
                TemaEntity(
                    id = "tema-ciclo-vida",
                    mundoId = "mundo-fundamentos",
                    nombre = "Ciclo de vida del software",
                    descripcion = "Las fases por las que pasa un proyecto, de la idea al mantenimiento.",
                    orden = 2,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                TemaEntity(
                    id = "tema-modelos-proceso",
                    mundoId = "mundo-fundamentos",
                    nombre = "Modelos de proceso",
                    descripcion = "Cascada, iterativo e incremental, y metodologías ágiles.",
                    orden = 3,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                // Requerimientos
                TemaEntity(
                    id = "tema-tipos-requerimientos",
                    mundoId = "mundo-requerimientos",
                    nombre = "Tipos de requerimientos",
                    descripcion = "Funcionales, no funcionales y de negocio.",
                    orden = 1,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                TemaEntity(
                    id = "tema-elicitacion",
                    mundoId = "mundo-requerimientos",
                    nombre = "Técnicas de elicitación",
                    descripcion = "Entrevistas, encuestas y talleres para descubrir necesidades.",
                    orden = 2,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                TemaEntity(
                    id = "tema-especificacion",
                    mundoId = "mundo-requerimientos",
                    nombre = "Especificación de requerimientos",
                    descripcion = "Cómo documentar requerimientos de forma clara y verificable.",
                    orden = 3,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                // Diseño y Arquitectura
                TemaEntity(
                    id = "tema-principios-diseno",
                    mundoId = "mundo-diseno",
                    nombre = "Principios de diseño",
                    descripcion = "Acoplamiento, cohesión y los principios SOLID.",
                    orden = 1,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                TemaEntity(
                    id = "tema-patrones",
                    mundoId = "mundo-diseno",
                    nombre = "Patrones de diseño",
                    descripcion = "Soluciones reutilizables a problemas comunes de diseño.",
                    orden = 2,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                TemaEntity(
                    id = "tema-arquitectura",
                    mundoId = "mundo-diseno",
                    nombre = "Estilos de arquitectura",
                    descripcion = "MVC, MVVM, arquitectura en capas y microservicios.",
                    orden = 3,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                // Pruebas y Calidad
                TemaEntity(
                    id = "tema-tipos-pruebas",
                    mundoId = "mundo-calidad",
                    nombre = "Tipos de pruebas",
                    descripcion = "Unitarias, de integración y de aceptación.",
                    orden = 1,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                TemaEntity(
                    id = "tema-tdd",
                    mundoId = "mundo-calidad",
                    nombre = "Desarrollo guiado por pruebas",
                    descripcion = "El ciclo rojo-verde-refactor.",
                    orden = 2,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                ),
                TemaEntity(
                    id = "tema-metricas-calidad",
                    mundoId = "mundo-calidad",
                    nombre = "Métricas de calidad",
                    descripcion = "Cómo medir qué tan bueno es un software.",
                    orden = 3,
                    desbloqueado = false,
                    completado = false,
                    introduccionVista = false
                )
            )

            database.mundoDao().insertAll(mundos)
            database.temaDao().insertAll(temas)
        }
    }
}
