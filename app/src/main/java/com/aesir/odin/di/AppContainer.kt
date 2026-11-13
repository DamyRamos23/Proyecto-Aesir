package com.aesir.odin.di

import android.content.Context
import com.aesir.odin.data.local.OdinDatabase
import com.aesir.odin.data.repository.AlmacenPreferenciasImpl
import com.aesir.odin.data.repository.DueloRepositoryImpl
import com.aesir.odin.domain.repository.DueloRepository
import com.aesir.odin.domain.usecase.duelo.AbandonarDueloUseCase
import com.aesir.odin.domain.usecase.duelo.ApostarDueloUseCase
import com.aesir.odin.domain.usecase.duelo.ArmarDueloUseCase
import com.aesir.odin.domain.usecase.duelo.FinalizarDueloUseCase
import com.aesir.odin.domain.usecase.duelo.ValidarAccesoDueloUseCase
import com.aesir.odin.data.repository.AlmacenEastereggsImpl
import com.aesir.odin.domain.eastereggs.AlmacenEastereggs
import com.aesir.odin.domain.heimdall.ModuloVisionHeimdall
import com.aesir.odin.data.repository.CatalogoNivelesImpl
import com.aesir.odin.data.repository.CrucigramaRepositoryImpl
import com.aesir.odin.data.repository.DiagnosticoRepositoryImpl
import com.aesir.odin.data.repository.HidromielRepositoryImpl
import com.aesir.odin.data.repository.HistorialRepositoryImpl
import com.aesir.odin.data.repository.LeccionRepositoryImpl
import com.aesir.odin.data.repository.PreguntaFlashRepositoryImpl
import com.aesir.odin.data.repository.HidromielFlashRepositoryImpl
import com.aesir.odin.data.repository.RetoContrarrelojRepositoryImpl
import com.aesir.odin.data.repository.MemoramaRepositoryImpl
import com.aesir.odin.data.repository.RoadmapRepositoryImpl
import com.aesir.odin.domain.hidromiel.ModuloHidromiel
import com.aesir.odin.domain.repository.CrucigramaRepository
import com.aesir.odin.domain.repository.DiagnosticoRepository
import com.aesir.odin.domain.repository.HistorialRepository
import com.aesir.odin.domain.repository.LeccionRepository
import com.aesir.odin.domain.repository.PreguntaFlashRepository
import com.aesir.odin.domain.repository.HidromielFlashRepository
import com.aesir.odin.domain.repository.RetoContrarrelojRepository
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
import com.aesir.odin.domain.usecase.preguntaflash.ObtenerPreguntaFlashUseCase
import com.aesir.odin.domain.usecase.preguntaflash.ObservarHidromielUseCase
import com.aesir.odin.domain.usecase.preguntaflash.RegistrarTiempoAgotadoPreguntaFlashUseCase
import com.aesir.odin.domain.usecase.preguntaflash.ValidarRespuestaPreguntaFlashUseCase
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
import com.aesir.odin.domain.usecase.reto.AbandonarRetoContrarrelojUseCase
import com.aesir.odin.domain.usecase.reto.ArmarRetoContrarrelojUseCase
import com.aesir.odin.domain.usecase.reto.FinalizarRetoContrarrelojUseCase
import com.aesir.odin.domain.usecase.reto.PagarEntradaRetoContrarrelojUseCase
import com.aesir.odin.domain.usecase.shared.ValidarRespuestaUseCase
import com.aesir.odin.data.repository.FlashcardsRepositoryImpl
import com.aesir.odin.data.repository.RetoFinMundoRepositoryImpl
import com.aesir.odin.data.repository.RetoRepositoryImpl
import com.aesir.odin.domain.hidromiel.ModoAdmin
import com.aesir.odin.domain.repository.FlashcardsRepository
import com.aesir.odin.domain.repository.RetoFinMundoRepository
import com.aesir.odin.domain.repository.RetoRepository
import com.aesir.odin.domain.usecase.diagnostico.ObtenerPreguntasRepasoUseCase
import com.aesir.odin.domain.usecase.flashcards.FinalizarFlashcardsUseCase
import com.aesir.odin.domain.usecase.flashcards.GenerarFlashcardsUseCase
import com.aesir.odin.domain.usecase.flashcards.RegistrarErrorFlashcardUseCase
import com.aesir.odin.domain.usecase.flashcards.RegistrarFlashcardsSinSuperarUseCase
import com.aesir.odin.domain.usecase.ragnarok.AbandonarRetoFinMundoUseCase
import com.aesir.odin.domain.usecase.ragnarok.FinalizarRetoFinMundoUseCase
import com.aesir.odin.domain.usecase.ragnarok.ObtenerRetoFinMundoUseCase
import com.aesir.odin.domain.usecase.ragnarok.RegistrarErrorRetoUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.AbandonarRetoUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.CalcularPuntajeRetoUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.FinalizarRetoUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.ObtenerRetoUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.RegistrarErrorRetoArrastrarUseCase
import com.aesir.odin.domain.usecase.retoarrastrar.ValidarColocacionUseCase
import com.aesir.odin.data.repository.VolvaRepositoryImpl
import com.aesir.odin.domain.repository.VolvaRepository
import com.aesir.odin.domain.usecase.volva.AbandonarConsultaVolvaUseCase
import com.aesir.odin.domain.usecase.volva.ApostarVolvaUseCase
import com.aesir.odin.domain.usecase.volva.ArmarConsultaVolvaUseCase
import com.aesir.odin.domain.usecase.volva.FinalizarConsultaVolvaUseCase
import com.aesir.odin.domain.usecase.volva.ResolverVisionUseCase
import com.aesir.odin.domain.usecase.volva.ValidarAccesoVolvaUseCase

/**
 * Contenedor manual de dependencias (no hay Hilt configurado en el
 * proyecto). No aparece en los diagramas, pero es lo que arma la cadena
 * ViewModel -> UseCase -> Repository -> DAO que sí describen.
 *
 * Se crea una sola vez en MainActivity y se pasa hacia abajo por Compose.
 */
class AppContainer(context: Context) {

    private val database = OdinDatabase.getInstance(context)

    private val preferencias = context.getSharedPreferences("odin", Context.MODE_PRIVATE)

    /** MOD-03 Visión de Heimdall: una sola instancia, la observa el tema de toda la app. */
    val moduloVisionHeimdall = ModuloVisionHeimdall(AlmacenPreferenciasImpl(preferencias))

    /** Juegos ocultos (easter eggs) que ya se descubrieron en Mundos. */
    val almacenEastereggs: AlmacenEastereggs = AlmacenEastereggsImpl(preferencias)

    init {
        // Modo admin oculto: cada apertura de la app inicia sin él.
        // Se borra el valor que guardaban las versiones anteriores.
        ModoAdmin.cargar(false)
        preferencias.edit().remove(CLAVE_MODO_ADMIN).apply()
    }

    val roadmapRepository: RoadmapRepository =
        RoadmapRepositoryImpl(database.mundoDao(), database.temaDao(), database.retoFinMundoDao())

    val leccionRepository: LeccionRepository =
        LeccionRepositoryImpl(database.leccionDao(), database.ejercicioDao())

    val historialRepository: HistorialRepository =
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

    // Validador compartido por Pregunta Flash y Reto Contrarreloj.
    val validarRespuestaUseCase = ValidarRespuestaUseCase()

    // ---- Pregunta Flash (CU-06) ----
    val preguntaFlashRepository: PreguntaFlashRepository =
        PreguntaFlashRepositoryImpl(database.preguntaFlashDao(), database.preguntaFlashBancoDao())

    // Los puntos de la pregunta flash son hidromiel (MOD-01).
    private val hidromielFlashRepository: HidromielFlashRepository =
        HidromielFlashRepositoryImpl(moduloHidromiel)

    val obtenerPreguntaFlashUseCase =
        ObtenerPreguntaFlashUseCase(roadmapRepository, preguntaFlashRepository)
    val validarRespuestaPreguntaFlashUseCase = ValidarRespuestaPreguntaFlashUseCase(
        preguntaFlashRepository, hidromielFlashRepository, historialRepository, validarRespuestaUseCase
    )
    val registrarTiempoAgotadoPreguntaFlashUseCase =
        RegistrarTiempoAgotadoPreguntaFlashUseCase(preguntaFlashRepository, hidromielFlashRepository)
    val observarHidromielUseCase = ObservarHidromielUseCase(hidromielFlashRepository)

    // ---- Reto Contrarreloj (CU-11): reutiliza el banco de Pregunta Flash ----
    private val retoContrarrelojRepository: RetoContrarrelojRepository =
        RetoContrarrelojRepositoryImpl(database.resultadoContrarrelojDao())

    val armarRetoContrarrelojUseCase =
        ArmarRetoContrarrelojUseCase(roadmapRepository, preguntaFlashRepository)
    val pagarEntradaRetoContrarrelojUseCase =
        PagarEntradaRetoContrarrelojUseCase(moduloHidromiel)
    val finalizarRetoContrarrelojUseCase =
        FinalizarRetoContrarrelojUseCase(retoContrarrelojRepository, moduloHidromiel)
    val abandonarRetoContrarrelojUseCase =
        AbandonarRetoContrarrelojUseCase(retoContrarrelojRepository, moduloHidromiel)
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

    // CU-03 Resolver flashcards de conceptos
    val flashcardsRepository: FlashcardsRepository =
        FlashcardsRepositoryImpl(database.conceptoCrucigramaDao(), database.resultadoFlashcardsDao())
    val generarFlashcardsUseCase = GenerarFlashcardsUseCase(flashcardsRepository, roadmapRepository)
    val registrarErrorFlashcardUseCase = RegistrarErrorFlashcardUseCase(historialRepository)
    val finalizarFlashcardsUseCase = FinalizarFlashcardsUseCase(
        flashcardsRepository,
        RegistrarLeccionCompletadaUseCase(roadmapRepository),
        moduloHidromiel
    )
    val registrarFlashcardsSinSuperarUseCase = RegistrarFlashcardsSinSuperarUseCase(moduloHidromiel)

    // CU-08 Reto de fin de mundo (Ragnarök)
    val retoFinMundoRepository: RetoFinMundoRepository = RetoFinMundoRepositoryImpl(database.retoFinMundoDao())
    val obtenerRetoFinMundoUseCase = ObtenerRetoFinMundoUseCase(roadmapRepository, leccionRepository, moduloHidromiel)
    val registrarErrorRetoUseCase = RegistrarErrorRetoUseCase(historialRepository)
    val finalizarRetoFinMundoUseCase = FinalizarRetoFinMundoUseCase(retoFinMundoRepository, roadmapRepository, moduloHidromiel)
    val abandonarRetoFinMundoUseCase = AbandonarRetoFinMundoUseCase(retoFinMundoRepository, moduloHidromiel)

    // CU-09 Resolver reto de arrastrar y soltar
    val retoRepository: RetoRepository = RetoRepositoryImpl(
        database.retoDao(), database.elementoArrastrableDao(), database.posicionDestinoDao()
    )
    val obtenerRetoUseCase = ObtenerRetoUseCase(retoRepository)
    val validarColocacionUseCase = ValidarColocacionUseCase()
    val calcularPuntajeRetoUseCase = CalcularPuntajeRetoUseCase()
    val registrarErrorRetoArrastrarUseCase = RegistrarErrorRetoArrastrarUseCase(historialRepository)
    val finalizarRetoUseCase = FinalizarRetoUseCase(
        retoRepository, calcularPuntajeRetoUseCase, RegistrarLeccionCompletadaUseCase(roadmapRepository), moduloHidromiel
    )
    val abandonarRetoUseCase = AbandonarRetoUseCase(moduloHidromiel)

    // CU-12 Retar a la bruja (La Völva)
    val volvaRepository: VolvaRepository = VolvaRepositoryImpl(database.volvaDao())
    val validarAccesoVolvaUseCase = ValidarAccesoVolvaUseCase(roadmapRepository, volvaRepository, moduloHidromiel)
    val apostarVolvaUseCase = ApostarVolvaUseCase(moduloHidromiel, volvaRepository, roadmapRepository)
    val armarConsultaVolvaUseCase = ArmarConsultaVolvaUseCase(roadmapRepository, volvaRepository)
    val resolverVisionUseCase = ResolverVisionUseCase(historialRepository)
    val finalizarConsultaVolvaUseCase = FinalizarConsultaVolvaUseCase(volvaRepository, moduloHidromiel)
    val abandonarConsultaVolvaUseCase = AbandonarConsultaVolvaUseCase(volvaRepository)

    // Repaso del mundo en el pozo de Mímir
    val obtenerPreguntasRepasoUseCase = ObtenerPreguntasRepasoUseCase(
        roadmapRepository, preguntaFlashRepository, diagnosticoRepository
    )

    // CU-13 Duelo de enigmas
    private val dueloRepository: DueloRepository = DueloRepositoryImpl(
        database.conceptoEnigmaDao(), database.preguntaEnigmaDao(), database.resultadoDueloDao()
    )
    val validarAccesoDueloUseCase = ValidarAccesoDueloUseCase(roadmapRepository, dueloRepository, moduloHidromiel)
    val armarDueloUseCase = ArmarDueloUseCase(roadmapRepository, dueloRepository)
    val apostarDueloUseCase = ApostarDueloUseCase(moduloHidromiel, dueloRepository)
    val finalizarDueloUseCase = FinalizarDueloUseCase(dueloRepository, moduloHidromiel, historialRepository)
    val abandonarDueloUseCase = AbandonarDueloUseCase(dueloRepository)

    private companion object {
        const val CLAVE_MODO_ADMIN = "modo_admin"
    }
}
