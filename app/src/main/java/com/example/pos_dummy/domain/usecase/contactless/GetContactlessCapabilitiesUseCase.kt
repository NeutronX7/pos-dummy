package com.example.pos_dummy.domain.usecase.contactless

import com.example.pos_dummy.domain.model.ContactlessCapabilities
import com.example.pos_dummy.domain.repository.ContactlessDiagnosticsRepository

class GetContactlessCapabilitiesUseCase(
    private val repository: ContactlessDiagnosticsRepository,
) {
    operator fun invoke(): ContactlessCapabilities {
        return repository.getCapabilities()
    }
}
