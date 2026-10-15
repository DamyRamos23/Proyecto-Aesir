package com.aesir.odin.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aesir.odin.data.local.converter.Converters
import com.aesir.odin.data.local.entity.PreguntaFlashEntity

/**
 * Banco de preguntas compartido (tabla `preguntas_flash`).
 *
 * Es la fuente única de preguntas de opción múltiple: lo usa CU-06 Pregunta
 * Flash y lo puede reutilizar cualquier otro módulo (p. ej. CU-11 Reto
 * Contrarreloj) a través de `PreguntaFlashRepository`, sin duplicar preguntas.
 *
 * Para agregar preguntas basta con añadirlas a [preguntas]: se insertan (o
 * actualizan) cada vez que se abre la base de datos, sin borrar el progreso.
 * Reglas: `id` único, `temaId` de un tema existente en [DatosIniciales] y
 * `respuestasCorrectas` con los índices (desde 0) de las opciones correctas;
 * si hay más de uno, la pregunta es de selección múltiple.
 */
object BancoPreguntas {

    val preguntas: List<PreguntaFlashEntity> = listOf(
        // ---- Preguntas originales de CU-06 (2 por tema) ----
        PreguntaFlashEntity("pf_uml_1_1", "tema_uml_1", "¿Qué símbolo indica visibilidad pública en un diagrama de clases?", listOf("~", "-", "+", "#"), listOf(2)),
        PreguntaFlashEntity("pf_uml_1_2", "tema_uml_1", "Selecciona las partes de una clase en UML:", listOf("Atributos", "Operaciones", "Línea de vida", "Nombre"), listOf(0, 1, 3)),
        PreguntaFlashEntity("pf_uml_2_1", "tema_uml_2", "En un diagrama de secuencia, ¿qué representa una línea de vida?", listOf("Una clase abstracta", "La existencia de un participante a lo largo del tiempo", "Una base de datos", "Un paquete"), listOf(1)),
        PreguntaFlashEntity("pf_uml_2_2", "tema_uml_2", "Selecciona los fragmentos combinados válidos en un diagrama de secuencia:", listOf("herencia", "opt", "alt", "loop"), listOf(1, 2, 3)),
        PreguntaFlashEntity("pf_uml_3_1", "tema_uml_3", "¿Cómo se representa un caso de uso en UML?", listOf("Con un rombo", "Con una elipse", "Con un rectángulo", "Con un muñeco de palo"), listOf(1)),
        PreguntaFlashEntity("pf_uml_3_2", "tema_uml_3", "Selecciona los elementos de un diagrama de casos de uso:", listOf("Límite del sistema", "Método privado", "Actor", "Caso de uso"), listOf(0, 2, 3)),
        PreguntaFlashEntity("pf_uml_4_1", "tema_uml_4", "¿Qué representa un rombo en un diagrama de actividades?", listOf("Un nodo inicial", "Un mensaje de retorno", "Una clase", "Una decisión"), listOf(3)),
        PreguntaFlashEntity("pf_uml_4_2", "tema_uml_4", "Las barras fork y join se usan para modelar:", listOf("Atributos estáticos", "Flujos concurrentes", "Errores del sistema", "Herencia"), listOf(1)),
        PreguntaFlashEntity("pf_cu_1_1", "tema_cu_1", "¿Qué separa lo que hace el sistema de su entorno en un modelo de casos de uso?", listOf("El actor", "La relación include", "El límite del sistema", "La precondición"), listOf(2)),
        PreguntaFlashEntity("pf_cu_1_2", "tema_cu_1", "Selecciona lo que puede ser un actor:", listOf("Otro sistema", "Una tabla de base de datos", "Un dispositivo de hardware", "Una persona"), listOf(0, 2, 3)),
        PreguntaFlashEntity("pf_cu_2_1", "tema_cu_2", "¿Qué relación indica un comportamiento obligatorio que un caso de uso necesita siempre?", listOf("Asociación", "include", "extend", "Generalización"), listOf(1)),
        PreguntaFlashEntity("pf_cu_2_2", "tema_cu_2", "¿Qué relación modela un comportamiento opcional o condicional?", listOf("Dependencia", "include", "Composición", "extend"), listOf(3)),
        PreguntaFlashEntity("pf_cu_3_1", "tema_cu_3", "Selecciona partes de la especificación textual de un caso de uso:", listOf("Código fuente", "Flujos alternativos", "Precondiciones", "Flujo normal"), listOf(1, 2, 3)),
        PreguntaFlashEntity("pf_cu_3_2", "tema_cu_3", "¿Qué describe el flujo normal de un caso de uso?", listOf("Todos los errores posibles", "La base de datos", "El camino principal sin errores", "El diseño de la interfaz"), listOf(2)),
        PreguntaFlashEntity("pf_cu_4_1", "tema_cu_4", "¿Cuál es un error común al escribir casos de uso?", listOf("Nombrarlos con un verbo", "Escribir precondiciones", "Indicar el actor", "Describir la interfaz de usuario (ej. clic en el botón azul)"), listOf(3)),
        PreguntaFlashEntity("pf_cu_4_2", "tema_cu_4", "Un caso de uso como 'CRUD de usuarios' suele ser un error porque:", listOf("Es demasiado pequeño y debería agruparse", "Tiene demasiados actores", "Usa include", "No tiene flujo normal"), listOf(0)),
        PreguntaFlashEntity("pf_cv_1_1", "tema_cv_1", "¿Cuál es el objetivo del análisis de requisitos?", listOf("Comprender y documentar qué debe hacer el sistema", "Dar mantenimiento", "Escribir el código", "Desplegar el sistema"), listOf(0)),
        PreguntaFlashEntity("pf_cv_1_2", "tema_cv_1", "Selecciona actividades propias del análisis de requisitos:", listOf("Compilar el código", "Entrevistar a los interesados", "Documentar requisitos", "Validar requisitos con el cliente"), listOf(1, 2, 3)),
        PreguntaFlashEntity("pf_cv_2_1", "tema_cv_2", "¿Qué se define en la fase de diseño del sistema?", listOf("El mantenimiento correctivo", "La arquitectura y cómo funcionará el sistema", "Las pruebas de aceptación", "Los requisitos del cliente"), listOf(1)),
        PreguntaFlashEntity("pf_cv_2_2", "tema_cv_2", "¿Cuál es un producto típico de la fase de diseño?", listOf("El código ejecutable", "Un acta de entrega", "Un reporte de pruebas", "Diagramas de arquitectura y de clases"), listOf(3)),
        PreguntaFlashEntity("pf_cv_3_1", "tema_cv_3", "¿Qué se hace en la fase de implementación?", listOf("Traducir el diseño a código fuente", "Retirar el sistema", "Entrevistar al cliente", "Definir los requisitos"), listOf(0)),
        PreguntaFlashEntity("pf_cv_3_2", "tema_cv_3", "Selecciona buenas prácticas durante la codificación:", listOf("Revisiones de código", "Seguir estándares de código", "Omitir la documentación", "Control de versiones"), listOf(0, 1, 3)),
        PreguntaFlashEntity("pf_cv_4_1", "tema_cv_4", "¿Para qué sirven las pruebas en el ciclo de vida?", listOf("Elegir al equipo", "Recopilar requisitos", "Diseñar la arquitectura", "Verificar la calidad y detectar defectos"), listOf(3)),
        PreguntaFlashEntity("pf_cv_4_2", "tema_cv_4", "El mantenimiento de software consiste en:", listOf("Definir el alcance inicial", "Corregir y evolucionar el sistema después de su entrega", "Contratar personal", "Escribir el primer prototipo"), listOf(1)),
        PreguntaFlashEntity("pf_mt_1_1", "tema_mt_1", "¿Cómo avanza el modelo en cascada?", listOf("En espiral con riesgos", "Fase por fase, sin regresar a la anterior", "Con iteraciones cortas", "Todas las fases a la vez"), listOf(1)),
        PreguntaFlashEntity("pf_mt_1_2", "tema_mt_1", "Una desventaja del modelo en cascada es:", listOf("Siempre tiene iteraciones", "Tiene demasiados prototipos", "No documenta nada", "Es rígido ante cambios tardíos"), listOf(3)),
        PreguntaFlashEntity("pf_mt_2_1", "tema_mt_2", "¿En qué se centra el modelo en espiral?", listOf("En la evaluación y reducción de riesgos", "En programar sin planear", "En eliminar las pruebas", "En un solo ciclo secuencial"), listOf(0)),
        PreguntaFlashEntity("pf_mt_2_2", "tema_mt_2", "Selecciona actividades de cada vuelta de la espiral:", listOf("Planificación", "Análisis de riesgos", "Desarrollo y pruebas", "Eliminar al cliente"), listOf(0, 1, 2)),
        PreguntaFlashEntity("pf_mt_3_1", "tema_mt_3", "En el modelo en V, las pruebas unitarias se emparejan con:", listOf("El mantenimiento", "Los requisitos", "El análisis de riesgos", "El diseño detallado"), listOf(3)),
        PreguntaFlashEntity("pf_mt_3_2", "tema_mt_3", "¿Qué enfatiza el modelo en V?", listOf("La entrega en incrementos", "Verificación y validación en cada etapa", "La ausencia de pruebas", "El prototipado rápido"), listOf(1)),
        PreguntaFlashEntity("pf_mt_4_1", "tema_mt_4", "En el modelo incremental el sistema se construye:", listOf("Solo con prototipos desechables", "Todo de una sola vez", "Sin planificación", "Por partes que agregan funcionalidad"), listOf(3)),
        PreguntaFlashEntity("pf_mt_4_2", "tema_mt_4", "Una ventaja del modelo incremental es:", listOf("El cliente recibe versiones funcionales antes", "No requiere arquitectura", "Elimina el análisis de requisitos", "Evita las pruebas"), listOf(0)),

        // ---- Mundo 1: UML ----
        PreguntaFlashEntity("pf_uml_1_3", "tema_uml_1", "¿Qué relación representa \"es un tipo de\" entre dos clases?", listOf("Agregación", "Generalización (herencia)", "Dependencia", "Asociación"), listOf(1)),
        PreguntaFlashEntity("pf_uml_1_4", "tema_uml_1", "La composición se diferencia de la agregación porque:", listOf("Las partes no pueden existir sin el todo", "Se dibuja con línea punteada", "Solo se usa entre interfaces", "No tiene multiplicidad"), listOf(0)),
        PreguntaFlashEntity("pf_uml_1_5", "tema_uml_1", "Selecciona los símbolos de visibilidad válidos en UML:", listOf("+", "-", "#", "*"), listOf(0, 1, 2)),
        PreguntaFlashEntity("pf_uml_2_3", "tema_uml_2", "¿Qué representa el rectángulo delgado sobre una línea de vida?", listOf("Una nota", "Una clase abstracta", "Una barra de activación (el objeto está ejecutando)", "Un actor"), listOf(2)),
        PreguntaFlashEntity("pf_uml_2_4", "tema_uml_2", "Un mensaje síncrono en un diagrama de secuencia se dibuja con:", listOf("Flecha con punta rellena", "Línea punteada sin punta", "Un rombo", "Una elipse"), listOf(0)),
        PreguntaFlashEntity("pf_uml_3_3", "tema_uml_3", "¿Qué representa la figura de \"muñeco de palo\" en UML?", listOf("Un caso de uso", "Un actor", "Un paquete", "Un componente"), listOf(1)),
        PreguntaFlashEntity("pf_uml_3_4", "tema_uml_3", "Selecciona relaciones válidas entre casos de uso:", listOf("«include»", "«extend»", "Generalización", "Composición"), listOf(0, 1, 2)),
        PreguntaFlashEntity("pf_uml_4_3", "tema_uml_4", "¿Cómo se representa el nodo inicial en un diagrama de actividades?", listOf("Un círculo relleno", "Un rombo vacío", "Un rectángulo redondeado", "Una barra gruesa"), listOf(0)),
        PreguntaFlashEntity("pf_uml_4_4", "tema_uml_4", "¿Para qué sirven los carriles (swimlanes) en un diagrama de actividades?", listOf("Indicar herencia", "Mostrar qué responsable realiza cada actividad", "Definir atributos", "Ordenar mensajes"), listOf(1)),
        // ---- Mundo 2: Casos de uso ----
        PreguntaFlashEntity("pf_cu_1_3", "tema_cu_1", "¿Cómo debe nombrarse un caso de uso?", listOf("Con un sustantivo técnico", "Con el nombre de la pantalla", "Con un verbo en infinitivo y un objeto", "Con el nombre del programador"), listOf(2)),
        PreguntaFlashEntity("pf_cu_1_4", "tema_cu_1", "Un actor representa:", listOf("Un rol que interactúa con el sistema", "Una clase del código", "Una tabla", "Un método"), listOf(0)),
        PreguntaFlashEntity("pf_cu_2_3", "tema_cu_2", "En «include», la flecha apunta:", listOf("Del caso incluido al base", "Al actor", "Del caso base al caso incluido", "No lleva flecha"), listOf(2)),
        PreguntaFlashEntity("pf_cu_2_4", "tema_cu_2", "En «extend», la flecha apunta:", listOf("Del caso que extiende al caso base", "Del caso base al que extiende", "Del actor al sistema", "A la base de datos"), listOf(0)),
        PreguntaFlashEntity("pf_cu_3_3", "tema_cu_3", "¿Qué es una precondición?", listOf("Lo que pasa al final", "Lo que debe cumplirse antes de iniciar el caso de uso", "Un error del sistema", "Una pantalla"), listOf(1)),
        PreguntaFlashEntity("pf_cu_3_4", "tema_cu_3", "Selecciona lo que describe una postcondición:", listOf("El estado del sistema al terminar", "Los cambios que quedaron guardados", "El lenguaje de programación", "El color de la interfaz"), listOf(0, 1)),
        PreguntaFlashEntity("pf_cu_4_3", "tema_cu_4", "Selecciona errores comunes al modelar casos de uso:", listOf("Incluir detalles de la interfaz", "Usarlos como diagrama de flujo", "Nombrarlos con verbo", "Describir pasos técnicos internos"), listOf(0, 1, 3)),
        PreguntaFlashEntity("pf_cu_4_4", "tema_cu_4", "Abusar de «include» para descomponer todo suele provocar:", listOf("Mayor claridad", "Un diagrama más simple", "Descomposición funcional difícil de leer", "Menos actores"), listOf(2)),
        // ---- Mundo 3: Ciclo de vida ----
        PreguntaFlashEntity("pf_cv_1_3", "tema_cv_1", "¿Cuál es un requisito NO funcional?", listOf("El sistema registra usuarios", "El sistema responde en menos de 2 segundos", "El sistema genera reportes", "El sistema envía correos"), listOf(1)),
        PreguntaFlashEntity("pf_cv_1_4", "tema_cv_1", "Selecciona técnicas de obtención de requisitos:", listOf("Entrevistas", "Cuestionarios", "Observación", "Compilación"), listOf(0, 1, 2)),
        PreguntaFlashEntity("pf_cv_2_3", "tema_cv_2", "La alta cohesión y el bajo acoplamiento buscan:", listOf("Más dependencias entre módulos", "Código más difícil de probar", "Módulos enfocados y poco dependientes entre sí", "Eliminar el diseño"), listOf(2)),
        PreguntaFlashEntity("pf_cv_2_4", "tema_cv_2", "¿Qué tipo de diseño define los módulos principales y cómo se comunican?", listOf("Diseño arquitectónico", "Diseño de colores", "Diseño de pruebas", "Diseño de contratos"), listOf(0)),
        PreguntaFlashEntity("pf_cv_3_3", "tema_cv_3", "¿Para qué sirve un sistema de control de versiones como Git?", listOf("Ejecutar pruebas de carga", "Diseñar la base de datos", "Registrar y coordinar los cambios del código", "Entrevistar usuarios"), listOf(2)),
        PreguntaFlashEntity("pf_cv_3_4", "tema_cv_3", "La refactorización consiste en:", listOf("Agregar funcionalidades nuevas", "Mejorar la estructura del código sin cambiar su comportamiento", "Borrar las pruebas", "Cambiar de lenguaje"), listOf(1)),
        PreguntaFlashEntity("pf_cv_4_3", "tema_cv_4", "Selecciona niveles de pruebas de software:", listOf("Unitarias", "Integración", "Sistema", "Requisitos"), listOf(0, 1, 2)),
        PreguntaFlashEntity("pf_cv_4_4", "tema_cv_4", "Adaptar el software a un nuevo sistema operativo es mantenimiento:", listOf("Correctivo", "Adaptativo", "Preventivo", "Perfectivo"), listOf(1)),
        // ---- Mundo 4: Modelos tradicionales ----
        PreguntaFlashEntity("pf_mt_1_3", "tema_mt_1", "¿Cuándo conviene el modelo en cascada?", listOf("Cuando los requisitos son claros y estables", "Cuando todo cambia cada semana", "Cuando no hay documentación", "Cuando el riesgo es muy alto"), listOf(0)),
        PreguntaFlashEntity("pf_mt_1_4", "tema_mt_1", "Selecciona fases típicas del modelo en cascada:", listOf("Requisitos", "Diseño", "Implementación", "Análisis de riesgos por vuelta"), listOf(0, 1, 2)),
        PreguntaFlashEntity("pf_mt_2_3", "tema_mt_2", "¿Quién propuso el modelo en espiral?", listOf("Winston Royce", "Barry Boehm", "Kent Beck", "Ivar Jacobson"), listOf(1)),
        PreguntaFlashEntity("pf_mt_2_4", "tema_mt_2", "El modelo en espiral es más adecuado para:", listOf("Proyectos pequeños y triviales", "Proyectos sin riesgos", "Proyectos grandes y con alto riesgo", "Proyectos sin cliente"), listOf(2)),
        PreguntaFlashEntity("pf_mt_3_3", "tema_mt_3", "En el modelo en V, las pruebas de aceptación validan:", listOf("El diseño detallado", "Los requisitos del usuario", "El código de una función", "La arquitectura de red"), listOf(1)),
        PreguntaFlashEntity("pf_mt_3_4", "tema_mt_3", "Una desventaja del modelo en V es:", listOf("Es poco flexible ante cambios de requisitos", "No tiene pruebas", "No tiene fases", "Exige iteraciones cortas"), listOf(0)),
        PreguntaFlashEntity("pf_mt_4_3", "tema_mt_4", "¿Qué se entrega al final de cada incremento?", listOf("Solo documentación", "Un prototipo desechable", "Una parte funcional del sistema", "Nada hasta el final"), listOf(2)),
        PreguntaFlashEntity("pf_mt_4_4", "tema_mt_4", "Selecciona ventajas del modelo incremental:", listOf("Entregas tempranas al cliente", "Retroalimentación frecuente", "Riesgo repartido entre incrementos", "Cero necesidad de planear"), listOf(0, 1, 2)),
        // ---- Banco ampliado: 6 preguntas más por tema (10 en total) ----
        // ---- Mundo 1: UML ----
        PreguntaFlashEntity("pf_uml_1_x1", "tema_uml_1", "¿Qué indica el símbolo \"-\" antes de un atributo?", listOf("Visibilidad protegida", "Visibilidad de paquete", "Visibilidad privada", "Visibilidad pública"), listOf(2)),
        PreguntaFlashEntity("pf_uml_1_x2", "tema_uml_1", "¿Cómo se representa un atributo o método estático en un diagrama de clases?", listOf("En cursiva", "Entre comillas", "En negritas", "Subrayado"), listOf(3)),
        PreguntaFlashEntity("pf_uml_1_x3", "tema_uml_1", "Una clase abstracta se distingue porque su nombre aparece:", listOf("Subrayado", "En mayúsculas", "En cursiva", "Entre corchetes"), listOf(2)),
        PreguntaFlashEntity("pf_uml_1_x4", "tema_uml_1", "La multiplicidad 1..* significa:", listOf("Ninguno", "Uno o más", "Exactamente uno", "Cero o uno"), listOf(1)),
        PreguntaFlashEntity("pf_uml_1_x5", "tema_uml_1", "Selecciona relaciones que existen en un diagrama de clases:", listOf("Realización", "Mensaje asíncrono", "Dependencia", "Asociación"), listOf(0, 2, 3)),
        PreguntaFlashEntity("pf_uml_1_x6", "tema_uml_1", "¿Qué relación se usa cuando una clase implementa una interfaz?", listOf("Agregación", "Realización", "Composición", "«extend»"), listOf(1)),
        PreguntaFlashEntity("pf_uml_2_x1", "tema_uml_2", "¿Cómo se dibuja un mensaje de retorno en un diagrama de secuencia?", listOf("Una línea doble", "Línea punteada con punta abierta", "Línea continua con punta rellena", "Un rombo"), listOf(1)),
        PreguntaFlashEntity("pf_uml_2_x2", "tema_uml_2", "¿Qué fragmento combinado representa una condición si / si no?", listOf("loop", "ref", "par", "alt"), listOf(3)),
        PreguntaFlashEntity("pf_uml_2_x3", "tema_uml_2", "El fragmento \"loop\" indica:", listOf("Un mensaje opcional", "Una repetición mientras se cumpla una condición", "Ejecución en paralelo", "El fin de un objeto"), listOf(1)),
        PreguntaFlashEntity("pf_uml_2_x4", "tema_uml_2", "¿Qué indica una X al final de una línea de vida?", listOf("La destrucción del objeto", "Una decisión", "Un error", "Un mensaje perdido"), listOf(0)),
        PreguntaFlashEntity("pf_uml_2_x5", "tema_uml_2", "En un diagrama de secuencia, el eje vertical representa:", listOf("El tamaño de los datos", "La jerarquía de clases", "El paso del tiempo", "La prioridad de los mensajes"), listOf(2)),
        PreguntaFlashEntity("pf_uml_2_x6", "tema_uml_2", "Selecciona elementos de un diagrama de secuencia:", listOf("Atributos con su tipo", "Mensajes", "Líneas de vida", "Barras de activación"), listOf(1, 2, 3)),
        PreguntaFlashEntity("pf_uml_3_x1", "tema_uml_3", "¿Qué representa el rectángulo que encierra los casos de uso?", listOf("Una clase", "Un actor secundario", "Un paquete de datos", "El límite del sistema"), listOf(3)),
        PreguntaFlashEntity("pf_uml_3_x2", "tema_uml_3", "¿Qué une a un actor con un caso de uso?", listOf("Una transición de estado", "Un mensaje síncrono", "Una composición", "Una asociación (línea simple)"), listOf(3)),
        PreguntaFlashEntity("pf_uml_3_x3", "tema_uml_3", "Los actores se dibujan:", listOf("No se dibujan", "Dentro de cada caso de uso", "Fuera del límite del sistema", "Dentro del límite del sistema"), listOf(2)),
        PreguntaFlashEntity("pf_uml_3_x4", "tema_uml_3", "La generalización entre actores indica que:", listOf("Un actor hereda las interacciones de otro", "Dos actores son el mismo", "El actor es un sistema externo", "El actor está inactivo"), listOf(0)),
        PreguntaFlashEntity("pf_uml_3_x5", "tema_uml_3", "¿Qué muestra principalmente un diagrama de casos de uso?", listOf("El orden de los mensajes", "Qué funcionalidades ofrece el sistema y a quién", "La estructura de la base de datos", "El despliegue en servidores"), listOf(1)),
        PreguntaFlashEntity("pf_uml_3_x6", "tema_uml_3", "Selecciona estereotipos que pueden aparecer entre casos de uso:", listOf("«extend»", "«thread»", "«table»", "«include»"), listOf(0, 3)),
        PreguntaFlashEntity("pf_uml_4_x1", "tema_uml_4", "¿Cómo se representa el nodo final de actividad?", listOf("Una flecha punteada", "Un rectángulo", "Un círculo relleno dentro de otro círculo", "Un rombo"), listOf(2)),
        PreguntaFlashEntity("pf_uml_4_x2", "tema_uml_4", "Una acción en un diagrama de actividades se dibuja como:", listOf("Un muñeco de palo", "Una elipse", "Un rectángulo con esquinas redondeadas", "Un triángulo"), listOf(2)),
        PreguntaFlashEntity("pf_uml_4_x3", "tema_uml_4", "La barra \"join\" sirve para:", listOf("Terminar el proceso", "Sincronizar flujos concurrentes en uno solo", "Dividir el flujo en varios", "Repetir una acción"), listOf(1)),
        PreguntaFlashEntity("pf_uml_4_x4", "tema_uml_4", "Las condiciones de guarda en un diagrama de actividades se escriben:", listOf("Entre llaves", "Entre comillas latinas", "Entre paréntesis", "Entre corchetes"), listOf(3)),
        PreguntaFlashEntity("pf_uml_4_x5", "tema_uml_4", "Un nodo de fusión (merge) se representa con:", listOf("Un rombo", "Un círculo", "Una X", "Una barra gruesa"), listOf(0)),
        PreguntaFlashEntity("pf_uml_4_x6", "tema_uml_4", "Selecciona elementos de un diagrama de actividades:", listOf("Nodo inicial", "Acciones", "Líneas de vida", "Nodos de decisión"), listOf(0, 1, 3)),
        // ---- Mundo 2: Casos de uso ----
        PreguntaFlashEntity("pf_cu_1_x1", "tema_cu_1", "Un actor principal es aquel que:", listOf("Programa el sistema", "Inicia el caso de uso para obtener un resultado de valor", "Solo recibe notificaciones", "Nunca interactúa con el sistema"), listOf(1)),
        PreguntaFlashEntity("pf_cu_1_x2", "tema_cu_1", "Un actor secundario o de apoyo:", listOf("Es una pantalla", "Inicia todos los casos de uso", "Presta un servicio al sistema durante el caso de uso", "Es el programador"), listOf(2)),
        PreguntaFlashEntity("pf_cu_1_x3", "tema_cu_1", "Un caso de uso describe:", listOf("Una interacción que produce un resultado de valor para un actor", "El diseño de la base de datos", "Un componente físico", "La estructura interna del código"), listOf(0)),
        PreguntaFlashEntity("pf_cu_1_x4", "tema_cu_1", "¿Cuál de estos es un buen nombre de caso de uso?", listOf("Tabla clientes", "Pantalla de inicio", "BotonPagar", "Registrar pedido"), listOf(3)),
        PreguntaFlashEntity("pf_cu_1_x5", "tema_cu_1", "¿Cuál de estos NO es un actor?", listOf("Un cliente", "Un sistema de pagos externo", "Un temporizador que dispara procesos", "Una clase interna del sistema"), listOf(3)),
        PreguntaFlashEntity("pf_cu_1_x6", "tema_cu_1", "Selecciona elementos fundamentales de un modelo de casos de uso:", listOf("Actores", "Casos de uso", "Límite del sistema", "Nodos de despliegue"), listOf(0, 1, 2)),
        PreguntaFlashEntity("pf_cu_2_x1", "tema_cu_2", "¿Para qué se usa principalmente «include»?", listOf("Mostrar herencia entre actores", "Reutilizar comportamiento común entre varios casos de uso", "Indicar comportamiento opcional", "Definir el límite del sistema"), listOf(1)),
        PreguntaFlashEntity("pf_cu_2_x2", "tema_cu_2", "En «extend», el caso base:", listOf("No puede ejecutarse sin la extensión", "Siempre es un actor", "Es completo por sí mismo y funciona sin la extensión", "Hereda del caso que lo extiende"), listOf(2)),
        PreguntaFlashEntity("pf_cu_2_x3", "tema_cu_2", "¿Dónde se indica el momento en que se aplica una extensión?", listOf("En el límite del sistema", "En un punto de extensión del caso base", "En la base de datos", "En el actor"), listOf(1)),
        PreguntaFlashEntity("pf_cu_2_x4", "tema_cu_2", "\"Iniciar sesión\" es requerido siempre por \"Realizar compra\". La relación adecuada es:", listOf("Asociación", "Generalización", "«extend»", "«include»"), listOf(3)),
        PreguntaFlashEntity("pf_cu_2_x5", "tema_cu_2", "\"Aplicar cupón\" ocurre solo a veces durante \"Pagar\". La relación adecuada es:", listOf("Composición", "«include»", "«extend»", "Dependencia"), listOf(2)),
        PreguntaFlashEntity("pf_cu_2_x6", "tema_cu_2", "Selecciona afirmaciones correctas sobre include y extend:", listOf("Ambas se dibujan con línea punteada y estereotipo", "«include» es obligatorio", "«extend» es condicional", "«extend» siempre se ejecuta"), listOf(0, 1, 2)),
        PreguntaFlashEntity("pf_cu_3_x1", "tema_cu_3", "Los flujos alternativos describen:", listOf("El camino principal", "El diseño de la interfaz", "El código fuente", "Variaciones o excepciones del flujo normal"), listOf(3)),
        PreguntaFlashEntity("pf_cu_3_x2", "tema_cu_3", "¿Con qué suele comenzar el flujo normal de un caso de uso?", listOf("El sistema se apaga", "El actor inicia la interacción", "El programador compila", "La base de datos se respalda"), listOf(1)),
        PreguntaFlashEntity("pf_cu_3_x3", "tema_cu_3", "Una garantía mínima indica:", listOf("Lo que el sistema asegura incluso si el caso de uso falla", "Qué pantalla mostrar", "El nombre del actor", "El estado del sistema si todo sale bien"), listOf(0)),
        PreguntaFlashEntity("pf_cu_3_x4", "tema_cu_3", "Selecciona campos habituales en una plantilla de caso de uso:", listOf("Actores", "Lenguaje de programación", "Precondiciones", "Nombre"), listOf(0, 2, 3)),
        PreguntaFlashEntity("pf_cu_3_x5", "tema_cu_3", "Los pasos del flujo deben redactarse:", listOf("Como interacciones actor-sistema en lenguaje claro", "En términos de tablas de la base de datos", "En pseudocódigo", "Con detalles de botones y colores"), listOf(0)),
        PreguntaFlashEntity("pf_cu_3_x6", "tema_cu_3", "¿Qué es el disparador (trigger) de un caso de uso?", listOf("El evento que lo inicia", "Su último paso", "Un actor secundario", "Un error del sistema"), listOf(0)),
        PreguntaFlashEntity("pf_cu_4_x1", "tema_cu_4", "Escribir \"el usuario da clic en el botón verde\" es un error porque:", listOf("Menciona al usuario", "Es muy corto", "Usa un verbo", "Describe la interfaz en lugar del comportamiento"), listOf(3)),
        PreguntaFlashEntity("pf_cu_4_x2", "tema_cu_4", "Representar la \"Base de datos\" propia como actor es un error porque:", listOf("Es demasiado grande", "Es parte interna del sistema, no algo externo", "No tiene nombre", "No se puede dibujar"), listOf(1)),
        PreguntaFlashEntity("pf_cu_4_x3", "tema_cu_4", "Unir casos de uso con flechas para indicar su orden es un error porque:", listOf("Falta un actor", "Son demasiados", "Se debería usar include", "Los casos de uso no modelan el orden de ejecución"), listOf(3)),
        PreguntaFlashEntity("pf_cu_4_x4", "tema_cu_4", "Un caso de uso como \"Escribir contraseña\" es un error porque:", listOf("Es opcional", "Es demasiado grande", "No aporta por sí solo un resultado de valor al actor", "Tiene muchos actores"), listOf(2)),
        PreguntaFlashEntity("pf_cu_4_x5", "tema_cu_4", "Selecciona señales de un mal modelo de casos de uso:", listOf("Muchos niveles de include anidados", "Casos nombrados con verbo y objeto", "Casos nombrados como pantallas", "Actores que son componentes internos"), listOf(0, 2, 3)),
        PreguntaFlashEntity("pf_cu_4_x6", "tema_cu_4", "¿Qué conviene hacer si un diagrama tiene demasiados casos de uso?", listOf("Convertirlos en clases", "Borrar los actores", "Agruparlos por subsistemas o paquetes", "Eliminar el límite del sistema"), listOf(2)),
        // ---- Mundo 3: Ciclo de vida ----
        PreguntaFlashEntity("pf_cv_1_x1", "tema_cv_1", "Un requisito funcional describe:", listOf("El color del logotipo", "Qué debe hacer el sistema", "El costo del servidor", "Qué tan rápido responde"), listOf(1)),
        PreguntaFlashEntity("pf_cv_1_x2", "tema_cv_1", "Selecciona ejemplos de requisitos no funcionales:", listOf("Usabilidad", "Registrar una venta", "Rendimiento", "Seguridad"), listOf(0, 2, 3)),
        PreguntaFlashEntity("pf_cv_1_x3", "tema_cv_1", "¿Qué documento reúne formalmente los requisitos del software?", listOf("El código fuente", "El plan de pruebas", "La especificación de requisitos de software (ERS)", "El manual de usuario"), listOf(2)),
        PreguntaFlashEntity("pf_cv_1_x4", "tema_cv_1", "Un buen requisito debe ser:", listOf("Verificable y sin ambigüedad", "Imposible de medir", "Escrito en código", "Ambiguo, para dar libertad"), listOf(0)),
        PreguntaFlashEntity("pf_cv_1_x5", "tema_cv_1", "¿Qué son los interesados (stakeholders)?", listOf("Personas u organizaciones afectadas por el sistema", "Los servidores", "Los errores del sistema", "Las tablas de la base de datos"), listOf(0)),
        PreguntaFlashEntity("pf_cv_1_x6", "tema_cv_1", "La validación de requisitos busca confirmar que:", listOf("Los requisitos reflejan lo que el cliente realmente necesita", "El código compila", "Los servidores están encendidos", "La interfaz es atractiva"), listOf(0)),
        PreguntaFlashEntity("pf_cv_2_x1", "tema_cv_2", "¿Qué patrón arquitectónico separa modelo, vista y controlador?", listOf("CRUD", "MVC", "FIFO", "SQL"), listOf(1)),
        PreguntaFlashEntity("pf_cv_2_x2", "tema_cv_2", "El bajo acoplamiento significa que:", listOf("No hay interfaces", "Los módulos dependen mucho entre sí", "Solo existe un módulo", "Los módulos dependen poco unos de otros"), listOf(3)),
        PreguntaFlashEntity("pf_cv_2_x3", "tema_cv_2", "La alta cohesión significa que un módulo:", listOf("Hace muchas cosas sin relación", "No tiene funciones", "Se enfoca en una responsabilidad bien definida", "Depende de todos los demás"), listOf(2)),
        PreguntaFlashEntity("pf_cv_2_x4", "tema_cv_2", "¿Qué diagrama UML describe la estructura estática en el diseño?", listOf("Diagrama de actividades", "Diagrama de secuencia", "Diagrama de casos de uso", "Diagrama de clases"), listOf(3)),
        PreguntaFlashEntity("pf_cv_2_x5", "tema_cv_2", "Selecciona principios de buen diseño:", listOf("Duplicar código", "Abstracción", "Encapsulamiento", "Modularidad"), listOf(1, 2, 3)),
        PreguntaFlashEntity("pf_cv_2_x6", "tema_cv_2", "El diseño detallado se ocupa de:", listOf("Los algoritmos y estructuras de datos de cada módulo", "Las entrevistas con el cliente", "La campaña de lanzamiento", "El presupuesto"), listOf(0)),
        PreguntaFlashEntity("pf_cv_3_x1", "tema_cv_3", "Un estándar de codificación sirve para:", listOf("Hacer el programa más lento", "Ocultar errores", "Evitar las pruebas", "Que el código sea consistente y legible para el equipo"), listOf(3)),
        PreguntaFlashEntity("pf_cv_3_x2", "tema_cv_3", "¿Qué es una revisión de código (code review)?", listOf("Que otro desarrollador examine los cambios antes de integrarlos", "Compilar el programa", "Borrar el código viejo", "Instalar el sistema"), listOf(0)),
        PreguntaFlashEntity("pf_cv_3_x3", "tema_cv_3", "En Git, un \"commit\" es:", listOf("Un error de compilación", "Un servidor remoto", "Un registro de un conjunto de cambios con su descripción", "Una rama eliminada"), listOf(2)),
        PreguntaFlashEntity("pf_cv_3_x4", "tema_cv_3", "La integración continua consiste en:", listOf("Trabajar sin control de versiones", "Programar sin pausas", "Entregar todo solo al final", "Integrar y probar automáticamente el código con frecuencia"), listOf(3)),
        PreguntaFlashEntity("pf_cv_3_x5", "tema_cv_3", "Selecciona buenas prácticas de implementación:", listOf("Copiar y pegar código repetido", "Comentar lo que no es obvio", "Nombres descriptivos", "Funciones pequeñas y enfocadas"), listOf(1, 2, 3)),
        PreguntaFlashEntity("pf_cv_3_x6", "tema_cv_3", "La \"deuda técnica\" se refiere a:", listOf("Dinero que se debe al cliente", "Las horas extra del equipo", "El precio de las licencias", "El costo futuro de los atajos tomados en el código"), listOf(3)),
        PreguntaFlashEntity("pf_cv_4_x1", "tema_cv_4", "Una prueba unitaria verifica:", listOf("La red de la empresa", "El sistema completo con usuarios reales", "Una unidad pequeña de código de forma aislada", "El hardware"), listOf(2)),
        PreguntaFlashEntity("pf_cv_4_x2", "tema_cv_4", "Las pruebas de caja negra se basan en:", listOf("El compilador", "Las entradas y salidas, sin ver la implementación", "El código interno", "La base de datos"), listOf(1)),
        PreguntaFlashEntity("pf_cv_4_x3", "tema_cv_4", "Las pruebas de regresión sirven para:", listOf("Probar algo por primera vez", "Comprobar que los cambios no rompieron lo que ya funcionaba", "Diseñar la interfaz", "Medir la velocidad"), listOf(1)),
        PreguntaFlashEntity("pf_cv_4_x4", "tema_cv_4", "Corregir un defecto reportado por un usuario es mantenimiento:", listOf("Adaptativo", "Preventivo", "Perfectivo", "Correctivo"), listOf(3)),
        PreguntaFlashEntity("pf_cv_4_x5", "tema_cv_4", "Agregar funcionalidades nuevas que pide el usuario es mantenimiento:", listOf("Adaptativo", "Correctivo", "Perfectivo", "De emergencia"), listOf(2)),
        PreguntaFlashEntity("pf_cv_4_x6", "tema_cv_4", "Selecciona tipos de mantenimiento de software:", listOf("Preventivo", "Correctivo", "Adaptativo", "Destructivo"), listOf(0, 1, 2)),
        // ---- Mundo 4: Modelos tradicionales ----
        PreguntaFlashEntity("pf_mt_1_x1", "tema_mt_1", "¿Quién describió el modelo en cascada en 1970?", listOf("Winston Royce", "Martin Fowler", "Barry Boehm", "Grady Booch"), listOf(0)),
        PreguntaFlashEntity("pf_mt_1_x2", "tema_mt_1", "En cascada, los errores de requisitos suelen detectarse:", listOf("En cada iteración", "Nunca", "Muy tarde, en las pruebas o la entrega", "Al inicio de cada semana"), listOf(2)),
        PreguntaFlashEntity("pf_mt_1_x3", "tema_mt_1", "¿Cómo es la documentación en el modelo en cascada?", listOf("Inexistente", "Mínima y opcional", "Extensa, y se produce al cerrar cada fase", "Solo se hace al final"), listOf(2)),
        PreguntaFlashEntity("pf_mt_1_x4", "tema_mt_1", "En cascada, el cliente normalmente ve el producto funcionando:", listOf("Desde el primer día", "Cada semana", "En cada iteración", "Hasta el final del proyecto"), listOf(3)),
        PreguntaFlashEntity("pf_mt_1_x5", "tema_mt_1", "Selecciona desventajas del modelo en cascada:", listOf("Riesgo alto si los requisitos son incorrectos", "Exceso de iteraciones", "Entrega tardía de software funcional", "Poca flexibilidad ante cambios"), listOf(0, 2, 3)),
        PreguntaFlashEntity("pf_mt_1_x6", "tema_mt_1", "Una ventaja del modelo en cascada es:", listOf("Adaptarse a cambios constantes", "Su estructura simple y fácil de gestionar", "Entregar algo cada día", "No necesitar documentación"), listOf(1)),
        PreguntaFlashEntity("pf_mt_2_x1", "tema_mt_2", "¿Qué representa cada vuelta de la espiral?", listOf("Un lenguaje de programación", "Un ciclo o fase del proyecto", "Un cliente nuevo", "Un error"), listOf(1)),
        PreguntaFlashEntity("pf_mt_2_x2", "tema_mt_2", "¿En qué cuadrante de la espiral se identifican y resuelven los riesgos?", listOf("Planificación", "Evaluación del cliente", "Análisis de riesgos", "Ingeniería"), listOf(2)),
        PreguntaFlashEntity("pf_mt_2_x3", "tema_mt_2", "El modelo en espiral combina:", listOf("Solo programación", "Elementos de cascada y prototipado de forma iterativa", "Únicamente pruebas", "Ninguna fase"), listOf(1)),
        PreguntaFlashEntity("pf_mt_2_x4", "tema_mt_2", "Una desventaja del modelo en espiral es:", listOf("No permite iteraciones", "Requiere experiencia en análisis de riesgos y puede ser costoso", "Es demasiado sencillo", "No gestiona riesgos"), listOf(1)),
        PreguntaFlashEntity("pf_mt_2_x5", "tema_mt_2", "Al terminar cada vuelta de la espiral:", listOf("El cliente evalúa los resultados y se planifica la siguiente vuelta", "Se cancela el proyecto", "El cliente no participa", "Se borra el prototipo sin revisarlo"), listOf(0)),
        PreguntaFlashEntity("pf_mt_2_x6", "tema_mt_2", "Selecciona características del modelo en espiral:", listOf("Uso de prototipos", "Estrictamente lineal", "Orientado a riesgos", "Iterativo"), listOf(0, 2, 3)),
        PreguntaFlashEntity("pf_mt_3_x1", "tema_mt_3", "En el modelo en V, el lado izquierdo representa:", listOf("El despliegue", "Las fases de especificación y diseño", "El mantenimiento", "Las pruebas"), listOf(1)),
        PreguntaFlashEntity("pf_mt_3_x2", "tema_mt_3", "En el modelo en V, el lado derecho representa:", listOf("Los requisitos", "Las fases de pruebas y validación", "El análisis de riesgos", "El diseño"), listOf(1)),
        PreguntaFlashEntity("pf_mt_3_x3", "tema_mt_3", "En el modelo en V, las pruebas de integración se emparejan con:", listOf("La codificación", "Los requisitos del usuario", "El mantenimiento", "El diseño arquitectónico"), listOf(3)),
        PreguntaFlashEntity("pf_mt_3_x4", "tema_mt_3", "En el modelo en V, las pruebas de sistema se emparejan con:", listOf("La especificación de requisitos del sistema", "El diseño detallado", "La codificación", "El mantenimiento"), listOf(0)),
        PreguntaFlashEntity("pf_mt_3_x5", "tema_mt_3", "Una ventaja del modelo en V es que:", listOf("No necesita diseño", "Permite cambiar requisitos sin costo", "No requiere pruebas", "Las pruebas se planean desde las primeras fases"), listOf(3)),
        PreguntaFlashEntity("pf_mt_3_x6", "tema_mt_3", "Selecciona niveles de prueba del lado derecho de la V:", listOf("Pruebas de integración", "Pruebas de aceptación", "Pruebas unitarias", "Análisis de requisitos"), listOf(0, 1, 2)),
        PreguntaFlashEntity("pf_mt_4_x1", "tema_mt_4", "¿Qué suele contener el primer incremento?", listOf("El núcleo con las funcionalidades esenciales", "Solo la documentación", "Nada funcional", "Las funciones menos importantes"), listOf(0)),
        PreguntaFlashEntity("pf_mt_4_x2", "tema_mt_4", "El modelo incremental se diferencia de la cascada en que:", listOf("No tiene fases", "Es más rígido", "Entrega el sistema por partes funcionales", "No hace pruebas"), listOf(2)),
        PreguntaFlashEntity("pf_mt_4_x3", "tema_mt_4", "Cada incremento normalmente pasa por:", listOf("Solo codificación", "Únicamente diseño", "Solo pruebas", "Análisis, diseño, implementación y pruebas"), listOf(3)),
        PreguntaFlashEntity("pf_mt_4_x4", "tema_mt_4", "Una desventaja del modelo incremental es:", listOf("Requiere una buena arquitectura inicial para integrar los incrementos", "Elimina la planificación", "No hay entregas", "El cliente nunca ve avances"), listOf(0)),
        PreguntaFlashEntity("pf_mt_4_x5", "tema_mt_4", "¿Por qué el modelo incremental reduce el riesgo?", listOf("Porque los problemas aparecen en entregas tempranas", "Porque no se documenta", "Porque todo se entrega al final", "Porque no hay pruebas"), listOf(0)),
        PreguntaFlashEntity("pf_mt_4_x6", "tema_mt_4", "Selecciona situaciones donde conviene el modelo incremental:", listOf("Los requisitos principales se conocen pero pueden crecer", "Se necesita entregar funcionalidad básica pronto", "El equipo es reducido al inicio", "No existe ningún requisito"), listOf(0, 1, 2))
    )

    fun insertar(db: SupportSQLiteDatabase) {
        val converters = Converters()
        preguntas.forEach { pregunta ->
            db.insert("preguntas_flash", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", pregunta.id)
                put("temaId", pregunta.temaId)
                put("enunciado", pregunta.enunciado)
                put("opciones", converters.fromStringList(pregunta.opciones))
                put("respuestasCorrectas", converters.fromIntList(pregunta.respuestasCorrectas))
            })
        }
    }
}
