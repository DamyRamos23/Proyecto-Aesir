package com.aesir.odin.di

import android.content.Context
import com.aesir.odin.data.local.OdinDatabase
import com.aesir.odin.data.repository.CatalogoNivelesImpl
import com.aesir.odin.data.repository.CrucigramaRepositoryImpl
import com.aesir.odin.data.repository.DiagnosticoRepositoryImpl
import com.aesir.odin.data.repository.HidromielRepositoryImpl
import com.aesir.odin.data.repository.HistorialRepositoryImpl
import com.aesir.odin.data.repository.LeccionRepositoryImpl
import com.aesir.odin.data.repository.MemoramaRepositoryImpl
import com.aesir.odin.data.repository.RoadmapRepositoryImpl
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.repository.CrucigramaRepository
import com.aesir.odin.domain.repository.DiagnosticoRepository
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.repository.MemoramaRepository
import com.aesir.odin.domain.repository.RoadmapRepository
import com.aesir.odin.domain.usecase.crucigrama.ActualizarPuntajeUseCase
import com.aesir.odin.domain.usecase.crucigrama.FinalizarCrucigramaUseCase
import com.aesir.odin.domain.usecase.crucigrama.GenerarCrucigramaUseCase
import com.aesir.odin.domain.usecase.crucigrama.ValidarPalabraUseCase
import com.aesir.odin.domain.usecase.diagnostico.EvaluarDiagnosticoUseCase
import com.aesir.odin.domain.usecase.diagnostico.FinalizarDiagnosticoUseCase
import com.aesir.odin.domain.usecase.diagnostico.ObtenerPreguntasDiagnosticoUseCase
import com.aesir.odin.domain.usecase.diagnostico.OmitirDiagnosticoUseCase
import com.aesir.odin.domain.usecase.diagnostico.ValidarAccesoDiagnosticoUseCase
import com.aesir.odin.domain.usecase.leccion.FinalizarLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.ObtenerLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarErrorLeccionUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarLeccionCompletadaUseCase
import com.aesir.odin.domain.usecase.leccion.RegistrarNivelSinSuperarUseCase
import com.aesir.odin.domain.usecase.leccion.ValidarRespuestaEjercicioUseCase
import com.aesir.odin.domain.usecase.memorama.CalcularPuntosMemoramaUseCase
import com.aesir.odin.domain.usecase.memorama.FinalizarMemoramaUseCase
import com.aesir.odin.domain.usecase.memorama.GenerarMemoramaUseCase
import com.aesir.odin.domain.usecase.memorama.ValidarAccesoMemoramaUseCase
import com.aesir.odin.domain.usecase.memorama.VerificarParejaUseCase
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

    // CU-04 Resolver memorama de conceptos
    val memoramaRepository: MemoramaRepository = MemoramaRepositoryImpl(database.memoramaDao())
    val validarAccesoMemoramaUseCase = ValidarAccesoMemoramaUseCase(roadmapRepository, memoramaRepository)
    val generarMemoramaUseCase = GenerarMemoramaUseCase(memoramaRepository)
    val verificarParejaUseCase = VerificarParejaUseCase()
    val finalizarMemoramaUseCase = FinalizarMemoramaUseCase(
        memoramaRepository,
        roadmapRepository,
        CalcularPuntosMemoramaUseCase(),
        moduloHidromiel
    )

    // CU-10 Realizar evaluación diagnóstico
    val diagnosticoRepository: DiagnosticoRepository = DiagnosticoRepositoryImpl(database.diagnosticoDao())
    val validarAccesoDiagnosticoUseCase = ValidarAccesoDiagnosticoUseCase(roadmapRepository, diagnosticoRepository)
    val obtenerPreguntasDiagnosticoUseCase = ObtenerPreguntasDiagnosticoUseCase(diagnosticoRepository)
    val omitirDiagnosticoUseCase = OmitirDiagnosticoUseCase(diagnosticoRepository)
    val finalizarDiagnosticoUseCase = FinalizarDiagnosticoUseCase(
        EvaluarDiagnosticoUseCase(ValidarRespuestaUseCase()), // ValidarRespuestaUseCase reutilizado de CU-05/CU-06
        diagnosticoRepository,
        roadmapRepository,
        moduloHidromiel
    )
}
