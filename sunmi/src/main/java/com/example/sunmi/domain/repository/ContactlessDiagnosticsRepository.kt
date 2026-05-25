package com.example.sunmi.domain.repository

import com.example.sunmi.domain.model.ContactlessCapabilities

interface ContactlessDiagnosticsRepository {
    fun getCapabilities(): ContactlessCapabilities
}
