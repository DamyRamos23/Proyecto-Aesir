package com.aesir.odin.data.local

import com.aesir.odin.data.local.entity.HistorialErrorEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v14 → v15. La base no exporta su esquema (exportSchema = false), así que no
 * hay MigrationTestHelper: se revisa que el SQL cree la tabla que Room espera.
 */
class Migracion14a15Test {

    private val sql = OdinDatabase.SQL_MIGRACION_14_15

    @Test
    fun `la tabla nueva tiene exactamente las columnas de HistorialErrorEntity`() {
        val crear = sql.first { it.startsWith("CREATE TABLE `historial_errores_nueva`") }
        val columnas = Regex("`(\\w+)` (TEXT|INTEGER)").findAll(crear).map { it.groupValues[1] }.toList()
        val campos = HistorialErrorEntity::class.java.declaredFields
            .filter { !it.isSynthetic && !java.lang.reflect.Modifier.isStatic(it.modifiers) }.map { it.name }
        assertEquals(campos.sorted(), columnas.sorted())
        // Solo respuestaAlumno y explicacion admiten null
        val nulos = Regex("`(\\w+)` (TEXT|INTEGER)(?! NOT NULL)").findAll(crear).map { it.groupValues[1] }.toSet()
        assertEquals(setOf("respuestaAlumno", "explicacion"), nulos)
    }

    @Test
    fun `copia las filas viejas, crea el indice unico y borra los ajustes manuales del saldo`() {
        val copia = sql.first { it.startsWith("INSERT INTO `historial_errores_nueva`") }
        assertTrue(copia.contains("`actividadId` || ':' || `id`"))
        assertTrue(copia.contains("'PENDIENTE', 1, `fecha`"))
        assertTrue(sql.contains("CREATE UNIQUE INDEX IF NOT EXISTS `index_historial_errores_preguntaId` ON `historial_errores` (`preguntaId`)"))
        assertTrue(sql.contains("DELETE FROM `movimientos_hidromiel` WHERE `tipo` = 'AJUSTE_ADMIN'"))
        // Orden: crear, copiar, borrar la vieja y renombrar antes del índice
        assertTrue(sql.indexOf("DROP TABLE `historial_errores`") < sql.indexOf("ALTER TABLE `historial_errores_nueva` RENAME TO `historial_errores`"))
    }
}
