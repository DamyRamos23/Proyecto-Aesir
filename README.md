# ODIN (Android)

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

Se guarda en las preferencias (`odin` / `modo_admin`), así que sigue activo al reabrir la app.
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

## Base de datos

Versión **9** con `fallbackToDestructiveMigration`: al instalar sobre una versión anterior se borra el
progreso y se vuelven a sembrar los datos.

## Tipografías opcionales

Los mockups usan **Cinzel** y **Alegreya** (Google Fonts, licencia OFL). Para usarlas, copia sus `.ttf`
a `app/src/main/res/font/` con estos nombres:

`cinzel_regular.ttf`, `cinzel_bold.ttf`, `cinzel_black.ttf`,
`alegreya_regular.ttf`, `alegreya_italic.ttf`, `alegreya_medium.ttf`, `alegreya_bold.ttf`

Si no están, la app usa la serif del sistema con versalitas simuladas.
