package com.aesir.odin.data.local

import com.aesir.odin.data.local.entity.PreguntaDiagnosticaEntity

/**
 * Contenido inicial del CU-10: 10 preguntas por mundo. Los dos primeros temas
 * tienen 3 preguntas y los dos últimos 2, así cada tema se puede reconocer
 * como conocido con EvaluarDiagnosticoUseCase.UMBRAL_ACIERTOS_TEMA = 2.
 *
 * Son preguntas distintas a las de las lecciones, para que el diagnóstico
 * no adelante las respuestas de los ejercicios.
 */
object DatosDiagnostico {

    private data class P(val enunciado: String, val opciones: List<String>, val correctas: List<Int>)

    /** temaId -> preguntas. El mundo se toma del prefijo del tema. */
    private val banco: Map<String, Pair<String, List<P>>> = linkedMapOf(
        // Mundo 1 - UML
        "tema_uml_1" to ("mundo_uml" to listOf(
            P("¿Qué representa el compartimento inferior de una clase en UML?", listOf("Sus atributos", "Sus operaciones", "Su nombre", "Sus relaciones"), listOf(1)),
            P("En una herencia, la clase hija…", listOf("Recibe atributos y métodos de la clase padre", "Contiene a la clase padre", "No puede agregar métodos propios", "Depende de un actor"), listOf(0)),
            P("Selecciona las relaciones que existen en un diagrama de clases:", listOf("Asociación", "Generalización", "Mensaje de retorno", "Agregación"), listOf(0, 1, 3))
        )),
        "tema_uml_2" to ("mundo_uml" to listOf(
            P("¿Qué muestra principalmente un diagrama de secuencia?", listOf("La estructura de la base de datos", "El orden de los mensajes entre participantes", "Los requisitos no funcionales", "La distribución física"), listOf(1)),
            P("¿Qué fragmento combinado modela una repetición?", listOf("alt", "opt", "loop", "break"), listOf(2)),
            P("Un mensaje asíncrono significa que el emisor…", listOf("Espera la respuesta", "Continúa sin esperar la respuesta", "Destruye al receptor", "Regresa un valor"), listOf(1))
        )),
        "tema_uml_3" to ("mundo_uml" to listOf(
            P("¿Cómo se representa un caso de uso en el diagrama?", listOf("Rectángulo", "Óvalo", "Rombo", "Figura de palito"), listOf(1)),
            P("¿Desde qué perspectiva se describe un diagrama de casos de uso?", listOf("La del programador", "La del usuario o actor", "La de la base de datos", "La del servidor"), listOf(1))
        )),
        "tema_uml_4" to ("mundo_uml" to listOf(
            P("En un diagrama de actividades, el rombo representa…", listOf("Un actor", "Una decisión o unión de flujos", "Un estado final", "Un objeto"), listOf(1)),
            P("¿Para qué sirven las barras de sincronización?", listOf("Dividir o unir flujos paralelos", "Marcar el inicio", "Representar clases", "Definir actores"), listOf(0))
        )),

        // Mundo 2 - Casos de Uso
        "tema_cu_1" to ("mundo_casos_uso" to listOf(
            P("¿Cuál de estos puede ser un actor?", listOf("Un usuario humano", "Otro sistema externo", "Una clase interna del sistema", "Un sensor de hardware"), listOf(0, 1, 3)),
            P("¿Qué representa el rectángulo que rodea a los casos de uso?", listOf("Una base de datos", "El límite del sistema", "Un paquete de clases", "Un actor"), listOf(1)),
            P("Un caso de uso debe aportar…", listOf("Un resultado de valor para el actor", "Detalles de la interfaz", "El código fuente", "Una tabla de la base de datos"), listOf(0))
        )),
        "tema_cu_2" to ("mundo_casos_uso" to listOf(
            P("«Pagar en línea» siempre requiere «Validar tarjeta». ¿Qué relación usarías?", listOf("Extend", "Include", "Generalización", "Asociación"), listOf(1)),
            P("La relación extend indica un comportamiento…", listOf("Obligatorio", "Opcional o condicional", "Que se ejecuta antes del caso base", "Que reemplaza al caso base"), listOf(1)),
            P("¿Hacia dónde apunta la flecha de una relación include?", listOf("Del caso incluido al caso base", "Del caso base al caso incluido", "Del actor al caso de uso", "No lleva dirección"), listOf(1))
        )),
        "tema_cu_3" to ("mundo_casos_uso" to listOf(
            P("¿Qué sección describe lo que debe ser cierto antes de iniciar el caso de uso?", listOf("Postcondiciones", "Flujo normal", "Precondiciones", "Flujos alternativos"), listOf(2)),
            P("Selecciona lo que debe contener una especificación de caso de uso:", listOf("Flujo normal", "Flujos alternativos", "Código de la interfaz", "Actores"), listOf(0, 1, 3))
        )),
        "tema_cu_4" to ("mundo_casos_uso" to listOf(
            P("¿Cuál paso está mejor redactado?", listOf("El alumno presiona el botón verde", "El alumno confirma su respuesta", "El sistema ejecuta un SELECT", "El alumno hace clic en OK"), listOf(1)),
            P("Dividir un caso de uso en «Alta», «Baja» y «Cambio» de usuario es…", listOf("Una buena práctica", "Un antipatrón por ser demasiado granular", "Obligatorio en UML", "Una relación extend"), listOf(1))
        )),

        // Mundo 3 - Ciclo de Vida
        "tema_cv_1" to ("mundo_ciclo_vida" to listOf(
            P("«El sistema debe responder en menos de 2 segundos» es un requisito…", listOf("Funcional", "No funcional", "De dominio", "De usuario"), listOf(1)),
            P("¿Por qué un error en los requisitos es el más costoso?", listOf("Porque se propaga a todas las fases siguientes", "Porque lo encuentra el cliente", "Porque requiere otro lenguaje", "No es costoso"), listOf(0)),
            P("Selecciona técnicas para obtener requisitos:", listOf("Entrevistas", "Cuestionarios", "Compilar el código", "Observación del trabajo"), listOf(0, 1, 3))
        )),
        "tema_cv_2" to ("mundo_ciclo_vida" to listOf(
            P("El diseño responde principalmente a la pregunta…", listOf("¿Qué debe hacer el sistema?", "¿Cómo se construirá el sistema?", "¿Cuánto costará?", "¿Quién lo usará?"), listOf(1)),
            P("La arquitectura en capas separa…", listOf("Presentación, negocio y datos", "Requisitos y pruebas", "Clientes y proveedores", "Hardware y usuarios"), listOf(0)),
            P("¿Qué se define en el diseño detallado?", listOf("Clases e interfaces internas", "El precio del producto", "El plan de mercadotecnia", "La fecha de retiro"), listOf(0))
        )),
        "tema_cv_3" to ("mundo_ciclo_vida" to listOf(
            P("¿Quién realiza normalmente las pruebas unitarias?", listOf("El cliente", "Los desarrolladores", "El usuario final", "El gerente comercial"), listOf(1)),
            P("La implementación consiste en…", listOf("Traducir el diseño a código ejecutable", "Entrevistar al cliente", "Retirar el sistema", "Definir el presupuesto"), listOf(0))
        )),
        "tema_cv_4" to ("mundo_ciclo_vida" to listOf(
            P("Adaptar el software a un nuevo sistema operativo es mantenimiento…", listOf("Correctivo", "Adaptativo", "Perfectivo", "Preventivo"), listOf(1)),
            P("¿Qué fase suele consumir la mayor parte del costo total del software?", listOf("Análisis", "Diseño", "Mantenimiento", "Codificación"), listOf(2))
        )),

        // Mundo 4 - Modelos Tradicionales
        "tema_mt_1" to ("mundo_modelos_trad" to listOf(
            P("En el modelo en cascada, una fase inicia cuando…", listOf("La anterior terminó", "El cliente lo pide", "Hay un riesgo", "Se entrega un incremento"), listOf(0)),
            P("¿Cuándo es más adecuado el modelo en cascada?", listOf("Cuando los requisitos son estables y claros", "Cuando todo cambia cada semana", "Cuando no hay cliente", "Cuando no hay pruebas"), listOf(0)),
            P("Su principal desventaja es…", listOf("La rigidez ante los cambios", "Que no tiene fases", "Que solo sirve para juegos", "Que no documenta"), listOf(0))
        )),
        "tema_mt_2" to ("mundo_modelos_trad" to listOf(
            P("El modelo en espiral está centrado en…", listOf("El análisis de riesgos", "La documentación mínima", "La codificación rápida", "Las pruebas unitarias"), listOf(0)),
            P("¿Quién propuso el modelo en espiral?", listOf("Winston Royce", "Barry Boehm", "Ivar Jacobson", "Kent Beck"), listOf(1)),
            P("Selecciona características del modelo en espiral:", listOf("Es iterativo", "Usa prototipos", "Es estrictamente lineal", "Evalúa riesgos en cada ciclo"), listOf(0, 1, 3))
        )),
        "tema_mt_3" to ("mundo_modelos_trad" to listOf(
            P("En el modelo en V, la prueba de aceptación corresponde a…", listOf("El análisis de requisitos", "La codificación", "El diseño detallado", "El mantenimiento"), listOf(0)),
            P("«¿Estamos construyendo el producto correcto?» se refiere a…", listOf("Verificación", "Validación", "Integración", "Despliegue"), listOf(1))
        )),
        "tema_mt_4" to ("mundo_modelos_trad" to listOf(
            P("El modelo incremental entrega el sistema…", listOf("Todo al final", "Por partes funcionales", "Solo como prototipo", "Sin pruebas"), listOf(1)),
            P("Una ventaja del modelo incremental es que…", listOf("El cliente recibe valor desde los primeros incrementos", "No requiere arquitectura", "No necesita requisitos", "Elimina el mantenimiento"), listOf(0))
        ))
    )

    val preguntas: List<PreguntaDiagnosticaEntity> = banco.flatMap { (temaId, mundoYPreguntas) ->
        val (mundoId, lista) = mundoYPreguntas
        lista.mapIndexed { i, p ->
            PreguntaDiagnosticaEntity(
                id = "pd_${temaId}_${i + 1}",
                mundoId = mundoId,
                temaId = temaId,
                enunciado = p.enunciado,
                opciones = p.opciones,
                respuestasCorrectas = p.correctas
            )
        }
    }
}
