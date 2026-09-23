package com.aesir.odin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aesir.odin.data.local.converter.Converters
import com.aesir.odin.data.local.dao.EjercicioDao
import com.aesir.odin.data.local.dao.HistorialErrorDao
import com.aesir.odin.data.local.dao.LeccionDao
import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.local.entity.EjercicioEntity
import com.aesir.odin.data.local.entity.HistorialErrorEntity
import com.aesir.odin.data.local.entity.LeccionEntity
import com.aesir.odin.data.local.entity.ResultadoLeccionEntity
import com.aesir.odin.data.local.entity.TemaEntity

@Database(
    entities = [
        TemaEntity::class,
        LeccionEntity::class,
        EjercicioEntity::class,
        ResultadoLeccionEntity::class,
        HistorialErrorEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OdinDatabase : RoomDatabase() {
    abstract fun temaDao(): TemaDao
    abstract fun leccionDao(): LeccionDao
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun historialErrorDao(): HistorialErrorDao

    companion object {
        private const val NOMBRE = "odin.db"

        fun crear(context: Context): OdinDatabase =
            Room.databaseBuilder(context, OdinDatabase::class.java, NOMBRE)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        DatosIniciales.insertar(db)
                    }
                })
                // Mientras el esquema cambie entre sprints, se recrea la BD en lugar de migrar.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
