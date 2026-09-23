package com.aesir.odin.ui.temaintro

import com.aesir.odin.data.local.dao.TemaDao
import com.aesir.odin.data.repository.RoadmapRepositoryImpl
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.RegistrarIntroduccionVistaUseCase

object TemaIntroDependencyProvider {

    lateinit var temaDao: TemaDao

    private val repository by lazy { RoadmapRepositoryImpl(temaDao) }

    val obtenerIntroduccionTemaUseCase by lazy { ObtenerIntroduccionTemaUseCase(repository) }
    val registrarIntroduccionVistaUseCase by lazy { RegistrarIntroduccionVistaUseCase(repository) }
}