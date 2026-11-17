# ODIN (Android)

## El escudo de Mundos es una moneda

El escudo de la pantalla **Mundos** (los cuatro cuadrantes con el Valknut) funciona como moneda (`ui/moneda/`):

- Al abrir la app el escudo **cae del cielo dando vueltas**, rebota, se bambolea y queda en la cara de Mundos.
  Mientras cae no se puede tocar; al terminar aparecen el título, el panel y lo demás.
- **Deslízalo** para lanzarlo: entre más fuerte el gesto, más alto sube y más vueltas da (3 a 41 medias vueltas).
  Vibra al caer y suelta chispas. Tocar los cuadrantes y el Valknut sigue funcionando igual.
- Cada lanzamiento cae en **la siguiente cara**: Mundos → Hasta el Alba → La Apuesta de Brokkr → La Völva →
  Por revelar → Mundos otra vez. En las caras de casos de uso el título y el panel cambian, y *Entrar* abre
  `CaraMonedaScreen` («este modo todavía se está forjando») hasta que exista el modo. Esas caras también giran
  con un toque.
- **Agarrarlo con el dedo**: al arrastrar, el escudo se levanta poco a poco y se voltea siguiendo el dedo
  (se asoma la siguiente cara al pasar de canto, con un toquecito de vibración). Soltado despacio regresa a su
  cara, o termina de voltearse si ya pasó de canto; soltado rápido (más de 700 dp/s) se lanza desde donde estaba.
- La pantalla de Mundos es fija (no se desplaza): el escudo toma el espacio libre y se adapta al tamaño de
  la pantalla; abajo queda un panel compacto de alto fijo (nombre, descripción corta y *Entrar*).
- **Easter egg**: mientras el escudo gira en el aire, deslízalo o tócalo para darle más impulso: vuelve a subir
  y sigue girando (tantas veces como quieras); siempre cae en la cara que tocaba.
- Si la descripción no cabe en el panel, aparece «Leer más ▴»: al tocar el texto se despliega hacia arriba con
  la descripción completa (y el botón *Entrar*); otro toque o girar el escudo la cierra.
- El canto del escudo es dorado (antes era oscuro y al girar se veía como un parpadeo negro).
- El panel de abajo ya no desaparece al girar: se atenúa mientras el escudo se mueve y su contenido se funde
  con el de la cara nueva.
- La caída solo se ve una vez por sesión (`EstadoMoneda.entradaVista`).

Archivos: `CarasMoneda.kt` (textos, colores, orden; aquí se renombra la cuarta cara), `FisicaMoneda.kt`
(fórmulas del vuelo; prueba `FisicaMonedaTest`), `EscudoGiratorio.kt` (envuelve el escudo y lo hace girar),
`DibujoMoneda.kt` (caras de los casos de uso con el mismo aro rúnico, canto, sombra y chispas) y
`CaraMonedaScreen.kt`. Ruta nueva en `OdinNavGraph`: `caraMoneda/{cara}`.

## Ícono

El ícono de la app es la moneda dorada: adaptativo en Android 8+ (`ic_launcher_moneda.png` sobre
`ic_launcher_fondo_moneda.xml`), PNG por densidad para Android 7 (`mipmap-*/ic_launcher*.png`) y
`drawable-nodpi/ic_launcher_ios.png` (1024 px) para el proyecto de iOS.

## Modo admin oculto

En la pantalla **Mundos**, toca los mundos en este orden, sin pausas de más de 1.5 s:

**1 → 2 → 4 → 3 → 1 → 2 → 4 → 3**

(arriba-izq, arriba-der, abajo-der, abajo-izq, dos veces). Vibra, aparece un aviso y
la insignia **ADMIN** junto al título. La misma combinación lo desactiva.

Con el modo admin:
- todos los mundos y niveles se abren, incluido el Ragnarök (reto de fin de mundo);
- las entradas y las ayudas del crucigrama no cobran hidromiel;
- el **pozo de Mímir** del roadmap despierta sin superar el Ragnarök (para probar el repaso);
- el **reto contrarreloj** enciende todas las runas: toma preguntas de todas las lecciones;
- superar niveles se registra normalmente.

No se guarda: cada apertura de la app inicia sin modo admin (se borra el valor `odin` / `modo_admin` de versiones anteriores).
Código: `domain/hidromiel/ModoAdmin.kt`; prueba: `ModoAdminTest`.

## Roadmap de cuatro anillos (SRS v3.2)

Cada mundo tiene ahora **10 temas** y **18 niveles** alrededor del Ragnarök (`ArbolHabilidades`):

| Anillo | Niveles | Conexiones |
|---|---|---|
| 1 · interior | 4 lecciones (temas 1 a 4) | en círculo, la última con la primera |
| 2 · exterior | 6 minijuegos: crucigrama, memorama y flashcards, 2 de cada uno | cada uno con **la lección de su tema** (`ArbolHabilidades.TEMA_DE_MINIJUEGO` = temas 1, 2, 3, 3, 4, 1) |
| 3 | 2 retos de arrastrar y soltar (CU-09) | cada uno con los dos minijuegos que tiene más cerca |
| 4 | 6 lecciones (temas 5 a 10) | con sus vecinas del anillo y con el reto o minijuego que tienen más cerca |
| centro | Ragnarök | se habilita al superar los 18 niveles |

- **Todo nivel pertenece a un tema** (`temas.temaBaseId`): el crucigrama (6 a 8 palabras), el memorama (6 pares) y las flashcards usan solo los conceptos de su tema; el reto de arrastrar sugiere repasar la lección de su tema; el diagnóstico reconoce como superados todos los niveles de cada tema conocido.
- Se quitaron las 6 piedras «por definir» del antiguo tercer anillo. Los dos retos conservan sus ids (`rt_<mundo>_1` y `rt_<mundo>_5`) para no perder el progreso.
- Contenido de los temas 5 a 10 en `data/local/ContenidoTemas.kt` (lección con 5 ejercicios, 7 conceptos, 2 preguntas diagnósticas, 3 preguntas flash y el enigma del gigante), más 2 conceptos nuevos para cada tema 1 a 4:
  - UML: Estados, Objetos, Componentes, Despliegue, Paquetes y Comunicación.
  - Casos de Uso: Generalización, Tipos de actores, Escenarios, Niveles de objetivo, Trazabilidad y De casos de uso a pruebas.
  - Ciclo de Vida: Viabilidad, Planificación, Despliegue e implantación, Gestión de la configuración, Aseguramiento de la calidad y Retiro.
  - Modelos Tradicionales: Prototipos, Evolutivo, RAD, Proceso Unificado, Iterativo y Basado en componentes.
- Ragnarök: 20 preguntas, 2 al azar de cada uno de los 10 temas. Evaluación diagnóstica: 20 preguntas, 2 por tema.
- Base de datos v14 (`MIGRACION_13_14`): columna `temas.temaBaseId` y, en el contrarreloj, `hidromiel` y `terminado`. `DatosIniciales.sincronizar` pone al día la estructura y el contenido en cada apertura sin tocar el progreso.
- Pruebas: `ArbolHabilidadesTest` y `ContenidoRoadmapTest` (18 niveles, 10 temas, contenido completo y crucigrama armable en los 40 temas).

## Reto de arrastrar y soltar (CU-09)

Posición 1 del anillo 3: **Ordenar el proceso** (`ORDENAR_PROCESO`); posición 2: **Completar el diagrama** (`COMPLETAR_DIAGRAMA`).
Contenido en `data/local/DatosArrastrar.kt`; diagramas en `ui/retoarrastrar/DiagramasReto.kt`.

Reglas: empieza en 100, −20 por confirmación incorrecta; las piedras mal colocadas se señalan y tiemblan;
al tercer fallo termina sin puntaje y **sin mostrar la solución** (RN-34) y sugiere repasar la lección del tema.
Las piedras se pueden arrastrar o tocar (piedra y luego lugar).

## Ajustes al SRS v3.2 (revisión de noviembre)

- **MOD-01, RN-07:** el módulo decide si un nivel se supera con el puntaje mínimo (60, o 70 en el Ragnarök); antes el crucigrama, el memorama y las flashcards se daban por superados con cualquier puntaje. Sus resúmenes dicen si el nivel quedó superado.
- **MOD-01, bono B:** puntaje ÷ 10 redondeado hacia abajo (antes se redondeaba al más cercano).
- **CU-07 crucigrama:** empieza en 100; −5 por respuesta incorrecta, −2 por letra revelada y −10 por palabra revelada; mínimo 0. Si no se puede generar y el alumno regresa al roadmap, se devuelve la entrada (`DEVOLUCION_ENTRADA`).
- **CU-11 contrarreloj:** hasta 20 preguntas; acierto +10 puntos y +2 de hidromiel, error −5 y −1, sin bajar de 0; máximo 3 por día; el contador se detiene mientras se confirma el abandono; abandonar resta 20; si la app se cierra durante el reto, al volver a abrirla cuenta como abandono.

## MOD-04 · Cuervos de Odín (notificaciones)

- Dominio: `domain/notificaciones/ModuloCuervos.kt` (tipos, prioridad, una al día, inactividad a los 3 días y una sola vez por periodo, nada con la app abierta). Prueba: `ModuloCuervosTest`.
- Android: `data/notificaciones/` — alarma diaria inexacta con `AlarmManager` a la hora configurada (19:00 por defecto), `ReceptorCuervos` (también reprograma al reiniciar el teléfono), canal «Cuervos de Odín» e ícono `ic_notificacion_cuervo`. Al tocar el aviso se abre la app en Mundos.
- La primera vez que se abre la app se pide el permiso (Android 13+). En **Ajustes** se activan o desactivan los avisos y se elige la hora; si el teléfono los tiene bloqueados, un enlace lleva a la configuración del sistema.

## Ragnarök animado

`ui/ragnarok/` replica los mockups del desafío:
- `ArteRagnarok.kt`: lobo, serpiente y suelo vectorizados (generado, no editar a mano);
- `EscenasRagnarok.kt`: escenas animadas (respiración del lobo, ojos, eclipse, fuego, rayos, brasas,
  ouroboros, medallones);
- `RagnarokScreen.kt`: entrada, batalla, superado, no superado y aviso.

Los fondos están en `res/drawable-nodpi/rag_fondo_*.jpg`.

## Flashcards de taberna

`ui/flashcards/` sigue el mockup «Flashcards de taberna», con un salón distinto por mundo:
- la carta muestra la descripción y el alumno elige el concepto;
- al fallar no se revela la correcta: la carta sale volando al mazo;
- al acertar la carta se voltea en 3D con un destello dorado y, al seguir, vuela en arco a «Dominadas»;
- al fallar la carta «dice que no», la opción tiembla y la carta se da la vuelta en el aire y cae al mazo;
- cada carta nueva se reparte desde el mazo y se voltea en el camino; los montones se aplastan al recibir una carta.

Las texturas de madera están en `res/drawable-nodpi/madera_veta_*.png`.

## Pozo de Mímir (CU-10) y repaso

- **Evaluación diagnóstica** en tres fases: el llamado (Mímir despierta), la prueba (20 preguntas, 2 por tema, con el
  rostro animado) y la revelación (temas conocidos marcados en el árbol).
- **Repaso**: la fuente del pozo aparece bajo el escudo del roadmap. Duerme hasta superar el Ragnarök del
  mundo (o con el modo admin) y entonces late; al tocarla abre un repaso de 10 preguntas repartidas entre las
  lecciones del mundo. No cobra ni da hidromiel. Código: `ObtenerPreguntasRepasoUseCase`, `ui/roadmap/FuenteMimir.kt`.

## Memorama por mundo (CU-04)

Cada mundo tiene su figura de carta: 1 lápidas de madera en arco, 2 rombos de piedra, 3 hexágonos de hierro,
4 escudos de obsidiana. Las cartas se reparten con un pop, se voltean en 3D, tiemblan al fallar y brincan al
hacer pareja. Código: `ui/memorama/MemoramaScreen.kt`.

## Hidromiel

- El contador (cuerno) se ve igual en todas las pantallas de juego: lección, crucigrama, memorama,
  flashcards, arrastrar, contrarreloj y Ragnarök.
- Los avisos emergentes solo salen para la **recompensa diaria** y la **recompensa por nivel** (cuando da
  hidromiel); los cobros ya se ven en el contador.

## Tipografías opcionales

Los mockups usan **Cinzel** y **Alegreya** (Google Fonts, licencia OFL). Para usarlas, copia sus `.ttf`
a `app/src/main/res/font/` con estos nombres:

`cinzel_regular.ttf`, `cinzel_bold.ttf`, `cinzel_black.ttf`,
`alegreya_regular.ttf`, `alegreya_italic.ttf`, `alegreya_medium.ttf`, `alegreya_bold.ttf`

Si no están, la app usa la serif del sistema con versalitas simuladas.

## Caras del escudo con pantalla propia

- **La Völva** → `VolvaScreen` (CU-12, ruta `volva`).
- La cuarta cara (`POR_REVELAR`) es ahora el **Duelo de enigmas** → `DueloEnigmasScreen` (CU-13, ruta `dueloEnigmas`).
- *Hasta el Alba* y *La Apuesta de Brokkr* siguen abriendo `CaraMonedaScreen` hasta que existan.
- El engrane junto al contador abre **Ajustes** (Visión de Heimdall). Tocar 7 veces el contador abre el menú de juegos ocultos;
  en modo admin, mantenerlo 3 s permite fijar el hidromiel.

## Duelo de enigmas contra el gigante (CU-13)

El gigante esconde un concepto entre 12 tarjetas; el alumno hace preguntas cerradas (sí/no) y descarta tarjetas.
El cuerno empieza con 6 sorbos (1 sorbo = 4 de hidromiel); preguntar gasta sorbos y, si se vacía, solo queda una adivinanza.
Código: `ui/duelo/`, `domain/model/DueloEnigmas.kt`, `domain/usecase/duelo/`, `DueloRepository(Impl)`,
`DueloDaos.kt`; contenido en `data/local/DatosEnigmas.kt`.

## Retar a la bruja — La Völva (CU-12)

Consulta de seis visiones (dos por cada Norna) sobre temas ya superados, con 15 s por visión.
- Apuesta de 15 a 70 de hidromiel, sin bajar del piso que asegura poder pagar el siguiente nivel (en modo admin es gratis).
- Lecturas del caldero: **Profecía cumplida** ×2.5 y **Lectura clara** ×1.5 (menos el diezmo de Odín del 10 %),
  **Humo del caldero** devuelve la apuesta y **Maldición** no paga.
- Cada consulta puede dar la siguiente de las 24 runas del Futhark antiguo.

Código: `ui/volva/`, `domain/model/Volva.kt`, `domain/usecase/volva/`, `VolvaRepository(Impl)`, `VolvaDao`;
visiones en `data/local/DatosVolva.kt`. Movimientos de hidromiel `APUESTA_VOLVA` y `PREMIO_VOLVA`.

## Base de datos

Versión 14, con migraciones que conservan el progreso y el hidromiel:
- `MIGRACION_9_10`: tablas del duelo de enigmas (CU-13).
- `MIGRACION_10_11`: tablas de la Völva (`visiones_volva`, `runas_obtenidas`, `consultas_volva`).
- `MIGRACION_11_12` y `MIGRACION_12_13`: Hasta el Alba, la forja y la racha.
- `MIGRACION_13_14`: roadmap de cuatro anillos (`temas.temaBaseId`) y contrarreloj (`hidromiel`, `terminado`).

## Pantallas de inicio de los modos del Valhalla

Las entradas de **La Völva**, **El gigante** (duelo), **Hasta el alba** y **La forja**
(Brokkr) usan los diseños aprobados en «ODÍN · Pantallas de inicio de los modos».

- El arte animado (SVG + animaciones CSS del diseño) está en `app/src/main/assets/escenas/escena_<id>.html`
  y se pinta con un `WebView` decorativo (`EscenaAnimada`). Se mueve en parallax con la inclinación
  del teléfono y se queda quieto si las animaciones del sistema están desactivadas.
- La interfaz (barra, título, lema, datos, apuesta, botón y «Cómo se juega») es Compose:
  `ui/components/odin/PantallaInicioModo.kt`, así que respeta TalkBack y la Visión de Heimdall.
- Al pulsar el botón la escena se activa (el caldero hierve y los cuervos huyen; las runas del
  gigante se agitan) y después de 1.7 s se cobra la apuesta y se abre el modo.
- La forja ya muestra su escena desde su cara del escudo, con el botón en
  «Próximamente» hasta que existan esos modos.
- Para regenerar las escenas si cambia el diseño: exporta los `.dc.html` del diseño a una carpeta
  (`volva.html`, `duelo.html`, `alba.html`, `brokkr.html`) y corre
  `python3 tools/escenas.py <carpeta> app/src/main/assets/escenas`.

## Sobrevivir oleadas — Hasta el Alba (CU-01)

Implementa el CU-01 del SRS v3.2 (RF-01 a RF-29). Se entra desde la cara «Hasta el Alba» del escudo de Mundos.

- **Inicio:** la pantalla aprobada del banquete (`escena_alba`), con la apuesta de 10 al menor entre 60 y el
  saldo menos el piso (RN-08), «1 noche hoy», los artefactos forjados y «Cómo se juega» con la tabla de las 7 oleadas.
  Bloqueos: Valhalla cerrado (A2.1), noche ya jugada con el tiempo que falta (A2.2), hidromiel insuficiente (A3.1)
  y sin preguntas de temas superados.
- **Batalla:** `escena_batalla` dibuja a los 7 enemigos (Draugr, Lobo de Fenrir, Jotun de hielo, Troll de montaña,
  Valquiria caída, Hijo de Muspel y El alba) con golpe, escudo roto y derrota animados. Arriba van la oleada, la hora
  y el multiplicador alcanzado; luego los golpes del enemigo, los 3 escudos, el contador (rojo en los últimos 5 s) y
  los artefactos; abajo la pregunta del banco de la pregunta flash (sin repetir en la noche, RF-09) y «Atacar».
- **Reglas:** acierto = golpe; incorrecta, incompleta o sin tiempo = escudo roto, sin revelar la respuesta, y va a la
  bitácora (`HASTA_EL_ALBA`). Al vencer a un enemigo: «¿Vuelves al banquete o sigues peleando?» con el premio neto
  que cobraría y los datos del siguiente. Al tercer escudo roto, caída en batalla. Tras la oleada 7, ×4, cuerno de Odín,
  logro «Hasta el Alba» y título de Einherjar (solo la primera vez).
- **Artefactos** (de la Apuesta de Brokkr, una vez por noche): Mjölnir, Gungnir, Draupnir (en la decisión y,
  si sobra, al llegar al alba: A18.1) y Skíðblaðnir. Un artefacto se tiene completo o no se tiene (sin calidades);
  antes de bajar al campo el alumno elige en la vitrina cuáles lleva a la noche.
- **MOD-01:** `APUESTA_ALBA` y `PREMIO_ALBA`; premio = apuesta × multiplicador (+ Draupnir), menos el diezmo del 10 %,
  todo redondeado hacia abajo.
- **Recuperación:** si la app se cierra en plena oleada, la noche cuenta como caída (E9.2); si se cierra en la
  decisión, se cobra sola (E15.1). Huir pide confirmación y equivale a caer (E9.1).
- **Datos:** tablas `noches_alba`, `artefactos_forjados` y `logros` (Room v12, `MIGRACION_11_12`).

## MOD-02 · Efectos de sonido

- Dominio: `ModuloSonido` (catálogo `EventoSonido` por categoría y prioridad, preferencias, máximo 4 efectos a la vez, separación mínima de 100 ms, tambor en los últimos 5 s, detener al pasar a segundo plano o si el sistema interrumpe el audio).
- Los casos de uso avisan sus eventos con `ModuloSonido.avisar(...)` (Android) / `ModuloSonido.sonar(...)` (iOS): respuestas, parejas del memorama, palabras del crucigrama, colocaciones del reto, adivinanzas del duelo, visiones de la Völva, golpes y escudos de Hasta el Alba, cuerno de Odín, desbloqueos y logros. El MOD-01 suena solo (cuerno que se llena o se vacía).
- Interfaz: botones, giro y caída de la moneda, volteo de cartas y piezas soltadas.
- Ajustes: interruptor de sonido y volumen de efectos (80 % por defecto); se guardan entre sesiones (RN-15).
- Música de fondo, una pista por tipo de pantalla con fundido de 1.2 s al cambiar (`musica_*`). No está en el SRS: se agregó junto al MOD-02.
  - Principal (menú: Mundos, moneda, roadmap, ajustes, racha): Re dórico, 80 ppm, lira, cellos, coro y cuernos; el zumbido grave de ambiente se bajó.
  - Batalla (Ragnarök, Hasta el Alba, la forja): 104 ppm, taikos y metales; graves recortados.
  - Contrarreloj: 138 ppm con reloj de madera.
  - Lección (lecciones, minijuegos, diagnóstico, repaso de Mímir, duelo): 66 ppm, sin tambores.
  - La Völva: frigio, 58 ppm, cristal frotado, vibráfono y campanas (aura mística).
  - Se pausa en segundo plano. Interruptor y volumen propios en Ajustes (45 % por defecto).
- Los efectos y el tema se generan con `tools/sonidos/nordico.py` y `tools/sonidos/tema.py`, con los instrumentos del banco GeneralUser GS de S. Christian Collins (licencia libre para usarlo en software, incluso comercial) más capas sintetizadas. `.ogg` en `res/raw` (Android) y `.m4a` en `Recursos/Sonidos` (iOS).

## Integración de la rama de Ximena (CU-02, MOD-05, MOD-06)

- CU-02 La apuesta de Brokkr: se entra por la cara de la moneda; el inicio usa la pantalla aprobada (escena de Brokkr). Tres etapas (calentar, martillar, templar en 60 s), apuesta de 20 a 80, dos forjas por día; perfecta paga ×3 menos el diezmo. La vitrina es la misma que usa Hasta el Alba.
- MOD-05 La carrera de Sól: contador en Mundos, pantalla de la racha, ofrendas (40, máximo 2), hitos con bono. Cuentan lecciones, minijuegos, retos, diagnóstico, repaso de Mímir, contrarreloj, duelo, Völva, Hasta el Alba y la forja. Sus sonidos pasan por el MOD-02.
- MOD-06 Bitácora: los fallos de la forja quedan en el historial de errores (repaso de Mímir).
- La moneda ya no gira al tocarla: tocar una cara entra a su modo; solo gira al arrastrarla.
- Vitrina del salón: una sola pantalla que cambia según el modo. Desde la forja (tema de la forja) muestra la colección; desde Hasta el Alba (tema del banquete) sirve para elegir qué artefactos llevar a la noche.
- Selección de la forja: el artefacto que tocó al azar aparece sobre el yunque con su mundo y su poder, y ahí se elige la apuesta.
