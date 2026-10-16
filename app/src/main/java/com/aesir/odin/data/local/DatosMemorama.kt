package com.aesir.odin.data.local

import com.aesir.odin.data.local.entity.ParConceptoEntity

/**
 * Contenido inicial del CU-04: banco de pares concepto-definición por mundo.
 * Cada memorama toma 6 pares al azar del banco de su mundo, así que dos
 * partidas seguidas casi nunca salen iguales. Las definiciones se mantienen
 * cortas para que quepan en una tarjeta.
 */
object DatosMemorama {

    private val banco: Map<String, List<Pair<String, String>>> = mapOf(
        "mundo_uml" to listOf(
            "Clase" to "Plantilla que define atributos y métodos de un objeto.",
            "Atributo" to "Dato que describe el estado de una clase.",
            "Herencia" to "Una clase hija recibe lo definido en su clase padre.",
            "Composición" to "Relación todo-parte: la parte no existe sin el todo.",
            "Línea de vida" to "Representa a un participante a lo largo del tiempo.",
            "Mensaje síncrono" to "El emisor espera la respuesta antes de seguir.",
            "Actor" to "Entidad externa que interactúa con el sistema.",
            "Include" to "Comportamiento obligatorio que se reutiliza.",
            "Nodo de decisión" to "Rombo que divide el flujo según una condición.",
            "Barra de sincronización" to "Divide o une flujos que ocurren en paralelo."
        ),
        "mundo_casos_uso" to listOf(
            "Caso de uso" to "Tarea que un actor realiza para lograr un objetivo.",
            "Límite del sistema" to "Rectángulo que separa lo interno de lo externo.",
            "Extend" to "Comportamiento opcional que se agrega bajo una condición.",
            "Generalización" to "Un actor o caso de uso hereda de otro más general.",
            "Precondición" to "Lo que debe ser cierto antes de iniciar el caso de uso.",
            "Postcondición" to "Estado del sistema al terminar el caso de uso.",
            "Flujo normal" to "Secuencia de pasos del camino exitoso.",
            "Flujo alternativo" to "Desvío o error que se aparta del flujo normal.",
            "Actor primario" to "Quien inicia el caso de uso para lograr su objetivo.",
            "Caso de uso CRUD" to "Antipatrón: dividir en altas, bajas y cambios sueltos."
        ),
        "mundo_ciclo_vida" to listOf(
            "Requisito funcional" to "Describe lo que el sistema debe hacer.",
            "Requisito no funcional" to "Restricción de calidad: rendimiento, seguridad…",
            "Stakeholder" to "Persona o grupo interesado en el sistema.",
            "Arquitectura" to "Organización de alto nivel de los componentes.",
            "Diseño detallado" to "Define clases, interfaces y algoritmos internos.",
            "Implementación" to "Traducir el diseño a código ejecutable.",
            "Prueba unitaria" to "Verifica un componente aislado del resto.",
            "Prueba de integración" to "Verifica que los componentes funcionen juntos.",
            "Mantenimiento correctivo" to "Corregir fallos encontrados después de la entrega.",
            "Mantenimiento adaptativo" to "Ajustar el software a un nuevo entorno."
        ),
        "mundo_modelos_trad" to listOf(
            "Cascada" to "Modelo secuencial: cada fase inicia al terminar la anterior.",
            "Espiral" to "Modelo iterativo guiado por el análisis de riesgos.",
            "Modelo en V" to "Relaciona cada fase de desarrollo con su nivel de prueba.",
            "Incremental" to "Entrega el sistema por partes funcionales.",
            "Prototipo" to "Versión preliminar para aclarar requisitos.",
            "Riesgo" to "Evento incierto que puede afectar al proyecto.",
            "Verificación" to "¿Estamos construyendo el producto correctamente?",
            "Validación" to "¿Estamos construyendo el producto correcto?",
            "Prueba de aceptación" to "El cliente confirma que el sistema cumple sus requisitos.",
            "Incremento" to "Parte funcional que se entrega al cliente."
        )
    )

    val pares: List<ParConceptoEntity> = banco.flatMap { (mundoId, lista) ->
        lista.mapIndexed { i, (concepto, definicion) ->
            ParConceptoEntity("pc_${mundoId}_${i + 1}", mundoId, concepto, definicion)
        }
    }
}
