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
        PreguntaFlashEntity("pf_mt_4_4", "tema_mt_4", "Selecciona ventajas del modelo incremental:", listOf("Entregas tempranas al cliente", "Retroalimentación frecuente", "Riesgo repartido entre incrementos", "Cero necesidad de planear"), listOf(0, 1, 2))
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
