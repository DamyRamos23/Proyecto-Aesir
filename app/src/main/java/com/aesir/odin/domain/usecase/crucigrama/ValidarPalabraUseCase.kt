package com.aesir.odin.domain.usecase.crucigrama

import com.aesir.odin.domain.model.EstadoRespuesta
import com.aesir.odin.domain.model.PalabraCrucigrama
import java.text.Normalizer

/**
 * Valida la respuesta de una palabra (RF-50).
 * - CORRECTA: coincide con la palabra (sin importar acentos ni mayúsculas).
 * - INCOMPLETA: lo escrito coincide con el inicio de la palabra, pero faltan letras.
 * - INCORRECTA: cualquier otro caso.
 */
class ValidarPalabraUseCase {

    operator fun invoke(palabra: PalabraCrucigrama, respuesta: String): EstadoRespuesta {
        val escrita = normalizar(respuesta)
        val esperada = normalizar(palabra.respuesta)
        return when {
            escrita == esperada -> EstadoRespuesta.CORRECTA
            escrita.length < esperada.length && esperada.startsWith(escrita) -> EstadoRespuesta.INCOMPLETA
            else -> EstadoRespuesta.INCORRECTA
        }
    }

    fun normalizar(texto: String): String = normalizarPalabra(texto)
}

/**
 * Mayúsculas, sin acentos ni diéresis (la Ñ se conserva) y solo letras.
 * La usan el validador y el generador, para que ambos comparen igual.
 */
fun normalizarPalabra(texto: String): String {
    val conMarcador = texto.uppercase().replace('Ñ', '\u0001')
    val sinAcentos = Normalizer.normalize(conMarcador, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
    return sinAcentos.replace('\u0001', 'Ñ').filter { it == 'Ñ' || it in 'A'..'Z' }
}
