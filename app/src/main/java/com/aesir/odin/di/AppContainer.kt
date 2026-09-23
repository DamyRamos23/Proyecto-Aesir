package com.aesir.odin.di

import android.content.Context
import com.aesir.odin.data.local.OdinDatabase
import com.aesir.odin.data.repository.HistorialRepositoryImpl
import com.aesir.odin.data.repository.LeccionRepositoryImpl
import com.aesir.odin.data.repository.RoadmapRepositoryImpl
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import com.aesir.odin.domain.usecase.leccion.FinalizarLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.ObtenerLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarErrorLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase
import com.aesir.odin.domain.usecase.leccion.ValidarRespuestaEjercicioUseCase
import com.aesir.odin.domain.usecase.roadmap.DesbloquearSiguienteTemaUseCase
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase

/** Inyección de dependencias manual: crea una sola vez la BD, los repositorios y los casos de uso. */
class AppContainer(context: Context) {

    private val database = OdinDatabase.crear(context)

    val roadmapRepository: RoadmapRepository = RoadmapRepositoryImpl(database.temaDao())
    val leccionRepository: LeccionRepository =
        LeccionRepositoryImpl(database.leccionDao(), database.ejercicioDao())
    val historialRepository: HistorialRepository =
        HistorialRepositoryImpl(database.historialErrorDao())

    val validarRespuestaUseCase = ValidarRespuestaUseCase()

    // CU-05 Realizar una lección interactiva
    val obtenerLeccionUseCase = ObtenerLeccionUseCase(leccionRepository)
    val validarRespuestaEjercicioUseCase = ValidarRespuestaEjercicioUseCase(validarRespuestaUseCase)
    val registrarErrorLeccionUseCase = RegistrarErrorLeccionUseCase(historialRepository)
    val finalizarLeccionUseCase = FinalizarLeccionUseCase(
        leccionRepository = leccionRepository,
        registrarCompletadaUseCase = RegistrarLeccionCompletadaUseCase(roadmapRepository),
        desbloquearUseCase = DesbloquearSiguienteTemaUseCase(roadmapRepository)
    )
}
