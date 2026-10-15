package com.aesir.odin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aesir.odin.data.local.converter.Converters
import com.aesir.odin.data.local.dao.ConceptoCrucigramaDao
import com.aesir.odin.data.local.dao.EjercicioDao
import com.aesir.odin.data.local.dao.HidromielDao
import com.aesir.odin.data.local.dao.HistorialErrorDao
import com.aesir.odin.data.local.dao.LeccionDao
import com.aesir.odin.data.local.dao.MundoDao
import com.aesir.odin.data.local.dao.PreguntaFlashBancoDao
import com.aesir.odin.data.local.dao.ResultadoCrucigramaDao
import com.aesir.odin.data.local.dao.ResultadoRetoDao
import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.local.entity.ConceptoCrucigramaEntity
import com.aesir.odin.data.local.entity.EjercicioEntity
import com.aesir.odin.data.local.entity.HistorialErrorEntity
import com.aesir.odin.data.local.entity.LeccionEntity
import com.aesir.odin.data.local.entity.MovimientoHidromielEntity
import com.aesir.odin.data.local.entity.MundoDesbloqueadoEntity
import com.aesir.odin.data.local.entity.MundoEntity
import com.aesir.odin.data.local.entity.NivelSuperadoEntity
import com.aesir.odin.data.local.entity.PreguntaFlashEntity
import com.aesir.odin.data.local.entity.ReservaHidromielEntity
import com.aesir.odin.data.local.entity.ResultadoCrucigramaEntity
import com.aesir.odin.data.local.entity.ResultadoLeccionEntity
import com.aesir.odin.data.local.entity.ResultadoRetoEntity
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
        ResultadoRetoEntity::class
    ],
    // v3: MOD-01 Sistema de Hidromiel (tablas nuevas y columna temas.tipo)
    // v4: CU-07 crucigrama (conceptos, resultados y columna temas.actividad)
    // v5: árbol de dos anillos (columna temas.anillo y minijuegos por mundo)
    // v6: CU-11 reto contrarreloj (banco de preguntas flash y resultados)
    version = 6,
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
    abstract fun resultadoRetoDao(): ResultadoRetoDao

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
                                if (vacia) {
                                    db.beginTransaction()
                                    try {
                                        DatosIniciales.insertar(db)
                                        db.setTransactionSuccessful()
                                    } finally {
                                        db.endTransaction()
                                    }
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