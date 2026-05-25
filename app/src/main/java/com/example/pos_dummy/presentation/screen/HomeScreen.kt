package com.example.pos_dummy.presentation.screen

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.sunmi.data.repository.AndroidContactlessDiagnosticsRepository
import com.example.sunmi.data.repository.SunmiContactlessPaymentRepository
import com.example.sunmi.domain.model.ContactlessPaymentStage
import com.example.sunmi.domain.model.ContactlessProbeStage
import com.example.pos_dummy.presentation.viewmodel.HomeViewModel
import com.example.pos_dummy.ui.theme.PosdummyTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = remember {
        val diagnosticsRepository = AndroidContactlessDiagnosticsRepository(context)
        HomeViewModel(
            contactlessDiagnosticsRepository = diagnosticsRepository,
            contactlessPaymentRepository = SunmiContactlessPaymentRepository(
                context = context,
                diagnosticsRepository = diagnosticsRepository
            )
        )
    }
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Prueba Contactless Sunmi",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Verificacion del lector NFC P2-A11",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CardBlock(title = "Tocar Para Probar") {
            Text(
                text = "Presiona el boton de abajo y luego acerca una tarjeta contactless o un telefono al lector Sunmi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "1. Toca `Iniciar prueba contactless`\n2. Espera a ver `Esperando tarjeta`\n3. Acerca la tarjeta o el telefono al area NFC",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.startContactlessPayment() },
                    enabled = !uiState.paymentUiState.isInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (uiState.paymentUiState.isInProgress) {
                            "Esperando..."
                        } else {
                            "Iniciar prueba contactless"
                        }
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.cancelContactlessPayment() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }

            KeyValueRow("Estado", uiState.paymentUiState.result.stage.name.replace('_', ' '))

            Text(
                text = uiState.paymentUiState.result.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = uiState.paymentUiState.result.message,
                style = MaterialTheme.typography.bodyMedium
            )

            if (uiState.paymentUiState.result.reference.isNotBlank()) {
                Text(
                    text = uiState.paymentUiState.result.reference,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.paymentUiState.result.stage == ContactlessPaymentStage.UNAVAILABLE) {
                Text(
                    text = "Si falla antes de llegar a esperar la tarjeta, revisa que NFC este habilitado y que el servicio de pagos de SUNMI este instalado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        CardBlock(title = "Estado Del Lector") {
            KeyValueRow("Funcion NFC", boolLabel(uiState.contactlessUiState.capabilities.hasNfcFeature))
            KeyValueRow("NFC habilitado", boolLabel(uiState.contactlessUiState.capabilities.isNfcEnabled))
            KeyValueRow("Servicio de pago", boolLabel(uiState.paymentUiState.availability.isVendorSdkConfigured))
            KeyValueRow("Terminal", uiState.paymentUiState.availability.terminalModel)
            Text(
                text = uiState.paymentUiState.availability.statusSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        CardBlock(title = "Prueba Avanzada") {
            Text(
                text = "Prueba opcional con NFC nativo de Android. Usala solo si la prueba de pago Sunmi de arriba no es suficiente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.startContactlessProbe() },
                    enabled = !uiState.contactlessUiState.isListening,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.contactlessUiState.isListening) "Escuchando..." else "Iniciar prueba nativa")
                }
                OutlinedButton(
                    onClick = { viewModel.clearContactlessState() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
            }
            Text(
                text = uiState.contactlessUiState.result.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = uiState.contactlessUiState.result.message,
                style = MaterialTheme.typography.bodyMedium
            )
            if (uiState.contactlessUiState.result.stage == ContactlessProbeStage.DETECTED) {
                KeyValueRow("UID", uiState.contactlessUiState.result.uid.ifBlank { "No disponible" })
                KeyValueRow("Tecnologias", uiState.contactlessUiState.result.technologies.ifBlank { "Desconocidas" })
                KeyValueRow("ISO-DEP", boolLabel(uiState.contactlessUiState.result.supportsIsoDep))
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider()
            content()
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
            fontWeight = FontWeight.Medium
        )
    }
}

private fun boolLabel(value: Boolean): String {
    return if (value) "Si" else "No"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PosdummyTheme {
        HomeScreen()
    }
}
