package com.example.pos_dummy.presentation.screen

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.pos_dummy.presentation.viewmodel.HomeUiState
import com.example.pos_dummy.presentation.viewmodel.HomeViewModelFactory
import com.example.pos_dummy.ui.theme.PosdummyTheme
import com.example.sunmi.domain.model.ContactlessPaymentStage
import com.example.sunmi.domain.model.ContactlessProbeStage

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = remember { HomeViewModelFactory.create(context) }
    val uiState = viewModel.uiState

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshContactlessCapabilities()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.dispose()
        }
    }

    ContactlessReaderEffect(
        isListening = uiState.contactlessUiState.isListening,
        onTagDetected = viewModel::onContactlessTagDetected,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    )
                )
            )
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PosHeader(
            terminalModel = uiState.paymentUiState.availability.terminalModel,
            paymentStage = uiState.paymentUiState.result.stage,
            statusMessage = uiState.lastMessage,
        )
        PosSaleSummary(
            amount = "${uiState.paymentUiState.request.currencyCode} ${uiState.paymentUiState.request.amountLabel}",
            paymentStage = uiState.paymentUiState.result.stage,
            readerReady = uiState.paymentUiState.availability.isVendorSdkConfigured,
        )
        PosCheckoutFlow(
            paymentStage = uiState.paymentUiState.result.stage,
            probeStage = uiState.contactlessUiState.result.stage,
        )
        PosTapToPayPanel(
            uiState = uiState,
            onStartPayment = viewModel::startContactlessPayment,
            onCancelPayment = viewModel::cancelContactlessPayment,
        )
        PosDeviceHealth(
            uiState = uiState,
            onRefresh = viewModel::refreshContactlessCapabilities,
        )
        PosAdvancedDiagnostics(
            uiState = uiState,
            onStartProbe = viewModel::startContactlessProbe,
            onClearProbe = viewModel::clearContactlessState,
        )
    }
}

@Composable
private fun PosHeader(
    terminalModel: String,
    paymentStage: ContactlessPaymentStage,
    statusMessage: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Metronorte",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Caja 03  •  Contactless checkout",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatusPill(
                text = paymentStage.posLabel(),
                containerColor = stageColor(paymentStage).copy(alpha = 0.14f),
                contentColor = stageColor(paymentStage),
            )
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = terminalModel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Tap, card, wallet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        tonalElevation = 2.dp,
    ) {
        Text(
            text = statusMessage,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PosSaleSummary(
    amount: String,
    paymentStage: ContactlessPaymentStage,
    readerReady: Boolean,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Venta actual",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Pedido #1048",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                StatusPill(
                    text = if (readerReady) "Reader online" else "Reader setup needed",
                    containerColor = if (readerReady) {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    },
                    contentColor = if (readerReady) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Items 3",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = paymentStage.posSupportLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PosCheckoutFlow(
    paymentStage: ContactlessPaymentStage,
    probeStage: ContactlessProbeStage,
) {
    CardBlock(title = "Flujo de cobro") {
        CheckoutStep(
            index = "01",
            title = "Preparar terminal",
            description = "Validar servicio de pagos y estado del equipo.",
            isActive = paymentStage == ContactlessPaymentStage.IDLE ||
                paymentStage == ContactlessPaymentStage.UNAVAILABLE,
            isComplete = paymentStage != ContactlessPaymentStage.IDLE &&
                paymentStage != ContactlessPaymentStage.UNAVAILABLE,
        )
        CheckoutStep(
            index = "02",
            title = "Esperar tarjeta o wallet",
            description = "Acercar tarjeta, reloj o telefono al lector.",
            isActive = paymentStage == ContactlessPaymentStage.INITIALIZING ||
                paymentStage == ContactlessPaymentStage.WAITING_FOR_CARD,
            isComplete = paymentStage == ContactlessPaymentStage.CARD_DETECTED ||
                paymentStage == ContactlessPaymentStage.APPROVED,
        )
        CheckoutStep(
            index = "03",
            title = "Capturar lectura NFC",
            description = if (probeStage == ContactlessProbeStage.DETECTED) {
                "El lector nativo detecto una senal ISO-DEP o NFC cercana."
            } else {
                "La prueba nativa queda disponible como verificacion avanzada."
            },
            isActive = paymentStage == ContactlessPaymentStage.CARD_DETECTED,
            isComplete = probeStage == ContactlessProbeStage.DETECTED ||
                paymentStage == ContactlessPaymentStage.CARD_DETECTED ||
                paymentStage == ContactlessPaymentStage.APPROVED,
        )
        CheckoutStep(
            index = "04",
            title = "Resolver pago",
            description = "Aprobar, declinar o cancelar la sesion actual.",
            isActive = paymentStage == ContactlessPaymentStage.PROCESSING,
            isComplete = paymentStage == ContactlessPaymentStage.APPROVED ||
                paymentStage == ContactlessPaymentStage.DECLINED ||
                paymentStage == ContactlessPaymentStage.CANCELED,
        )
    }
}

@Composable
private fun PosTapToPayPanel(
    uiState: HomeUiState,
    onStartPayment: () -> Unit,
    onCancelPayment: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Aceptar pago",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = uiState.paymentUiState.result.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusPill(
                    text = uiState.paymentUiState.result.stage.posLabel(),
                    containerColor = stageColor(uiState.paymentUiState.result.stage).copy(alpha = 0.14f),
                    contentColor = stageColor(uiState.paymentUiState.result.stage),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
                            )
                        )
                    )
                    .border(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.54f)
                            .aspectRatio(1.1f),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TAP",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                Text(
                                    text = "CARD / PHONE / WATCH",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f),
                                )
                            }
                        }
                    }

                    Text(
                        text = uiState.paymentUiState.result.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )

                    if (uiState.paymentUiState.result.reference.isNotBlank())                                                                                                                                                                {
                        Text(
                            text = uiState.paymentUiState.result.reference,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onStartPayment,
                    enabled = !uiState.paymentUiState.isInProgress,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(if (uiState.paymentUiState.isInProgress) "Esperando tap" else "Cobrar ahora")
                }
                OutlinedButton(
                    onClick = onCancelPayment,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancelar venta")
                }
            }

            if (uiState.paymentUiState.result.stage == ContactlessPaymentStage.UNAVAILABLE) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = "Antes de cobrar, confirma que NFC este habilitado y que el servicio SUNMI de pagos este instalado.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun PosDeviceHealth(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
) {
    CardBlock(
        title = "Salud del terminal",
        action = {
            TextButton(onClick = onRefresh) {
                Text("Actualizar")
            }
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                modifier = Modifier.weight(1f),
                label = "NFC",
                value = boolLabel(uiState.contactlessUiState.capabilities.hasNfcFeature),
                accent = if (uiState.contactlessUiState.capabilities.hasNfcFeature) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            MetricTile(
                modifier = Modifier.weight(1f),
                label = "Radio",
                value = boolLabel(uiState.contactlessUiState.capabilities.isNfcEnabled),
                accent = if (uiState.contactlessUiState.capabilities.isNfcEnabled) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            MetricTile(
                modifier = Modifier.weight(1f),
                label = "SDK",
                value = boolLabel(uiState.paymentUiState.availability.isVendorSdkConfigured),
                accent = if (uiState.paymentUiState.availability.isVendorSdkConfigured) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }

        KeyValueRow("Modelo", uiState.paymentUiState.availability.terminalModel)
        Text(
            text = uiState.paymentUiState.availability.statusSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PosAdvancedDiagnostics(
    uiState: HomeUiState,
    onStartProbe: () -> Unit,
    onClearProbe: () -> Unit,
) {
    CardBlock(title = "Diagnostico avanzado") {
        Text(
            text = "Usa el lector NFC nativo solo cuando necesites comprobar el hardware fuera del flujo de cobro principal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onStartProbe,
                enabled = !uiState.contactlessUiState.isListening,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (uiState.contactlessUiState.isListening) "Escuchando..." else "Iniciar prueba")
            }
            OutlinedButton(
                onClick = onClearProbe,
                modifier = Modifier.weight(1f),
            ) {
                Text("Limpiar")
            }
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = uiState.contactlessUiState.result.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = uiState.contactlessUiState.result.message,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (uiState.contactlessUiState.result.stage == ContactlessProbeStage.DETECTED) {
                    HorizontalDivider()
                    KeyValueRow("UID", uiState.contactlessUiState.result.uid.ifBlank { "No disponible" })
                    KeyValueRow("Tecnologias", uiState.contactlessUiState.result.technologies.ifBlank { "Desconocidas" })
                    KeyValueRow("ISO-DEP", boolLabel(uiState.contactlessUiState.result.supportsIsoDep))
                }
            }
        }
    }
}

@Composable
private fun ContactlessReaderEffect(
    isListening: Boolean,
    onTagDetected: (uid: String, technologies: String, supportsIsoDep: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val nfcAdapter = remember(context) { NfcAdapter.getDefaultAdapter(context) }

    DisposableEffect(activity, nfcAdapter, isListening) {
        if (activity == null || nfcAdapter == null || !isListening) {
            onDispose { }
        } else {
            nfcAdapter.enableReaderMode(
                activity,
                { tag ->
                    val uid = tag.id?.joinToString(":") { byte -> "%02X".format(byte) }.orEmpty()
                    val technologies = tag.techList
                        .map { it.substringAfterLast('.') }
                        .joinToString(", ")
                    val supportsIsoDep = tag.techList.contains(IsoDep::class.java.name)

                    activity.runOnUiThread {
                        onTagDetected(uid, technologies, supportsIsoDep)
                    }
                },
                NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null,
            )

            onDispose {
                nfcAdapter.disableReaderMode(activity)
            }
        }
    }
}

@Composable
private fun CardBlock(
    title: String,
    action: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (action != null) {
                    action()
                }
            }
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun CheckoutStep(
    index: String,
    title: String,
    description: String,
    isActive: Boolean,
    isComplete: Boolean,
) {
    val accent = when {
        isComplete -> MaterialTheme.colorScheme.tertiary
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = MaterialTheme.shapes.large,
            color = accent.copy(alpha = 0.14f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = index,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun KeyValueRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = accent.copy(alpha = 0.10f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}

private fun boolLabel(value: Boolean): String {
    return if (value) "Si" else "No"
}

private fun ContactlessPaymentStage.posLabel(): String {
    return when (this) {
        ContactlessPaymentStage.IDLE -> "Ready"
        ContactlessPaymentStage.UNAVAILABLE -> "Blocked"
        ContactlessPaymentStage.READY -> "Online"
        ContactlessPaymentStage.INITIALIZING -> "Connecting"
        ContactlessPaymentStage.WAITING_FOR_CARD -> "Waiting for tap"
        ContactlessPaymentStage.CARD_DETECTED -> "Card detected"
        ContactlessPaymentStage.PROCESSING -> "Processing"
        ContactlessPaymentStage.APPROVED -> "Approved"
        ContactlessPaymentStage.DECLINED -> "Declined"
        ContactlessPaymentStage.CANCELED -> "Canceled"
        ContactlessPaymentStage.ERROR -> "Error"
    }
}

private fun ContactlessPaymentStage.posSupportLabel(): String {
    return when (this) {
        ContactlessPaymentStage.IDLE -> "Terminal ready for next customer"
        ContactlessPaymentStage.UNAVAILABLE -> "Resolve terminal setup before charging"
        ContactlessPaymentStage.READY -> "Payment service connected"
        ContactlessPaymentStage.INITIALIZING -> "Opening secure payment session"
        ContactlessPaymentStage.WAITING_FOR_CARD -> "Customer action required at reader"
        ContactlessPaymentStage.CARD_DETECTED -> "Tap captured by terminal"
        ContactlessPaymentStage.PROCESSING -> "Authorizing transaction"
        ContactlessPaymentStage.APPROVED -> "Customer can remove card"
        ContactlessPaymentStage.DECLINED -> "Try another card or retry"
        ContactlessPaymentStage.CANCELED -> "Session stopped by cashier"
        ContactlessPaymentStage.ERROR -> "Manual review required"
    }
}

private fun stageColor(stage: ContactlessPaymentStage): Color {
    return when (stage) {
        ContactlessPaymentStage.APPROVED -> Color(0xFF15803D)
        ContactlessPaymentStage.CARD_DETECTED,
        ContactlessPaymentStage.INITIALIZING,
        ContactlessPaymentStage.READY,
        ContactlessPaymentStage.WAITING_FOR_CARD,
        ContactlessPaymentStage.PROCESSING,
        ContactlessPaymentStage.IDLE,
        -> Color(0xFF0F766E)
        ContactlessPaymentStage.UNAVAILABLE,
        ContactlessPaymentStage.DECLINED,
        ContactlessPaymentStage.CANCELED,
        ContactlessPaymentStage.ERROR,
        -> Color(0xFFB42318)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PosdummyTheme {
        HomeScreen()
    }
}
