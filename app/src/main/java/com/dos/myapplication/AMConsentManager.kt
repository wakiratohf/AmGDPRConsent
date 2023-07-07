package com.dos.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.provider.Settings.Secure
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class AMConsentManager {
    private var consentForm: ConsentForm? = null
    private lateinit var consentInformation: ConsentInformation

    companion object {
        val instance: AMConsentManager by lazy { AMConsentManager() }
    }

    fun init(
        context: Activity,
        errorCallback: ((String, Int) -> Unit)? = null,
        successCallback: (() -> Unit)? = null,
        testMode: Boolean = false,
    ) {
        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false);
        if (testMode) {
            val debugSettings = ConsentDebugSettings.Builder(context).setDebugGeography(
                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
            ).addTestDeviceHashedId(getTestDeviceHashKey(context)).build()
            params.setConsentDebugSettings(debugSettings)
        }
        consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation.requestConsentInfoUpdate(context, params.build(), {
            if (consentInformation.isConsentFormAvailable) {
                loadForm(context, errorCallback, successCallback)
            } else {
                errorCallback?.invoke("Consent Form is Unavailable", -5)
            }
        }, { formError ->
            errorCallback?.invoke(formError.message, formError.errorCode)
        })
    }

    @SuppressLint("HardwareIds")
    private fun getTestDeviceHashKey(context: Context): String {
        val deviceId = Secure.getString(
            context.contentResolver,
            Secure.ANDROID_ID
        )
        return md5(deviceId)
    }

    private fun md5(s: String): String {
        try {
            val digest = MessageDigest
                .getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    fun showFormIfAvailable(
        activity: Activity,
        callback: ConsentForm.OnConsentFormDismissedListener,
    ): Boolean {
        if (consentForm != null) {
            consentForm!!.show(activity, callback)
            return true
        }
        return false
    }

    fun reset() {
        consentInformation.reset()
    }

    private fun loadForm(
        context: Context,
        errorCallback: ((String, Int) -> Unit)?,
        successCallback: (() -> Unit)?,
    ) {
        UserMessagingPlatform.loadConsentForm(context, { consentForm ->
            this.consentForm = consentForm
            successCallback?.invoke()
        }, { formError ->
            errorCallback?.invoke(formError.message, formError.errorCode)
        })
    }
}