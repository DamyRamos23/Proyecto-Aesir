package com.aesir.odin.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aesir.odin.data.local.converter.Converters
import com.aesir.odin.data.local.entity.EjercicioEntity
import com.aesir.odin.data.local.entity.LeccionEntity
import com.aesir.odin.data.local.entity.MundoEntity
import com.aesir.odin.data.local.entity.TemaEntity

/**
 * Contenido inicial de la base de datos con 4 mundos sobre Ingeniería de Software.
 */
object DatosIniciales {

    val mundos = listOf(
        MundoEntity("mundo_uml", "UML", "Lenguaje de modelado visual para especificar y documentar sistemas de software.", 1, desbloqueado = true),
        MundoEntity("mundo_casos_uso", "Casos de Uso", "Técnica para capturar requisitos funcionales mediante la interacción actor-sistema.", 2, desbloqueado = false),
        MundoEntity("mundo_ciclo_vida", "Ciclo de Vida", "Fases y actividades que atraviesa un sistema desde su concepción hasta su retiro.", 3, desbloqueado = false),
        MundoEntity("mundo_modelos_trad", "Modelos Tradicionales", "Enfoques clásicos y estructurados para organizar el proceso de desarrollo de software.", 4, desbloqueado = false)
    )

    val temas = listOf(
        // Mundo 1 - UML
        TemaEntity("tema_uml_1", "mundo_uml", "Diagramas de Clases", "Estructura estática del sistema: clases, atributos y métodos.", 1, desbloqueado = true, introduccionVista = false, completado = false),
        TemaEntity("tema_uml_2", "mundo_uml", "Diagramas de Secuencia", "Interacción dinámica: intercambio de mensajes en el tiempo.", 2, desbloqueado = false, introduccionVista = false, completado = false),
        TemaEntity("tema_uml_3", "mundo_uml", "Diagramas de Casos de Uso", "Visión general de las funcionalidades desde la perspectiva del usuario.", 3, desbloqueado = false, introduccionVista = false, completado = false),
        TemaEntity("tema_uml_4", "mundo_uml", "Diagramas de Actividades", "Flujo de control y procesos de negocio paso a paso.", 4, desbloqueado = false, introduccionVista = false, completado = false),

        // Mundo 2 - Casos de Uso
        TemaEntity("tema_cu_1", "mundo_casos_uso", "Elementos de un Caso de Uso", "Actores, sistema, y el caso de uso en sí.", 1, desbloqueado = true, introduccionVista = false, completado = false),
        TemaEntity("tema_cu_2", "mundo_casos_uso", "Relaciones (Include, Extend)", "Cómo reutilizar y extender casos de uso.", 2, desbloqueado = false, introduccionVista = false, completado = false),
        TemaEntity("tema_cu_3", "mundo_casos_uso", "Especificación de Casos de Uso", "Documentación textual detallada: precondiciones, flujo principal y alterno.", 3, desbloqueado = false, introduccionVista = false, completado = false),
        TemaEntity("tema_cu_4", "mundo_casos_uso", "Errores Comunes en Casos de Uso", "Evitar el diseño de interfaz o detalles técnicos en los casos de uso.", 4, desbloqueado = false, introduccionVista = false, completado = false),

        // Mundo 3 - Ciclo de Vida
        TemaEntity("tema_cv_1", "mundo_ciclo_vida", "Análisis de Requisitos", "Comprender y documentar qué debe hacer el sistema.", 1, desbloqueado = true, introduccionVista = false, completado = false),
        TemaEntity("tema_cv_2", "mundo_ciclo_vida", "Diseño del Sistema", "Definir la arquitectura y cómo funcionará el sistema.", 2, desbloqueado = false, introduccionVista = false, completado = false),
        TemaEntity("tema_cv_3", "mundo_ciclo_vida", "Implementación y Codificación", "Traducir el diseño a código fuente ejecutable.", 3, desbloqueado = false, introduccionVista = false, completado = false),
        TemaEntity("tema_cv_4", "mundo_ciclo_vida", "Pruebas y Mantenimiento", "Verificar la calidad y evolucionar el sistema.", 4, desbloqueado = false, introduccionVista = false, completado = false),

        // Mundo 4 - Modelos Tradicionales
        TemaEntity("tema_mt_1", "mundo_modelos_trad", "Modelo en Cascada", "Enfoque puramente secuencial y estricto.", 1, desbloqueado = true, introduccionVista = false, completado = false),
        TemaEntity("tema_mt_2", "mundo_modelos_trad", "Modelo en Espiral", "Enfoque iterativo centrado en la evaluación de riesgos.", 2, desbloqueado = false, introduccionVista = false, completado = false),
        TemaEntity("tema_mt_3", "mundo_modelos_trad", "Modelo en V", "Extensión de la cascada que enfatiza la planificación de pruebas.", 3, desbloqueado = false, introduccionVista = false, completado = false),
        TemaEntity("tema_mt_4", "mundo_modelos_trad", "Modelo Incremental", "Construcción por partes o incrementos funcionales.", 4, desbloqueado = false, introduccionVista = false, completado = false)
    )

    val lecciones = listOf(
        // Mundo 1 - UML
        LeccionEntity("leccion_uml_1", "tema_uml_1", "Fundamentos de Diagramas de Clases", 
            "Los Diagramas de Clases son el pilar del modelado orientado a objetos en UML. Representan la estructura estática del sistema mostrando sus clases, atributos, operaciones (métodos) y las relaciones entre los objetos.\n\nSon esenciales para traducir los requisitos al código fuente real, ya que cada clase en el diagrama suele corresponder a una clase en el código.", 1, completada = false),
        LeccionEntity("leccion_uml_2", "tema_uml_2", "Interacciones en el Tiempo", 
            "Los Diagramas de Secuencia muestran cómo los objetos interactúan entre sí y el orden en que lo hacen. El tiempo fluye de arriba hacia abajo.\n\nSon muy útiles para detallar el flujo lógico de un Caso de Uso específico, mostrando las llamadas a métodos y retornos de datos.", 1, completada = false),
        LeccionEntity("leccion_uml_3", "tema_uml_3", "Modelado Funcional", 
            "El Diagrama de Casos de Uso proporciona una visión de alto nivel de lo que hace el sistema desde la perspectiva del usuario externo (actor). No muestra cómo funciona por dentro.\n\nEs la herramienta principal para la comunicación entre los desarrolladores y los clientes (stakeholders).", 1, completada = false),
        LeccionEntity("leccion_uml_4", "tema_uml_4", "Flujos de Trabajo", 
            "Los Diagramas de Actividades son la versión orientada a objetos de los tradicionales diagramas de flujo. Muestran el flujo de control de una actividad a otra.\n\nSon excelentes para modelar lógica de negocio compleja, algoritmos o flujos de trabajo paralelos (usando barras de sincronización).", 1, completada = false),

        // Mundo 2 - Casos de Uso
        LeccionEntity("leccion_cu_1", "tema_cu_1", "Actores y el Sistema", 
            "Un Caso de Uso describe una tarea específica que un actor realiza usando el sistema para alcanzar un objetivo. Un actor es cualquier entidad externa (humano, otro sistema o hardware) que interactúa con el sistema.\n\nEl límite del sistema se representa con un rectángulo, separando lo que está dentro de nuestra responsabilidad de lo que está fuera.", 1, completada = false),
        LeccionEntity("leccion_cu_2", "tema_cu_2", "Include y Extend", 
            "La relación 'include' (incluir) se usa cuando un caso de uso siempre requiere de otro para completarse (ej. Retirar Dinero incluye Validar PIN). Es obligatorio.\n\nLa relación 'extend' (extender) indica un comportamiento opcional o condicional que se agrega a un caso de uso base bajo ciertas circunstancias.", 1, completada = false),
        LeccionEntity("leccion_cu_3", "tema_cu_3", "Documentando Casos de Uso", 
            "El diagrama por sí solo no es suficiente. Cada caso de uso debe estar respaldado por una especificación textual que incluya: Precondiciones (qué debe ser cierto antes de iniciar), Flujo Principal (el camino feliz) y Flujos Alternativos (errores o desvíos).\n\nEsto elimina la ambigüedad que un simple óvalo dibujado podría tener.", 1, completada = false),
        LeccionEntity("leccion_cu_4", "tema_cu_4", "Antipatrones en Casos de Uso", 
            "Un error muy común es usar casos de uso para describir el diseño de la interfaz de usuario (ej. 'El actor hace clic en el botón azul'). Los casos de uso deben ser independientes de la tecnología y centrarse en la intención ('El actor confirma la compra').\n\nOtro error es hacer casos de uso demasiado pequeños ('CRUD de usuarios'), que deben agruparse en algo más significativo ('Gestionar Usuarios').", 1, completada = false),

        // Mundo 3 - Ciclo de Vida
        LeccionEntity("leccion_cv_1", "tema_cv_1", "Entendiendo el Problema", 
            "El Análisis de Requisitos es la fase donde se descubre, negocia y documenta qué debe hacer el software (requerimientos funcionales) y con qué restricciones (requerimientos no funcionales).\n\nUn error en esta fase es el más costoso de reparar, ya que propaga defectos a todas las fases posteriores.", 1, completada = false),
        LeccionEntity("leccion_cv_2", "tema_cv_2", "Ideando la Solución", 
            "El Diseño del Sistema toma los requisitos y define la arquitectura, las bases de datos, los componentes y las interfaces necesarias para cumplirlos.\n\nEs el puente entre el 'qué' queremos lograr y el 'cómo' lo vamos a construir en código.", 1, completada = false),
        LeccionEntity("leccion_cv_3", "tema_cv_3", "Construcción del Software", 
            "La fase de Implementación (o Codificación) consiste en traducir los modelos de diseño a un lenguaje de programación ejecutable.\n\nAdemás de escribir código, incluye la integración de componentes y la realización de pruebas unitarias por parte de los propios desarrolladores.", 1, completada = false),
        LeccionEntity("leccion_cv_4", "tema_cv_4", "Garantizando la Calidad", 
            "Las pruebas (Testing) verifican que el software cumple con los requisitos y no tiene defectos críticos. El mantenimiento ocurre después de la entrega, para corregir fallos, adaptar el software a nuevos entornos o agregar mejoras.\n\nEl mantenimiento suele consumir la mayor parte del presupuesto a lo largo de la vida del software.", 1, completada = false),

        // Mundo 4 - Modelos Tradicionales
        LeccionEntity("leccion_mt_1", "tema_mt_1", "Desarrollo en Cascada", 
            "El Modelo en Cascada (Waterfall) es el enfoque original de la ingeniería de software. Las fases (Requisitos, Diseño, Implementación, Pruebas, Despliegue) ocurren estrictamente una tras otra, como una cascada.\n\nEs ideal para proyectos con requisitos muy claros y estables desde el día 1, pero su principal debilidad es la nula flexibilidad ante cambios.", 1, completada = false),
        LeccionEntity("leccion_mt_2", "tema_mt_2", "Mitigando Riesgos", 
            "El Modelo en Espiral, propuesto por Barry Boehm, divide el proyecto en ciclos repetitivos (bucles). Su característica distintiva es que en cada ciclo se realiza un exhaustivo análisis de riesgos.\n\nSe construyen prototipos continuamente para reducir incertidumbre. Es excelente para proyectos masivos y costosos, pero muy complejo de administrar.", 1, completada = false),
        LeccionEntity("leccion_mt_3", "tema_mt_3", "Verificación y Validación", 
            "El Modelo en V modifica la cascada clásica doblándola en forma de 'V'. Su principal aportación es que empareja cada fase de desarrollo (lado izquierdo) con una fase de pruebas correspondiente (lado derecho).\n\nPor ejemplo, las pruebas de aceptación se planifican en paralelo con el análisis de requisitos.", 1, completada = false),
        LeccionEntity("leccion_mt_4", "tema_mt_4", "Entrega por Partes", 
            "El Modelo Incremental no intenta construir el sistema entero de una vez. En su lugar, el sistema se divide en incrementos (módulos funcionales), y cada uno se desarrolla, prueba y entrega al cliente.\n\nPermite al cliente obtener valor rápidamente con los primeros incrementos, aunque requiere una buena arquitectura base que soporte las futuras adiciones.", 1, completada = false)
    )

    val ejercicios = listOf(
        // Mundo 1 - Leccion 1
        EjercicioEntity("ej_uml_1_1", "leccion_uml_1", "¿Qué tipo de diagrama UML muestra la estructura estática del sistema?", listOf("Diagrama de Secuencia", "Diagrama de Clases", "Diagrama de Actividades", "Diagrama de Casos de Uso"), listOf(1), 1),
        EjercicioEntity("ej_uml_1_2", "leccion_uml_1", "Selecciona los elementos principales que contiene una clase en un diagrama UML:", listOf("Atributos", "Actores", "Operaciones (Métodos)", "Nodos de hardware"), listOf(0, 2), 2),
        EjercicioEntity("ej_uml_1_3", "leccion_uml_1", "¿Qué símbolo se usa típicamente para la visibilidad 'privada' en los atributos?", listOf("+", "#", "~", "-"), listOf(3), 3),
        EjercicioEntity("ej_uml_1_4", "leccion_uml_1", "La relación que indica que un objeto es 'parte de' otro (todo-parte) se llama:", listOf("Herencia", "Asociación", "Agregación / Composición", "Dependencia"), listOf(2), 4),
        EjercicioEntity("ej_uml_1_5", "leccion_uml_1", "Verdadero o Falso: Los diagramas de clases muestran el orden en que se ejecutan los métodos a lo largo del tiempo.", listOf("Verdadero", "Falso"), listOf(1), 5),

        // Mundo 1 - Leccion 2
        EjercicioEntity("ej_uml_2_1", "leccion_uml_2", "En un diagrama de secuencia, el tiempo fluye de:", listOf("Izquierda a derecha", "Derecha a izquierda", "Arriba hacia abajo", "Abajo hacia arriba"), listOf(2), 1),
        EjercicioEntity("ej_uml_2_2", "leccion_uml_2", "¿Qué elemento representa a un participante (objeto o actor) a lo largo del tiempo?", listOf("Línea de vida (Lifeline)", "Paquete", "Mensaje síncrono", "Fragmento combinado"), listOf(0), 2),
        EjercicioEntity("ej_uml_2_3", "leccion_uml_2", "Selecciona los tipos de mensajes válidos en un diagrama de secuencia:", listOf("Mensaje Síncrono", "Mensaje Asíncrono", "Mensaje de Retorno", "Mensaje Estático"), listOf(0, 1, 2), 3),
        EjercicioEntity("ej_uml_2_4", "leccion_uml_2", "¿Para qué se usan los 'Fragmentos Combinados' (Combined Fragments) como 'alt' o 'loop'?", listOf("Para mostrar variables estáticas", "Para modelar lógica condicional o bucles", "Para eliminar objetos", "Para importar paquetes"), listOf(1), 4),
        EjercicioEntity("ej_uml_2_5", "leccion_uml_2", "Un mensaje síncrono significa que el emisor:", listOf("Espera la respuesta antes de continuar", "No espera y sigue ejecutando", "Envía el mensaje al pasado", "Es un mensaje de error"), listOf(0), 5),

        // Mundo 1 - Leccion 3
        EjercicioEntity("ej_uml_3_1", "leccion_uml_3", "¿Cuál es el propósito principal de un Diagrama de Casos de Uso?", listOf("Mostrar la base de datos", "Mostrar el flujo de algoritmos", "Mostrar las funcionalidades desde la vista del usuario", "Mostrar clases y objetos"), listOf(2), 1),
        EjercicioEntity("ej_uml_3_2", "leccion_uml_3", "Selecciona los componentes válidos en un Diagrama de Casos de Uso:", listOf("Actor", "Caso de Uso", "Límite del Sistema", "Línea de Vida"), listOf(0, 1, 2), 2),
        EjercicioEntity("ej_uml_3_3", "leccion_uml_3", "Un 'Actor' en UML puede ser:", listOf("Solo una persona humana", "Una persona, un hardware u otro sistema", "Una tabla de base de datos", "Una clase abstracta"), listOf(1), 3),
        EjercicioEntity("ej_uml_3_4", "leccion_uml_3", "El símbolo gráfico para representar un caso de uso es:", listOf("Un rectángulo", "Un muñeco de palo", "Una elipse (óvalo)", "Un rombo"), listOf(2), 4),
        EjercicioEntity("ej_uml_3_5", "leccion_uml_3", "Verdadero o Falso: Los diagramas de casos de uso explican detalladamente CÓMO el sistema resuelve el problema por dentro.", listOf("Verdadero", "Falso"), listOf(1), 5),

        // Mundo 1 - Leccion 4
        EjercicioEntity("ej_uml_4_1", "leccion_uml_4", "El diagrama de actividades es la versión orientada a objetos del clásico:", listOf("Diagrama de clases", "Diagrama de flujo", "Diagrama de red", "Diagrama de Gantt"), listOf(1), 1),
        EjercicioEntity("ej_uml_4_2", "leccion_uml_4", "¿Qué símbolo representa el nodo inicial de un diagrama de actividades?", listOf("Un círculo sólido negro", "Un rombo", "Un rectángulo con bordes redondeados", "Una 'X' roja"), listOf(0), 2),
        EjercicioEntity("ej_uml_4_3", "leccion_uml_4", "Las barras horizontales o verticales gruesas (Fork / Join) se usan para modelar:", listOf("Errores", "Finalización del sistema", "Flujos concurrentes o paralelos", "Herencia"), listOf(2), 3),
        EjercicioEntity("ej_uml_4_4", "leccion_uml_4", "Un rombo en el diagrama de actividades representa:", listOf("Un nodo inicial", "Un nodo final", "Un nodo de decisión (bifurcación)", "Una tarea manual"), listOf(2), 4),
        EjercicioEntity("ej_uml_4_5", "leccion_uml_4", "Las 'Calles' o 'Swimlanes' sirven para:", listOf("Separar responsabilidades (quién hace qué)", "Conectar a internet", "Borrar objetos", "Hacer el diagrama más bonito"), listOf(0), 5),

        // Mundo 2 - Leccion 1
        EjercicioEntity("ej_cu_1_1", "leccion_cu_1", "¿Qué define el 'Límite del Sistema' en un modelo de casos de uso?", listOf("El hardware usado", "La frontera entre lo que el sistema hace y su entorno exterior", "El presupuesto del proyecto", "La velocidad de respuesta"), listOf(1), 1),
        EjercicioEntity("ej_cu_1_2", "leccion_cu_1", "Verdadero o Falso: Un sistema externo de validación bancaria puede ser considerado un Actor.", listOf("Verdadero", "Falso"), listOf(0), 2),
        EjercicioEntity("ej_cu_1_3", "leccion_cu_1", "Los casos de uso deben nombrarse usando:", listOf("Sustantivos complejos", "Verbos en infinitivo u oraciones de acción (ej. Registrar Usuario)", "Acrónimos técnicos", "Variables booleanas"), listOf(1), 3),
        EjercicioEntity("ej_cu_1_4", "leccion_cu_1", "Selecciona ejemplos correctos de actores:", listOf("Cajero Automático (el hardware)", "Cliente", "Servidor de Correos", "Botón de 'Aceptar'"), listOf(0, 1, 2), 4),
        EjercicioEntity("ej_cu_1_5", "leccion_cu_1", "El objetivo de un caso de uso es:", listOf("Detallar el código", "Aportar valor observable a un actor", "Diseñar la interfaz", "Elegir el lenguaje de programación"), listOf(1), 5),

        // Mundo 2 - Leccion 2
        EjercicioEntity("ej_cu_2_1", "leccion_cu_2", "Si el caso de uso 'A' SIEMPRE ejecuta el caso de uso 'B' como parte de su flujo normal, la relación es:", listOf("<<extend>>", "<<include>>", "Generalización", "Asociación"), listOf(1), 1),
        EjercicioEntity("ej_cu_2_2", "leccion_cu_2", "La relación <<extend>> se utiliza cuando un comportamiento es:", listOf("Opcional o condicional", "Obligatorio siempre", "Un error del sistema", "Un tipo de herencia entre actores"), listOf(0), 2),
        EjercicioEntity("ej_cu_2_3", "leccion_cu_2", "¿Hacia dónde apunta la flecha en una relación <<include>>?", listOf("Del caso de uso base al caso de uso incluido", "Del caso de uso incluido al base", "Hacia el actor", "No lleva flecha"), listOf(0), 3),
        EjercicioEntity("ej_cu_2_4", "leccion_cu_2", "¿Hacia dónde apunta la flecha en una relación <<extend>>?", listOf("Del caso de uso base al caso de uso de extensión", "Del caso de uso de extensión al caso base", "Hacia el actor", "No lleva flecha"), listOf(1), 4),
        EjercicioEntity("ej_cu_2_5", "leccion_cu_2", "Selecciona escenarios correctos para usar <<include>>:", listOf("Reutilizar 'Iniciar Sesión' en 'Realizar Compra' y 'Ver Perfil'", "Agregar seguro de viaje si el usuario marca la casilla", "Evitar duplicar los mismos pasos en múltiples casos de uso", "Manejar un error de red"), listOf(0, 2), 5),

        // Mundo 2 - Leccion 3
        EjercicioEntity("ej_cu_3_1", "leccion_cu_3", "¿Qué son las 'Precondiciones' de un caso de uso?", listOf("Lo que el usuario opina del sistema", "El estado en que debe estar el sistema ANTES de iniciar el caso de uso", "El estado del sistema DESPUÉS de ejecutarlo", "Los errores de código"), listOf(1), 2),
        EjercicioEntity("ej_cu_3_2", "leccion_cu_3", "El 'Camino Feliz' (Happy Path) se documenta en:", listOf("El flujo principal", "Los flujos alternativos", "Las postcondiciones", "La interfaz de usuario"), listOf(0), 1),
        EjercicioEntity("ej_cu_3_3", "leccion_cu_3", "Los 'Flujos Alternativos' sirven para describir:", listOf("Opciones secundarias válidas (ej. pagar con PayPal en vez de tarjeta)", "Manejo de errores o excepciones", "El flujo principal", "Los colores de la app"), listOf(0, 1), 3),
        EjercicioEntity("ej_cu_3_4", "leccion_cu_3", "¿Qué son las 'Postcondiciones'?", listOf("El hardware usado", "Garantías o estado final del sistema tras completarse con éxito", "Las opiniones del actor", "Lo que pasa antes de iniciar"), listOf(1), 4),
        EjercicioEntity("ej_cu_3_5", "leccion_cu_3", "Una buena especificación textual se escribe típicamente en forma de:", listOf("Código Java", "Diálogo Ping-Pong (Actor hace esto, Sistema responde esto)", "Poesía", "Consultas SQL"), listOf(1), 5),

        // Mundo 2 - Leccion 4
        EjercicioEntity("ej_cu_4_1", "leccion_cu_4", "¿Por qué NO debemos describir interfaces gráficas en un caso de uso?", listOf("Porque las UIs pueden cambiar sin alterar el requisito funcional de fondo", "Porque gastan mucha tinta", "Porque a los programadores no les gustan", "Porque la ISO lo prohíbe"), listOf(0), 1),
        EjercicioEntity("ej_cu_4_2", "leccion_cu_4", "Identifica un caso de uso mal nombrado (demasiado técnico o granular):", listOf("Comprar Libro", "Actualizar Registro en Tabla User", "Consultar Historial", "Reservar Vuelo"), listOf(1), 2),
        EjercicioEntity("ej_cu_4_3", "leccion_cu_4", "Selecciona las características de un BUEN caso de uso:", listOf("Se enfoca en la intención del usuario ('Qué', no 'Cómo')", "Describe clics de botones", "Aporta valor medible al actor", "Es tecnológicamente agnóstico"), listOf(0, 2, 3), 3),
        EjercicioEntity("ej_cu_4_4", "leccion_cu_4", "Verdadero o Falso: Es una buena práctica crear un diagrama con 50 casos de uso interconectados para impresionar al cliente.", listOf("Verdadero", "Falso (reduce la legibilidad)"), listOf(1), 4),
        EjercicioEntity("ej_cu_4_5", "leccion_cu_4", "En lugar de decir 'El usuario pulsa el botón verde OK', un caso de uso debería decir:", listOf("El usuario pulsa el botón azul OK", "El usuario confirma la operación", "El sistema lanza un NullPointerException", "El usuario invoca la función System.exit(0)"), listOf(1), 5),

        // Mundo 3 - Leccion 1
        EjercicioEntity("ej_cv_1_1", "leccion_cv_1", "¿Cuál es el objetivo principal del Análisis de Requisitos?", listOf("Codificar la app", "Descubrir y documentar las necesidades y expectativas del cliente", "Diseñar los colores", "Publicar la app"), listOf(1), 1),
        EjercicioEntity("ej_cv_1_2", "leccion_cv_1", "Un requerimiento funcional describe:", listOf("La velocidad del sistema", "Qué debe hacer el sistema (comportamientos y funciones)", "El lenguaje de programación", "La seguridad"), listOf(1), 2),
        EjercicioEntity("ej_cv_1_3", "leccion_cv_1", "Un requerimiento NO funcional describe:", listOf("Qué debe hacer el sistema", "Restricciones de calidad (rendimiento, seguridad, usabilidad, etc.)", "Lo que el sistema NO debe hacer", "Errores del programador"), listOf(1), 3),
        EjercicioEntity("ej_cv_1_4", "leccion_cv_1", "Selecciona ejemplos de requerimientos NO funcionales:", listOf("El sistema debe responder en menos de 2 segundos", "El usuario podrá cancelar su suscripción", "La aplicación debe soportar 1000 usuarios concurrentes", "Los datos deben encriptarse"), listOf(0, 2, 3), 4),
        EjercicioEntity("ej_cv_1_5", "leccion_cv_1", "Un error no detectado en la fase de requerimientos suele ser:", listOf("Fácil de corregir al final", "El más costoso de reparar si se descubre en producción", "Irrelevante", "Culpa del tester"), listOf(1), 5),

        // Mundo 3 - Leccion 2
        EjercicioEntity("ej_cv_2_1", "leccion_cv_2", "La fase de Diseño transforma los requerimientos del 'Qué' al:", listOf("Cuánto", "Dónde", "'Cómo' técnico", "Cuándo"), listOf(2), 1),
        EjercicioEntity("ej_cv_2_2", "leccion_cv_2", "¿Cuáles son actividades típicas del Diseño de Software?", listOf("Programar en Java", "Diseño de la arquitectura del software", "Diseño de la base de datos", "Diseño de la interfaz de usuario"), listOf(1, 2, 3), 2),
        EjercicioEntity("ej_cv_2_3", "leccion_cv_2", "El Diseño Arquitectónico (alto nivel) define:", listOf("El código de una función", "Los subsistemas principales y cómo se comunican", "El color de la pantalla", "El nombre de las variables"), listOf(1), 3),
        EjercicioEntity("ej_cv_2_4", "leccion_cv_2", "El Diseño Detallado (bajo nivel) puede utilizar:", listOf("Lenguaje natural", "Diagramas de Clases y Secuencia UML", "Pintura al óleo", "Casos de Uso"), listOf(1), 4),
        EjercicioEntity("ej_cv_2_5", "leccion_cv_2", "Un buen diseño debe priorizar características como:", listOf("Alta cohesión y bajo acoplamiento", "Baja cohesión y alto acoplamiento", "Código espagueti", "Todo en una sola clase"), listOf(0), 5),

        // Mundo 3 - Leccion 3
        EjercicioEntity("ej_cv_3_1", "leccion_cv_3", "La fase de Implementación consiste principalmente en:", listOf("Hablar con el cliente", "Escribir los casos de uso", "Traducir el diseño a código fuente en un lenguaje de programación", "Diseñar los menús"), listOf(2), 1),
        EjercicioEntity("ej_cv_3_2", "leccion_cv_3", "Verdadero o Falso: Durante la implementación, los desarrolladores NO hacen pruebas.", listOf("Verdadero", "Falso (hacen pruebas unitarias e integración)"), listOf(1), 2),
        EjercicioEntity("ej_cv_3_3", "leccion_cv_3", "Selecciona buenas prácticas durante la codificación:", listOf("Nombrado descriptivo de variables", "Control de versiones (Git)", "Crear clases de 5000 líneas", "Escribir código limpio y refactorizar"), listOf(0, 1, 3), 3),
        EjercicioEntity("ej_cv_3_4", "leccion_cv_3", "El resultado final de esta fase es:", listOf("Un manual PDF", "El diagrama UML", "Código ejecutable e integrado", "El acta de reunión"), listOf(2), 4),
        EjercicioEntity("ej_cv_3_5", "leccion_cv_3", "Un error común en esta fase es:", listOf("Comentar el código", "Hacer pruebas unitarias", "Programar sin seguir el diseño o los requerimientos", "Usar repositorios"), listOf(2), 5),

        // Mundo 3 - Leccion 4
        EjercicioEntity("ej_cv_4_1", "leccion_cv_4", "El objetivo de la fase de Pruebas es:", listOf("Demostrar que el software no tiene NINGÚN error (imposible)", "Encontrar defectos antes de que lleguen al usuario final", "Optimizar la base de datos", "Crear las interfaces"), listOf(1), 1),
        EjercicioEntity("ej_cv_4_2", "leccion_cv_4", "Tipos comunes de pruebas de software:", listOf("Pruebas Unitarias", "Pruebas de Integración", "Pruebas de Aceptación", "Pruebas de Gravedad"), listOf(0, 1, 2), 2),
        EjercicioEntity("ej_cv_4_3", "leccion_cv_4", "¿Qué es el Mantenimiento del software?", listOf("Limpiar el teclado", "Modificar el software después de ser entregado para corregir errores o mejorar rendimiento", "Reinstalar Windows", "Comprar más RAM"), listOf(1), 3),
        EjercicioEntity("ej_cv_4_4", "leccion_cv_4", "Mantenimiento Correctivo significa:", listOf("Agregar nuevas funciones", "Adaptar el software a un nuevo SO", "Corregir bugs encontrados en producción", "Mejorar la usabilidad sin cambiar la función"), listOf(2), 4),
        EjercicioEntity("ej_cv_4_5", "leccion_cv_4", "A lo largo de 10 años, ¿qué fase suele consumir la mayor parte del presupuesto de un proyecto?", listOf("Diseño", "Codificación", "Pruebas", "Mantenimiento"), listOf(3), 5),

        // Mundo 4 - Leccion 1
        EjercicioEntity("ej_mt_1_1", "leccion_mt_1", "La característica principal del Modelo en Cascada es:", listOf("Fases estrictamente secuenciales sin superposición", "Desarrollo iterativo", "Mucha comunicación diaria con el cliente", "Entrega de prototipos cada semana"), listOf(0), 1),
        EjercicioEntity("ej_mt_1_2", "leccion_mt_1", "En el modelo Cascada clásico, una fase no puede comenzar hasta que:", listOf("El cliente paga", "La fase anterior haya terminado completamente y sido aprobada", "El programador lo decida", "Haya un prototipo"), listOf(1), 2),
        EjercicioEntity("ej_mt_1_3", "leccion_mt_1", "El modelo Cascada es recomendado cuando:", listOf("Los requisitos cambian constantemente", "Los requisitos son muy estables, claros y conocidos desde el inicio", "Se necesita entregar rápido", "El equipo no tiene experiencia"), listOf(1), 3),
        EjercicioEntity("ej_mt_1_4", "leccion_mt_1", "Una desventaja MAYOR del modelo Cascada es:", listOf("Genera mucha documentación", "El software ejecutable se ve muy tarde en el proyecto (poca flexibilidad a cambios)", "Es fácil de gestionar", "Sus fases son claras"), listOf(1), 4),
        EjercicioEntity("ej_mt_1_5", "leccion_mt_1", "Si el cliente cambia de idea a mitad de la fase de codificación en un proyecto Cascada estricto:", listOf("Es muy fácil y barato adaptarse", "Es costoso y difícil volver a la fase de requisitos", "No se puede cambiar nunca", "El código se auto-ajusta"), listOf(1), 5),

        // Mundo 4 - Leccion 2
        EjercicioEntity("ej_mt_2_1", "leccion_mt_2", "El Modelo en Espiral fue diseñado por:", listOf("Bill Gates", "Linus Torvalds", "Barry Boehm", "Alan Turing"), listOf(2), 1),
        EjercicioEntity("ej_mt_2_2", "leccion_mt_2", "El enfoque central y distintivo del modelo Espiral es:", listOf("Velocidad de entrega", "Análisis y resolución de Riesgos", "No hacer documentación", "La programación orientada a objetos"), listOf(1), 2),
        EjercicioEntity("ej_mt_2_3", "leccion_mt_2", "En el modelo Espiral, la mitigación de riesgos se suele lograr mediante:", listOf("Firmar contratos", "Desarrollar prototipos en cada iteración", "Evitar programar", "Contratar más gente"), listOf(1), 3),
        EjercicioEntity("ej_mt_2_4", "leccion_mt_2", "Selecciona las características del modelo Espiral:", listOf("Se repite en bucles (iterativo)", "Evaluación de alternativas y riesgos", "Revisión con el cliente al final de cada ciclo", "Es ideal para proyectos muy pequeños y simples"), listOf(0, 1, 2), 4),
        EjercicioEntity("ej_mt_2_5", "leccion_mt_2", "¿Por qué el modelo Espiral no se usa en todos los proyectos?", listOf("Por su alta complejidad de gestión y el costo de análisis de riesgos", "Porque es muy rápido", "Porque no permite cambios", "Porque prohíbe el uso de UML"), listOf(0), 5),

        // Mundo 4 - Leccion 3
        EjercicioEntity("ej_mt_3_1", "leccion_mt_3", "El Modelo en V se considera una variante del modelo:", listOf("Espiral", "Prototipado", "Cascada", "Agile"), listOf(2), 1),
        EjercicioEntity("ej_mt_3_2", "leccion_mt_3", "El énfasis principal del Modelo en V está en:", listOf("La planificación temprana y exhaustiva de las pruebas", "Entregar software en 2 semanas", "Evitar la fase de diseño", "Reducir la documentación"), listOf(0), 2),
        EjercicioEntity("ej_mt_3_3", "leccion_mt_3", "En el Modelo en V, el 'Diseño del Sistema' se empareja con las:", listOf("Pruebas Unitarias", "Pruebas de Aceptación", "Pruebas de Integración y Sistema", "Pruebas de Usabilidad"), listOf(2), 3),
        EjercicioEntity("ej_mt_3_4", "leccion_mt_3", "En el Modelo en V, la 'Especificación de Requisitos' se empareja con las:", listOf("Pruebas de Integración", "Pruebas Unitarias", "Pruebas de Aceptación del Usuario (UAT)", "Mantenimiento"), listOf(2), 4),
        EjercicioEntity("ej_mt_3_5", "leccion_mt_3", "Verdadero o Falso: El Modelo en V soluciona la inflexibilidad a cambios tardíos típica del modelo Cascada.", listOf("Verdadero", "Falso (sigue siendo igual de rígido)"), listOf(1), 5),

        // Mundo 4 - Leccion 4
        EjercicioEntity("ej_mt_4_1", "leccion_mt_4", "En el Modelo Incremental, el sistema se desarrolla:", listOf("Todo de una vez (Big Bang)", "Por partes o módulos que añaden funcionalidad", "Solo en teoría", "Solo si el riesgo es alto"), listOf(1), 1),
        EjercicioEntity("ej_mt_4_2", "leccion_mt_4", "Una ventaja clave del Modelo Incremental frente a la Cascada es:", listOf("Genera más documentación", "El cliente puede usar versiones funcionales más tempranamente", "No requiere análisis de requisitos", "Evita las pruebas"), listOf(1), 2),
        EjercicioEntity("ej_mt_4_3", "leccion_mt_4", "Si construimos un procesador de textos incrementalmente, el incremento 1 podría ser:", listOf("Solo el color del texto", "Las funciones básicas de edición y guardado de archivos", "El corrector ortográfico avanzado de francés", "La interfaz sin botones"), listOf(1), 3),
        EjercicioEntity("ej_mt_4_4", "leccion_mt_4", "Selecciona requisitos previos importantes para el éxito del modelo incremental:", listOf("Una arquitectura general bien definida desde el principio", "Ningún plan, solo programar", "Interfaces claras entre módulos", "Requisitos básicos conocidos"), listOf(0, 2, 3), 4),
        EjercicioEntity("ej_mt_4_5", "leccion_mt_4", "Un riesgo del Modelo Incremental es:", listOf("El sistema puede degradarse si se añaden incrementos sin cuidar la arquitectura global", "Entregar valor demasiado rápido", "Es imposible de probar", "No usa bases de datos"), listOf(0), 5)
    )

    fun insertar(db: SupportSQLiteDatabase) {
        val converters = Converters()
        mundos.forEach { mundo ->
            db.insert("mundos", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", mundo.id)
                put("nombre", mundo.nombre)
                put("descripcion", mundo.descripcion)
                put("orden", mundo.orden)
                put("desbloqueado", mundo.desbloqueado)
            })
        }
        temas.forEach { tema ->
            db.insert("temas", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", tema.id)
                put("mundoId", tema.mundoId)
                put("nombre", tema.nombre)
                put("descripcion", tema.descripcion)
                put("orden", tema.orden)
                put("desbloqueado", tema.desbloqueado)
                put("introduccionVista", tema.introduccionVista)
                put("completado", tema.completado)
            })
        }
        lecciones.forEach { leccion ->
            db.insert("lecciones", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", leccion.id)
                put("temaId", leccion.temaId)
                put("titulo", leccion.titulo)
                put("introduccion", leccion.introduccion)
                put("orden", leccion.orden)
                put("completada", leccion.completada)
            })
        }
        ejercicios.forEach { ejercicio ->
            db.insert("ejercicios", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", ejercicio.id)
                put("leccionId", ejercicio.leccionId)
                put("enunciado", ejercicio.enunciado)
                put("opciones", converters.fromStringList(ejercicio.opciones))
                put("respuestasCorrectas", converters.fromIntList(ejercicio.respuestasCorrectas))
                put("orden", ejercicio.orden)
            })
        }
    }
}
