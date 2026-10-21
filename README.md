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

## Tercer anillo y retos de arrastrar y soltar (CU-09)

El escudo de cada mundo tiene ahora tres anillos alrededor del Ragnarök:
1. lecciones (círculos), 2. minijuegos (rombos) y 3. **retos** (hexágonos, 8 por mundo).
Cada reto se conecta con los dos minijuegos del anillo 2 que tiene más cerca (`ArbolHabilidades`).

- Posición 1 del anillo 3: **Ordenar el proceso** (modalidad `ORDENAR_PROCESO`).
- Posición 5: **Completar el diagrama** (modalidad `COMPLETAR_DIAGRAMA`).
- Las otras 6: **Por definir** (se ven atenuadas y no se pueden jugar ni cuentan para el Ragnarök).

Contenido en `data/local/DatosArrastrar.kt`; diagramas en `ui/retoarrastrar/DiagramasReto.kt`.
Capas según los diagramas de CU-09: `RetoArrastrarScreen` (+ `OrdenarProcesoContent` / `CompletarDiagramaContent`),
`ResumenRetoScreen`, `RetoArrastrarViewModel`, casos de uso en `domain/usecase/retoarrastrar`,
`RetoRepository(Impl)` y los DAOs `RetoDao`, `ElementoArrastrableDao`, `PosicionDestinoDao`.
Base de datos v7 (se vuelve a sembrar al actualizar).

Reglas: empieza en 100, −20 por confirmación incorrecta; las piedras mal colocadas se señalan y tiemblan;
al tercer fallo termina sin puntaje, muestra la solución y sugiere repasar la lección más cercana.
Las piedras se pueden arrastrar o tocar (piedra y luego lugar).

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

- **Evaluación diagnóstica** en tres fases: el llamado (Mímir despierta), la prueba (10 preguntas con el
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

Versión 11, con migraciones que conservan el progreso y el hidromiel:
- `MIGRACION_9_10`: tablas del duelo de enigmas (CU-13).
- `MIGRACION_10_11`: tablas de la Völva (`visiones_volva`, `runas_obtenidas`, `consultas_volva`).

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
