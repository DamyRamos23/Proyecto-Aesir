package com.aesir.odin.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aesir.odin.data.local.converter.Converters
import com.aesir.odin.data.local.entity.ConceptoEnigmaEntity
import com.aesir.odin.data.local.entity.PreguntaEnigmaEntity
import java.text.Normalizer

/**
 * Contenido del CU-13. Los conceptos son los mismos del banco del crucigrama
 * (nombre y descripción como explicación). Las preguntas se arman a partir de
 * esos datos, así la respuesta del gigante siempre es correcta:
 * - Directas: por mundo, por tema, por grupo de conceptos y por el nombre.
 * - Enigmas: un acertijo por tema y algunos sobre el nombre o el grupo.
 * Dos conceptos con el mismo nombre (p. ej. «Actor» en UML y en Casos de Uso)
 * responden igual: una pregunta dice «sí» para todos los que comparten nombre.
 */
object DatosEnigmas {

    private data class TemaInfo(val mundo: String, val nombre: String, val enigma: String)

    private val temas = linkedMapOf(
        "tema_uml_1" to TemaInfo("UML", "Diagramas de Clases", "«Dibujo cajas con nombre, atributos y métodos, pero nunca el paso del tiempo». ¿Vienes de mi taller?"),
        "tema_uml_2" to TemaInfo("UML", "Diagramas de Secuencia", "«Mis líneas bajan como el tiempo y por ellas viajan mensajes». ¿Naciste en mí?"),
        "tema_uml_3" to TemaInfo("UML", "Diagramas de Casos de Uso", "«Pinto óvalos y figuras que miran al sistema desde afuera». ¿Eres de mi aldea?"),
        "tema_uml_4" to TemaInfo("UML", "Diagramas de Actividades", "«Tengo rombos que deciden y barras que reparten caminos paralelos». ¿Caminas por mí?"),
        "tema_cu_1" to TemaInfo("Casos de Uso", "Elementos de un Caso de Uso", "«Soy quien actúa, el muro que delimita y la meta que da valor». ¿Eres una de mis piezas?"),
        "tema_cu_2" to TemaInfo("Casos de Uso", "Relaciones (Include, Extend)", "«Uno siempre incluye, otro a veces extiende». ¿Eres un lazo entre casos?"),
        "tema_cu_3" to TemaInfo("Casos de Uso", "Especificación de Casos de Uso", "«Antes y después, flujo y alternativa: todo lo escribo en prosa». ¿Vives en mis páginas?"),
        "tema_cu_4" to TemaInfo("Casos de Uso", "Errores Comunes en Casos de Uso", "«Hablo de botones donde no debía y parto en pedazos lo que era uno». ¿Eres uno de mis tropiezos?"),
        "tema_cv_1" to TemaInfo("Ciclo de Vida", "Análisis de Requisitos", "«Pregunto qué debe hacer el sistema antes de saber cómo». ¿Eres de mi primera fase?"),
        "tema_cv_2" to TemaInfo("Ciclo de Vida", "Diseño del Sistema", "«Trazo planos y arquitecturas antes de escribir una línea». ¿Saliste de mi mesa?"),
        "tema_cv_3" to TemaInfo("Ciclo de Vida", "Implementación y Codificación", "«Convierto planos en código que corre». ¿Te forjé yo?"),
        "tema_cv_4" to TemaInfo("Ciclo de Vida", "Pruebas y Mantenimiento", "«Busco fallas y cuido al sistema cuando ya está vivo». ¿Eres de mi guardia?"),
        "tema_mt_1" to TemaInfo("Modelos Tradicionales", "Modelo en Cascada", "«Bajo como el agua, una fase tras otra, sin volver atrás». ¿Eres de mi corriente?"),
        "tema_mt_2" to TemaInfo("Modelos Tradicionales", "Modelo en Espiral", "«Doy vueltas y en cada una mido el riesgo». ¿Giras conmigo?"),
        "tema_mt_3" to TemaInfo("Modelos Tradicionales", "Modelo en V", "«Bajo diseñando y subo probando, cada lado frente al otro». ¿Eres de mi forma?"),
        "tema_mt_4" to TemaInfo("Modelos Tradicionales", "Modelo Incremental", "«Entrego por partes y cada una suma a la anterior». ¿Eres uno de mis pedazos?")
    )

    /** Grupos de conceptos por significado: (directa, enigma, nombres). */
    private val grupos = listOf(
        Triple(
            "¿Es un tipo de prueba o de verificación?",
            "«Busco el error antes de que lo encuentre el usuario». ¿Eres de mi oficio?",
            setOf("Prueba", "Unitaria", "Integración", "Regresión", "Verificación", "Validación", "Aceptación", "Unidad")
        ),
        Triple(
            "¿Es una relación entre clases o entre casos de uso?",
            "«No existo solo: siempre ato a dos». ¿Eres un lazo?",
            setOf("Herencia", "Composición", "Include", "Extend")
        ),
        Triple(
            "¿Es una persona o un rol que participa?",
            "«Tengo voz y deseos, pero no vivo dentro del sistema». ¿Hablo de ti?",
            setOf("Actor", "Stakeholder")
        )
    )

    val conceptos: List<ConceptoEnigmaEntity> = DatosCrucigrama.banco.flatMap { (temaId, lista) ->
        lista.mapIndexed { i, (nombre, descripcion) -> ConceptoEnigmaEntity("ce_${temaId}_${i + 1}", temaId, nombre, descripcion) }
    }

    private fun sinAcentos(texto: String) =
        Normalizer.normalize(texto, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

    /** Ids de todos los conceptos cuyo nombre cumple la condición (los homónimos responden igual). */
    private fun idsCon(condicion: (ConceptoEnigmaEntity) -> Boolean): List<String> {
        val nombres = conceptos.filter(condicion).map { it.nombre.lowercase() }.toSet()
        return conceptos.filter { it.nombre.lowercase() in nombres }.map { it.id }
    }

    val preguntas: List<PreguntaEnigmaEntity> = buildList {
        temas.values.map { it.mundo }.distinct().forEachIndexed { i, mundo ->
            val temasDelMundo = temas.filterValues { it.mundo == mundo }.keys
            add(PreguntaEnigmaEntity("pe_mundo_${i + 1}", "¿Se estudia en el mundo «$mundo»?", "DIRECTA", idsCon { it.temaId in temasDelMundo }))
        }
        temas.forEach { (temaId, info) ->
            add(PreguntaEnigmaEntity("pe_tema_$temaId", "¿Se estudia en «${info.nombre}»?", "DIRECTA", idsCon { it.temaId == temaId }))
            add(PreguntaEnigmaEntity("pe_enigma_$temaId", info.enigma, "ENIGMA", idsCon { it.temaId == temaId }))
        }
        grupos.forEachIndexed { i, (directa, enigma, nombres) ->
            add(PreguntaEnigmaEntity("pe_grupo_${i + 1}", directa, "DIRECTA", idsCon { it.nombre in nombres }))
            add(PreguntaEnigmaEntity("pe_grupo_enigma_${i + 1}", enigma, "ENIGMA", idsCon { it.nombre in nombres }))
        }
        add(PreguntaEnigmaEntity("pe_nombre_largo", "¿Su nombre tiene más de 8 letras?", "DIRECTA", idsCon { it.nombre.length > 8 }))
        add(PreguntaEnigmaEntity("pe_nombre_am", "¿Su nombre empieza con una letra de la A a la M?", "DIRECTA", idsCon { sinAcentos(it.nombre).first().uppercaseChar() in 'A'..'M' }))
        add(PreguntaEnigmaEntity("pe_nombre_vocal", "¿Su nombre termina en vocal?", "DIRECTA", idsCon { sinAcentos(it.nombre).last().lowercaseChar() in "aeiou" }))
        add(PreguntaEnigmaEntity("pe_nombre_corto", "«Mi nombre es breve: no llega a siete letras». ¿El tuyo tampoco?", "ENIGMA", idsCon { it.nombre.length < 7 }))
        add(PreguntaEnigmaEntity("pe_nombre_tilde", "«Una tilde marca dónde suena fuerte mi nombre». ¿El tuyo también la lleva?", "ENIGMA", idsCon { it.nombre != sinAcentos(it.nombre) }))
    }

    /** Contenido fijo: se reescribe en cada apertura, como el banco de preguntas flash. */
    fun insertar(db: SupportSQLiteDatabase) {
        val converters = Converters()
        conceptos.forEach { c ->
            db.insert("conceptos_enigma", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", c.id); put("temaId", c.temaId); put("nombre", c.nombre); put("explicacion", c.explicacion)
            })
        }
        preguntas.forEach { p ->
            db.insert("preguntas_enigma", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                put("id", p.id); put("texto", p.texto); put("tipo", p.tipo); put("conceptosSi", converters.fromStringList(p.conceptosSi))
            })
        }
    }
}
