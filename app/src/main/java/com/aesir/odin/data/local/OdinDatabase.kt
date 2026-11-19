package com.aesir.odin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aesir.odin.data.local.converter.Converters
import com.aesir.odin.data.local.dao.ConceptoCrucigramaDao
import com.aesir.odin.data.local.dao.DiagnosticoDao
import com.aesir.odin.data.local.dao.EjercicioDao
import com.aesir.odin.data.local.dao.HidromielDao
import com.aesir.odin.data.local.dao.HistorialErrorDao
import com.aesir.odin.data.local.dao.LeccionDao
import com.aesir.odin.data.local.dao.ElementoArrastrableDao
import com.aesir.odin.data.local.dao.MemoramaDao
import com.aesir.odin.data.local.dao.PosicionDestinoDao
import com.aesir.odin.data.local.dao.ResultadoFlashcardsDao
import com.aesir.odin.data.local.dao.RetoDao
import com.aesir.odin.data.local.dao.RetoFinMundoDao
import com.aesir.odin.data.local.dao.MundoDao
import com.aesir.odin.data.local.dao.PreguntaFlashBancoDao
import com.aesir.odin.data.local.dao.PreguntaFlashDao
import com.aesir.odin.data.local.dao.ResultadoCrucigramaDao
import com.aesir.odin.data.local.dao.ResultadoContrarrelojDao
import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.local.dao.VolvaDao
import com.aesir.odin.data.local.dao.AlbaDao
import com.aesir.odin.data.local.dao.ArtefactoDao
import com.aesir.odin.data.local.dao.EtapaForjaDao
import com.aesir.odin.data.local.dao.ForjaDao
import com.aesir.odin.data.local.dao.VitrinaDao
import com.aesir.odin.data.local.dao.RachaDao
import com.aesir.odin.data.local.entity.RachaEntity
import com.aesir.odin.data.local.entity.HitoRachaEntity
import com.aesir.odin.data.local.entity.ArtefactoEntity
import com.aesir.odin.data.local.entity.ForjaEntity
import com.aesir.odin.data.local.entity.ProblemaMartillarEntity
import com.aesir.odin.data.local.entity.TempladoEntity
import com.aesir.odin.data.local.entity.VitrinaArtefactoEntity
import com.aesir.odin.data.local.entity.ConceptoCrucigramaEntity
import com.aesir.odin.data.local.entity.EjercicioEntity
import com.aesir.odin.data.local.entity.HistorialErrorEntity
import com.aesir.odin.data.local.entity.LeccionEntity
import com.aesir.odin.data.local.entity.MovimientoHidromielEntity
import com.aesir.odin.data.local.entity.ElementoArrastrableEntity
import com.aesir.odin.data.local.entity.MundoDesbloqueadoEntity
import com.aesir.odin.data.local.entity.PosicionDestinoEntity
import com.aesir.odin.data.local.entity.ResultadoFlashcardsEntity
import com.aesir.odin.data.local.entity.ResultadoRetoEntity
import com.aesir.odin.data.local.entity.ResultadoRetoFinMundoEntity
import com.aesir.odin.data.local.entity.RetoArrastrarEntity
import com.aesir.odin.data.local.entity.MundoEntity
import com.aesir.odin.data.local.entity.NivelSuperadoEntity
import com.aesir.odin.data.local.entity.PreguntaFlashAttemptEntity
import com.aesir.odin.data.local.entity.PreguntaFlashEntity
import com.aesir.odin.data.local.entity.ParConceptoEntity
import com.aesir.odin.data.local.entity.PreguntaDiagnosticaEntity
import com.aesir.odin.data.local.entity.ReservaHidromielEntity
import com.aesir.odin.data.local.entity.ResultadoCrucigramaEntity
import com.aesir.odin.data.local.entity.ResultadoDiagnosticoEntity
import com.aesir.odin.data.local.entity.ResultadoLeccionEntity
import com.aesir.odin.data.local.entity.ResultadoContrarrelojEntity
import com.aesir.odin.data.local.entity.ResultadoMemoramaEntity
import com.aesir.odin.data.local.entity.TemaEntity
import com.aesir.odin.data.local.entity.ConceptoEnigmaEntity
import com.aesir.odin.data.local.entity.PreguntaEnigmaEntity
import com.aesir.odin.data.local.entity.ResultadoDueloEntity
import com.aesir.odin.data.local.dao.ConceptoEnigmaDao
import com.aesir.odin.data.local.dao.PreguntaEnigmaDao
import com.aesir.odin.data.local.dao.ResultadoDueloDao
import androidx.room.migration.Migration
import com.aesir.odin.data.local.entity.ConsultaVolvaEntity
import com.aesir.odin.data.local.entity.NocheAlbaEntity
import com.aesir.odin.data.local.entity.ArtefactoForjadoEntity
import com.aesir.odin.data.local.entity.LogroEntity
import com.aesir.odin.data.local.entity.RunaObtenidaEntity
import com.aesir.odin.data.local.entity.VisionEntity

@Database(
    entities = [
        MundoEntity::class,
        TemaEntity::class,
        LeccionEntity::class,
        EjercicioEntity::class,
        ResultadoLeccionEntity::class,
        HistorialErrorEntity::class,
        ReservaHidromielEntity::class,
        MovimientoHidromielEntity::class,
        NivelSuperadoEntity::class,
        MundoDesbloqueadoEntity::class,
        ConceptoCrucigramaEntity::class,
        ResultadoCrucigramaEntity::class,
        PreguntaFlashEntity::class,
        PreguntaFlashAttemptEntity::class,
        ResultadoContrarrelojEntity::class,
        ParConceptoEntity::class,
        ResultadoMemoramaEntity::class,
        PreguntaDiagnosticaEntity::class,
        ResultadoDiagnosticoEntity::class,
        ResultadoFlashcardsEntity::class,
        ResultadoRetoFinMundoEntity::class,
        RetoArrastrarEntity::class,
        ElementoArrastrableEntity::class,
        PosicionDestinoEntity::class,
        ResultadoRetoEntity::class,
        ConceptoEnigmaEntity::class,
        PreguntaEnigmaEntity::class,
        ResultadoDueloEntity::class,
        VisionEntity::class,
        RunaObtenidaEntity::class,
        ConsultaVolvaEntity::class,
        NocheAlbaEntity::class,
        ArtefactoForjadoEntity::class,
        LogroEntity::class,
        ArtefactoEntity::class,
        VitrinaArtefactoEntity::class,
        ForjaEntity::class,
        ProblemaMartillarEntity::class,
        TempladoEntity::class,
        RachaEntity::class,
        HitoRachaEntity::class
    ],
    // v3: MOD-01 Sistema de Hidromiel (tablas nuevas y columna temas.tipo)
    // v4: CU-07 crucigrama (conceptos, resultados y columna temas.actividad)
    // v5: árbol de dos anillos (columna temas.anillo y minijuegos por mundo)
    // v6: CU-06 pregunta flash (banco de preguntas e historial de intentos)
    // v7: CU-11 reto contrarreloj (resultados del reto)
    // v8: CU-04 memorama (pares y resultados), CU-10 diagnóstico (preguntas,
    //     resultados) y columna temas.conocido
    // v9: tercer anillo, CU-03 flashcards, CU-08 Ragnarök y CU-09 arrastrar y soltar
    // v10: CU-13 duelo de enigmas (conceptos, preguntas y duelos). Con migración:
    //      solo crea las tablas nuevas y conserva el progreso y el hidromiel.
    // v11: CU-12 Retar a la bruja (visiones, runas obtenidas y consultas). Con
    //      migración: solo crea las tablas nuevas.
    // v12: CU-01 Hasta el Alba (noches, vitrina de artefactos y logros). Con
    //      migración: solo crea las tablas nuevas.
    // v13: CU-02 forja de Brokkr (artefactos, vitrina, forjas, problemas y templados)
    //      y MOD-05 racha (rachas e hitos cobrados). Con migración: solo crea las
    //      tablas nuevas y conserva el progreso y el hidromiel.
    // v14: roadmap de cuatro anillos del SRS v3.2 (18 niveles y 10 temas por
    //      mundo). Con migración: agrega la columna temas.temaBaseId; los
    //      temas nuevos y el contenido se siembran en onOpen sin tocar el progreso.
    version = 14,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OdinDatabase : RoomDatabase() {

    abstract fun mundoDao(): MundoDao
    abstract fun temaDao(): TemaDao
    abstract fun leccionDao(): LeccionDao
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun historialErrorDao(): HistorialErrorDao
    abstract fun hidromielDao(): HidromielDao
    abstract fun conceptoCrucigramaDao(): ConceptoCrucigramaDao
    abstract fun resultadoCrucigramaDao(): ResultadoCrucigramaDao
    abstract fun preguntaFlashBancoDao(): PreguntaFlashBancoDao
    abstract fun preguntaFlashDao(): PreguntaFlashDao
    abstract fun resultadoContrarrelojDao(): ResultadoContrarrelojDao
    abstract fun memoramaDao(): MemoramaDao
    abstract fun diagnosticoDao(): DiagnosticoDao
    abstract fun resultadoFlashcardsDao(): ResultadoFlashcardsDao
    abstract fun retoFinMundoDao(): RetoFinMundoDao
    abstract fun retoDao(): RetoDao
    abstract fun elementoArrastrableDao(): ElementoArrastrableDao
    abstract fun posicionDestinoDao(): PosicionDestinoDao
    abstract fun conceptoEnigmaDao(): ConceptoEnigmaDao
    abstract fun preguntaEnigmaDao(): PreguntaEnigmaDao
    abstract fun resultadoDueloDao(): ResultadoDueloDao
    abstract fun volvaDao(): VolvaDao
    abstract fun albaDao(): AlbaDao
    abstract fun artefactoDao(): ArtefactoDao
    abstract fun vitrinaDao(): VitrinaDao
    abstract fun forjaDao(): ForjaDao
    abstract fun etapaForjaDao(): EtapaForjaDao
    abstract fun rachaDao(): RachaDao

    companion object {
        private const val NOMBRE = "odin.db"

        /** CU-13: agrega las tablas del duelo sin tocar las demás (no borra el progreso). */
        val MIGRACION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `conceptos_enigma` (`id` TEXT NOT NULL, `temaId` TEXT NOT NULL, `nombre` TEXT NOT NULL, `explicacion` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `preguntas_enigma` (`id` TEXT NOT NULL, `texto` TEXT NOT NULL, `tipo` TEXT NOT NULL, `conceptosSi` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `duelos_enigmas` (`id` TEXT NOT NULL, `conceptoId` TEXT NOT NULL, `resultado` TEXT, `preguntasRealizadas` INTEGER NOT NULL, `adivinanzasFallidas` INTEGER NOT NULL, `sorbosSobrantes` INTEGER NOT NULL, `hidromielObtenido` INTEGER NOT NULL, `fecha` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        /** CU-12: agrega las tablas de la Völva sin tocar las demás (no borra el progreso). */
        val MIGRACION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `visiones_volva` (`id` TEXT NOT NULL, `temaId` TEXT NOT NULL, `norna` TEXT NOT NULL, `caso` TEXT NOT NULL, `opciones` TEXT NOT NULL, `indiceCorrecta` INTEGER NOT NULL, `pista` TEXT NOT NULL, `explicacion` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `runas_obtenidas` (`orden` INTEGER NOT NULL, `fecha` INTEGER NOT NULL, PRIMARY KEY(`orden`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `consultas_volva` (`id` TEXT NOT NULL, `apuesta` INTEGER NOT NULL, `puntosDestino` INTEGER NOT NULL, `visionesCumplidas` INTEGER NOT NULL, `visionesErradas` INTEGER NOT NULL, `lectura` TEXT, `pago` INTEGER NOT NULL, `runaOrden` INTEGER, `abandonada` INTEGER NOT NULL, `terminada` INTEGER NOT NULL, `fecha` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        /** CU-01: agrega las tablas de Hasta el Alba sin tocar las demás (no borra el progreso). */
        val MIGRACION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `noches_alba` (`id` TEXT NOT NULL, `fecha` INTEGER NOT NULL, `apuesta` INTEGER NOT NULL, `oleada` INTEGER NOT NULL, `golpesRestantes` INTEGER NOT NULL, `escudos` INTEGER NOT NULL, `multiplicador` INTEGER NOT NULL, `bonoDraupnir` INTEGER NOT NULL, `aciertos` INTEGER NOT NULL, `errores` INTEGER NOT NULL, `artefactosUsados` TEXT NOT NULL, `preguntasMostradas` TEXT NOT NULL, `preguntasFalladas` TEXT NOT NULL, `temasConError` TEXT NOT NULL, `fase` TEXT NOT NULL, `desenlace` TEXT, `premioBruto` INTEGER NOT NULL, `diezmo` INTEGER NOT NULL, `premioNeto` INTEGER NOT NULL, `premioPendiente` INTEGER NOT NULL, `terminada` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `artefactos_forjados` (`artefacto` TEXT NOT NULL, `calidad` TEXT NOT NULL, `fecha` INTEGER NOT NULL, PRIMARY KEY(`artefacto`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `logros` (`id` TEXT NOT NULL, `fecha` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        /**
         * v12 → v13: solo crea las tablas de la forja (CU-02) y de la racha
         * (MOD-05). El contenido fijo de la forja se siembra en onOpen.
         */
        val MIGRACION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `artefactos` (`id` TEXT NOT NULL, `mundoId` TEXT NOT NULL, `nombre` TEXT NOT NULL, `poderPerfecto` TEXT NOT NULL, `poderConDefecto` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_artefactos_mundoId` ON `artefactos` (`mundoId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `vitrina` (`artefactoId` TEXT NOT NULL, `calidad` TEXT NOT NULL, `fechaObtencion` INTEGER NOT NULL, `ultimaNocheUsada` INTEGER, PRIMARY KEY(`artefactoId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `forjas` (`id` TEXT NOT NULL, `artefactoId` TEXT, `apuesta` INTEGER NOT NULL, `estado` TEXT NOT NULL, `errores` INTEGER NOT NULL, `hidromielNeto` INTEGER NOT NULL, `abandonada` INTEGER NOT NULL, `fecha` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_forjas_fecha` ON `forjas` (`fecha`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `problemas_martillar` (`id` TEXT NOT NULL, `mundoId` TEXT NOT NULL, `enunciado` TEXT NOT NULL, `piezas` TEXT NOT NULL, `piezaCorrectaId` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_problemas_martillar_mundoId` ON `problemas_martillar` (`mundoId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `templados` (`id` TEXT NOT NULL, `mundoId` TEXT NOT NULL, `tipo` TEXT NOT NULL, `enunciado` TEXT NOT NULL, `plantilla` TEXT NOT NULL, `respuestas` TEXT NOT NULL, `opciones` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_templados_mundoId` ON `templados` (`mundoId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `rachas` (`idUsuario` TEXT NOT NULL, `actual` INTEGER NOT NULL, `maxima` INTEGER NOT NULL, `ultimaActividad` TEXT, `ofrendas` INTEGER NOT NULL, `titulos` TEXT NOT NULL, PRIMARY KEY(`idUsuario`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `hitos_racha` (`idUsuario` TEXT NOT NULL, `dias` INTEGER NOT NULL, `bono` INTEGER NOT NULL, `titulo` TEXT, `fecha` INTEGER NOT NULL, PRIMARY KEY(`idUsuario`, `dias`))")
            }
        }

        /**
         * v13 → v14: el nivel sabe a qué tema pertenece (temas.temaBaseId) y el
         * contrarreloj guarda el hidromiel ganado aparte del puntaje. Los
         * temas 5 a 10, los niveles que cambian de anillo y los que se quitan los
         * pone al día DatosIniciales.sincronizar en onOpen.
         */
        val MIGRACION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `temas` ADD COLUMN `temaBaseId` TEXT NOT NULL DEFAULT ''")
                // CU-11, RF-157: el resultado del contrarreloj guarda también el hidromiel ganado
                db.execSQL("ALTER TABLE `resultados_contrarreloj` ADD COLUMN `hidromiel` INTEGER NOT NULL DEFAULT 0")
                // CU-11, RF-147 y E7.2: cada reto queda registrado desde que empieza
                db.execSQL("ALTER TABLE `resultados_contrarreloj` ADD COLUMN `terminado` INTEGER NOT NULL DEFAULT 1")
            }
        }

        @Volatile
        private var INSTANCE: OdinDatabase? = null

        fun getInstance(context: Context): OdinDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OdinDatabase::class.java,
                    NOMBRE
                )
                    .addCallback(
                        object : Callback() {
                            // Se siembra en onOpen y no en onCreate: al subir la versión,
                            // fallbackToDestructiveMigration recrea las tablas vacías sin
                            // llamar a onCreate, y la app se quedaba sin mundos.
                            override fun onOpen(
                                db: androidx.sqlite.db.SupportSQLiteDatabase
                            ) {
                                super.onOpen(db)
                                val vacia = db.query("SELECT COUNT(*) FROM mundos").use { c ->
                                    c.moveToFirst() && c.getInt(0) == 0
                                }
                                db.beginTransaction()
                                try {
                                    // Estructura del roadmap y contenido fijo: se ponen al día en cada
                                    // apertura sin tocar el progreso (temas 5 a 10, anillos del SRS v3.2).
                                    if (vacia) DatosIniciales.insertar(db) else DatosIniciales.sincronizar(db)
                                    // El banco de preguntas es contenido fijo: se actualiza en cada
                                    // apertura para que las preguntas nuevas lleguen sin borrar datos.
                                    BancoPreguntas.insertar(db)
                                    DatosEnigmas.insertar(db)
                                    // CU-12: las visiones de la Völva también son contenido fijo.
                                    DatosVolva.insertar(db)
                                    // CU-02: artefactos y preguntas de la forja (contenido fijo)
                                    DatosForja.insertar(db)
                                    db.setTransactionSuccessful()
                                } finally {
                                    db.endTransaction()
                                }
                            }
                        }
                    )
                    .addMigrations(MIGRACION_9_10, MIGRACION_10_11, MIGRACION_11_12, MIGRACION_12_13, MIGRACION_13_14)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}