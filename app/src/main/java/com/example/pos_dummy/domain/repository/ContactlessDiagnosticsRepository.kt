package com.example.pos_dummy.domain.repository

import com.example.pos_dummy.domain.model.ContactlessCapabilities

interface ContactlessDiagnosticsRepository {
    fun getCapabilities(): ContactlessCapabilities
}
