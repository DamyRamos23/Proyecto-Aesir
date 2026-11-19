# Graph Report - Proyecto-Aesir  (2026-11-11)

## Corpus Check
- 235 files · ~225,370 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 48 file(s) not represented in the graph (top: .drawio 20, .xml 14, (none) 6)

## Summary
- 2462 nodes · 8041 edges · 117 communities (86 shown, 31 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 305 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Repositorio Diagnóstico
- Pantallas: animaciones comunes
- Repositorio Roadmap
- ModoAdmin y UseCases de mundo
- Pantallas: diagnóstico/flashcards/memorama
- Hidromiel flash y AppContainer
- Estilo Ragnarök (fuentes, botones)
- Repositorio Memorama
- Script generar_diagramas.py
- Arte madera/Mímir/Ragnarök
- Escena Pozo de Mímir
- Navegación (OdinNavGraph)
- Tests ModuloHidromiel
- Título pintado y fuentes mockup
- UI Hidromiel (contador/avisos)
- Cartas Memorama mundo
- DAOs Reto Arrastrar
- Dominio Reto Contrarreloj
- Dibujo escenas Ragnarök
- UI Completar Diagrama
- DAO Historial de errores
- Catálogo niveles y control acceso
- Tests Repaso Mímir
- DAOs Minijuegos
- DAO Hidromiel
- Repositorio Pregunta Flash
- Tests ObtenerPreguntaFlash
- Componentes UI y Ragnarök
- Roadmap temporal y Próximamente
- Repositorio Reto Fin de Mundo
- Componentes rúnicos comunes
- Salón flashcards UI
- Docs: arquitectura SCS Sprint 2
- Pantalla diagnóstico (pozo)
- RagnarokViewModel
- Archivos de test
- DAOs Crucigrama
- Estilo de mundos (emblemas)
- Repositorio Reto Arrastrar
- CrucigramaViewModel
- Fakes Hidromiel (tests)
- Estado pregunta flash
- Repositorio Hidromiel
- ModuloHidromiel: reglas y avisos
- Movimientos y reserva hidromiel
- Dominio Flashcards
- Dibujo paisajes y Valknut
- DAO Lección
- Persistencia Contrarreloj
- CombinacionSecreta y tests admin
- Cartas flashcards en juego
- Tests Memorama
- Pantalla resultado crucigrama/escudo
- Tests Diagnóstico
- Banco preguntas y datos iniciales
- DAO Diagnóstico
- DAO Memorama
- DAO Tema
- Formas carta/tablilla
- Cálculo recompensas y costos
- Tests Crucigrama
- MainActivity
- FlashcardsViewModel
- PreguntaFlashViewModel
- RetoArrastrarViewModel
- DAO Mundo
- Modelos de hidromiel
- Fakes lección/historial
- Tests Reto Arrastrar
- Pantalla Pregunta Flash
- Validar respuesta ejercicio
- Pantalla Crucigrama
- Pantalla Lección
- DAO banco Pregunta Flash
- Repositorio Crucigrama
- Docs: pruebas integración CU-06/CU-11
- Árbol de habilidades: actividades
- Tema Material
- DAO Ejercicio
- DAO intentos Pregunta Flash
- Modelo Crucigrama
- Resumen de lección
- Pantalla Roadmap
- RoadmapViewModel
- OdinDatabase
- Árbol de habilidades: geometría
- Tablero crucigrama
- Validar palabra
- Tests ValidarRespuestaUseCase
- Adornos del salón
- Pantalla intro de tema
- Diagramas de reto
- Tests Contrarreloj
- CatalogoNivelesImpl
- ModuloHidromiel: flujos
- Tests Árbol de habilidades
- Tests tercer anillo
- Test instrumentado
- Globo de pista
- Opciones del pozo
- Avisos Memorama
- Destino de carta
- Clima del salón
- Resumen Memorama
- Arte Ragnarök
- Estado de nodo
- Test ObservarHidromiel
- Tipo de saber
- Test ejemplo
- gradlew

## God Nodes (most connected - your core abstractions)
1. `AppContainer` - 114 edges
2. `EstiloMundo` - 93 edges
3. `ModuloHidromiel` - 92 edges
4. `Tema` - 58 edges
5. `OdinDatabase` - 57 edges
6. `RoadmapRepository` - 53 edges
7. `Mundo` - 46 edges
8. `TextoAjustable()` - 39 edges
9. `ModuloHidromielTest` - 38 edges
10. `FakeRoadmapRepository` - 37 edges

## Surprising Connections (you probably didn't know these)
- `INT-031 Saldo e historial persisten al reabrir` --references--> `OdinDatabase`  [INFERRED]
  docs/pruebas/PruebasIntegracion_CU06_CU11.md → app/src/main/java/com/aesir/odin/data/local/OdinDatabase.kt
- `INT-032 Flujo normal contrarreloj desde Mundos` --references--> `OdinDatabase`  [INFERRED]
  docs/pruebas/PruebasIntegracion_CU06_CU11.md → app/src/main/java/com/aesir/odin/data/local/OdinDatabase.kt
- `INT-035 Abandonar con el boton Atras` --references--> `OdinDatabase`  [INFERRED]
  docs/pruebas/PruebasIntegracion_CU06_CU11.md → app/src/main/java/com/aesir/odin/data/local/OdinDatabase.kt
- `Capa de Fuentes de datos (Room, DAOs, Entities, contenido semilla)` --references--> `OdinDatabase`  [INFERRED]
  docs/Secciones_SCS_Sprint2.md → app/src/main/java/com/aesir/odin/data/local/OdinDatabase.kt
- `Excepcion controlada HidromielFlashRepositoryImpl` --references--> `HidromielFlashRepositoryImpl`  [INFERRED]
  docs/Secciones_SCS_Sprint2.md → app/src/main/java/com/aesir/odin/data/repository/HidromielFlashRepositoryImpl.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Arquitectura por capas de ODIN** — docs_secciones_scs_sprint2_capa_presentacion, docs_secciones_scs_sprint2_capa_negocio, docs_secciones_scs_sprint2_capa_persistencia, docs_secciones_scs_sprint2_capa_fuentes_datos [EXTRACTED 1.00]
- **Flujo de integracion del reto contrarreloj** — app_src_main_java_com_aesir_odin_ui_retocontrareloj_retocontrarrelojviewmodel_retocontrarrelojviewmodel, app_src_main_java_com_aesir_odin_domain_usecase_reto_armarretocontrarrelojusecase_armarretocontrarrelojusecase, app_src_main_java_com_aesir_odin_domain_usecase_reto_pagarentradaretocontrarrelojusecase_pagarentradaretocontrarrelojusecase, app_src_main_java_com_aesir_odin_domain_usecase_reto_finalizarretocontrarrelojusecase_finalizarretocontrarrelojusecase, app_src_main_java_com_aesir_odin_domain_hidromiel_modulohidromiel_modulohidromiel [EXTRACTED 1.00]
- **Banco de preguntas compartido (pregunta flash, contrarreloj, repaso Mimir)** — docs_secciones_scs_sprint2_cu_06_pregunta_flash, docs_secciones_scs_sprint2_cu_11_reto_contrarreloj, readme_pozo_mimir_repaso, app_src_main_java_com_aesir_odin_domain_repository_preguntaflashrepository_preguntaflashrepository [INFERRED 0.85]

## Communities (117 total, 31 thin omitted)

### Community 0 - "Repositorio Diagnóstico"
Cohesion: 0.06
Nodes (23): DiagnosticoRepositoryImpl, EstadoDiagnostico, COMPLETADA, OMITIDA, PENDIENTE, PreguntaDiagnostica, RespuestaDiagnostica, ResultadoDiagnostico (+15 more)

### Community 1 - "Pantallas: animaciones comunes"
Cohesion: 0.15
Nodes (4): FondoMundo(), cubic(), EstilosMundo, OdinPaleta

### Community 2 - "Repositorio Roadmap"
Cohesion: 0.06
Nodes (10): RoadmapRepositoryImpl, Mundo, Tema, RoadmapRepository, ValidarAccesoDiagnosticoUseCase, conModoAdmin(), ObtenerMundosUseCase, ValidarAccesoNivelUseCase (+2 more)

### Community 3 - "ModoAdmin y UseCases de mundo"
Cohesion: 0.11
Nodes (12): ModoAdmin, ObtenerIntroduccionTemaUseCase, ObtenerMundoUseCase, ObtenerTemasPorMundoUseCase, RegistrarIntroduccionVistaUseCase, ContextoResumen, ResumenLeccionViewModel, Cargando (+4 more)

### Community 4 - "Pantallas: diagnóstico/flashcards/memorama"
Cohesion: 0.08
Nodes (3): centroEn(), pulsable(), Lienzo

### Community 5 - "Hidromiel flash y AppContainer"
Cohesion: 0.09
Nodes (28): HidromielFlashRepositoryImpl, AppContainer, ResultadoNivel, ABANDONADO, NO_SUPERADO, SUPERADO, ModuloHidromiel, FinalizarCrucigramaUseCase (+20 more)

### Community 6 - "Estilo Ragnarök (fuentes, botones)"
Cohesion: 0.15
Nodes (39): alegreya(), BarraRagnarok(), BotonRagnarok(), cinzel(), desdeRG(), enRG(), hastaRG(), MedallonVista() (+31 more)

### Community 7 - "Repositorio Memorama"
Cohesion: 0.07
Nodes (18): MemoramaRepositoryImpl, EstadoTarjeta, ENCONTRADA, OCULTA, VISIBLE, ParConcepto, ResultadoMemorama, TarjetaMemorama (+10 more)

### Community 8 - "Script generar_diagramas.py"
Cohesion: 0.06
Nodes (11): diagrama_capas(), Doc, draw_layer(), layer_width(), q(), sub_size(), dibujar(), run() (+3 more)

### Community 9 - "Arte madera/Mímir/Ragnarök"
Cohesion: 0.07
Nodes (5): ColoresSalon, RG, dibujarFuente(), elipse(), FuenteMimir()

### Community 11 - "Escena Pozo de Mímir"
Cohesion: 0.13
Nodes (33): agua(), aguja(), anillos(), aspas(), BotonPozo(), brocal(), chorro(), cilindro() (+25 more)

### Community 12 - "Navegación (OdinNavGraph)"
Cohesion: 0.10
Nodes (7): PantallaProximamente(), LeccionScreen(), ResumenLeccionPlaceholder(), MundosScreen(), OdinNavGraph, Dato(), ResumenRetoScreen()

### Community 13 - "Tests ModuloHidromiel"
Cohesion: 0.13
Nodes (3): ContrarrelojAbandonado, PreguntaFlashResuelta, ModuloHidromielTest

### Community 14 - "Título pintado y fuentes mockup"
Cohesion: 0.09
Nodes (4): HandPaintedTitle(), en(), FuentesMockup, LienzoDiseno()

### Community 15 - "UI Hidromiel (contador/avisos)"
Cohesion: 0.10
Nodes (10): LeccionRepositoryImpl, toDomain(), Ejercicio, Leccion, ResultadoLeccion, LeccionRepository, FinalizarLeccionUseCase, ObtenerLeccionUseCase (+2 more)

### Community 16 - "Cartas Memorama mundo"
Cohesion: 0.09
Nodes (5): AvisosHidromiel(), ContadorHidromiel(), IconoCuerno(), PlacaAviso(), LineaEntrada()

### Community 17 - "DAOs Reto Arrastrar"
Cohesion: 0.10
Nodes (16): AnversoCarta(), CartaMemoramaMundo(), dibujarMaterialReverso(), EncabezadoMemoramaMundo(), enMK(), FiguraCarta, ARCO, ESCUDO (+8 more)

### Community 18 - "Dominio Reto Contrarreloj"
Cohesion: 0.12
Nodes (15): ElementoArrastrableDao, PosicionDestinoDao, RetoDao, Contenido, DatosArrastrar, Diagrama, Hueco, Modalidad (+7 more)

### Community 19 - "Dibujo escenas Ragnarök"
Cohesion: 0.08
Nodes (14): RetoContrareloj, SinPreguntasDisponiblesException, SinTemasEstudiadosException, Abandonado, Cargando, EnCurso, Error, Estado (+6 more)

### Community 20 - "UI Completar Diagrama"
Cohesion: 0.13
Nodes (27): rememberTiempo(), Brasas(), circulo(), colorEmblemaRagnarok(), dibujarArbolito(), dibujarEclipse(), dibujarEmblemaRagnarok(), dibujarEscamas() (+19 more)

### Community 21 - "DAO Historial de errores"
Cohesion: 0.13
Nodes (25): ElementoArrastrable, BotonOdin(), Banco(), Cabecera(), CompletarDiagramaContent(), EstadoArrastre, EstadoPiedra, CORRECTA (+17 more)

### Community 22 - "Catálogo niveles y control acceso"
Cohesion: 0.11
Nodes (7): HistorialErrorDao, HistorialErrorEntity, HistorialRepositoryImpl, toDomain(), HistorialError, HistorialRepository, RegistrarErrorLeccionUseCase

### Community 23 - "Tests Repaso Mímir"
Cohesion: 0.12
Nodes (16): CatalogoNiveles, ControlAcceso, EstadoNivel, BLOQUEADO, DISPONIBLE, GRATUITO, SUPERADO, NivelInfo (+8 more)

### Community 24 - "DAOs Minijuegos"
Cohesion: 0.12
Nodes (5): RepasoMimirTest, FakeRoadmapRepository, tema(), ArmarRetoContrarrelojUseCaseTest, preguntaFlash()

### Community 25 - "DAO Hidromiel"
Cohesion: 0.14
Nodes (7): ResultadoFlashcardsDao, RetoFinMundoDao, ResultadoFlashcardsEntity, ResultadoRetoFinMundoEntity, FlashcardsRepositoryImpl, ResultadoFlashcards, FlashcardsRepository

### Community 26 - "Repositorio Pregunta Flash"
Cohesion: 0.15
Nodes (15): RetoFinMundoRepositoryImpl, NivelFinalizado, Bloqueado, DisponibilidadReto, Disponible, IntentoRetoFinMundo, PreguntaReto, ReglasReto (+7 more)

### Community 27 - "Tests ObtenerPreguntaFlash"
Cohesion: 0.18
Nodes (20): BotonVolver(), DivisorOrnamental(), estiloRuna(), estiloTitulo(), MarcadorErrores(), PanelTallado(), PuntosProgreso(), SelloRunico() (+12 more)

### Community 28 - "Componentes UI y Ragnarök"
Cohesion: 0.22
Nodes (20): TextoAjustable(), alegreya(), aplastado(), Banner(), BarraSuperior(), BotonTablilla(), CabeceraSalon(), cinzel() (+12 more)

### Community 30 - "Repositorio Reto Fin de Mundo"
Cohesion: 0.17
Nodes (5): HidromielDao, MovimientoHidromielEntity, MundoDesbloqueadoEntity, NivelSuperadoEntity, ReservaHidromielEntity

### Community 31 - "Componentes rúnicos comunes"
Cohesion: 0.17
Nodes (8): aDominio(), PreguntaFlashRepositoryImpl, PreguntaFlash, PreguntaFlashAttempt, PreguntaFlashRepository, RegistrarTiempoAgotadoPreguntaFlashUseCase, ValidarRespuestaPreguntaFlashUseCase, FakePreguntaFlashRepository

### Community 32 - "Salón flashcards UI"
Cohesion: 0.20
Nodes (6): ObtenerPreguntaFlashUseCase, FakePreguntaFlashRepository, preguntaFlash(), ObtenerPreguntaFlashUseCaseTest, RegistrarTiempoAgotadoPreguntaFlashUseCaseTest, ValidarRespuestaPreguntaFlashUseCaseTest

### Community 34 - "Pantalla diagnóstico (pozo)"
Cohesion: 0.14
Nodes (21): Capa de Fuentes de datos (Room, DAOs, Entities, contenido semilla), Capa de Negocio, Capa de Persistencia (RepositoryImpl), Capa de Presentacion (Jetpack Compose), CU-06 Pregunta flash, CU-09 Arrastrar y soltar, CU-10 Evaluacion diagnostica, CU-11 Reto contrarreloj (+13 more)

### Community 35 - "RagnarokViewModel"
Cohesion: 0.21
Nodes (16): RotuloPozo(), CuerpoPregunta(), EvaluacionDiagnosticaScreen(), LienzoBoton(), OpcionPozo(), PantallaLlamado(), PantallaPrueba(), ReglaPozo() (+8 more)

### Community 36 - "Archivos de test"
Cohesion: 0.12
Nodes (9): Batalla, Bloqueado, Cargando, Contexto, Entrada, Error, RagnarokUi, RagnarokViewModel (+1 more)

### Community 38 - "DAOs Crucigrama"
Cohesion: 0.18
Nodes (24): CU-06 Pregunta Flash (pruebas unitarias), CU-11 Reto Contrarreloj (pruebas unitarias), Android Studio Database Inspector, Hidromiel pregunta flash +5 correcta / -2 incorrecta, incompleta o sin respuesta, Puntaje contrarreloj +2 correcta / -1 incorrecta o incompleta; abandono -20 hidromiel, Reporte de pruebas Sprint 2 CU-06 y CU-11 (UNI-019 a UNI-035), RN-01 Saldo nunca negativo, UNI-019 Aparicion cada 5 minutos (+16 more)

### Community 39 - "Estilo de mundos (emblemas)"
Cohesion: 0.14
Nodes (5): ConceptoCrucigramaDao, ResultadoCrucigramaDao, DatosCrucigrama, ConceptoCrucigramaEntity, ResultadoCrucigramaEntity

### Community 40 - "Repositorio Reto Arrastrar"
Cohesion: 0.11
Nodes (18): FilaRunas(), FranjaRunica(), Emblema, MJOLNIR, OJO, VEGVISIR, YGGDRASIL, Escena (+10 more)

### Community 41 - "CrucigramaViewModel"
Cohesion: 0.17
Nodes (9): RetoRepositoryImpl, ModalidadReto, COMPLETAR_DIAGRAMA, ORDENAR_PROCESO, PosicionDestino, ResultadoReto, RetoArrastrar, RetoRepository (+1 more)

### Community 43 - "Estado pregunta flash"
Cohesion: 0.11
Nodes (3): FakeCatalogo, FakeHidromielRepository, FinalizarLeccionUseCaseTest

### Community 44 - "Repositorio Hidromiel"
Cohesion: 0.14
Nodes (9): EstadoPreguntaFlash, CORRECTA, INCOMPLETA, INCORRECTA, SIN_RESPUESTA, CambioHidromiel, HidromielFlashRepository, ObservarHidromielUseCase (+1 more)

### Community 45 - "ModuloHidromiel: reglas y avisos"
Cohesion: 0.11
Nodes (4): HidromielRepositoryImpl, HidromielRepository, MundoDesbloqueado, NivelSuperado

### Community 46 - "Movimientos y reserva hidromiel"
Cohesion: 0.16
Nodes (5): AvisoHidromiel, ResultadoAcceso, TipoAyuda, REVELAR_LETRA, REVELAR_PALABRA

### Community 47 - "Dominio Flashcards"
Cohesion: 0.12
Nodes (12): TipoMovimiento, ABANDONO_CONTRARRELOJ, AJUSTE_ADMIN, AYUDA_CRUCIGRAMA, ENTRADA_CONTRARRELOJ, ENTRADA_NIVEL, PREGUNTA_FLASH, RECOMPENSA_CONTRARRELOJ (+4 more)

### Community 48 - "Dibujo paisajes y Valknut"
Cohesion: 0.13
Nodes (13): EstadoTarjetaFlashcard, DOMINADA, FALLADA, PENDIENTE, PilaFlashcards, RespuestaFlashcard, TarjetaFlashcard, EstadoTablilla (+5 more)

### Community 49 - "DAO Lección"
Cohesion: 0.26
Nodes (18): amanecer(), aurora(), cordillera(), dibujarEmblema(), dibujarMaterial(), dibujarPaisaje(), dibujarValknut(), grieta() (+10 more)

### Community 50 - "Persistencia Contrarreloj"
Cohesion: 0.17
Nodes (3): LeccionDao, LeccionEntity, ResultadoLeccionEntity

### Community 51 - "CombinacionSecreta y tests admin"
Cohesion: 0.16
Nodes (6): ResultadoContrarrelojDao, ResultadoContrarrelojEntity, RetoContrarrelojRepositoryImpl, ResultadoContrarreloj, RetoContrarrelojRepository, FakeRetoContrarrelojRepository

### Community 52 - "Cartas flashcards en juego"
Cohesion: 0.14
Nodes (3): CombinacionSecreta, ModoAdminTest, ContrarrelojModoAdminTest

### Community 53 - "Tests Memorama"
Cohesion: 0.24
Nodes (16): AbanicoSalon(), CaraConcepto(), CaraDescripcion(), CartaEnJuego(), CuerpoCarta(), dibujarClima(), dibujarMadera(), DorsoCarta() (+8 more)

### Community 54 - "Pantalla resultado crucigrama/escudo"
Cohesion: 0.16
Nodes (6): banco(), CalcularPuntosMemoramaUseCaseTest, FakeMemoramaRepository, FinalizarMemoramaUseCaseTest, GenerarMemoramaUseCaseTest, VerificarParejaUseCaseTest

### Community 55 - "Tests Diagnóstico"
Cohesion: 0.15
Nodes (13): Dato(), ResultadoCrucigramaScreen(), AnilloRunico(), Cuadrante, INFERIOR_DER, INFERIOR_IZQ, SUPERIOR_DER, SUPERIOR_IZQ (+5 more)

### Community 56 - "Banco preguntas y datos iniciales"
Cohesion: 0.20
Nodes (6): AccesoYOmisionDiagnosticoTest, EvaluarDiagnosticoUseCaseTest, FakeDiagnosticoRepository, FinalizarDiagnosticoUseCaseTest, responder(), mundo()

### Community 57 - "DAO Diagnóstico"
Cohesion: 0.17
Nodes (3): BancoPreguntas, Converters, DatosIniciales

### Community 58 - "DAO Memorama"
Cohesion: 0.21
Nodes (5): DiagnosticoDao, DatosDiagnostico, P, PreguntaDiagnosticaEntity, ResultadoDiagnosticoEntity

### Community 59 - "DAO Tema"
Cohesion: 0.20
Nodes (4): MemoramaDao, DatosMemorama, ParConceptoEntity, ResultadoMemoramaEntity

### Community 61 - "Cálculo recompensas y costos"
Cohesion: 0.15
Nodes (10): FondoTablilla(), EmblemaSalon, MARTILLO, OJO, VALKNUT, YGGDRASIL, FormaCarta, FormaTablilla (+2 more)

### Community 62 - "Tests Crucigrama"
Cohesion: 0.16
Nodes (3): CalculadoraRecompensa, PoliticaCosto, ReglasYCalculosTest

### Community 63 - "MainActivity"
Cohesion: 0.23
Nodes (5): GenerarCrucigramaUseCase, FakeCrucigramaRepository, FinalizarCrucigramaUseCaseTest, GenerarCrucigramaUseCaseTest, ValidarYPuntajeTest

### Community 64 - "FlashcardsViewModel"
Cohesion: 0.14
Nodes (3): MainActivity, PreguntaFlashScreen(), AppContainer

### Community 65 - "PreguntaFlashViewModel"
Cohesion: 0.20
Nodes (4): ConceptoDificil, Contexto, FlashcardsViewModel, ResumenFlashcards

### Community 69 - "Fakes lección/historial"
Cohesion: 0.12
Nodes (11): ContrarrelojFinalizado, DiagnosticoCompletado, MovimientoHidromiel, OrigenSuperado, DIAGNOSTICO, JUGADO, ResultadoFlash, CORRECTA (+3 more)

### Community 70 - "Tests Reto Arrastrar"
Cohesion: 0.15
Nodes (4): FakeHistorialRepository, FakeLeccionRepository, RegistrarErrorLeccionUseCaseTest, RegistrarErrorRetoTest

### Community 71 - "Pantalla Pregunta Flash"
Cohesion: 0.25
Nodes (5): FakeRetoRepository, FinalizarRetoTest, ObtenerRetoTest, reto(), ValidarYPuntajeTest

### Community 72 - "Validar respuesta ejercicio"
Cohesion: 0.29
Nodes (10): ResultadoPreguntaFlash, BannerResultado(), CabeceraFlash(), ContenidoPreguntaFlash(), DivisorCuadro(), OpcionFlash(), PanelPregunta(), PreguntaFlashDialogo() (+2 more)

### Community 73 - "Pantalla Crucigrama"
Cohesion: 0.17
Nodes (7): EstadoRespuesta, CORRECTA, INCOMPLETA, INCORRECTA, ResultadoRespuesta, ActualizarPuntajeUseCase, ValidarRespuestaEjercicioUseCase

### Community 74 - "Pantalla Lección"
Cohesion: 0.27
Nodes (11): BotonAyuda(), Casilla(), Colocacion, CrucigramaScreen(), ErrorGeneracion(), FilaRunas(), FilaTeclas(), fondoTecla() (+3 more)

### Community 75 - "DAO banco Pregunta Flash"
Cohesion: 0.19
Nodes (12): ContenidoLeccion(), ContenidoLeccionPreview(), DialogoLeccionFallida(), DialogoSalir(), EstadoOpcion, CORRECTA, FALTANTE, INCORRECTA (+4 more)

### Community 77 - "Docs: pruebas integración CU-06/CU-11"
Cohesion: 0.27
Nodes (4): CrucigramaRepositoryImpl, ConceptoCrucigrama, ResultadoCrucigrama, CrucigramaRepository

### Community 78 - "Árbol de habilidades: actividades"
Cohesion: 0.28
Nodes (13): CU-06 Pregunta Flash (integracion), CU-11 Reto Contrarreloj (integracion), INT-027 Flujo normal pregunta flash desde Mundos, INT-028 Pregunta pendiente al terminar una leccion, INT-029 Leccion completada habilita preguntas flash, INT-030 Pregunta flash no interrumpe el contrarreloj, INT-031 Saldo e historial persisten al reabrir, INT-033 Saldo insuficiente para la entrada (+5 more)

### Community 79 - "Tema Material"
Cohesion: 0.17
Nodes (7): Actividad, ARRASTRAR, CRUCIGRAMA, FLASHCARDS, LECCION, MEMORAMA, POR_DEFINIR

### Community 83 - "Resumen de lección"
Cohesion: 0.42
Nodes (9): Celda, Crucigrama, PalabraCrucigrama, ResultadoPalabra, AreaCuadricula(), colocarGlobo(), ContenidoCrucigrama(), EncabezadoCrucigrama() (+1 more)

### Community 84 - "Pantalla Roadmap"
Cohesion: 0.36
Nodes (9): Chispa, Dato(), Estadisticas(), PiedraRunica(), ResumenAnimado(), ResumenLeccionScreen(), suave(), TarjetaDesbloqueo() (+1 more)

### Community 85 - "RoadmapViewModel"
Cohesion: 0.29
Nodes (5): EscudoTemas(), Insignia(), NodoTema(), PanelTema(), RoadmapScreen()

### Community 89 - "Validar palabra"
Cohesion: 0.36
Nodes (5): Direccion, HORIZONTAL, VERTICAL, Posicion, Tablero

### Community 90 - "Tests ValidarRespuestaUseCase"
Cohesion: 0.24
Nodes (3): normalizarPalabra(), ValidarPalabraUseCase, aRunas()

### Community 92 - "Pantalla intro de tema"
Cohesion: 0.42
Nodes (8): AdornosSalon(), brujula(), cuerno(), escalado(), escudo(), gema(), moneda(), Quad

### Community 93 - "Diagramas de reto"
Cohesion: 0.28
Nodes (5): ContenidoPergamino(), dibujarPergamino(), Pergamino(), rodillo(), TemaIntroScreen()

### Community 98 - "Tests Árbol de habilidades"
Cohesion: 0.25
Nodes (8): estadoOpcion(), EstadoOpcionBatalla, APAGADA, CORRECTA, FALTO, INCORRECTA, NEUTRA, SELECCIONADA

### Community 102 - "Opciones del pozo"
Cohesion: 0.29
Nodes (6): FormaGlobo, LadoGlobo, ABAJO, ARRIBA, DERECHA, IZQUIERDA

### Community 103 - "Avisos Memorama"
Cohesion: 0.33
Nodes (6): EstadoOpcionPozo, APAGADA, CORRECTA, ELEGIDA, INCORRECTA, NEUTRA

### Community 104 - "Destino de carta"
Cohesion: 0.33
Nodes (6): Aviso, COMPLETO, ELIGE_SEGUNDA, ELIGE_UNA, NO_SON_PAREJA, PAREJA

### Community 105 - "Clima del salón"
Cohesion: 0.40
Nodes (4): DestinoCarta, DOMINADAS, MAZO, estrella()

### Community 106 - "Resumen Memorama"
Cohesion: 0.40
Nodes (5): ClimaSalon, BRASAS, HOJAS, NIEVE, NINGUNO

### Community 107 - "Arte Ragnarök"
Cohesion: 0.50
Nodes (3): Dato(), mensajeSegun(), ResumenMemoramaScreen()

### Community 109 - "Test ObservarHidromiel"
Cohesion: 0.40
Nodes (5): EstadoNodo, ACTUAL, BLOQUEADO, COMPLETADO, PROXIMAMENTE

### Community 111 - "Test ejemplo"
Cohesion: 0.50
Nodes (4): TipoSaber, COMIENZO, DOMINADO, PENDIENTE

## Knowledge Gaps
- **142 isolated node(s):** `ORDENAR`, `DIAGRAMA`, `BLOQUEADO`, `DISPONIBLE`, `GRATUITO` (+137 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 416 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **31 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AppContainer` connect `Hidromiel flash y AppContainer` to `Repositorio Diagnóstico`, `Pantallas: animaciones comunes`, `Repositorio Roadmap`, `ModoAdmin y UseCases de mundo`, `Repositorio Memorama`, `Navegación (OdinNavGraph)`, `UI Hidromiel (contador/avisos)`, `Cartas Memorama mundo`, `Dibujo escenas Ragnarök`, `Catálogo niveles y control acceso`, `DAO Hidromiel`, `Repositorio Pregunta Flash`, `Roadmap temporal y Próximamente`, `Componentes rúnicos comunes`, `Salón flashcards UI`, `Archivos de test`, `CrucigramaViewModel`, `Fakes Hidromiel (tests)`, `Repositorio Hidromiel`, `ModuloHidromiel: reglas y avisos`, `CombinacionSecreta y tests admin`, `MainActivity`, `FlashcardsViewModel`, `DAO Mundo`, `Pantalla Crucigrama`, `Docs: pruebas integración CU-06/CU-11`, `Pantalla Roadmap`, `RoadmapViewModel`, `Árbol de habilidades: geometría`, `Tests ValidarRespuestaUseCase`, `Diagramas de reto`, `ModuloHidromiel: flujos`?**
  _High betweenness centrality (0.188) - this node is a cross-community bridge._
- **Are the 9 inferred relationships involving `ModuloHidromiel` (e.g. with `ModoAdminTest` and `ModuloHidromielTest`) actually correct?**
  _`ModuloHidromiel` has 9 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ORDENAR`, `DIAGRAMA`, `BLOQUEADO` to the rest of the system?**
  _142 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Repositorio Diagnóstico` be split into smaller, more focused modules?**
  _Cohesion score 0.06398730830248546 - nodes in this community are weakly interconnected._
- **Why does `ModuloHidromiel` connect `Hidromiel flash y AppContainer` to `Repositorio Diagnóstico`, `ModoAdmin y UseCases de mundo`, `Repositorio Memorama`, `Tests ModuloHidromiel`, `UI Hidromiel (contador/avisos)`, `Tests Repaso Mímir`, `Repositorio Pregunta Flash`, `Pantalla diagnóstico (pozo)`, `Archivos de test`, `Docs: pruebas unitarias CU-06/CU-11`, `Fakes Hidromiel (tests)`, `Estado pregunta flash`, `ModuloHidromiel: reglas y avisos`, `Movimientos y reserva hidromiel`, `Cartas flashcards en juego`, `Pantalla resultado crucigrama/escudo`, `Banco preguntas y datos iniciales`, `Tests Crucigrama`, `MainActivity`, `PreguntaFlashViewModel`, `DAO Mundo`, `Fakes lección/historial`, `Pantalla Pregunta Flash`, `Árbol de habilidades: actividades`, `OdinDatabase`, `CatalogoNivelesImpl`, `Opciones de batalla`?**
  _High betweenness centrality (0.101) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `OdinDatabase` (e.g. with `INT-031 Saldo e historial persisten al reabrir` and `INT-032 Flujo normal contrarreloj desde Mundos`) actually correct?**
  _`OdinDatabase` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Should `Repositorio Roadmap` be split into smaller, more focused modules?**
  _Cohesion score 0.05827067669172932 - nodes in this community are weakly interconnected._