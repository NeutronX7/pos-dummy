package com.example.sunmi.data.source

import android.content.Context
import android.os.Bundle
import android.os.RemoteException
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import sunmi.paylib.SunmiPayKernel

internal data class ContactlessCardDetails(
    val uuid: String,
    val ats: String?,
    val cardCategory: Int?,
    val cardType: Int?,
    val pan: String?,
)

internal sealed interface PaymentSdkOperationResult {
    data object Success : PaymentSdkOperationResult
    data class Failure(val message: String) : PaymentSdkOperationResult
}

internal interface SunmiPaymentSdkListener {
    fun onSdkConnected()
    fun onSdkDisconnected()
    fun onCardDetected(cardDetails: ContactlessCardDetails)
    fun onError(code: Int?, message: String?)
}

internal interface SunmiPaymentSdkDataSource {
    fun isConnected(): Boolean
    fun connect(listener: SunmiPaymentSdkListener): Boolean
    fun startContactlessCardCheck(timeoutSeconds: Int): PaymentSdkOperationResult
    fun cancelCardCheck(): PaymentSdkOperationResult
    fun disconnect()
}

internal class SunmiPayKernelDataSource(
    context: Context,
) : SunmiPaymentSdkDataSource {
    private val appContext = context.applicationContext
    private val kernel = SunmiPayKernel.getInstance()
    private var listener: SunmiPaymentSdkListener? = null
    private var connected = false

    private val connectCallback = object : SunmiPayKernel.ConnectCallback {
        override fun onConnectPaySDK() {
            connected = true
            listener?.onSdkConnected()
        }

        override fun onDisconnectPaySDK() {
            connected = false
            listener?.onSdkDisconnected()
        }
    }

    private val checkCardCallback = object : CheckCardCallbackV2.Stub() {
        override fun findMagCard(info: Bundle?) = Unit

        override fun findICCard(atr: String?) = Unit

        override fun findRFCard(uuid: String?) {
            listener?.onCardDetected(
                ContactlessCardDetails(
                    uuid = uuid.orEmpty(),
                    ats = null,
                    cardCategory = null,
                    cardType = null,
                    pan = null,
                )
            )
        }

        override fun onError(code: Int, message: String?) {
            listener?.onError(code, message)
        }

        override fun findICCardEx(info: Bundle?) = Unit

        override fun findRFCardEx(info: Bundle?) {
            listener?.onCardDetected(
                ContactlessCardDetails(
                    uuid = info?.getString("uuid").orEmpty(),
                    ats = info?.getString("ats"),
                    cardCategory = info?.getInt("cardCategory"),
                    cardType = info?.getInt("cardType"),
                    pan = info?.getString("pan"),
                )
            )
        }

        override fun onErrorEx(info: Bundle?) {
            listener?.onError(
                code = info?.getInt("code"),
                message = info?.getString("message"),
            )
        }
    }

    override fun isConnected(): Boolean {
        return connected && kernel.mReadCardOptV2 != null
    }

    override fun connect(listener: SunmiPaymentSdkListener): Boolean {
        this.listener = listener
        return kernel.initPaySDK(appContext, connectCallback)
    }

    override fun startContactlessCardCheck(timeoutSeconds: Int): PaymentSdkOperationResult {
        return try {
            val readCardOpt = kernel.mReadCardOptV2
                ?: return PaymentSdkOperationResult.Failure("SUNMI card reader is not connected.")
            readCardOpt.cancelCheckCard()
            readCardOpt.checkCard(
                AidlConstants.CardType.NFC.value,
                checkCardCallback,
                timeoutSeconds,
            )
            PaymentSdkOperationResult.Success
        } catch (error: RemoteException) {
            PaymentSdkOperationResult.Failure(error.message.orEmpty())
        }
    }

    override fun cancelCardCheck(): PaymentSdkOperationResult {
        return try {
            kernel.mReadCardOptV2?.cancelCheckCard()
            PaymentSdkOperationResult.Success
        } catch (error: RemoteException) {
            PaymentSdkOperationResult.Failure(error.message.orEmpty())
        }
    }

    override fun disconnect() {
        kernel.removeConnectCallback(connectCallback)
        try {
            kernel.mReadCardOptV2?.cancelCheckCard()
        } catch (_: RemoteException) {
        }
        kernel.destroyPaySDK()
        connected = false
        listener = null
    }
}
