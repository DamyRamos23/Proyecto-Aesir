package com.aesir.odin.domain.usecase.retoarrastrar

import com.aesir.odin.domain.bitacora.FalloBitacora
import com.aesir.odin.domain.bitacora.ModuloBitacora
import com.aesir.odin.domain.bitacora.OrigenError
import com.aesir.odin.domain.model.RetoArrastrar

/**
 * A7.1 y A7.2 del CU-09: avisa a la bitácora (MOD-06, E1: sin opciones, con una
 * descripción) la confirmación incorrecta y dice si ya se llegó al límite.
 *
 * No confundir con el RegistrarErrorRetoUseCase del reto de fin de mundo
 * (CU-08), que vive en otro paquete.
 */
class RegistrarErrorRetoArrastrarUseCase(
    private val moduloBitacora: ModuloBitacora,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    /**
     * @param elementosIncorrectos textos de los elementos mal colocados.
     * @param intentosActuales confirmaciones incorrectas contando esta.
     * @return true si se alcanzó el límite de intentos.
     */
    operator fun invoke(reto: RetoArrastrar, elementosIncorrectos: List<String>, intentosActuales: Int): Boolean {
        moduloBitacora.registrarFallo(
            FalloBitacora(
                origen = OrigenError.ARRASTRAR_SOLTAR,
                preguntaId = reto.id,
                temaId = reto.temaId,
                enunciado = "${reto.instrucciones} Mal colocados: " + elementosIncorrectos.joinToString(", ") { "«$it»" },
                respuestaAlumno = elementosIncorrectos.joinToString(", "),
                fecha = reloj()
            )
        )
        return intentosActuales >= LIMITE_INTENTOS
    }

    companion object {
        const val LIMITE_INTENTOS = 3
    }
}
