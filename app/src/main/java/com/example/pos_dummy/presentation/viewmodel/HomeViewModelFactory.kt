package com.example.pos_dummy.presentation.viewmodel

import android.content.Context
import com.example.sunmi.data.repository.AndroidContactlessDiagnosticsRepository
import com.example.sunmi.data.repository.FakeSunmiPrinterRepository
import com.example.sunmi.data.repository.SunmiContactlessPaymentRepository
import com.example.sunmi.domain.usecase.GetPrinterInfoUseCase
import com.example.sunmi.domain.usecase.contactless.CancelContactlessPaymentUseCase
import com.example.sunmi.domain.usecase.contactless.ClearContactlessStateUseCase
import com.example.sunmi.domain.usecase.contactless.DisposeContactlessPaymentUseCase
import com.example.sunmi.domain.usecase.contactless.GetContactlessCapabilitiesUseCase
import com.example.sunmi.domain.usecase.contactless.GetContactlessPaymentAvailabilityUseCase
import com.example.sunmi.domain.usecase.contactless.ObserveContactlessPaymentEventsUseCase
import com.example.sunmi.domain.usecase.contactless.StartContactlessPaymentUseCase
import com.example.sunmi.domain.usecase.contactless.StartContactlessProbeUseCase

object HomeViewModelFactory {
    fun create(context: Context): HomeViewModel {
        val appContext = context.applicationContext
        val diagnosticsRepository = AndroidContactlessDiagnosticsRepository(appContext)
        val paymentRepository = SunmiContactlessPaymentRepository(
            context = appContext,
            diagnosticsRepository = diagnosticsRepository,
        )
        val printerRepository = FakeSunmiPrinterRepository()

        return HomeViewModel(
            getPrinterInfoUseCase = GetPrinterInfoUseCase(printerRepository),
            getContactlessCapabilitiesUseCase = GetContactlessCapabilitiesUseCase(diagnosticsRepository),
            getContactlessPaymentAvailabilityUseCase = GetContactlessPaymentAvailabilityUseCase(paymentRepository),
            startContactlessProbeUseCase = StartContactlessProbeUseCase(),
            startContactlessPaymentUseCase = StartContactlessPaymentUseCase(paymentRepository),
            cancelContactlessPaymentUseCase = CancelContactlessPaymentUseCase(paymentRepository),
            clearContactlessStateUseCase = ClearContactlessStateUseCase(),
            observeContactlessPaymentEventsUseCase = ObserveContactlessPaymentEventsUseCase(paymentRepository),
            disposeContactlessPaymentUseCase = DisposeContactlessPaymentUseCase(paymentRepository),
        )
    }
}
