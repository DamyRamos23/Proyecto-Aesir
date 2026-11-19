package com.aesir.odin.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aesir.odin.data.local.converter.Converters
import com.aesir.odin.data.local.entity.VisionEntity

/**
 * Contenido semilla del CU-12: una visión de cada Norna por tema (48 en total).
 * Urd pide encontrar el error, Verdandi identificar el caso y Skuld predecir la
 * consecuencia. Cada visión tiene tres opciones y una sola correcta.
 *
 * Igual que [BancoPreguntas], se inserta o actualiza cada vez que se abre la
 * base de datos, así que agregar visiones no borra el progreso.
 */
object DatosVolva {

    private fun v(id: String, tema: String, norna: String, caso: String, opciones: List<String>, correcta: Int, pista: String, explicacion: String) =
        VisionEntity(id, tema, norna, caso, opciones, correcta, pista, explicacion)

    val visiones: List<VisionEntity> = listOf(
        // ---------------- Mundo UML ----------------
        v("vv_uml_1_u", "tema_uml_1", "URD", "En un diagrama de clases, la clase Pedido muestra «+ total: Double» y «- calcularTotal(): void». ¿Qué está mal?",
            listOf("calcularTotal() regresa void aunque debería regresar el total", "Los atributos no pueden ser públicos", "Los métodos no llevan paréntesis"), 0,
            "Revisa el tipo de retorno de los métodos.", "Un método que calcula un total debe regresar ese valor; con void el resultado se pierde."),
        v("vv_uml_1_v", "tema_uml_1", "VERDANDI", "Un Auto tiene un Motor que se crea y se destruye junto con él. ¿Qué relación es?",
            listOf("Agregación", "Composición", "Asociación simple"), 1,
            "Piensa en el ciclo de vida de la parte.", "En la composición la parte no existe sin el todo: si el auto se destruye, su motor también."),
        v("vv_uml_1_s", "tema_uml_1", "SKULD", "Si la clase Cuenta cambia su atributo saldo de privado a público, ¿qué pasará?",
            listOf("Nada, la visibilidad solo es decorativa", "Otras clases podrán modificar el saldo sin pasar por sus reglas", "La clase dejará de compilar"), 1,
            "Piensa en el encapsulamiento.", "Al hacerlo público se rompe el encapsulamiento: cualquiera puede cambiar el saldo sin validaciones."),

        v("vv_uml_2_u", "tema_uml_2", "URD", "En un diagrama de secuencia, el mensaje de respuesta de «buscarAlumno()» se dibujó con línea sólida y punta rellena. ¿Qué está mal?",
            listOf("Los mensajes de respuesta van con línea punteada", "Las respuestas no se dibujan nunca", "La punta debe ser un rombo"), 0,
            "Compara la llamada y el retorno.", "Las llamadas síncronas usan línea sólida con punta rellena; los retornos, línea punteada con punta abierta."),
        v("vv_uml_2_v", "tema_uml_2", "VERDANDI", "Un recuadro con la etiqueta «loop [para cada producto]» rodea varios mensajes. ¿Qué representa?",
            listOf("Un flujo alternativo", "Mensajes que se repiten mientras se cumpla la condición", "Un mensaje opcional"), 1,
            "Fíjate en la palabra del fragmento.", "El fragmento loop repite los mensajes que contiene mientras la guarda sea verdadera."),
        v("vv_uml_2_s", "tema_uml_2", "SKULD", "Si en un diagrama de secuencia falta la línea de vida del repositorio, ¿qué consecuencia tendrá en el equipo?",
            listOf("No se sabrá quién guarda los datos al implementar", "El diagrama será más preciso", "El repositorio se creará solo"), 0,
            "Piensa en lo que el desarrollador necesita saber.", "Sin ese participante, el diagrama no indica quién persiste los datos y cada quien lo implementará distinto."),

        v("vv_uml_3_u", "tema_uml_3", "URD", "Un diagrama de casos de uso tiene la elipse «Base de datos guarda alumno». ¿Qué está mal?",
            listOf("Las elipses deben ser rectángulos", "Describe una acción interna, no un objetivo del actor", "Le falta un número de versión"), 1,
            "¿Quién busca lograr algo con ese caso?", "Un caso de uso describe un objetivo del actor (por ejemplo, «Registrar alumno»), no lo que hace un componente interno."),
        v("vv_uml_3_v", "tema_uml_3", "VERDANDI", "Un sistema externo de pagos aparece fuera del límite del sistema y se conecta con «Pagar inscripción». ¿Qué es?",
            listOf("Un actor", "Un caso de uso", "Una clase"), 0,
            "Está fuera del límite del sistema.", "Cualquier persona u otro sistema que interactúa desde afuera del límite es un actor."),
        v("vv_uml_3_s", "tema_uml_3", "SKULD", "Si se dibuja el diagrama de casos de uso sin el límite del sistema, ¿qué pasará?",
            listOf("No se distinguirá qué está dentro del alcance del sistema", "El diagrama será más formal", "Los actores se volverán casos de uso"), 0,
            "El límite marca el alcance.", "Sin el límite no queda claro qué funcionalidades le corresponden al sistema y cuáles no."),

        v("vv_uml_4_u", "tema_uml_4", "URD", "Un diagrama de actividades tiene un rombo de decisión con dos salidas sin guardas. ¿Qué está mal?",
            listOf("Las decisiones deben indicar la condición de cada salida", "Un rombo solo puede tener una salida", "Las decisiones van en diagramas de clases"), 0,
            "¿Cómo se elige el camino?", "Cada flujo que sale de una decisión necesita su guarda, por ejemplo [aprobado] y [rechazado]."),
        v("vv_uml_4_v", "tema_uml_4", "VERDANDI", "Una barra gruesa divide un flujo en «Validar pago» y «Preparar envío», que ocurren al mismo tiempo. ¿Qué es?",
            listOf("Un nodo final", "Un fork (bifurcación)", "Un nodo de decisión"), 1,
            "Las actividades ocurren en paralelo.", "El fork divide el flujo en actividades concurrentes; el join las vuelve a unir."),
        v("vv_uml_4_s", "tema_uml_4", "SKULD", "Si un fork divide el flujo en dos ramas y nunca se usa un join antes del nodo final, ¿qué puede pasar?",
            listOf("El proceso podría terminar sin esperar a la otra rama", "Las ramas se ejecutarán una tras otra", "El diagrama se vuelve de secuencia"), 0,
            "El join sincroniza las ramas.", "Sin join, nada obliga a esperar a que ambas ramas terminen antes de cerrar el proceso."),

        // ---------------- Mundo Casos de Uso ----------------
        v("vv_cu_1_u", "tema_cu_1", "URD", "Un caso de uso de una biblioteca tiene como actor «Botón de préstamo». ¿Qué está mal?",
            listOf("Un elemento de la interfaz no es un actor", "Los actores deben ir en plural", "Le falta la precondición"), 0,
            "Un actor tiene un rol fuera del sistema.", "Los actores son roles externos (Bibliotecario, Alumno); un botón es parte de la interfaz del propio sistema."),
        v("vv_cu_1_v", "tema_cu_1", "VERDANDI", "«El reloj del sistema dispara el envío de recordatorios cada noche». ¿Qué papel tiene el reloj?",
            listOf("Caso de uso incluido", "Actor que inicia el caso de uso", "Precondición"), 1,
            "Algo externo inicia la interacción.", "Un temporizador o reloj puede ser el actor que inicia un caso de uso programado."),
        v("vv_cu_1_s", "tema_cu_1", "SKULD", "Si un caso de uso no tiene ningún actor asociado, ¿qué pasará al especificarlo?",
            listOf("No quedará claro quién lo inicia ni quién obtiene valor", "Será más fácil de probar", "Se convertirá en un requisito no funcional"), 0,
            "Todo caso de uso aporta valor a alguien.", "Sin actor no se sabe quién inicia el caso ni para quién es el resultado."),

        v("vv_cu_2_u", "tema_cu_2", "URD", "«Imprimir recibo», que solo ocurre si el usuario lo pide, se unió a «Pagar» con «include». ¿Qué está mal?",
            listOf("Debería ser extend, porque es opcional", "Debería ser una generalización", "No debería tener relación"), 0,
            "¿Ocurre siempre o solo a veces?", "include se usa para comportamiento obligatorio; un comportamiento opcional o condicional es extend."),
        v("vv_cu_2_v", "tema_cu_2", "VERDANDI", "«Iniciar sesión» se reutiliza en «Comprar», «Ver historial» y «Pagar», y siempre se ejecuta. ¿Qué relación es?",
            listOf("extend", "include", "Asociación"), 1,
            "Siempre se ejecuta y se reutiliza.", "Un comportamiento común y obligatorio que comparten varios casos se modela con include."),
        v("vv_cu_2_s", "tema_cu_2", "SKULD", "Si se copia el mismo flujo de «Validar tarjeta» en cinco casos de uso en lugar de usar include, ¿qué pasará?",
            listOf("Cada cambio tendrá que hacerse en cinco lugares", "El sistema será más rápido", "Los actores desaparecerán"), 0,
            "Piensa en el mantenimiento.", "Duplicar el flujo obliga a actualizar todas las copias; include lo deja en un solo lugar."),

        v("vv_cu_3_u", "tema_cu_3", "URD", "El flujo normal de «Registrar alumno» dice: «3. El sistema muestra un error si el correo es inválido». ¿Qué está mal?",
            listOf("Los errores van en flujos alternativos o de excepción", "El flujo normal no lleva números", "El sistema nunca muestra mensajes"), 0,
            "El flujo normal es el camino sin problemas.", "El flujo normal describe el camino exitoso; los casos de error se separan en flujos alternativos o de excepción."),
        v("vv_cu_3_v", "tema_cu_3", "VERDANDI", "«El alumno debe haber iniciado sesión» aparece antes del flujo normal. ¿Qué parte de la especificación es?",
            listOf("Postcondición", "Precondición", "Flujo alternativo"), 1,
            "Se cumple antes de empezar.", "Una precondición es lo que debe ser verdad antes de que el caso de uso inicie."),
        v("vv_cu_3_s", "tema_cu_3", "SKULD", "Si una especificación no tiene postcondiciones, ¿qué pasará al probar el caso de uso?",
            listOf("No habrá un criterio claro de cuándo terminó bien", "Las pruebas serán automáticas", "Los actores no podrán usarlo"), 0,
            "¿Cómo sabes que el caso salió bien?", "Las postcondiciones describen el estado final esperado; sin ellas no hay criterio para verificar el resultado."),

        v("vv_cu_4_u", "tema_cu_4", "URD", "Un paso dice: «El alumno da clic en el botón azul de la esquina superior derecha». ¿Qué está mal?",
            listOf("Describe detalles de la interfaz en lugar de la intención", "Le falta el número de paso", "Debería decir «el usuario»"), 0,
            "Los casos de uso no diseñan pantallas.", "Un caso de uso describe qué hace el actor («solicita guardar»), no cómo se ve la interfaz."),
        v("vv_cu_4_v", "tema_cu_4", "VERDANDI", "Un caso de uso dice: «El sistema ejecuta un INSERT en la tabla alumnos». ¿Qué error común es?",
            listOf("Incluir detalles técnicos de implementación", "Omitir al actor", "Usar include de más"), 0,
            "¿Es lo que ve el actor?", "Mencionar sentencias SQL o tablas mete decisiones de implementación en un documento de requisitos."),
        v("vv_cu_4_s", "tema_cu_4", "SKULD", "Si los casos de uso describen pantallas detalladas y luego cambia el diseño, ¿qué pasará?",
            listOf("Habrá que reescribir los casos de uso aunque los requisitos no cambien", "El diseño se ajustará solo", "Los casos de uso seguirán correctos"), 0,
            "Mezclar requisitos e interfaz los acopla.", "Si los requisitos dependen de la interfaz, cualquier cambio visual obliga a reescribirlos."),

        // ---------------- Mundo Ciclo de Vida ----------------
        v("vv_cv_1_u", "tema_cv_1", "URD", "Un requisito dice: «El sistema debe ser rápido». ¿Qué está mal?",
            listOf("No es medible ni verificable", "Es demasiado técnico", "Es un requisito funcional"), 0,
            "¿Cómo lo probarías?", "Un buen requisito es verificable: por ejemplo, «responder en menos de 2 segundos al 95 % de las consultas»."),
        v("vv_cv_1_v", "tema_cv_1", "VERDANDI", "«La aplicación debe funcionar sin conexión a internet». ¿Qué tipo de requisito es?",
            listOf("Funcional", "No funcional", "Regla de negocio"), 1,
            "Describe una cualidad, no una función.", "Es un requisito no funcional: restringe cómo opera el sistema, no qué función ofrece."),
        v("vv_cv_1_s", "tema_cv_1", "SKULD", "Si no se validan los requisitos con el cliente antes de diseñar, ¿qué es más probable?",
            listOf("Construir algo que el cliente no necesita y rehacerlo después", "Terminar antes de tiempo", "Que no haga falta probar"), 0,
            "Validar es confirmar con el cliente.", "Los errores de requisitos que llegan al diseño y al código son los más caros de corregir."),

        v("vv_cv_2_u", "tema_cv_2", "URD", "En el diseño, la pantalla de registro consulta directamente la base de datos sin pasar por otras capas. ¿Qué está mal?",
            listOf("Rompe la separación por capas", "Las pantallas no deben mostrar datos", "La base de datos debe estar en la nube"), 0,
            "Piensa en la arquitectura por capas.", "La presentación debe pasar por la lógica de negocio y el repositorio; así cada capa cambia sin romper las demás."),
        v("vv_cv_2_v", "tema_cv_2", "VERDANDI", "Un documento muestra los componentes del sistema, sus responsabilidades y cómo se comunican. ¿Qué es?",
            listOf("Una especificación de requisitos", "Un diseño de arquitectura", "Un plan de pruebas"), 1,
            "Habla de cómo está construido el sistema.", "El diseño de arquitectura define los componentes y sus relaciones antes de programar."),
        v("vv_cv_2_s", "tema_cv_2", "SKULD", "Si dos módulos dependen fuertemente uno del otro (alto acoplamiento), ¿qué pasará al modificar uno?",
            listOf("Probablemente habrá que modificar también el otro", "Ninguno se verá afectado", "El sistema será más fácil de probar"), 0,
            "El acoplamiento une los cambios.", "Con alto acoplamiento los cambios se propagan; por eso se busca bajo acoplamiento y alta cohesión."),

        v("vv_cv_3_u", "tema_cv_3", "URD", "Un método llamado «x2» calcula el promedio del alumno. ¿Qué está mal?",
            listOf("El nombre no comunica lo que hace", "Los métodos no pueden calcular promedios", "Debería ser estático"), 0,
            "El código también se lee.", "Los nombres deben expresar la intención, por ejemplo calcularPromedio(); así el código se entiende y se mantiene."),
        v("vv_cv_3_v", "tema_cv_3", "VERDANDI", "Antes de unir sus cambios, un compañero revisa el código del otro buscando defectos. ¿Qué práctica es?",
            listOf("Revisión de código", "Prueba de aceptación", "Refactorización"), 0,
            "Otra persona revisa antes de integrar.", "La revisión de código detecta defectos y comparte conocimiento antes de integrar los cambios."),
        v("vv_cv_3_s", "tema_cv_3", "SKULD", "Si el equipo programa sin control de versiones, ¿qué es más probable que pase?",
            listOf("Perder cambios o sobrescribir el trabajo de otros", "Que el código compile más rápido", "Que no haya defectos"), 0,
            "¿Cómo recuperas una versión anterior?", "Sin control de versiones no hay historial ni forma segura de integrar el trabajo de varias personas."),

        v("vv_cv_4_u", "tema_cv_4", "URD", "Un plan de pruebas solo incluye casos con datos válidos. ¿Qué está mal?",
            listOf("Faltan casos con datos inválidos y límites", "Las pruebas no necesitan plan", "Debería probar solo la interfaz"), 0,
            "Los defectos se esconden en los bordes.", "Hay que probar datos inválidos y valores límite, no solo el camino feliz."),
        v("vv_cv_4_v", "tema_cv_4", "VERDANDI", "Se modifica el sistema para que funcione con una nueva versión del sistema operativo, sin cambiar lo que hace. ¿Qué tipo de mantenimiento es?",
            listOf("Correctivo", "Adaptativo", "Perfectivo"), 1,
            "El cambio viene del entorno.", "El mantenimiento adaptativo ajusta el software a cambios en su entorno."),
        v("vv_cv_4_s", "tema_cv_4", "SKULD", "Si después de corregir un defecto no se ejecutan las pruebas de regresión, ¿qué puede pasar?",
            listOf("La corrección puede romper algo que antes funcionaba sin que nadie lo note", "El defecto se corregirá dos veces", "Las pruebas se ejecutarán solas"), 0,
            "Regresión es volver atrás.", "Las pruebas de regresión confirman que un cambio no dañó funcionalidades que ya servían."),

        // ---------------- Mundo Modelos Tradicionales ----------------
        v("vv_mt_1_u", "tema_mt_1", "URD", "Un equipo usa cascada y empieza a programar mientras todavía se están definiendo los requisitos. ¿Qué está mal?",
            listOf("En cascada cada fase termina antes de iniciar la siguiente", "En cascada no se programa", "En cascada no hay requisitos"), 0,
            "La cascada es secuencial.", "El modelo en cascada es estrictamente secuencial: el diseño y el código empiezan cuando los requisitos están cerrados."),
        v("vv_mt_1_v", "tema_mt_1", "VERDANDI", "Un sistema de nómina tiene requisitos estables, bien conocidos y fijados por ley. ¿Qué modelo encaja mejor?",
            listOf("Cascada", "Espiral", "Prototipos desechables"), 0,
            "Los requisitos casi no cambiarán.", "La cascada funciona bien cuando los requisitos son estables y se conocen desde el inicio."),
        v("vv_mt_1_s", "tema_mt_1", "SKULD", "En cascada, el cliente descubre en la entrega final que un requisito estaba mal entendido. ¿Qué pasará?",
            listOf("Corregirlo será costoso porque hay que regresar a fases ya cerradas", "Se corrige en minutos sin costo", "El requisito se ignora"), 0,
            "El cliente ve el producto hasta el final.", "En cascada el cliente valida tarde, así que los errores de requisitos se descubren cuando son más caros."),

        v("vv_mt_2_u", "tema_mt_2", "URD", "Un equipo dice usar el modelo en espiral, pero nunca analiza riesgos en sus iteraciones. ¿Qué está mal?",
            listOf("El análisis de riesgos es el centro de cada vuelta de la espiral", "La espiral no tiene iteraciones", "La espiral prohíbe los prototipos"), 0,
            "¿Qué distingue a la espiral?", "Cada ciclo de la espiral incluye identificar y reducir riesgos; sin eso no es espiral."),
        v("vv_mt_2_v", "tema_mt_2", "VERDANDI", "Un proyecto grande y con tecnología nueva, donde un fallo sería muy costoso, hace prototipos para reducir riesgos en cada ciclo. ¿Qué modelo es?",
            listOf("Cascada", "Espiral", "En V"), 1,
            "Riesgo alto y ciclos.", "El modelo en espiral está pensado para proyectos grandes y riesgosos, con evaluación de riesgos en cada vuelta."),
        v("vv_mt_2_s", "tema_mt_2", "SKULD", "Si se usa el modelo en espiral en un proyecto pequeño y de bajo riesgo, ¿qué es probable?",
            listOf("El análisis de riesgos agregará costo sin aportar mucho", "El proyecto fallará siempre", "Se volverá cascada automáticamente"), 0,
            "La espiral tiene un costo de gestión.", "En proyectos pequeños, el esfuerzo de analizar riesgos en cada ciclo puede superar el beneficio."),

        v("vv_mt_3_u", "tema_mt_3", "URD", "En un modelo en V, las pruebas de aceptación se planean hasta después de programar. ¿Qué está mal?",
            listOf("Cada prueba se planea junto con su fase de especificación", "En V no hay pruebas de aceptación", "Las pruebas se planean al final en todos los modelos"), 0,
            "Cada lado de la V tiene su pareja.", "En el modelo en V, las pruebas de aceptación se planean al definir los requisitos."),
        v("vv_mt_3_v", "tema_mt_3", "VERDANDI", "Las pruebas unitarias se diseñan a partir del diseño detallado de cada módulo. ¿Qué modelo describe esta correspondencia?",
            listOf("Modelo en V", "Modelo incremental", "Espiral"), 0,
            "Fases y pruebas emparejadas.", "El modelo en V empareja cada fase de desarrollo con un nivel de prueba."),
        v("vv_mt_3_s", "tema_mt_3", "SKULD", "Si en el modelo en V los requisitos cambian a mitad del desarrollo, ¿qué pasará?",
            listOf("Habrá que actualizar también los planes de prueba ligados a ellos", "Las pruebas seguirán igual", "El modelo se adapta sin costo"), 0,
            "Los requisitos y las pruebas están ligados.", "Como cada prueba depende de su especificación, un cambio de requisitos obliga a rehacer sus pruebas."),

        v("vv_mt_4_u", "tema_mt_4", "URD", "Un equipo «incremental» entrega todo el sistema al final, en una sola entrega. ¿Qué está mal?",
            listOf("El modelo incremental entrega funcionalidad por partes", "El incremental no tiene entregas", "El incremental solo sirve para diseño"), 0,
            "Incremento = parte funcional.", "En el modelo incremental se entregan incrementos funcionales a lo largo del proyecto."),
        v("vv_mt_4_v", "tema_mt_4", "VERDANDI", "Una tienda en línea publica primero el catálogo, luego el carrito y después los pagos. ¿Qué modelo sigue?",
            listOf("Incremental", "Cascada", "En V"), 0,
            "Se entrega en partes que ya funcionan.", "Cada entrega agrega funcionalidad utilizable: es un desarrollo incremental."),
        v("vv_mt_4_s", "tema_mt_4", "SKULD", "Si el primer incremento entrega lo más importante para el cliente, ¿qué es probable?",
            listOf("El cliente obtiene valor pronto y da retroalimentación temprana", "El proyecto tardará más", "Ya no habrá que probar"), 0,
            "Valor temprano.", "Priorizar lo más valioso en el primer incremento da beneficios pronto y permite ajustar el resto.")
    )

    fun insertar(db: SupportSQLiteDatabase) {
        val converters = Converters()
        visiones.forEach { vision ->
            db.insert("visiones_volva", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", vision.id)
                put("temaId", vision.temaId)
                put("norna", vision.norna)
                put("caso", vision.caso)
                put("opciones", converters.fromStringList(vision.opciones))
                put("indiceCorrecta", vision.indiceCorrecta)
                put("pista", vision.pista)
                put("explicacion", vision.explicacion)
            })
        }
    }
}
