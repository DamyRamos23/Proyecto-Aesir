package com.aesir.odin.domain.usecase.retoarrastrar

import com.aesir.odin.domain.model.HistorialError
import com.aesir.odin.domain.repository.HistorialRepository
import java.util.UUID

/**
 * A7.1 y A7.2 del CU-09 (RF-78): registra la confirmación incorrecta en el
 * historial de errores y avisa si ya se llegó al límite de intentos.
 *
 * No confundir con el RegistrarErrorRetoUseCase del reto de fin de mundo
 * (CU-08), que vive en otro paquete.
 */
class RegistrarErrorRetoArrastrarUseCase(
    private val repository: HistorialRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    /**
     * @param elementosIncorrectos textos de los elementos mal colocados.
     * @param intentosActuales confirmaciones incorrectas contando esta.
     * @return true si se alcanzó el límite de intentos.
     */
    suspend operator fun invoke(retoId: String, elementosIncorrectos: List<String>, intentosActuales: Int): Boolean {
        val limite = intentosActuales >= LIMITE_INTENTOS
        repository.registrarError(
            HistorialError(
                id = UUID.randomUUID().toString(),
                tipoDesafio = TIPO_ARRASTRAR,
                actividadId = retoId,
                descripcion = buildString {
                    append(if (limite) "Reto no superado (intento $intentosActuales de $LIMITE_INTENTOS)" else "Intento $intentosActuales de $LIMITE_INTENTOS")
                    append(": mal colocados ")
                    append(elementosIncorrectos.joinToString(", ") { "«$it»" })
                },
                fecha = reloj()
            )
        )
        return limite
    }

    companion object {
        const val LIMITE_INTENTOS = 3
        const val TIPO_ARRASTRAR = "ARRASTRAR"
    }
}
