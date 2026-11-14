package com.aesir.odin.data.repository

import com.aesir.odin.data.local.dao.ArtefactoDao
import com.aesir.odin.data.local.dao.EtapaForjaDao
import com.aesir.odin.data.local.dao.ForjaDao
import com.aesir.odin.data.local.dao.MundoDao
import com.aesir.odin.data.local.dao.VitrinaDao
import com.aesir.odin.data.local.entity.ArtefactoEntity
import com.aesir.odin.data.local.entity.ForjaEntity
import com.aesir.odin.data.local.entity.VitrinaArtefactoEntity
import com.aesir.odin.domain.model.Artefacto
import com.aesir.odin.domain.model.ArtefactoVitrina
import com.aesir.odin.domain.model.CalidadArtefacto
import com.aesir.odin.domain.model.EtapaMartillar
import com.aesir.odin.domain.model.EtapaTemplar
import com.aesir.odin.domain.model.Forja
import com.aesir.odin.domain.model.HuecoTemple
import com.aesir.odin.domain.model.PiezaDiseno
import com.aesir.odin.domain.model.ResultadoForja
import com.aesir.odin.domain.repository.ForjaRepository
import kotlin.random.Random

/** Implementación del repositorio de la forja de Brokkr (CU-02) sobre Room. */
class ForjaRepositoryImpl(
    private val artefactoDao: ArtefactoDao,
    private val vitrinaDao: VitrinaDao,
    private val forjaDao: ForjaDao,
    private val etapaForjaDao: EtapaForjaDao,
    private val mundoDao: MundoDao,
    private val azar: Random = Random.Default
) : ForjaRepository {

    override suspend fun obtenerArtefactos(mundoIds: List<String>): List<Artefacto> {
        if (mundoIds.isEmpty()) return emptyList()
        val nombres = nombresDeMundos()
        return artefactoDao.getPorMundos(mundoIds).map { it.aArtefacto(nombres) }
    }

    override suspend fun obtenerVitrina(): List<ArtefactoVitrina> {
        val nombres = nombresDeMundos()
        val artefactos = artefactoDao.getAll().associateBy { it.id }
        return vitrinaDao.getAll().mapNotNull { v ->
            val artefacto = artefactos[v.artefactoId] ?: return@mapNotNull null
            ArtefactoVitrina(
                artefacto = artefacto.aArtefacto(nombres),
                calidad = CalidadArtefacto.entries.firstOrNull { it.name == v.calidad } ?: CalidadArtefacto.CON_DEFECTO,
                fechaObtencion = v.fechaObtencion,
                ultimaNocheUsada = v.ultimaNocheUsada
            )
        }
    }

    override suspend fun contarForjasDelDia(inicio: Long, fin: Long): Int = forjaDao.contarEntre(inicio, fin)

    override suspend fun registrarForja(forja: Forja, fecha: Long) {
        forjaDao.insertar(
            ForjaEntity(
                id = forja.id,
                artefactoId = forja.asignacion.artefacto?.id,
                apuesta = forja.apuesta,
                estado = EN_CURSO,
                errores = 0,
                hidromielNeto = 0,
                abandonada = false,
                fecha = fecha
            )
        )
    }

    override suspend fun obtenerProblemaMartillar(mundoId: String): EtapaMartillar {
        val problema = etapaForjaDao.getProblemas(mundoId).randomOrNull(azar)
            ?: error("El mundo no tiene problemas de diseño para martillar.")
        return EtapaMartillar(
            mundoId = problema.mundoId,
            enunciado = problema.enunciado,
            piezas = problema.piezas.mapIndexed { i, texto -> PiezaDiseno(i.toString(), texto) },
            piezaCorrectaId = problema.piezaCorrectaId
        )
    }

    override suspend fun obtenerTemplado(mundoId: String): EtapaTemplar {
        val templado = etapaForjaDao.getTemplados(mundoId).randomOrNull(azar)
            ?: error("El mundo no tiene definiciones para templar.")
        return EtapaTemplar(
            mundoId = templado.mundoId,
            enunciado = templado.enunciado,
            plantilla = templado.plantilla,
            huecos = templado.respuestas.mapIndexed { i, respuesta -> HuecoTemple("h${i + 1}", respuesta) },
            opciones = templado.opciones.shuffled(azar)
        )
    }

    override suspend fun guardarEnVitrina(artefactoId: String, calidad: CalidadArtefacto, fecha: Long) {
        val actual = vitrinaDao.getById(artefactoId)
        // Un perfecto nunca baja a con defecto; el perfecto sí reemplaza al que tenía defecto.
        if (actual?.calidad == CalidadArtefacto.PERFECTO.name && calidad != CalidadArtefacto.PERFECTO) return
        vitrinaDao.upsert(VitrinaArtefactoEntity(artefactoId, calidad.name, fecha, actual?.ultimaNocheUsada))
    }

    override suspend fun guardarResultado(resultado: ResultadoForja) {
        val actual = forjaDao.getById(resultado.id)
        val entidad = ForjaEntity(
            id = resultado.id,
            artefactoId = resultado.artefacto?.id,
            apuesta = resultado.apuesta,
            estado = resultado.resultado.name,
            errores = resultado.errores,
            hidromielNeto = resultado.hidromielGanado - resultado.hidromielPerdido,
            abandonada = resultado.abandonada,
            fecha = actual?.fecha ?: resultado.fecha
        )
        if (actual == null) forjaDao.insertar(entidad) else forjaDao.actualizar(entidad)
    }

    override suspend fun marcarForjasEnCursoComoRotas(fecha: Long): Int = forjaDao.marcarEnCursoComoRotas()

    private suspend fun nombresDeMundos(): Map<String, String> = mundoDao.getAll().associate { it.id to it.nombre }

    private fun ArtefactoEntity.aArtefacto(nombres: Map<String, String>) = Artefacto(
        id = id,
        mundoId = mundoId,
        mundoNombre = nombres[mundoId] ?: mundoId,
        nombre = nombre,
        poderPerfecto = poderPerfecto,
        poderConDefecto = poderConDefecto
    )

    private companion object {
        const val EN_CURSO = "EN_CURSO"
    }
}
