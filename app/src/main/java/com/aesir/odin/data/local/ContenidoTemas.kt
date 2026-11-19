package com.aesir.odin.data.local

/**
 * Contenido de los temas 5 a 10 de cada mundo (cuarto anillo del roadmap,
 * SRS v3.2: «cada mundo queda con 10 temas») y conceptos que completan los
 * temas 1 a 4.
 *
 * Cada tema nuevo trae todo lo que necesitan los casos de uso que lo usan:
 * - CU-05: la lección con su introducción y 5 ejercicios.
 * - CU-07, CU-04, CU-03 y CU-13: de 7 a 8 conceptos con su pista (el
 *   crucigrama necesita al menos 6, el memorama 6 pares).
 * - CU-10: 2 preguntas de la evaluación diagnóstica (2 por tema, RF-130).
 * - CU-06, CU-11 y CU-01: 3 preguntas del banco de pregunta flash.
 * - CU-13: el enigma del tema para el duelo contra el gigante.
 *
 * Las opciones se escriben con la correcta donde sea más cómodo y se rotan al
 * generar las entidades ([mezclar]), siempre igual para el mismo id.
 */
internal object ContenidoTemas {

    class Pregunta(val enunciado: String, val opciones: List<String>, val correctas: List<Int>)

    class TemaNuevo(
        val id: String,
        val mundoId: String,
        val orden: Int,
        val nombre: String,
        val descripcion: String,
        val tituloLeccion: String,
        val introduccion: String,
        val ejercicios: List<Pregunta>,
        val conceptos: List<Pair<String, String>>,
        val diagnostico: List<Pregunta>,
        val flash: List<Pregunta>,
        val enigma: String
    ) {
        val sufijo: String get() = id.removePrefix("tema_")
    }

    private fun p(enunciado: String, opciones: List<String>, vararg correctas: Int) =
        Pregunta(enunciado, opciones, correctas.toList())

    private val VF = listOf("Verdadero", "Falso")

    /**
     * Rota las opciones según el id para que la correcta no quede siempre en el
     * mismo lugar. Las de verdadero o falso se dejan en su orden.
     */
    fun mezclar(id: String, pregunta: Pregunta): Pregunta {
        val n = pregunta.opciones.size
        if (n < 3) return pregunta
        val k = (id.hashCode() and 0x7fffffff) % n
        val opciones = List(n) { pregunta.opciones[(it + k) % n] }
        val correctas = pregunta.correctas.map { (it - k + n) % n }.sorted()
        return Pregunta(pregunta.enunciado, opciones, correctas)
    }

    /** Conceptos extra de los temas 1 a 4, para que cada tema tenga al menos 6 (CU-07, CU-04). */
    val conceptosExtra: Map<String, List<Pair<String, String>>> = mapOf(
        "tema_uml_1" to listOf(
            "Asociación" to "Relación estructural que conecta a dos clases.",
            "Agregación" to "Relación todo-parte en la que la parte puede existir sola."
        ),
        "tema_uml_2" to listOf(
            "Participante" to "Objeto o actor que intercambia mensajes en el diagrama.",
            "Asíncrono" to "Mensaje en el que el emisor no espera la respuesta."
        ),
        "tema_uml_3" to listOf(
            "Comunicación" to "Línea que une a un actor con el caso de uso que usa.",
            "Perspectiva" to "Punto de vista del usuario con el que se describe el diagrama."
        ),
        "tema_uml_4" to listOf(
            "Join" to "Barra que une varios flujos paralelos en uno solo.",
            "Unión" to "Nodo que junta caminos alternativos en uno."
        ),
        "tema_cu_1" to listOf(
            "Rectángulo" to "Figura que marca el límite del sistema en el diagrama.",
            "Caso" to "Unidad que describe lo que un actor logra con el sistema."
        ),
        "tema_cu_2" to listOf(
            "Obligatorio" to "Cómo es el comportamiento que agrega include.",
            "Condición" to "Lo que debe cumplirse para que se active un extend."
        ),
        "tema_cu_3" to listOf(
            "Garantía" to "Estado asegurado al terminar con éxito el caso de uso.",
            "Plantilla" to "Formato estándar para escribir la especificación."
        ),
        "tema_cu_4" to listOf(
            "Diseño" to "Lo que no se mezcla en un caso de uso: pantallas y detalles técnicos.",
            "Verbo" to "Palabra con la que debe empezar el nombre de un caso de uso."
        ),
        "tema_cv_1" to listOf(
            "Entrevista" to "Técnica para obtener requisitos hablando con los interesados.",
            "Especificar" to "Documentar con precisión los requisitos acordados."
        ),
        "tema_cv_2" to listOf(
            "Patrón" to "Solución probada a un problema de diseño que se repite.",
            "Abstracción" to "Ocultar detalles para enfocarse en lo esencial."
        ),
        "tema_cv_3" to listOf(
            "Depurar" to "Encontrar y corregir errores en el código.",
            "Lenguaje" to "Herramienta con la que se escribe el código fuente."
        ),
        "tema_cv_4" to listOf(
            "Perfectivo" to "Mantenimiento que mejora o agrega funciones.",
            "Preventivo" to "Mantenimiento que evita fallas futuras."
        ),
        "tema_mt_1" to listOf(
            "Royce" to "Apellido de quien describió el modelo en cascada en 1970.",
            "Documento" to "Producto que debe aprobarse al terminar cada fase."
        ),
        "tema_mt_2" to listOf(
            "Cuadrante" to "Cada una de las cuatro zonas de una vuelta del espiral.",
            "Planificar" to "Actividad con la que inicia cada vuelta del espiral."
        ),
        "tema_mt_3" to listOf(
            "Sistema" to "Nivel de prueba que corresponde a la especificación del sistema.",
            "Emparejar" to "Lo que hace la V con cada fase de desarrollo y su prueba."
        ),
        "tema_mt_4" to listOf(
            "Núcleo" to "Primer incremento, con las funciones básicas.",
            "Prioridad" to "Criterio para decidir qué incremento se construye primero."
        )
    )

    val temas: List<TemaNuevo> = listOf(
        // =================================================================
        // Mundo 1 · UML
        // =================================================================
        TemaNuevo(
            id = "tema_uml_5", mundoId = "mundo_uml", orden = 5,
            nombre = "Diagramas de Estados",
            descripcion = "Ciclo de vida de un objeto: estados, eventos y transiciones.",
            tituloLeccion = "Estados y Transiciones",
            introduccion = "El Diagrama de Estados (o máquina de estados) describe los estados por los que pasa un objeto durante su vida y los eventos que lo hacen cambiar de uno a otro. Cada estado se dibuja como un rectángulo con esquinas redondeadas y cada cambio como una flecha llamada transición.\n\n" +
                "Una transición puede llevar un evento, una guarda entre corchetes (la condición que debe cumplirse) y una acción. El círculo negro marca el estado inicial y el círculo negro rodeado por otro, el final.",
            ejercicios = listOf(
                p("¿Qué describe un diagrama de estados?", listOf("Los estados por los que pasa un objeto y lo que provoca cada cambio", "Las clases y sus atributos", "El orden de los mensajes entre objetos", "La distribución física del sistema"), 0),
                p("Selecciona los elementos de un diagrama de estados:", listOf("Estado", "Transición", "Evento", "Línea de vida"), 0, 1, 2),
                p("¿Qué provoca que un objeto cambie de estado?", listOf("Un evento", "Un paquete", "Un actor", "Una tabla"), 0),
                p("La condición entre corchetes en una transición, como [saldo > 0], se llama:", listOf("Guarda", "Acción", "Estado compuesto", "Nodo final"), 0),
                p("Verdadero o Falso: El estado final se dibuja como un círculo negro rodeado por otro círculo.", VF, 0)
            ),
            conceptos = listOf(
                "Estado" to "Situación en la que se encuentra un objeto durante un tiempo.",
                "Transición" to "Paso de un estado a otro provocado por un evento.",
                "Evento" to "Suceso que puede provocar un cambio de estado.",
                "Guarda" to "Condición que debe cumplirse para que ocurra una transición.",
                "Acción" to "Operación que se ejecuta al realizarse una transición.",
                "Compuesto" to "Estado que contiene subestados dentro de él.",
                "Final" to "Pseudoestado que indica que el objeto terminó su ciclo de vida."
            ),
            diagnostico = listOf(
                p("¿Qué representa una flecha en un diagrama de estados?", listOf("Una transición entre estados", "Una herencia", "Un mensaje asíncrono", "Una dependencia"), 0),
                p("¿Qué elemento indica la condición para que ocurra una transición?", listOf("La guarda", "El actor", "El carril", "La línea de vida"), 0)
            ),
            flash = listOf(
                p("Un diagrama de estados modela el comportamiento de:", listOf("Un objeto a lo largo de su vida", "Una base de datos", "Un equipo de trabajo", "Un servidor físico"), 0),
                p("Selecciona lo que puede llevar la etiqueta de una transición:", listOf("Evento", "Guarda", "Acción", "Multiplicidad"), 0, 1, 2),
                p("¿Cómo se dibuja un estado en UML?", listOf("Rectángulo con esquinas redondeadas", "Elipse", "Rombo", "Figura de palito"), 0)
            ),
            enigma = "«Cuento la vida de un objeto: dónde está, qué lo despierta y a dónde va». ¿Vienes de mis transiciones?"
        ),
        TemaNuevo(
            id = "tema_uml_6", mundoId = "mundo_uml", orden = 6,
            nombre = "Diagramas de Objetos",
            descripcion = "Instantánea de los objetos y sus enlaces en un momento dado.",
            tituloLeccion = "Instantáneas del Sistema",
            introduccion = "Un Diagrama de Objetos muestra instancias concretas de las clases y los enlaces entre ellas en un momento determinado, como si fuera una fotografía del sistema en ejecución.\n\n" +
                "El nombre de cada objeto va subrayado con la forma objeto : Clase y puede mostrar los valores de sus atributos. Es útil para explicar o validar un diagrama de clases con un ejemplo real.",
            ejercicios = listOf(
                p("¿Qué muestra un diagrama de objetos?", listOf("Las instancias y sus enlaces en un momento concreto", "El orden de los mensajes", "Los casos de uso del sistema", "Las fases del proyecto"), 0),
                p("¿Cómo se escribe el nombre de un objeto?", listOf("Subrayado, como objeto : Clase", "En cursiva", "Entre comillas angulares", "En mayúsculas"), 0),
                p("Un enlace entre dos objetos es una instancia de:", listOf("Una asociación", "Una herencia", "Un estado", "Un paquete"), 0),
                p("Selecciona lo que se muestra en un diagrama de objetos:", listOf("Objetos", "Valores de los atributos", "Enlaces", "Métodos abstractos"), 0, 1, 2),
                p("Verdadero o Falso: Un diagrama de objetos sirve para validar un diagrama de clases con un ejemplo concreto.", VF, 0)
            ),
            conceptos = listOf(
                "Objeto" to "Instancia concreta de una clase.",
                "Instancia" to "Ejemplar creado a partir de una clase.",
                "Enlace" to "Conexión entre dos objetos; instancia de una asociación.",
                "Valor" to "Dato concreto que tiene un atributo en un objeto.",
                "Instantánea" to "Vista del sistema en un momento determinado.",
                "Subrayado" to "Formato con el que se escribe el nombre de un objeto.",
                "Anónimo" to "Objeto que solo indica su clase, sin nombre propio."
            ),
            diagnostico = listOf(
                p("¿En qué se diferencia un diagrama de objetos de uno de clases?", listOf("Muestra instancias concretas en lugar de clases", "Muestra el paso del tiempo", "No tiene relaciones", "Solo muestra actores"), 0),
                p("La notación «cliente1 : Cliente» indica:", listOf("Un objeto llamado cliente1 de la clase Cliente", "Una clase llamada cliente1", "Una herencia", "Un paquete"), 0)
            ),
            flash = listOf(
                p("Un enlace en un diagrama de objetos es una instancia de:", listOf("Asociación", "Generalización", "Transición", "Dependencia"), 0),
                p("Selecciona lo que puede aparecer en un diagrama de objetos:", listOf("Nombre del objeto subrayado", "Valores de atributos", "Enlaces entre objetos", "Fragmentos loop"), 0, 1, 2),
                p("¿Para qué es útil un diagrama de objetos?", listOf("Para ilustrar un ejemplo concreto de un diagrama de clases", "Para planear el presupuesto", "Para modelar el despliegue", "Para medir riesgos"), 0)
            ),
            enigma = "«Soy la foto de un instante: no dibujo moldes, sino lo que salió de ellos». ¿Eres uno de mis retratos?"
        ),
        TemaNuevo(
            id = "tema_uml_7", mundoId = "mundo_uml", orden = 7,
            nombre = "Diagramas de Componentes",
            descripcion = "Partes reemplazables del sistema y las interfaces que las conectan.",
            tituloLeccion = "Piezas del Software",
            introduccion = "El Diagrama de Componentes muestra cómo se divide el sistema en partes modulares y reemplazables, y cómo se conectan entre sí. Un componente encapsula su implementación y solo se comunica por sus interfaces.\n\n" +
                "La interfaz provista se dibuja como un círculo (piruleta) y la requerida como un semicírculo (enchufe). Unir una con otra forma un ensamble: si un componente ofrece las mismas interfaces, puede sustituir a otro.",
            ejercicios = listOf(
                p("¿Qué representa un componente en UML?", listOf("Una parte modular y reemplazable del sistema", "Un usuario", "Un estado", "Un mensaje"), 0),
                p("Una interfaz provista se dibuja como:", listOf("Un círculo (piruleta)", "Un semicírculo (enchufe)", "Un rombo", "Una flecha punteada"), 0),
                p("Una interfaz requerida se dibuja como:", listOf("Un semicírculo (enchufe)", "Un círculo", "Un rectángulo", "Una elipse"), 0),
                p("Selecciona lo que muestra un diagrama de componentes:", listOf("Componentes", "Interfaces", "Dependencias entre componentes", "Líneas de vida"), 0, 1, 2),
                p("Verdadero o Falso: Un componente puede sustituirse por otro que ofrezca las mismas interfaces.", VF, 0)
            ),
            conceptos = listOf(
                "Componente" to "Parte modular y reemplazable de un sistema.",
                "Provista" to "Interfaz que un componente ofrece a otros.",
                "Requerida" to "Interfaz que un componente necesita de otro.",
                "Puerto" to "Punto de interacción de un componente con su entorno.",
                "Dependencia" to "Relación en la que un componente usa a otro.",
                "Ensamble" to "Conector que une una interfaz requerida con una provista.",
                "Modular" to "Cualidad de un sistema dividido en partes independientes."
            ),
            diagnostico = listOf(
                p("¿Qué representa el símbolo de «piruleta» (círculo) en un componente?", listOf("Una interfaz provista", "Un estado final", "Un actor", "Un nodo de decisión"), 0),
                p("Un diagrama de componentes sirve sobre todo para mostrar:", listOf("La organización del sistema en partes y sus dependencias", "El orden de los mensajes", "El cronograma", "Los requisitos"), 0)
            ),
            flash = listOf(
                p("Un componente que necesita un servicio de otro tiene una interfaz:", listOf("Requerida", "Provista", "Abstracta", "Estática"), 0),
                p("Selecciona características de un componente:", listOf("Es reemplazable", "Ofrece interfaces", "Encapsula su implementación", "Es siempre una persona"), 0, 1, 2),
                p("El conector que une una interfaz requerida con una provista se llama:", listOf("Ensamble", "Herencia", "Transición", "Extend"), 0)
            ),
            enigma = "«Encajo como pieza: ofrezco una piruleta y pido un enchufe». ¿Eres de mi ensamble?"
        ),
        TemaNuevo(
            id = "tema_uml_8", mundoId = "mundo_uml", orden = 8,
            nombre = "Diagramas de Despliegue",
            descripcion = "Distribución física del software en nodos de hardware.",
            tituloLeccion = "Dónde Vive el Software",
            introduccion = "El Diagrama de Despliegue muestra la arquitectura física del sistema: los nodos (servidores, teléfonos, sensores) y los artefactos de software que se instalan en ellos.\n\n" +
                "Cada nodo se dibuja como un cubo y dentro van sus artefactos, como un ejecutable o una base de datos. Las rutas de comunicación unen los nodos e indican cómo se comunican, por ejemplo con HTTP.",
            ejercicios = listOf(
                p("¿Qué muestra un diagrama de despliegue?", listOf("La arquitectura física: nodos y artefactos desplegados en ellos", "El flujo de actividades", "Las clases y atributos", "Los casos de uso"), 0),
                p("¿Cómo se representa un nodo?", listOf("Con un cubo (caja 3D)", "Con una elipse", "Con un rombo", "Con un círculo negro"), 0),
                p("Un archivo ejecutable instalado en un nodo es un:", listOf("Artefacto", "Actor", "Estado", "Fragmento"), 0),
                p("Selecciona ejemplos de nodos:", listOf("Servidor web", "Teléfono móvil", "Servidor de base de datos", "Caso de uso"), 0, 1, 2),
                p("La línea que une dos nodos y representa su comunicación (por ejemplo, HTTP) se llama:", listOf("Ruta de comunicación", "Herencia", "Include", "Guarda"), 0)
            ),
            conceptos = listOf(
                "Nodo" to "Recurso físico o de ejecución donde se despliega el software.",
                "Artefacto" to "Archivo físico, como un ejecutable, que se despliega en un nodo.",
                "Servidor" to "Nodo que ofrece servicios a otros equipos de la red.",
                "Dispositivo" to "Nodo de hardware, como un teléfono o un sensor.",
                "Protocolo" to "Reglas de comunicación entre nodos, como HTTP.",
                "Entorno" to "Software de ejecución, como un sistema operativo, que aloja artefactos.",
                "Físico" to "Tipo de arquitectura que muestra el diagrama de despliegue."
            ),
            diagnostico = listOf(
                p("¿Qué figura representa un nodo en un diagrama de despliegue?", listOf("Un cubo tridimensional", "Un óvalo", "Un rombo", "Una barra gruesa"), 0),
                p("¿Qué se coloca dentro de un nodo?", listOf("Los artefactos que se ejecutan en él", "Los actores", "Los requisitos", "Los riesgos"), 0)
            ),
            flash = listOf(
                p("Un .apk instalado en un teléfono se modela como:", listOf("Un artefacto desplegado en un nodo", "Un actor", "Un estado", "Un caso de uso"), 0),
                p("Selecciona lo que aparece en un diagrama de despliegue:", listOf("Nodos", "Artefactos", "Rutas de comunicación", "Fragmentos alt"), 0, 1, 2),
                p("El diagrama de despliegue modela la vista:", listOf("Física del sistema", "De los requisitos", "Del presupuesto", "Del equipo"), 0)
            ),
            enigma = "«Soy de metal y silicio: en mis cubos se instalan los programas». ¿Corres sobre mí?"
        ),
        TemaNuevo(
            id = "tema_uml_9", mundoId = "mundo_uml", orden = 9,
            nombre = "Diagramas de Paquetes",
            descripcion = "Organización de los elementos del modelo en grupos.",
            tituloLeccion = "Ordenar el Modelo",
            introduccion = "Cuando un modelo crece, los Diagramas de Paquetes ayudan a organizarlo. Un paquete agrupa elementos relacionados (clases, casos de uso u otros paquetes) y se dibuja como una carpeta con pestaña.\n\n" +
                "Las dependencias entre paquetes, como «import», indican que un paquete usa elementos públicos de otro. Un buen diseño agrupa por responsabilidad, mantiene bajo el acoplamiento y evita las dependencias circulares.",
            ejercicios = listOf(
                p("¿Para qué sirve un paquete en UML?", listOf("Para agrupar elementos relacionados del modelo", "Para representar un estado", "Para enviar mensajes", "Para mostrar un nodo físico"), 0),
                p("¿Cómo se dibuja un paquete?", listOf("Como una carpeta con pestaña", "Como un cubo", "Como una elipse", "Como un rombo"), 0),
                p("Una flecha punteada «import» entre paquetes indica:", listOf("Que un paquete usa los elementos públicos de otro", "Una herencia", "Un estado final", "Un mensaje síncrono"), 0),
                p("Selecciona buenas prácticas al organizar paquetes:", listOf("Agrupar por responsabilidad", "Evitar dependencias circulares", "Mantener bajo acoplamiento entre paquetes", "Poner todo en un solo paquete"), 0, 1, 2),
                p("Verdadero o Falso: Un paquete puede contener otros paquetes.", VF, 0)
            ),
            conceptos = listOf(
                "Paquete" to "Agrupador de elementos del modelo, dibujado como carpeta.",
                "Agrupar" to "Lo que hace un paquete con los elementos relacionados.",
                "Import" to "Dependencia para usar los elementos públicos de otro paquete.",
                "Anidado" to "Paquete que está dentro de otro paquete.",
                "Carpeta" to "Figura con pestaña con la que se dibuja un paquete.",
                "Circular" to "Tipo de dependencia entre paquetes que se debe evitar.",
                "Visibilidad" to "Indica si un elemento del paquete es público o privado."
            ),
            diagnostico = listOf(
                p("¿Qué figura representa un paquete en UML?", listOf("Una carpeta con pestaña", "Un cubo", "Un círculo negro", "Una figura de palito"), 0),
                p("¿Qué tipo de dependencias entre paquetes conviene evitar?", listOf("Las circulares", "Las de import", "Las de uso", "Ninguna"), 0)
            ),
            flash = listOf(
                p("Un paquete sirve para:", listOf("Organizar y agrupar elementos del modelo", "Ejecutar el código", "Representar usuarios", "Modelar el tiempo"), 0),
                p("Selecciona lo que puede contener un paquete:", listOf("Clases", "Otros paquetes", "Casos de uso", "Una ejecución del programa"), 0, 1, 2),
                p("La relación «import» entre paquetes se dibuja como:", listOf("Flecha punteada con el estereotipo «import»", "Línea con rombo negro", "Triángulo hueco", "Barra gruesa"), 0)
            ),
            enigma = "«Guardo elementos como una carpeta y odio las dependencias en círculo». ¿Duermes en mí?"
        ),
        TemaNuevo(
            id = "tema_uml_10", mundoId = "mundo_uml", orden = 10,
            nombre = "Diagramas de Comunicación",
            descripcion = "Interacciones entre objetos organizadas por sus enlaces.",
            tituloLeccion = "Mensajes Numerados",
            introduccion = "El Diagrama de Comunicación (llamado de colaboración en UML 1) muestra la misma interacción que un diagrama de secuencia, pero organizada por los enlaces entre objetos en lugar de por el tiempo.\n\n" +
                "Como no hay un eje de tiempo, el orden de los mensajes se indica con números de secuencia: 1, 1.1, 2… Un asterisco marca un mensaje que se repite (iteración).",
            ejercicios = listOf(
                p("¿Qué enfatiza un diagrama de comunicación?", listOf("Los enlaces entre objetos y los mensajes que viajan por ellos", "El paso del tiempo de arriba hacia abajo", "Los estados de un objeto", "La distribución física"), 0),
                p("¿Cómo se indica el orden de los mensajes?", listOf("Con números de secuencia (1, 1.1, 2…)", "Con colores", "Con el tamaño de la flecha", "No se indica"), 0),
                p("Un diagrama de comunicación muestra información equivalente a la de un diagrama de:", listOf("Secuencia", "Despliegue", "Paquetes", "Casos de uso"), 0),
                p("Selecciona elementos de un diagrama de comunicación:", listOf("Objetos", "Enlaces", "Mensajes numerados", "Carriles"), 0, 1, 2),
                p("Verdadero o Falso: En un diagrama de comunicación el tiempo fluye de arriba hacia abajo.", VF, 1)
            ),
            conceptos = listOf(
                "Colaboración" to "Nombre del diagrama de comunicación en UML 1.",
                "Numeración" to "Forma de indicar el orden de los mensajes.",
                "Vínculo" to "Conexión entre objetos por la que viajan los mensajes.",
                "Receptor" to "Objeto que recibe un mensaje.",
                "Emisor" to "Objeto que envía un mensaje.",
                "Interacción" to "Intercambio de mensajes entre objetos para cumplir una tarea.",
                "Iteración" to "Mensaje que se repite, marcado con un asterisco."
            ),
            diagnostico = listOf(
                p("¿Qué indica la etiqueta «2.1» en un mensaje de un diagrama de comunicación?", listOf("Su orden dentro de la interacción", "Su prioridad de red", "Su costo", "Su versión"), 0),
                p("¿Qué otro diagrama muestra la misma interacción pero ordenada en el tiempo?", listOf("El de secuencia", "El de clases", "El de paquetes", "El de despliegue"), 0)
            ),
            flash = listOf(
                p("En un diagrama de comunicación, el orden de los mensajes se lee:", listOf("En su numeración", "De arriba hacia abajo", "De izquierda a derecha", "No tiene orden"), 0),
                p("Selecciona lo que comparte con el diagrama de secuencia:", listOf("Muestra objetos que interactúan", "Muestra mensajes entre objetos", "Es un diagrama de interacción", "Muestra nodos de hardware"), 0, 1, 2),
                p("En UML 1, el diagrama de comunicación se llamaba:", listOf("Diagrama de colaboración", "Diagrama de flujo", "Diagrama de Gantt", "Diagrama de red"), 0)
            ),
            enigma = "«Mis mensajes no bajan con el tiempo: llevan números y viajan por enlaces». ¿Te contaron mi secuencia?"
        ),

        // =================================================================
        // Mundo 2 · Casos de Uso
        // =================================================================
        TemaNuevo(
            id = "tema_cu_5", mundoId = "mundo_casos_uso", orden = 5,
            nombre = "Generalización en Casos de Uso",
            descripcion = "Herencia entre actores y entre casos de uso.",
            tituloLeccion = "Herencia de Actores y Casos",
            introduccion = "La generalización permite que un actor o un caso de uso herede de otro más general. Si «Administrador» es hijo de «Usuario», puede realizar todos los casos de uso de Usuario y además los suyos.\n\n" +
                "Se dibuja con una línea continua y un triángulo hueco que apunta al elemento general. Un caso de uso hijo hereda el comportamiento del padre y puede especializarlo; un caso de uso abstracto existe solo para ser especializado.",
            ejercicios = listOf(
                p("La generalización entre actores indica que:", listOf("El actor hijo puede hacer todo lo que hace el padre y más", "El padre es parte del hijo", "Los actores son el mismo", "El hijo nunca usa el sistema"), 0),
                p("¿Cómo se dibuja la generalización?", listOf("Línea con triángulo hueco hacia el elemento general", "Flecha punteada «include»", "Línea con rombo", "Sin flecha"), 0),
                p("Si «Administrador» hereda de «Usuario», el administrador:", listOf("Puede realizar los casos de uso de Usuario", "No puede iniciar sesión", "Pierde sus permisos", "Es un sistema externo"), 0),
                p("Selecciona los elementos que pueden generalizarse en un modelo de casos de uso:", listOf("Actores", "Casos de uso", "Límite del sistema", "Notas"), 0, 1),
                p("Verdadero o Falso: Un caso de uso hijo hereda el comportamiento del padre y puede especializarlo.", VF, 0)
            ),
            conceptos = listOf(
                "Padre" to "Elemento general del que heredan otros.",
                "Hijo" to "Elemento especializado que hereda de otro.",
                "Triángulo" to "Punta hueca con la que se dibuja la generalización.",
                "Especializar" to "Agregar o redefinir comportamiento en el elemento hijo.",
                "Abstracto" to "Caso de uso general que no se ejecuta por sí solo.",
                "Rol" to "Papel que juega un actor frente al sistema.",
                "Hereda" to "Lo que hace el actor hijo con los casos de uso del padre."
            ),
            diagnostico = listOf(
                p("¿Qué indica una línea con triángulo hueco entre dos actores?", listOf("Generalización (herencia)", "Include", "Extend", "Comunicación"), 0),
                p("Si Gerente es hijo de Empleado, Gerente puede:", listOf("Hacer todo lo que hace Empleado", "Hacer solo casos propios", "No usar el sistema", "Ser un caso de uso"), 0)
            ),
            flash = listOf(
                p("La punta de la flecha de generalización apunta hacia:", listOf("El elemento más general (padre)", "El elemento hijo", "El límite del sistema", "El actor secundario"), 0),
                p("Selecciona ventajas de generalizar actores:", listOf("Evita repetir asociaciones", "Refleja roles con más permisos", "Simplifica el diagrama", "Elimina los casos de uso"), 0, 1, 2),
                p("Un caso de uso que solo existe para ser especializado por otros se llama:", listOf("Abstracto", "Primario", "Concreto final", "Secundario"), 0)
            ),
            enigma = "«El hijo puede todo lo del padre y algo más; mi flecha termina en un triángulo vacío». ¿Heredaste de mí?"
        ),
        TemaNuevo(
            id = "tema_cu_6", mundoId = "mundo_casos_uso", orden = 6,
            nombre = "Tipos de Actores",
            descripcion = "Actores primarios, secundarios y sistemas externos.",
            tituloLeccion = "¿Quién Inicia y Quién Ayuda?",
            introduccion = "No todos los actores participan igual. El actor primario inicia el caso de uso porque quiere lograr un objetivo; el actor secundario (o de soporte) le da un servicio al sistema mientras el caso de uso ocurre, como un banco que autoriza un pago.\n\n" +
                "Un actor puede ser una persona, otro sistema o incluso un temporizador que dispara un proceso a cierta hora. Un mismo actor puede ser primario en un caso de uso y secundario en otro.",
            ejercicios = listOf(
                p("El actor primario es quien:", listOf("Inicia el caso de uso para cumplir su objetivo", "Solo recibe notificaciones", "Programa el sistema", "Nunca interactúa"), 0),
                p("Un servicio externo de pagos que el sistema consulta durante una compra es un actor:", listOf("Secundario o de soporte", "Primario", "Interno", "Abstracto"), 0),
                p("Selecciona ejemplos que pueden ser actores secundarios:", listOf("Servidor de correo", "Banco que autoriza pagos", "Sistema de facturación externo", "El botón Aceptar"), 0, 1, 2),
                p("¿Un temporizador puede ser actor?", listOf("Sí, cuando dispara un caso de uso en un momento dado", "No, nunca", "Solo si es una persona", "Solo en diagramas de clases"), 0),
                p("Verdadero o Falso: Un mismo actor puede ser primario en un caso de uso y secundario en otro.", VF, 0)
            ),
            conceptos = listOf(
                "Primario" to "Actor que inicia el caso de uso para lograr su objetivo.",
                "Secundario" to "Actor que da un servicio al sistema durante el caso de uso.",
                "Soporte" to "Otro nombre del actor secundario que ayuda al sistema.",
                "Temporizador" to "Actor que dispara un caso de uso en un momento programado.",
                "Externo" to "Sistema fuera del límite que interactúa con el nuestro.",
                "Usuario" to "Persona que utiliza el sistema para lograr algo.",
                "Interesado" to "Quien tiene interés en el resultado aunque no use el sistema."
            ),
            diagnostico = listOf(
                p("¿Quién es el actor primario en «Retirar dinero» de un cajero?", listOf("El cliente del banco", "El sistema del banco central", "La impresora de recibos", "El técnico"), 0),
                p("Un sistema externo que solo responde cuando nuestro sistema lo consulta es un actor:", listOf("Secundario", "Primario", "Abstracto", "Interno"), 0)
            ),
            flash = listOf(
                p("¿Qué distingue al actor primario?", listOf("Inicia el caso de uso", "Siempre es una máquina", "Nunca aparece en el diagrama", "Está dentro del sistema"), 0),
                p("Selecciona lo que puede ser un actor:", listOf("Una persona", "Un sistema externo", "Un temporizador", "Una clase interna del código"), 0, 1, 2),
                p("Un actor secundario suele colocarse en el diagrama:", listOf("Del lado derecho, fuera del límite del sistema", "Dentro del límite", "Encima del caso de uso", "No se dibuja"), 0)
            ),
            enigma = "«Unos llegan pidiendo, otros llegan ayudando; todos se quedan afuera del muro». ¿De qué lado estás?"
        ),
        TemaNuevo(
            id = "tema_cu_7", mundoId = "mundo_casos_uso", orden = 7,
            nombre = "Escenarios",
            descripcion = "Recorridos concretos de un caso de uso: el camino feliz y sus variantes.",
            tituloLeccion = "Una Historia Concreta",
            introduccion = "Un escenario es un recorrido concreto por los pasos de un caso de uso, con datos y decisiones específicas. El escenario principal de éxito, o camino feliz, es aquel en el que todo sale bien y el actor logra su objetivo.\n\n" +
                "Cada desvío (un pago rechazado, un dato inválido) es un escenario alternativo o de excepción. Para encontrarlos se pregunta qué puede salir mal en cada paso, y cada escenario puede convertirse después en un caso de prueba.",
            ejercicios = listOf(
                p("Un escenario es:", listOf("Un recorrido concreto de pasos dentro de un caso de uso", "Un actor", "Un diagrama de clases", "Una prueba unitaria"), 0),
                p("El escenario principal de éxito también se conoce como:", listOf("Camino feliz", "Flujo de excepción", "Precondición", "Diagrama de estados"), 0),
                p("¿Cuántos escenarios puede tener un caso de uso?", listOf("Uno principal y varios alternativos", "Solo uno", "Ninguno", "Exactamente dos"), 0),
                p("Selecciona lo que ayuda a encontrar escenarios alternativos:", listOf("Preguntar qué puede salir mal en cada paso", "Revisar las reglas del negocio", "Preguntar al actor por opciones distintas", "Elegir los colores de la interfaz"), 0, 1, 2),
                p("Verdadero o Falso: Los escenarios sirven de base para diseñar casos de prueba.", VF, 0)
            ),
            conceptos = listOf(
                "Camino" to "Recorrido por los pasos de un caso de uso; el feliz no tiene errores.",
                "Variante" to "Escenario que se aparta del camino principal.",
                "Excepción" to "Escenario en el que algo falla y el objetivo no se logra.",
                "Historia" to "Relato concreto de cómo un actor usa el sistema.",
                "Concreto" to "Cómo es un escenario: con datos y pasos específicos.",
                "Desvío" to "Punto donde un escenario se separa del flujo principal.",
                "Éxito" to "Resultado del escenario principal: el actor logra su objetivo."
            ),
            diagnostico = listOf(
                p("¿Qué relación hay entre un caso de uso y sus escenarios?", listOf("Un caso de uso agrupa varios escenarios", "Un escenario agrupa varios casos de uso", "Son lo mismo", "No tienen relación"), 0),
                p("Un escenario donde el pago es rechazado es un escenario:", listOf("Alternativo o de excepción", "Principal de éxito", "De diseño", "De despliegue"), 0)
            ),
            flash = listOf(
                p("El camino feliz describe:", listOf("El escenario en que todo sale bien", "Todos los errores posibles", "Las pruebas de carga", "El modelo de datos"), 0),
                p("Selecciona usos de los escenarios:", listOf("Validar requisitos con el cliente", "Diseñar casos de prueba", "Descubrir flujos alternativos", "Compilar el código"), 0, 1, 2),
                p("Un escenario es una ___ de un caso de uso:", listOf("Instancia", "Generalización", "Clase", "Interfaz"), 0)
            ),
            enigma = "«Soy una historia con nombre y datos: a veces feliz, a veces con tropiezos». ¿Me has recorrido?"
        ),
        TemaNuevo(
            id = "tema_cu_8", mundoId = "mundo_casos_uso", orden = 8,
            nombre = "Niveles de Objetivo",
            descripcion = "Tamaño de un caso de uso: resumen, objetivo de usuario y subfunción.",
            tituloLeccion = "Del Cielo al Fondo del Mar",
            introduccion = "Alistair Cockburn propuso clasificar los casos de uso por su nivel de objetivo con una metáfora del mar. En las nubes están los de resumen, que agrupan varios objetivos; al nivel del mar, los de objetivo de usuario; y bajo el agua, las subfunciones.\n\n" +
                "La mayoría de los casos de uso deberían ser de objetivo de usuario: el actor lo completa en una sola sesión y queda satisfecho (por ejemplo, «Comprar boleto»). Algo como «Validar contraseña» es una subfunción y no aporta valor por sí sola.",
            ejercicios = listOf(
                p("Según Alistair Cockburn, el nivel ideal de la mayoría de los casos de uso es:", listOf("Objetivo de usuario (nivel del mar)", "Subfunción", "Resumen estratégico", "Nivel de código"), 0),
                p("«Validar contraseña» suele ser un caso de uso de nivel:", listOf("Subfunción", "Objetivo de usuario", "Resumen", "Estratégico"), 0),
                p("Una prueba para saber si un caso de uso es de objetivo de usuario es:", listOf("Que el actor lo complete en una sola sesión y quede satisfecho", "Que tenga más de 50 pasos", "Que use una base de datos", "Que tenga include"), 0),
                p("Selecciona los niveles de objetivo de Cockburn:", listOf("Resumen", "Objetivo de usuario", "Subfunción", "Compilación"), 0, 1, 2),
                p("Verdadero o Falso: Un caso de uso de resumen agrupa varios casos de uso de objetivo de usuario.", VF, 0)
            ),
            conceptos = listOf(
                "Granularidad" to "Nivel de detalle o tamaño de un caso de uso.",
                "Resumen" to "Nivel alto que agrupa varios objetivos de usuario.",
                "Subfunción" to "Nivel bajo: paso reutilizable que no da valor por sí solo.",
                "Cockburn" to "Apellido del autor de los niveles de objetivo.",
                "Sesión" to "Tiempo en el que el actor completa un caso de uso de objetivo de usuario.",
                "Mar" to "Metáfora del nivel de objetivo de usuario.",
                "Meta" to "Lo que el actor quiere conseguir con el caso de uso."
            ),
            diagnostico = listOf(
                p("«Comprar un boleto de avión» es un caso de uso de nivel:", listOf("Objetivo de usuario", "Subfunción", "Código", "Prueba"), 0),
                p("¿Qué problema tiene modelar solo subfunciones como «Validar campo»?", listOf("Son demasiado pequeñas y no dan valor al actor", "Son demasiado grandes", "No tienen actores", "No se pueden escribir"), 0)
            ),
            flash = listOf(
                p("El nivel «del mar» corresponde a:", listOf("Objetivo de usuario", "Subfunción", "Resumen", "Arquitectura"), 0),
                p("Selecciona señales de que un caso de uso es demasiado pequeño:", listOf("No aporta valor por sí solo", "Siempre se usa dentro de otro", "Es un solo paso técnico", "El actor logra su meta completa"), 0, 1, 2),
                p("¿Quién propuso los niveles de objetivo de los casos de uso?", listOf("Alistair Cockburn", "Barry Boehm", "James Martin", "Winston Royce"), 0)
            ),
            enigma = "«Unos vuelan como nubes, otros nadan como peces; los mejores flotan al nivel del mar». ¿A qué altura vives?"
        ),
        TemaNuevo(
            id = "tema_cu_9", mundoId = "mundo_casos_uso", orden = 9,
            nombre = "Trazabilidad de Requisitos",
            descripcion = "Relación entre requisitos, casos de uso, diseño y pruebas.",
            tituloLeccion = "Hilos que Conectan",
            introduccion = "La trazabilidad es la capacidad de seguir un requisito desde su origen hasta los casos de uso, el diseño, el código y las pruebas que lo cumplen. Hacia atrás, cada caso de uso debería llevar a la necesidad que lo originó.\n\n" +
                "La herramienta más común es la matriz de trazabilidad, una tabla que cruza requisitos con otros artefactos. Sirve para analizar el impacto de un cambio y para detectar requisitos sin implementar o sin prueba.",
            ejercicios = listOf(
                p("La trazabilidad permite:", listOf("Seguir un requisito desde su origen hasta el diseño, el código y las pruebas", "Borrar requisitos", "Dibujar interfaces", "Elegir el lenguaje"), 0),
                p("Una matriz de trazabilidad relaciona:", listOf("Requisitos con casos de uso, diseño o pruebas", "Empleados con salarios", "Clases con colores", "Servidores con precios"), 0),
                p("Si cambia un requisito, la trazabilidad ayuda a:", listOf("Saber qué casos de uso, diseños y pruebas se ven afectados", "Ocultar el cambio", "Evitar probar", "Cambiar de cliente"), 0),
                p("Selecciona beneficios de la trazabilidad:", listOf("Analizar el impacto de un cambio", "Verificar que todo requisito tenga una prueba", "Detectar requisitos sin implementar", "Escribir código sin diseño"), 0, 1, 2),
                p("Verdadero o Falso: Cada caso de uso debería poder rastrearse hasta al menos un requisito del cliente.", VF, 0)
            ),
            conceptos = listOf(
                "Trazabilidad" to "Capacidad de seguir un requisito a lo largo del proyecto.",
                "Matriz" to "Tabla que cruza requisitos con otros artefactos.",
                "Impacto" to "Lo que se analiza para saber qué afecta un cambio.",
                "Origen" to "De dónde viene un requisito: cliente, ley o negocio.",
                "Rastrear" to "Seguir la relación de un requisito hacia adelante o hacia atrás.",
                "Cobertura" to "Porcentaje de requisitos que tienen una prueba asociada.",
                "Cambio" to "Modificación de un requisito que obliga a revisar lo relacionado."
            ),
            diagnostico = listOf(
                p("¿Qué herramienta muestra qué prueba verifica cada requisito?", listOf("La matriz de trazabilidad", "El diagrama de despliegue", "El diagrama de Gantt", "La bitácora de errores"), 0),
                p("Un requisito sin ningún caso de uso ni prueba asociados indica:", listOf("Un hueco en la cobertura", "Que el sistema está terminado", "Que el requisito es opcional", "Nada importante"), 0)
            ),
            flash = listOf(
                p("La trazabilidad hacia atrás conecta un caso de uso con:", listOf("El requisito o la necesidad que lo originó", "El código compilado", "El servidor", "El presupuesto"), 0),
                p("Selecciona artefactos que se suelen rastrear:", listOf("Requisitos", "Casos de uso", "Casos de prueba", "El color del logotipo"), 0, 1, 2),
                p("El análisis de impacto sirve para:", listOf("Saber qué partes cambian si se modifica un requisito", "Medir la velocidad del servidor", "Calcular salarios", "Diseñar el logotipo"), 0)
            ),
            enigma = "«Ato cada necesidad del cliente con su caso, su diseño y su prueba». ¿Puedes seguir mi hilo?"
        ),
        TemaNuevo(
            id = "tema_cu_10", mundoId = "mundo_casos_uso", orden = 10,
            nombre = "De Casos de Uso a Pruebas",
            descripcion = "Derivar casos de prueba a partir de los flujos de un caso de uso.",
            tituloLeccion = "Probar lo Prometido",
            introduccion = "Cada flujo de un caso de uso, el normal y los alternativos, es una promesa del sistema que se puede comprobar. Por eso de cada flujo se deriva al menos un caso de prueba, normalmente de sistema o de aceptación.\n\n" +
                "Un caso de prueba indica las precondiciones (el estado que hay que preparar), los datos de entrada, los pasos y el resultado esperado, que sale de las postcondiciones. La prueba pasa cuando el resultado obtenido coincide con el esperado.",
            ejercicios = listOf(
                p("A partir de cada flujo de un caso de uso se puede derivar:", listOf("Al menos un caso de prueba", "Un diagrama de despliegue", "Un nuevo actor", "Una clase abstracta"), 0),
                p("Un caso de prueba incluye:", listOf("Datos de entrada, pasos y resultado esperado", "Solo el nombre del programador", "El presupuesto", "El diagrama de clases"), 0),
                p("Las precondiciones del caso de uso, en una prueba, se convierten en:", listOf("El estado inicial que debe prepararse antes de ejecutarla", "El resultado esperado", "El reporte final", "Un actor"), 0),
                p("Selecciona lo que deberían cubrir las pruebas derivadas de un caso de uso:", listOf("El flujo normal", "Los flujos alternativos", "Las excepciones", "La paleta de colores"), 0, 1, 2),
                p("Las pruebas basadas en casos de uso suelen ser pruebas de:", listOf("Aceptación o de sistema", "Unidad de un método", "Compilación", "Hardware"), 0)
            ),
            conceptos = listOf(
                "Entrada" to "Datos que se dan al sistema en un caso de prueba.",
                "Esperado" to "Resultado que debería obtenerse si el sistema funciona bien.",
                "Obtenido" to "Resultado real que dio el sistema al ejecutar la prueba.",
                "Criterio" to "Condición que define si la prueba pasa o falla.",
                "Derivar" to "Obtener casos de prueba a partir de los flujos.",
                "Ejecutar" to "Correr los pasos de una prueba sobre el sistema.",
                "Oráculo" to "Fuente que dice cuál es el resultado correcto de una prueba."
            ),
            diagnostico = listOf(
                p("¿Qué parte del caso de uso indica el resultado esperado de una prueba?", listOf("Las postcondiciones", "Los actores secundarios", "El nombre", "El límite del sistema"), 0),
                p("¿Cuántas pruebas conviene derivar de un caso de uso?", listOf("Al menos una por cada flujo, normal y alternativos", "Solo una", "Ninguna", "Una por cada actor del mundo"), 0)
            ),
            flash = listOf(
                p("Una prueba pasa cuando:", listOf("El resultado obtenido coincide con el esperado", "Tarda poco", "Nadie la revisa", "Tiene muchos pasos"), 0),
                p("Selecciona partes de un caso de prueba:", listOf("Precondiciones", "Pasos", "Resultado esperado", "Nombre del cliente favorito"), 0, 1, 2),
                p("Las pruebas derivadas de casos de uso verifican principalmente:", listOf("Que el sistema cumpla lo que el actor necesita", "El rendimiento del compilador", "El estilo del código", "La red eléctrica"), 0)
            ),
            enigma = "«De cada flujo saco una promesa y luego compruebo si se cumple». ¿Eres de mis comprobaciones?"
        ),

        // =================================================================
        // Mundo 3 · Ciclo de Vida
        // =================================================================
        TemaNuevo(
            id = "tema_cv_5", mundoId = "mundo_ciclo_vida", orden = 5,
            nombre = "Estudio de Viabilidad",
            descripcion = "Decidir si el proyecto conviene antes de invertir en él.",
            tituloLeccion = "¿Vale la Pena?",
            introduccion = "Antes de comprometer la mayor parte del presupuesto, el estudio de viabilidad (o factibilidad) responde si el proyecto es posible y conveniente. Su resultado es una recomendación: seguir, ajustar o cancelar.\n\n" +
                "Se revisan varios tipos de viabilidad: técnica (¿hay tecnología y conocimiento?), económica (¿los beneficios superan los costos?), operativa (¿la organización lo podrá usar?) y legal (¿cumple leyes y normas?).",
            ejercicios = listOf(
                p("El estudio de viabilidad responde principalmente:", listOf("Si el proyecto es posible y conveniente", "Qué lenguaje usar", "Qué colores tendrá", "Cuántos errores habrá"), 0),
                p("Selecciona tipos de viabilidad:", listOf("Técnica", "Económica", "Operativa", "Decorativa"), 0, 1, 2),
                p("La viabilidad económica compara:", listOf("Costos contra beneficios esperados", "Pantallas contra botones", "Clases contra objetos", "Pruebas contra errores"), 0),
                p("La viabilidad técnica evalúa:", listOf("Si existe la tecnología y el conocimiento para construirlo", "Si el cliente es amable", "El color del logotipo", "Los días festivos"), 0),
                p("Verdadero o Falso: El estudio de viabilidad se hace antes de comprometer la mayor parte del presupuesto.", VF, 0)
            ),
            conceptos = listOf(
                "Viabilidad" to "Grado en que un proyecto es posible y conveniente.",
                "Factibilidad" to "Sinónimo de viabilidad.",
                "Técnica" to "Viabilidad que revisa si hay tecnología y conocimiento.",
                "Económica" to "Viabilidad que compara costos y beneficios.",
                "Operativa" to "Viabilidad que revisa si la organización podrá usar el sistema.",
                "Beneficio" to "Ganancia o ahorro que se espera del sistema.",
                "Legal" to "Viabilidad que revisa leyes y normas que afectan al proyecto."
            ),
            diagnostico = listOf(
                p("¿Cuándo se realiza el estudio de viabilidad?", listOf("Al inicio, antes de comprometer recursos", "Después de entregar", "Durante el mantenimiento", "Al retirar el sistema"), 0),
                p("Si los costos superan por mucho los beneficios, el proyecto es poco viable:", listOf("Económicamente", "Técnicamente", "Visualmente", "Legalmente"), 0)
            ),
            flash = listOf(
                p("La viabilidad operativa revisa:", listOf("Si los usuarios y la organización podrán usar el sistema", "El precio del servidor", "El tamaño del código", "La velocidad de la red"), 0),
                p("Selecciona preguntas de un estudio de viabilidad:", listOf("¿Se puede construir?", "¿Conviene económicamente?", "¿Lo usarán?", "¿Qué fuente tendrá el menú?"), 0, 1, 2),
                p("El resultado de un estudio de viabilidad es:", listOf("Una recomendación de seguir o no con el proyecto", "El código fuente", "El manual de usuario", "La prueba de aceptación"), 0)
            ),
            enigma = "«Antes de forjar pregunto si hay hierro, oro y brazos para usar la espada». ¿Pasaste mi juicio?"
        ),
        TemaNuevo(
            id = "tema_cv_6", mundoId = "mundo_ciclo_vida", orden = 6,
            nombre = "Planificación del Proyecto",
            descripcion = "Estimar tiempos, recursos y costos, y organizar las tareas.",
            tituloLeccion = "Trazar el Camino",
            introduccion = "La planificación define el alcance del proyecto y lo divide en tareas con sus tiempos, recursos y costos estimados. El resultado es un plan que guía y permite medir el avance.\n\n" +
                "El diagrama de Gantt muestra las tareas como barras sobre un calendario; los hitos marcan puntos de control importantes. La ruta crítica es la secuencia de tareas que fija la duración mínima: si una se retrasa, se retrasa todo el proyecto.",
            ejercicios = listOf(
                p("La planificación del proyecto define:", listOf("Tareas, tiempos, recursos y costos", "Solo el diseño de la base de datos", "El código fuente", "Los colores"), 0),
                p("Un diagrama de Gantt muestra:", listOf("Las tareas en una línea de tiempo", "Las clases del sistema", "Los estados de un objeto", "Los nodos físicos"), 0),
                p("Un hito es:", listOf("Un punto de control importante del proyecto", "Un tipo de error", "Una clase abstracta", "Un actor secundario"), 0),
                p("Selecciona elementos de un plan de proyecto:", listOf("Cronograma", "Estimación de costos", "Asignación de recursos", "Contraseña del servidor"), 0, 1, 2),
                p("La ruta crítica es:", listOf("La secuencia de tareas que determina la duración mínima del proyecto", "La tarea más fácil", "El camino del servidor", "Una prueba unitaria"), 0)
            ),
            conceptos = listOf(
                "Cronograma" to "Calendario de tareas y fechas del proyecto.",
                "Gantt" to "Diagrama de barras que muestra tareas en el tiempo.",
                "Hito" to "Punto de control importante del proyecto.",
                "Estimación" to "Cálculo anticipado de esfuerzo, tiempo o costo.",
                "Recurso" to "Persona, equipo o dinero que se asigna a una tarea.",
                "Crítica" to "Ruta de tareas que fija la duración mínima del proyecto.",
                "Alcance" to "Lo que el proyecto incluye y lo que no."
            ),
            diagnostico = listOf(
                p("¿Qué diagrama representa las tareas como barras en un calendario?", listOf("Gantt", "Clases", "Estados", "Casos de uso"), 0),
                p("Si una tarea de la ruta crítica se retrasa:", listOf("Se retrasa todo el proyecto", "No pasa nada", "Se acorta el proyecto", "Se eliminan las pruebas"), 0)
            ),
            flash = listOf(
                p("Un hito en un proyecto es:", listOf("Un evento de control, como entregar un prototipo", "Un error de código", "Un requisito no funcional", "Un empleado"), 0),
                p("Selecciona lo que se estima al planificar:", listOf("Esfuerzo", "Duración", "Costo", "Color favorito del equipo"), 0, 1, 2),
                p("El alcance del proyecto define:", listOf("Qué se incluye y qué no", "El lenguaje de programación", "La marca de las computadoras", "El horario del café"), 0)
            ),
            enigma = "«Pinto barras sobre el calendario y clavo hitos en el camino». ¿Estás en mi cronograma?"
        ),
        TemaNuevo(
            id = "tema_cv_7", mundoId = "mundo_ciclo_vida", orden = 7,
            nombre = "Despliegue e Implantación",
            descripcion = "Instalar, configurar y poner el sistema en manos de los usuarios.",
            tituloLeccion = "Llegar a Producción",
            introduccion = "El despliegue lleva el sistema del entorno de desarrollo al de producción, donde lo usan los usuarios reales. Incluye instalar y configurar el software, migrar los datos del sistema anterior y capacitar a los usuarios.\n\n" +
                "Hay varias estrategias: directa (se cambia todo de una vez), paralela (conviven el sistema viejo y el nuevo), piloto (primero con un grupo reducido) y por fases. Elegir bien reduce el riesgo de la puesta en marcha.",
            ejercicios = listOf(
                p("El despliegue consiste en:", listOf("Instalar y poner en operación el sistema en su entorno real", "Escribir los requisitos", "Diseñar la base de datos", "Hacer la entrevista inicial"), 0),
                p("Selecciona actividades del despliegue:", listOf("Instalación", "Migración de datos", "Capacitación de usuarios", "Elegir el nombre del proyecto"), 0, 1, 2),
                p("Una estrategia en la que el sistema nuevo y el viejo funcionan al mismo tiempo se llama:", listOf("Paralela", "Directa", "Piloto", "Retiro"), 0),
                p("Un despliegue piloto significa:", listOf("Probar el sistema primero con un grupo reducido de usuarios", "Instalarlo en todos de golpe", "No instalarlo", "Desinstalarlo"), 0),
                p("El entorno donde los usuarios reales usan el sistema se llama:", listOf("Producción", "Desarrollo", "Diseño", "Análisis"), 0)
            ),
            conceptos = listOf(
                "Producción" to "Entorno donde los usuarios reales usan el sistema.",
                "Instalación" to "Colocar el software en los equipos donde funcionará.",
                "Migración" to "Pasar los datos del sistema viejo al nuevo.",
                "Capacitación" to "Enseñar a los usuarios a usar el sistema.",
                "Piloto" to "Despliegue de prueba con un grupo reducido.",
                "Paralela" to "Estrategia en la que conviven el sistema viejo y el nuevo.",
                "Versión" to "Entrega identificada del software, como la 1.0."
            ),
            diagnostico = listOf(
                p("¿Qué estrategia reduce riesgos al probar primero con pocos usuarios?", listOf("Piloto", "Directa", "Retiro", "Big bang"), 0),
                p("Pasar los registros del sistema anterior al nuevo se llama:", listOf("Migración de datos", "Compilación", "Refactorización", "Elicitación"), 0)
            ),
            flash = listOf(
                p("La implantación directa consiste en:", listOf("Reemplazar el sistema viejo por el nuevo de una sola vez", "Usar ambos sistemas", "Probar con un grupo", "No instalar nada"), 0),
                p("Selecciona lo que se entrega junto con el sistema al desplegar:", listOf("Manual de usuario", "Capacitación", "Datos migrados", "Las entrevistas sin resumir"), 0, 1, 2),
                p("El entorno de producción es:", listOf("Donde el sistema opera con usuarios reales", "Donde se escriben los requisitos", "El salón de clases", "Un diagrama de clases"), 0)
            ),
            enigma = "«Saco al sistema del taller y lo dejo en manos de su gente». ¿Ya llegaste a producción?"
        ),
        TemaNuevo(
            id = "tema_cv_8", mundoId = "mundo_ciclo_vida", orden = 8,
            nombre = "Gestión de la Configuración",
            descripcion = "Control de versiones y de cambios de los artefactos del proyecto.",
            tituloLeccion = "Cada Cambio Cuenta",
            introduccion = "La gestión de la configuración controla las versiones y los cambios de todo lo que produce el proyecto: código, documentos, diagramas y scripts. Así se sabe qué cambió, quién lo cambió y cuándo, y se puede volver a una versión anterior.\n\n" +
                "Herramientas como Git guardan el historial en un repositorio. Las ramas permiten trabajar cambios aislados que luego se fusionan; si dos personas tocan lo mismo, surge un conflicto. Una línea base es una versión aprobada que sirve de referencia.",
            ejercicios = listOf(
                p("La gestión de la configuración se encarga de:", listOf("Controlar las versiones y los cambios de los artefactos", "Contratar personal", "Diseñar logotipos", "Elegir el cliente"), 0),
                p("Git es un ejemplo de:", listOf("Sistema de control de versiones", "Lenguaje de modelado", "Base de datos", "Diagrama UML"), 0),
                p("Una línea base es:", listOf("Una versión aprobada que sirve de referencia para los cambios", "Un error de compilación", "Un actor", "Un tipo de prueba"), 0),
                p("Selecciona beneficios del control de versiones:", listOf("Recuperar versiones anteriores", "Trabajar en equipo sin perder cambios", "Saber quién cambió qué y cuándo", "Eliminar la necesidad de probar"), 0, 1, 2),
                p("Una rama (branch) permite:", listOf("Desarrollar cambios aislados sin afectar la versión principal", "Borrar el repositorio", "Compilar más rápido", "Diseñar la interfaz"), 0)
            ),
            conceptos = listOf(
                "Repositorio" to "Lugar donde se guarda el historial de versiones.",
                "Rama" to "Línea de desarrollo separada de la principal.",
                "Commit" to "Registro de un conjunto de cambios en el repositorio.",
                "Fusión" to "Unir los cambios de una rama con otra.",
                "Conflicto" to "Choque entre cambios de dos personas en la misma parte.",
                "Historial" to "Registro de todos los cambios hechos a un artefacto.",
                "Etiqueta" to "Nombre que marca una versión importante, como v1.0."
            ),
            diagnostico = listOf(
                p("¿Qué ocurre cuando dos personas modifican la misma línea de un archivo?", listOf("Un conflicto que se debe resolver", "Nada", "Se borra el repositorio", "Se crea un actor"), 0),
                p("¿Qué herramienta es un sistema de control de versiones?", listOf("Git", "UML", "Gantt", "SQL"), 0)
            ),
            flash = listOf(
                p("Unir los cambios de una rama con la principal se llama:", listOf("Fusión (merge)", "Commit", "Despliegue", "Elicitación"), 0),
                p("Selecciona artefactos que deben estar bajo control de versiones:", listOf("Código fuente", "Documentos de requisitos", "Scripts de base de datos", "El café de la oficina"), 0, 1, 2),
                p("Una línea base es:", listOf("Una versión aprobada que sirve de referencia", "Un error", "Una tarea del Gantt", "Un actor"), 0)
            ),
            enigma = "«Recuerdo cada cambio, quién lo hizo y cuándo; si te pierdes, te regreso a la versión anterior». ¿Estás en mi repositorio?"
        ),
        TemaNuevo(
            id = "tema_cv_9", mundoId = "mundo_ciclo_vida", orden = 9,
            nombre = "Aseguramiento de la Calidad",
            descripcion = "Revisiones, estándares y métricas a lo largo de todo el ciclo.",
            tituloLeccion = "La Calidad se Construye",
            introduccion = "El aseguramiento de la calidad (QA) se enfoca en el proceso para prevenir defectos durante todo el ciclo de vida, no solo en probar al final. Define estándares, organiza revisiones y audita que el equipo los siga.\n\n" +
                "Las revisiones e inspecciones permiten que otros examinen un artefacto y encuentren defectos cuando aún es barato corregirlos. Las métricas, como los defectos por cada mil líneas o la cobertura de pruebas, ayudan a medir la calidad.",
            ejercicios = listOf(
                p("El aseguramiento de la calidad (QA) se enfoca en:", listOf("El proceso, para prevenir defectos durante todo el ciclo", "Solo probar al final", "Vender el producto", "Diseñar el logotipo"), 0),
                p("Una revisión entre pares consiste en:", listOf("Que otros miembros del equipo examinen un artefacto para encontrar defectos", "Revisar el salario", "Probar el hardware", "Comprar licencias"), 0),
                p("Selecciona actividades de aseguramiento de la calidad:", listOf("Inspecciones de código", "Definir estándares", "Auditorías del proceso", "Eliminar las pruebas"), 0, 1, 2),
                p("Una métrica de calidad es:", listOf("Una medida, como la cantidad de defectos por cada mil líneas", "Un tipo de actor", "Un diagrama de paquetes", "Un requisito funcional"), 0),
                p("Verdadero o Falso: Es más barato encontrar un defecto en una revisión del diseño que en producción.", VF, 0)
            ),
            conceptos = listOf(
                "Calidad" to "Grado en que el software cumple los requisitos y expectativas.",
                "Revisión" to "Examen de un artefacto por otras personas para hallar defectos.",
                "Inspección" to "Revisión formal y planificada de un artefacto.",
                "Estándar" to "Regla o norma que el equipo acuerda seguir.",
                "Métrica" to "Medida cuantitativa de un atributo del software.",
                "Auditoría" to "Verificación independiente de que se sigue el proceso.",
                "Prevención" to "Enfoque de QA: evitar defectos antes de que aparezcan."
            ),
            diagnostico = listOf(
                p("¿En qué se diferencia el aseguramiento de la calidad de las pruebas?", listOf("QA previene en el proceso; las pruebas detectan defectos en el producto", "Son lo mismo", "QA solo se hace al final", "Las pruebas no encuentran defectos"), 0),
                p("ISO/IEC 25010 es un estándar que define:", listOf("Características de calidad del producto de software", "Un lenguaje de programación", "Un diagrama UML", "Un modelo de costos de hardware"), 0)
            ),
            flash = listOf(
                p("Una inspección de código es:", listOf("Una revisión formal del código por el equipo", "Una compilación", "Un despliegue", "Una entrevista"), 0),
                p("Selecciona ejemplos de métricas de software:", listOf("Defectos por cada mil líneas", "Tiempo de respuesta", "Cobertura de pruebas", "Color del tema"), 0, 1, 2),
                p("El aseguramiento de la calidad se aplica:", listOf("Durante todo el ciclo de vida", "Solo al final", "Solo en el análisis", "Nunca"), 0)
            ),
            enigma = "«No espero al final para buscar fallas: reviso, mido y pongo normas desde el principio». ¿Cumples mis estándares?"
        ),
        TemaNuevo(
            id = "tema_cv_10", mundoId = "mundo_ciclo_vida", orden = 10,
            nombre = "Retiro del Sistema",
            descripcion = "Cierre ordenado del software al final de su vida útil.",
            tituloLeccion = "El Último Ocaso",
            introduccion = "Todo sistema llega al final de su vida útil: cuando su tecnología queda obsoleta, mantenerlo cuesta demasiado o aparece un reemplazo mejor. El retiro es la última fase del ciclo de vida y debe hacerse de forma ordenada.\n\n" +
                "Antes de darlo de baja se avisa a los usuarios, se respaldan o migran los datos (muchas veces por obligación legal) y se planea la transición al sistema que lo sustituye. Un sistema antiguo que sigue en uso se conoce como heredado o legacy.",
            ejercicios = listOf(
                p("El retiro del sistema ocurre cuando:", listOf("El software deja de ser útil o rentable y se da de baja", "Se escribe el primer requisito", "Se compila por primera vez", "Se contrata al equipo"), 0),
                p("Selecciona actividades del retiro:", listOf("Respaldar o migrar los datos", "Avisar a los usuarios", "Desinstalar el software", "Agregar nuevas funciones"), 0, 1, 2),
                p("Un sistema heredado (legacy) es:", listOf("Un sistema antiguo que sigue en uso aunque su tecnología es obsoleta", "Un sistema recién creado", "Un prototipo desechable", "Un diagrama"), 0),
                p("¿Por qué es importante conservar los datos al retirar un sistema?", listOf("Por obligaciones legales y para no perder información del negocio", "Porque ocupan poco", "Para venderlos sin permiso", "No es importante"), 0),
                p("Verdadero o Falso: El retiro es la última fase del ciclo de vida del software.", VF, 0)
            ),
            conceptos = listOf(
                "Retiro" to "Fase final en la que el sistema deja de operar.",
                "Heredado" to "Sistema antiguo que sigue en uso con tecnología obsoleta.",
                "Obsoleto" to "Que ya no se usa o quedó superado por otra tecnología.",
                "Respaldo" to "Copia de seguridad de los datos antes de dar de baja.",
                "Archivo" to "Guardar datos históricos que ya no se usan a diario.",
                "Baja" to "Acción de dejar fuera de servicio un sistema.",
                "Reemplazo" to "Sistema nuevo que toma el lugar del que se retira."
            ),
            diagnostico = listOf(
                p("¿Qué se debe hacer con los datos de un sistema antes de retirarlo?", listOf("Respaldarlos o migrarlos", "Borrarlos sin aviso", "Imprimirlos todos", "Nada"), 0),
                p("Un sistema legacy es:", listOf("Un sistema antiguo aún en operación", "Un sistema nuevo", "Un lenguaje de modelado", "Una metodología ágil"), 0)
            ),
            flash = listOf(
                p("¿Cuál es la última fase del ciclo de vida del software?", listOf("El retiro", "El análisis", "El diseño", "La implementación"), 0),
                p("Selecciona motivos para retirar un sistema:", listOf("Tecnología obsoleta", "Costo de mantenimiento muy alto", "Un reemplazo mejor", "Que funciona perfecto y es barato"), 0, 1, 2),
                p("Antes de dar de baja un sistema conviene:", listOf("Avisar a los usuarios y planear la transición", "Apagarlo sin avisar", "Borrar los respaldos", "Agregar funciones"), 0)
            ),
            enigma = "«Llego al final del camino: guardo la memoria y apago la luz». ¿Eres de mi último ocaso?"
        ),

        // =================================================================
        // Mundo 4 · Modelos Tradicionales
        // =================================================================
        TemaNuevo(
            id = "tema_mt_5", mundoId = "mundo_modelos_trad", orden = 5,
            nombre = "Modelo de Prototipos",
            descripcion = "Construir versiones preliminares para aclarar los requisitos.",
            tituloLeccion = "Ver para Entender",
            introduccion = "Cuando el cliente no sabe bien lo que quiere, el modelo de prototipos construye rápido una versión preliminar del sistema para que la vea, la use y opine. Con sus comentarios se refina el prototipo hasta aclarar los requisitos.\n\n" +
                "Un prototipo desechable se tira al terminar y se construye el sistema real; uno evolutivo se sigue mejorando. El riesgo principal es que el cliente crea que el prototipo ya es el producto terminado.",
            ejercicios = listOf(
                p("El modelo de prototipos es útil cuando:", listOf("Los requisitos no están claros", "Los requisitos son totalmente estables", "No hay cliente", "No hay pantallas"), 0),
                p("Un prototipo desechable:", listOf("Se usa para aprender y luego se descarta", "Se convierte en el producto final", "Nunca se muestra al cliente", "Es el manual"), 0),
                p("Selecciona pasos del modelo de prototipos:", listOf("Recolectar requisitos iniciales", "Construir el prototipo", "Evaluarlo con el cliente", "Entregarlo sin revisar"), 0, 1, 2),
                p("Un riesgo del modelo de prototipos es:", listOf("Que el cliente crea que el prototipo ya es el sistema terminado", "Que sea muy rígido", "Que no haya retroalimentación", "Que no use requisitos"), 0),
                p("Verdadero o Falso: Un prototipo permite que el cliente vea y opine sobre el sistema temprano.", VF, 0)
            ),
            conceptos = listOf(
                "Desechable" to "Prototipo que se tira después de aprender de él.",
                "Maqueta" to "Representación simple de la interfaz para validar ideas.",
                "Evaluación" to "Revisión del prototipo con el cliente para obtener opiniones.",
                "Opinión" to "Lo que el cliente aporta al ver el prototipo.",
                "Refinar" to "Mejorar el prototipo con base en los comentarios.",
                "Boceto" to "Dibujo rápido de una pantalla.",
                "Expectativa" to "Riesgo: el cliente cree que el prototipo ya es el producto."
            ),
            diagnostico = listOf(
                p("¿Cuándo conviene usar el modelo de prototipos?", listOf("Cuando el cliente no sabe bien lo que quiere", "Cuando todo está definido", "Cuando no hay usuarios", "Cuando no hay tiempo para hablar con el cliente"), 0),
                p("¿Qué se hace con un prototipo desechable al terminar?", listOf("Se descarta y se construye el sistema real", "Se entrega como versión final", "Se vende", "Se usa como código de producción"), 0)
            ),
            flash = listOf(
                p("El objetivo principal de un prototipo es:", listOf("Aclarar requisitos con el cliente", "Reemplazar las pruebas", "Eliminar el diseño", "Reducir el número de usuarios"), 0),
                p("Selecciona tipos de prototipo:", listOf("Desechable", "Evolutivo", "De papel o boceto", "De producción final certificado"), 0, 1, 2),
                p("Un riesgo del prototipado es:", listOf("Que el cliente lo confunda con el producto terminado", "Que no muestre nada", "Que sea secuencial", "Que prohíba cambios"), 0)
            ),
            enigma = "«Te muestro algo que parece real para que me digas qué quieres de verdad». ¿Me confundiste con el final?"
        ),
        TemaNuevo(
            id = "tema_mt_6", mundoId = "mundo_modelos_trad", orden = 6,
            nombre = "Modelo Evolutivo",
            descripcion = "El sistema crece en versiones a partir de una inicial que se refina.",
            tituloLeccion = "Crecer por Versiones",
            introduccion = "En el modelo evolutivo se construye pronto una versión inicial y se mejora en ciclos sucesivos con la retroalimentación del cliente, hasta convertirse en el sistema final. Es adecuado cuando los requisitos se descubren durante el desarrollo.\n\n" +
                "A diferencia del prototipo desechable, aquí lo construido se conserva y crece. Su riesgo es que, con tantos cambios, la estructura del sistema se degrade si no se cuida la arquitectura.",
            ejercicios = listOf(
                p("En el modelo evolutivo el sistema:", listOf("Se construye con una versión inicial que se refina con cada ciclo", "Se entrega completo de una sola vez", "No tiene versiones", "Nunca cambia"), 0),
                p("A diferencia del prototipo desechable, el prototipo evolutivo:", listOf("Se mejora hasta convertirse en el sistema final", "Se tira", "No se muestra", "Solo es de papel"), 0),
                p("Selecciona ventajas del modelo evolutivo:", listOf("Se adapta a requisitos cambiantes", "El cliente ve avances pronto", "Recibe retroalimentación continua", "La documentación está completa desde el inicio"), 0, 1, 2),
                p("Una desventaja del modelo evolutivo es:", listOf("La estructura del sistema puede degradarse si no se cuida", "Es demasiado rígido", "No permite cambios", "No hay entregas"), 0),
                p("Verdadero o Falso: El modelo evolutivo es apropiado cuando los requisitos se van descubriendo durante el desarrollo.", VF, 0)
            ),
            conceptos = listOf(
                "Evolutivo" to "Modelo en que el sistema crece a partir de una versión inicial.",
                "Refinamiento" to "Mejora sucesiva de cada versión del sistema.",
                "Degradación" to "Desgaste de la estructura por cambios sin control.",
                "Exploratorio" to "Desarrollo evolutivo que trabaja con el cliente para descubrir requisitos.",
                "Adaptable" to "Cualidad del modelo ante requisitos cambiantes.",
                "Crecimiento" to "Lo que tiene el sistema en cada ciclo evolutivo.",
                "Semilla" to "Primera versión de la que parte el sistema."
            ),
            diagnostico = listOf(
                p("¿Qué pasa con el prototipo en el modelo evolutivo?", listOf("Se refina hasta ser el producto final", "Se desecha siempre", "Nunca se construye", "Se entrega a otro cliente"), 0),
                p("El modelo evolutivo es adecuado cuando los requisitos:", listOf("Cambian o se descubren poco a poco", "Son totalmente fijos", "No existen", "Los define solo el programador"), 0)
            ),
            flash = listOf(
                p("Una desventaja del modelo evolutivo es:", listOf("Puede degradar la estructura del sistema", "Es muy rígido", "No permite ver avances", "Prohíbe al cliente opinar"), 0),
                p("Selecciona características del modelo evolutivo:", listOf("Versiones sucesivas", "Retroalimentación del cliente", "Requisitos que se aclaran con el tiempo", "Una sola entrega al final"), 0, 1, 2),
                p("El desarrollo exploratorio trabaja:", listOf("Con el cliente para descubrir los requisitos", "Sin el cliente", "Solo con documentación", "Solo en el retiro"), 0)
            ),
            enigma = "«Nazco pequeño y cambio de piel en cada ciclo hasta ser completo». ¿Creciste conmigo?"
        ),
        TemaNuevo(
            id = "tema_mt_7", mundoId = "mundo_modelos_trad", orden = 7,
            nombre = "Desarrollo Rápido (RAD)",
            descripcion = "Entregas en poco tiempo con prototipos, reutilización y usuarios cerca.",
            tituloLeccion = "Contra el Reloj",
            introduccion = "El Desarrollo Rápido de Aplicaciones (RAD), popularizado por James Martin, busca entregar sistemas funcionales en periodos muy cortos. Se apoya en prototipos rápidos, componentes reutilizables, herramientas que aceleran la construcción y talleres intensivos con los usuarios.\n\n" +
                "El trabajo se encierra en timeboxes: periodos fijos para entregar una funcionalidad. RAD no conviene si el sistema no se puede dividir en módulos, si hay alto riesgo técnico o si los usuarios no pueden participar.",
            ejercicios = listOf(
                p("El modelo RAD busca:", listOf("Desarrollar sistemas en periodos muy cortos", "Hacer proyectos de diez años", "Eliminar al usuario", "Evitar los prototipos"), 0),
                p("Un timebox en RAD es:", listOf("Un periodo fijo de tiempo para entregar una funcionalidad", "Una caja de herramientas", "Un tipo de servidor", "Un diagrama"), 0),
                p("Selecciona prácticas de RAD:", listOf("Prototipado rápido", "Reutilización de componentes", "Participación activa del usuario", "Fases largas sin retroalimentación"), 0, 1, 2),
                p("RAD es poco recomendable cuando:", listOf("El sistema no se puede dividir en módulos o tiene alto riesgo técnico", "Hay usuarios disponibles", "Se pueden reutilizar componentes", "El tiempo es corto"), 0),
                p("¿Quién popularizó el modelo RAD?", listOf("James Martin", "Barry Boehm", "Winston Royce", "Alan Turing"), 0)
            ),
            conceptos = listOf(
                "Rápido" to "Cómo es el desarrollo en el modelo RAD.",
                "Timebox" to "Periodo fijo de tiempo para entregar algo.",
                "Martin" to "Apellido de quien popularizó el modelo RAD.",
                "Reutilizar" to "Aprovechar componentes ya construidos.",
                "Taller" to "Sesión intensiva con usuarios para definir requisitos.",
                "Herramientas" to "Generadores y entornos que aceleran la construcción.",
                "Plazo" to "Tiempo límite muy corto que caracteriza al RAD."
            ),
            diagnostico = listOf(
                p("La principal característica de RAD es:", listOf("Entregas en tiempos muy cortos", "Fases estrictamente secuenciales", "Análisis de riesgos en espiral", "Prohibir prototipos"), 0),
                p("RAD depende mucho de:", listOf("La participación constante del usuario", "Que no haya usuario", "Documentación exhaustiva antes de iniciar", "Un solo programador"), 0)
            ),
            flash = listOf(
                p("Un timebox es:", listOf("Un periodo fijo para entregar una parte", "Una caja de componentes", "Un servidor", "Una prueba"), 0),
                p("Selecciona en qué se apoya RAD:", listOf("Prototipos", "Componentes reutilizables", "Herramientas de desarrollo rápido", "Fases de un año"), 0, 1, 2),
                p("RAD no conviene cuando:", listOf("Hay alto riesgo técnico o no se puede modularizar", "El tiempo es corto", "Hay usuarios dispuestos", "Se reutilizan componentes"), 0)
            ),
            enigma = "«Encierro el trabajo en cajas de tiempo y corro con el usuario a mi lado». ¿Alcanzaste mi plazo?"
        ),
        TemaNuevo(
            id = "tema_mt_8", mundoId = "mundo_modelos_trad", orden = 8,
            nombre = "Proceso Unificado (RUP)",
            descripcion = "Proceso iterativo guiado por casos de uso y centrado en la arquitectura.",
            tituloLeccion = "Cuatro Fases, Muchas Iteraciones",
            introduccion = "El Proceso Unificado, y su versión comercial RUP de Rational, organiza el proyecto en cuatro fases: Inicio (visión y alcance), Elaboración (arquitectura base), Construcción (desarrollo de la mayor parte) y Transición (entrega a los usuarios).\n\n" +
                "Dentro de cada fase se trabaja en iteraciones que recorren disciplinas como requisitos, diseño, implementación y pruebas. Se describe como iterativo e incremental, dirigido por casos de uso y centrado en la arquitectura.",
            ejercicios = listOf(
                p("Las cuatro fases del Proceso Unificado son:", listOf("Inicio, Elaboración, Construcción y Transición", "Análisis, Diseño, Código y Prueba", "Planear, Hacer, Verificar y Actuar", "Requisitos, Riesgo, Prototipo y Entrega"), 0),
                p("El Proceso Unificado está dirigido por:", listOf("Casos de uso", "Diagramas de Gantt", "Contratos", "Pruebas de hardware"), 0),
                p("Selecciona características del Proceso Unificado:", listOf("Iterativo e incremental", "Centrado en la arquitectura", "Dirigido por casos de uso", "Sin documentación"), 0, 1, 2),
                p("En la fase de Elaboración se busca principalmente:", listOf("Definir y validar la arquitectura base", "Entregar el sistema al usuario", "Retirar el sistema", "Elegir el nombre del producto"), 0),
                p("En la fase de Transición:", listOf("El sistema se entrega a los usuarios", "Se escribe la visión inicial", "Se define la arquitectura", "Se estudia la viabilidad"), 0)
            ),
            conceptos = listOf(
                "Inicio" to "Fase del RUP que define la visión y el alcance.",
                "Elaboración" to "Fase del RUP que establece la arquitectura base.",
                "Construcción" to "Fase del RUP donde se desarrolla la mayor parte del sistema.",
                "Transición" to "Fase del RUP que entrega el sistema a los usuarios.",
                "Disciplina" to "Área de trabajo del RUP, como requisitos o pruebas.",
                "Rational" to "Empresa que creó el RUP.",
                "Iterativo" to "Cómo avanza el RUP: en ciclos repetidos dentro de cada fase."
            ),
            diagnostico = listOf(
                p("¿Qué fase del Proceso Unificado entrega el producto a los usuarios?", listOf("Transición", "Inicio", "Elaboración", "Construcción"), 0),
                p("El Proceso Unificado se describe como:", listOf("Iterativo, centrado en la arquitectura y dirigido por casos de uso", "Secuencial y sin iteraciones", "Solo para hardware", "Sin fases"), 0)
            ),
            flash = listOf(
                p("¿Qué se establece principalmente en la fase de Elaboración?", listOf("La arquitectura base", "El manual final", "El retiro", "El precio de venta"), 0),
                p("Selecciona fases del Proceso Unificado:", listOf("Inicio", "Elaboración", "Transición", "Ocaso"), 0, 1, 2),
                p("RUP significa:", listOf("Rational Unified Process", "Rapid Unit Programming", "Real Use Plan", "Risk Under Prototype"), 0)
            ),
            enigma = "«Tengo cuatro fases, me guían los casos de uso y en cada fase doy varias vueltas». ¿Me unificaste?"
        ),
        TemaNuevo(
            id = "tema_mt_9", mundoId = "mundo_modelos_trad", orden = 9,
            nombre = "Modelo Iterativo",
            descripcion = "Repetir ciclos de desarrollo para mejorar el sistema paso a paso.",
            tituloLeccion = "Vuelta tras Vuelta",
            introduccion = "En el modelo iterativo el desarrollo se repite en ciclos cortos de análisis, diseño, implementación y pruebas. Al final de cada iteración hay una versión que se puede evaluar, y lo aprendido se usa para mejorar la siguiente.\n\n" +
                "Mientras el modelo incremental agrega partes nuevas, el iterativo refina lo que ya existe; en la práctica suelen combinarse. Los métodos ágiles como Scrum trabajan así, con iteraciones llamadas sprints.",
            ejercicios = listOf(
                p("En el modelo iterativo:", listOf("Se repiten ciclos de análisis, diseño, código y prueba", "Se hace cada fase una sola vez", "No se prueba", "No hay retroalimentación"), 0),
                p("La diferencia entre iterativo e incremental es que el iterativo:", listOf("Mejora y refina lo existente en cada ciclo", "Solo agrega partes nuevas sin mejorar", "No tiene ciclos", "Es igual a la cascada"), 0),
                p("Selecciona ventajas del modelo iterativo:", listOf("Detecta problemas temprano", "Incorpora cambios entre iteraciones", "Reduce riesgos poco a poco", "Elimina la necesidad de planear"), 0, 1, 2),
                p("Al final de cada iteración normalmente se obtiene:", listOf("Una versión que se puede evaluar", "Nada que mostrar", "Solo documentos", "El retiro del sistema"), 0),
                p("Verdadero o Falso: Los métodos ágiles como Scrum se basan en un enfoque iterativo.", VF, 0)
            ),
            conceptos = listOf(
                "Iteración" to "Cada ciclo repetido de desarrollo.",
                "Repetir" to "Lo que se hace con el ciclo de desarrollo en este modelo.",
                "Mejora" to "Lo que se busca en cada vuelta sobre lo ya construido.",
                "Evaluable" to "Cómo es la versión al final de cada iteración.",
                "Scrum" to "Marco ágil que trabaja en iteraciones llamadas sprints.",
                "Sprint" to "Iteración corta de Scrum, de una a cuatro semanas.",
                "Temprano" to "Cuándo se detectan los problemas en el modelo iterativo."
            ),
            diagnostico = listOf(
                p("¿Qué se obtiene al final de cada iteración?", listOf("Una versión que se puede probar y evaluar", "Solo un acta", "El retiro", "Nada"), 0),
                p("¿Qué marco ágil trabaja en sprints?", listOf("Scrum", "Cascada", "Modelo en V", "RAD"), 0)
            ),
            flash = listOf(
                p("El modelo iterativo se caracteriza por:", listOf("Repetir ciclos que mejoran el sistema", "Una sola pasada por cada fase", "No tener pruebas", "No tener cliente"), 0),
                p("Selecciona lo que ocurre en cada iteración:", listOf("Análisis", "Diseño", "Pruebas", "Retiro del sistema"), 0, 1, 2),
                p("Iterativo e incremental se diferencian en que el iterativo:", listOf("Refina lo existente; el incremental agrega partes", "Son idénticos", "No tiene ciclos", "No entrega nada"), 0)
            ),
            enigma = "«Vuelvo sobre mis pasos para pulir lo que hice; cada vuelta deja algo mejor». ¿Cuántas vueltas me diste?"
        ),
        TemaNuevo(
            id = "tema_mt_10", mundoId = "mundo_modelos_trad", orden = 10,
            nombre = "Desarrollo Basado en Componentes",
            descripcion = "Construir sistemas ensamblando componentes reutilizables.",
            tituloLeccion = "Armar con Piezas",
            introduccion = "El desarrollo basado en componentes construye el sistema ensamblando piezas que ya existen: bibliotecas de código abierto, componentes comerciales (COTS) o módulos de proyectos anteriores. Así se reduce el tiempo y se aprovecha software ya probado.\n\n" +
                "Para ensamblar componentes es clave que tengan interfaces bien definidas. Su desventaja es que a veces hay que ajustar los requisitos a lo que ofrecen los componentes disponibles, y se depende de sus proveedores.",
            ejercicios = listOf(
                p("El desarrollo basado en componentes consiste en:", listOf("Construir el sistema ensamblando componentes ya existentes", "Escribir todo desde cero", "No usar interfaces", "Evitar la reutilización"), 0),
                p("Un componente COTS es:", listOf("Un componente comercial que se compra listo para usar", "Un tipo de prueba", "Un diagrama", "Un requisito"), 0),
                p("Selecciona ventajas del desarrollo basado en componentes:", listOf("Reduce el tiempo de desarrollo", "Aprovecha componentes probados", "Disminuye costos", "Garantiza que nunca haya que ajustar requisitos"), 0, 1, 2),
                p("Una desventaja de este enfoque es:", listOf("Puede ser necesario ajustar los requisitos a lo que ofrecen los componentes", "Siempre es más lento", "No permite reutilizar", "No usa interfaces"), 0),
                p("Para poder ensamblar componentes es clave que tengan:", listOf("Interfaces bien definidas", "Colores iguales", "El mismo programador", "Ninguna documentación"), 0)
            ),
            conceptos = listOf(
                "Ensamblar" to "Unir componentes para formar el sistema.",
                "Reutilizable" to "Componente que se puede usar en varios sistemas.",
                "Cots" to "Componente comercial que se compra listo para usar.",
                "Biblioteca" to "Colección de componentes o funciones listas para usarse.",
                "Adaptación" to "Ajuste de un componente o requisito para que encajen.",
                "Proveedor" to "Empresa que desarrolla y vende componentes.",
                "Catálogo" to "Lista de componentes disponibles para elegir."
            ),
            diagnostico = listOf(
                p("¿Qué significa COTS?", listOf("Commercial Off-The-Shelf: componente comercial listo para usar", "Code Of Test Suite", "Central Object Tree System", "Customer Order Tracking"), 0),
                p("¿Qué se necesita para ensamblar componentes?", listOf("Interfaces bien definidas", "Que todos usen el mismo color", "Un solo programador", "Eliminar las pruebas"), 0)
            ),
            flash = listOf(
                p("Una desventaja del desarrollo basado en componentes es:", listOf("Hay que adaptar requisitos a los componentes disponibles", "Es siempre más caro", "No reutiliza nada", "No tiene interfaces"), 0),
                p("Selecciona fuentes de componentes:", listOf("Bibliotecas de código abierto", "Proveedores comerciales", "Proyectos anteriores de la empresa", "El diagrama de Gantt"), 0, 1, 2),
                p("El desarrollo basado en componentes prioriza:", listOf("La reutilización", "Escribir todo desde cero", "Eliminar interfaces", "Una sola fase"), 0)
            ),
            enigma = "«No forjo desde cero: armo mi espada con piezas que otros ya templaron». ¿Encajas en mi ensamble?"
        )
    )
}
