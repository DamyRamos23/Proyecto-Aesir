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
        ResultadoRetoEntity::class
    ],
    // v3: MOD-01 Sistema de Hidromiel (tablas nuevas y columna temas.tipo)
    // v4: CU-07 crucigrama (conceptos, resultados y columna temas.actividad)
    // v5: árbol de dos anillos (columna temas.anillo y minijuegos por mundo)
    // v6: CU-06 pregunta flash (banco de preguntas e historial de intentos)
    // v7: CU-11 reto contrarreloj (resultados del reto)
    // v8: CU-04 memorama (pares y resultados), CU-10 diagnóstico (preguntas,
    //     resultados) y columna temas.conocido
    // v9: tercer anillo, CU-03 flashcards, CU-08 Ragnarök y CU-09 arrastrar y soltar
    version = 9,
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

    companion object {
        private const val NOMBRE = "odin.db"

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
                                    if (vacia) DatosIniciales.insertar(db)
                                    // El banco de preguntas es contenido fijo: se actualiza en cada
                                    // apertura para que las preguntas nuevas lleguen sin borrar datos.
                                    BancoPreguntas.insertar(db)
                                    db.setTransactionSuccessful()
                                } finally {
                                    db.endTransaction()
                                }
                            }
                        }
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}