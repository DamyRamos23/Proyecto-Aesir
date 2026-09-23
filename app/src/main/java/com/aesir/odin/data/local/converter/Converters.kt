package com.aesir.odin.data.local.converter

import androidx.room.TypeConverter

/**
 * Guarda listas como texto en una sola columna. Las opciones se separan con un
 * carácter de control (no aparece en textos normales, así que pueden llevar comas).
 */
class Converters {
    @TypeConverter
    fun fromStringList(lista: List<String>): String = lista.joinToString(SEPARADOR_TEXTO)

    @TypeConverter
    fun toStringList(data: String): List<String> =
        if (data.isEmpty()) emptyList() else data.split(SEPARADOR_TEXTO)

    @TypeConverter
    fun fromIntList(lista: List<Int>): String = lista.joinToString(",")

    @TypeConverter
    fun toIntList(data: String): List<Int> =
        if (data.isEmpty()) emptyList() else data.split(",").map { it.trim().toInt() }

    companion object {
        const val SEPARADOR_TEXTO = "\u001F"
    }
}
