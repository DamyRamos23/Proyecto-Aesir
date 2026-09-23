package com.aesir.odin.domain.model

enum class EstadoRespuesta {
    CORRECTA,
    INCORRECTA,
    INCOMPLETA
}

data class ResultadoRespuesta(
    val estado: EstadoRespuesta,
    val opcionesCorrectas: List<Int>
)
