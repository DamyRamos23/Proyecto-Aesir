package com.aesir.odin.di

import android.content.Context
import com.aesir.odin.data.local.OdinDatabase
import com.aesir.odin.data.repository.CatalogoNivelesImpl
import com.aesir.odin.data.repository.CrucigramaRepositoryImpl
import com.aesir.odin.data.repository.HidromielRepositoryImpl
import com.aesir.odin.data.repository.HistorialRepositoryImpl
import com.aesir.odin.data.repository.LeccionRepositoryImpl
import com.aesir.odin.data.repository.PreguntaFlashRepositoryImpl
import com.aesir.odin.data.repository.HidromielFlashRepositoryImpl
import com.aesir.odin.data.repository.RoadmapRepositoryImpl
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.repository.CrucigramaRepository
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.HidromielFlashRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import com.aesir.odin.domain.usecase.crucigrama.ActualizarPuntajeUseCase
import com.aesir.odin.domain.usecase.crucigrama.FinalizarCrucigramaUseCase
import com.aesir.odin.domain.usecase.crucigrama.GenerarCrucigramaUseCase
import com.aesir.odin.domain.usecase.crucigrama.ValidarPalabraUseCase
import com.aesir.odin.domain.usecase.leccion.FinalizarLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.ObtenerLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarErrorLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarNivelSinSuperarUseCase
import com.aesir.odin.domain.usecase.leccion.ValidarRespuestaEjercicioUseCase
import com.aesir.odin.domain.usecase.preguntaflash.ObtenerPreguntaFlashUseCase
import com.aesir.odin.domain.usecase.preguntaflash.ObservarHidromielUseCase
import com.aesir.odin.domain.usecase.preguntaflash.RegistrarTiempoAgotadoPreguntaFlashUseCase
import com.aesir.odin.domain.usecase.preguntaflash.ValidarRespuestaPreguntaFlashUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerMundosUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerTemasPorMundoUseCase
import com.aesir.odin.domain.usecase.roadmap.ValidarAccesoNivelUseCase
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.RegistrarIntroduccionVistaUseCase
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase

/**
 * Contenedor manual de dependencias (no hay Hilt configurado en el
 * proyecto). No aparece en los diagramas, pero es lo que arma la cadena
 * ViewModel -> UseCase -> Repository -> DAO que sí describen.
 *
 * Se crea una sola vez en MainActivity y se pasa hacia abajo por Compose.
 */
class AppContainer(context: Context) {

    private val database = OdinDatabase.getInstance(context)

    val roadmapRepository: RoadmapRepository =
        RoadmapRepositoryImpl(database.mundoDao(), database.temaDao())

    val leccionRepository: LeccionRepository =
        LeccionRepositoryImpl(database.leccionDao(), database.ejercicioDao())

    private val historialRepository: HistorialRepository =
        HistorialRepositoryImpl(database.historialErrorDao())

    /**
     * MOD-01 Sistema de Hidromiel. Una sola instancia para toda la app: es la
     * única que decide qué niveles se pueden jugar y cuánto hidromiel hay.
     */
    val moduloHidromiel = ModuloHidromiel(
        catalogo = CatalogoNivelesImpl(database.temaDao(), database.mundoDao()),
        repositorio = HidromielRepositoryImpl(database)
    )

    val obtenerMundosUseCase = ObtenerMundosUseCase(roadmapRepository)
    val obtenerMundoUseCase = ObtenerMundoUseCase(roadmapRepository)
    val obtenerTemasPorMundoUseCase = ObtenerTemasPorMundoUseCase(roadmapRepository)
    val validarAccesoNivelUseCase = ValidarAccesoNivelUseCase(roadmapRepository)
    val obtenerIntroduccionTemaUseCase = ObtenerIntroduccionTemaUseCase(roadmapRepository)
    val registrarIntroduccionVistaUseCase = RegistrarIntroduccionVistaUseCase(roadmapRepository)

    val obtenerLeccionUseCase = ObtenerLeccionUseCase(leccionRepository)
    val validarRespuestaEjercicioUseCase =
        ValidarRespuestaEjercicioUseCase(ValidarRespuestaUseCase())
    val registrarErrorLeccionUseCase = RegistrarErrorLeccionUseCase(historialRepository)
    val finalizarLeccionUseCase = FinalizarLeccionUseCase(leccionRepository, moduloHidromiel)
    val registrarNivelSinSuperarUseCase = RegistrarNivelSinSuperarUseCase(leccionRepository, moduloHidromiel)

    // CU-07 Resolver crucigrama de conceptos
    val crucigramaRepository: CrucigramaRepository =
        CrucigramaRepositoryImpl(database.conceptoCrucigramaDao(), database.resultadoCrucigramaDao())
    val generarCrucigramaUseCase = GenerarCrucigramaUseCase(crucigramaRepository)
    val validarPalabraUseCase = ValidarPalabraUseCase()
    val actualizarPuntajeUseCase = ActualizarPuntajeUseCase()
    val finalizarCrucigramaUseCase = FinalizarCrucigramaUseCase(
        crucigramaRepository,
        RegistrarLeccionCompletadaUseCase(roadmapRepository), // reutilizado de CU-05
        moduloHidromiel
    )

    // ---- Pregunta Flash (CU-06) ----
    private val preguntaFlashRepository: PreguntaFlashRepository =
        PreguntaFlashRepositoryImpl(database.preguntaFlashDao(), database.preguntaFlashBancoDao())

    // Los puntos de la pregunta flash son hidromiel (MOD-01).
    private val hidromielFlashRepository: HidromielFlashRepository =
        HidromielFlashRepositoryImpl(moduloHidromiel)

    val obtenerPreguntaFlashUseCase =
        ObtenerPreguntaFlashUseCase(roadmapRepository, preguntaFlashRepository)
    val validarRespuestaPreguntaFlashUseCase = ValidarRespuestaPreguntaFlashUseCase(
        preguntaFlashRepository, hidromielFlashRepository, historialRepository, ValidarRespuestaUseCase()
    )
    val registrarTiempoAgotadoPreguntaFlashUseCase =
        RegistrarTiempoAgotadoPreguntaFlashUseCase(preguntaFlashRepository, hidromielFlashRepository)
    val observarHidromielUseCase = ObservarHidromielUseCase(hidromielFlashRepository)
}
