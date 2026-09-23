package com.aesir.odin.ui.temaintro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aesir.odin.domain.usecase.roadmap.ObtenerIntroduccionTemaUseCase
import com.aesir.odin.domain.usecase.roadmap.RegistrarIntroduccionVistaUseCase

class TemaIntroViewModelFactory(
    private val obtenerIntroduccionTemaUseCase: ObtenerIntroduccionTemaUseCase,
    private val registrarIntroduccionVistaUseCase: RegistrarIntroduccionVistaUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TemaIntroViewModel(
            obtenerIntroduccionTemaUseCase,
            registrarIntroduccionVistaUseCase
        ) as T
    }
}