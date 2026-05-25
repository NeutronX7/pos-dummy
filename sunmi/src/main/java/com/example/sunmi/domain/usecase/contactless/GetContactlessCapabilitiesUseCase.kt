package com.example.sunmi.domain.usecase.contactless

import com.example.sunmi.domain.model.ContactlessCapabilities
import com.example.sunmi.domain.repository.ContactlessDiagnosticsRepository

class GetContactlessCapabilitiesUseCase(
    private val repository: ContactlessDiagnosticsRepository,
) {
    operator fun invoke(): ContactlessCapabilities {
        return repository.getCapabilities()
    }
}
