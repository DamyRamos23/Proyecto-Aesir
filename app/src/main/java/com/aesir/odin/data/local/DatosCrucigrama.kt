package com.aesir.odin.data.local

import com.aesir.odin.data.local.entity.ConceptoCrucigramaEntity
import com.aesir.odin.data.local.entity.MundoEntity
import com.aesir.odin.data.local.entity.TemaEntity

/**
 * Contenido inicial del CU-07: banco de conceptos por tema y un nivel de
 * crucigrama por mundo que repasa los temas de ese mundo.
 *
 * Reglas del banco: una sola palabra por respuesta, de 3 a 12 letras (el
 * generador quita acentos y la Ñ se conserva). La descripción es la pista.
 */
object DatosCrucigrama {

    /**
     * Anillo exterior (minijuegos) de cada mundo: 2 crucigramas, 2 memoramas
     * y 2 de flash cards, intercalados. Cada uno queda entre las dos lecciones
     * que repasa (ArbolHabilidades). Todos son jugables: crucigrama (CU-07),
     * memorama (CU-04) y flash cards (CU-03).
     */
    fun minijuegos(mundos: List<MundoEntity>): List<TemaEntity> = mundos.flatMap { mundo ->
        MINIJUEGOS.mapIndexed { k, (actividad, nombre, descripcion) ->
            TemaEntity(
                id = "mj_${mundo.id}_${k + 1}",
                mundoId = mundo.id,
                nombre = nombre,
                descripcion = descripcion,
                orden = k + 1,
                desbloqueado = false,
                introduccionVista = false,
                completado = false,
                tipo = "NORMAL",
                actividad = actividad,
                anillo = 2
            )
        }
    }

    private val MINIJUEGOS = listOf(
        Triple("CRUCIGRAMA", "Crucigrama I", "Resuelve un crucigrama con los conceptos de las lecciones cercanas."),
        Triple("MEMORAMA", "Memorama I", "Encuentra los pares de concepto y definición."),
        Triple("FLASHCARDS", "Flash cards I", "Repasa los conceptos con tarjetas de pregunta y respuesta."),
        Triple("CRUCIGRAMA", "Crucigrama II", "Resuelve un crucigrama con los conceptos de las lecciones cercanas."),
        Triple("MEMORAMA", "Memorama II", "Encuentra los pares de concepto y definición."),
        Triple("FLASHCARDS", "Flash cards II", "Repasa los conceptos con tarjetas de pregunta y respuesta.")
    )

    private val banco: Map<String, List<Pair<String, String>>> = mapOf(
        // Mundo 1 · UML
        "tema_uml_1" to listOf(
            "Clase" to "Plantilla que define los atributos y métodos de un objeto.",
            "Atributo" to "Propiedad o dato que describe a una clase.",
            "Método" to "Operación que puede realizar un objeto.",
            "Herencia" to "Relación en la que una clase hija recibe lo de su clase padre.",
            "Composición" to "Relación todo-parte en la que la parte no existe sin el todo."
        ),
        "tema_uml_2" to listOf(
            "Mensaje" to "Llamada que un participante envía a otro en el tiempo.",
            "Síncrono" to "Tipo de mensaje en el que el emisor espera la respuesta.",
            "Retorno" to "Mensaje que devuelve el resultado de una llamada.",
            "Activación" to "Barra que indica cuándo un objeto está ejecutando una operación.",
            "Fragmento" to "Marco como alt o loop que agrupa interacciones."
        ),
        "tema_uml_3" to listOf(
            "Actor" to "Entidad externa que interactúa con el sistema.",
            "Include" to "Relación en la que un caso de uso siempre usa a otro.",
            "Extend" to "Relación que agrega comportamiento opcional a un caso de uso.",
            "Elipse" to "Figura con la que se dibuja un caso de uso.",
            "Límite" to "Rectángulo que separa lo que está dentro del sistema."
        ),
        "tema_uml_4" to listOf(
            "Decisión" to "Nodo en forma de rombo que bifurca el flujo.",
            "Fork" to "Barra que divide el flujo en caminos paralelos.",
            "Carril" to "Partición que indica quién realiza cada actividad.",
            "Inicial" to "Nodo representado por un círculo negro sólido.",
            "Acción" to "Paso individual dentro de una actividad."
        ),
        // Mundo 2 · Casos de Uso
        "tema_cu_1" to listOf(
            "Actor" to "Persona, sistema o dispositivo externo que usa el sistema.",
            "Sistema" to "Lo que se modela; su rectángulo separa lo interno de lo externo.",
            "Objetivo" to "Lo que el actor quiere lograr al usar el sistema.",
            "Escenario" to "Una secuencia concreta de pasos de un caso de uso.",
            "Frontera" to "Límite entre el sistema y su entorno."
        ),
        "tema_cu_2" to listOf(
            "Include" to "Relación obligatoria: el caso base siempre usa al incluido.",
            "Extend" to "Relación condicional que agrega comportamiento al caso base.",
            "Reutilizar" to "Lo que permite include: aprovechar un caso de uso en varios.",
            "Opcional" to "Cómo es el comportamiento que agrega extend.",
            "Base" to "Caso de uso que es extendido o que incluye a otro."
        ),
        "tema_cu_3" to listOf(
            "Precondición" to "Lo que debe ser cierto antes de iniciar el caso de uso.",
            "Flujo" to "Secuencia de pasos; el principal es el camino feliz.",
            "Alternativo" to "Tipo de flujo que describe errores o desvíos.",
            "Disparador" to "Evento que inicia el caso de uso.",
            "Paso" to "Cada acción numerada del flujo de eventos."
        ),
        "tema_cu_4" to listOf(
            "Interfaz" to "Detalle que un caso de uso no debe describir: botones y pantallas.",
            "Intención" to "Lo que debe expresar cada paso, en lugar de la tecnología.",
            "Tecnología" to "Los casos de uso deben ser independientes de ella.",
            "Ambigüedad" to "Problema que evita una especificación textual detallada.",
            "Crud" to "Error de modelar por separado crear, leer, actualizar y borrar."
        ),
        // Mundo 3 · Ciclo de Vida
        "tema_cv_1" to listOf(
            "Requisito" to "Necesidad o condición que el sistema debe cumplir.",
            "Funcional" to "Tipo de requisito que describe qué hace el sistema.",
            "Stakeholder" to "Persona interesada o afectada por el sistema.",
            "Elicitación" to "Actividad de descubrir los requisitos con los interesados.",
            "Restricción" to "Límite impuesto a la solución, como un requisito no funcional."
        ),
        "tema_cv_2" to listOf(
            "Arquitectura" to "Estructura de alto nivel: componentes y cómo se comunican.",
            "Componente" to "Parte del sistema con una responsabilidad definida.",
            "Interfaz" to "Punto por el que se comunican dos componentes.",
            "Cohesión" to "Qué tan relacionadas están las tareas de un módulo.",
            "Acoplamiento" to "Grado de dependencia entre módulos."
        ),
        "tema_cv_3" to listOf(
            "Codificación" to "Traducir el diseño a un lenguaje de programación.",
            "Compilar" to "Convertir el código fuente en un programa ejecutable.",
            "Integración" to "Unir los componentes para que trabajen juntos.",
            "Unitaria" to "Prueba que verifica una pieza pequeña de código.",
            "Refactorizar" to "Mejorar la estructura del código sin cambiar su comportamiento."
        ),
        "tema_cv_4" to listOf(
            "Prueba" to "Ejecución del software para encontrar defectos.",
            "Defecto" to "Falla en el código que provoca un comportamiento incorrecto.",
            "Correctivo" to "Mantenimiento que repara los fallos encontrados.",
            "Adaptativo" to "Mantenimiento que ajusta el software a un nuevo entorno.",
            "Regresión" to "Prueba que confirma que lo que funcionaba sigue funcionando."
        ),
        // Mundo 4 · Modelos Tradicionales
        "tema_mt_1" to listOf(
            "Cascada" to "Modelo en el que las fases ocurren una tras otra sin regresar.",
            "Secuencial" to "Cómo avanzan las fases en el modelo en cascada.",
            "Despliegue" to "Fase en la que el sistema se entrega para su uso.",
            "Estables" to "Cómo deben ser los requisitos para que la cascada funcione bien.",
            "Rigidez" to "Principal debilidad de la cascada ante los cambios."
        ),
        "tema_mt_2" to listOf(
            "Espiral" to "Modelo iterativo centrado en la evaluación de riesgos.",
            "Riesgo" to "Lo que se analiza en cada ciclo del modelo en espiral.",
            "Prototipo" to "Versión preliminar que se construye para reducir la incertidumbre.",
            "Boehm" to "Apellido de quien propuso el modelo en espiral.",
            "Ciclo" to "Cada vuelta del espiral."
        ),
        "tema_mt_3" to listOf(
            "Verificación" to "Comprobar que el producto se construye correctamente.",
            "Validación" to "Comprobar que se construye el producto correcto.",
            "Aceptación" to "Prueba que corresponde al análisis de requisitos en el modelo en V.",
            "Unidad" to "Nivel de prueba que corresponde al diseño detallado.",
            "Integración" to "Nivel de prueba que corresponde al diseño arquitectónico."
        ),
        "tema_mt_4" to listOf(
            "Incremento" to "Parte funcional del sistema que se entrega al cliente.",
            "Módulo" to "Unidad funcional en la que se divide el sistema.",
            "Entrega" to "Momento en que el cliente recibe un incremento.",
            "Valor" to "Lo que el cliente obtiene pronto con los primeros incrementos.",
            "Arquitectura" to "Base que debe estar bien definida para soportar los incrementos."
        )
    )

    val conceptos: List<ConceptoCrucigramaEntity> = banco.flatMap { (temaId, lista) ->
        lista.mapIndexed { i, (respuesta, descripcion) ->
            ConceptoCrucigramaEntity("cc_${temaId}_${i + 1}", temaId, respuesta.uppercase(), descripcion)
        }
    }
}
