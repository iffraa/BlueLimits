package com.app.bluelimits.util

import android.content.Context
import com.app.bluelimits.model.FortTokenRequest
import com.app.bluelimits.model.PayFortData
import com.payfort.fortpaymentsdk.FortSdk
import com.payfort.fortpaymentsdk.domain.model.FortRequest
import java.security.MessageDigest
import java.util.*


class PaymentHelper {

    private val staticFields = arrayOf(
        "merchant_identifier", "language", "command",
        "installments", "payment_option", "signature", "sdk_token", "access_code"
    )
    private val staticParams = ArrayList<String>()


    fun getTokenRequest(context: Context): FortTokenRequest {
        val lang = "en"
        val deviceID =
            FortSdk.getDeviceId(context);//Settings.Secure.getString(context.getContentResolver(),
        // Settings.Secure.ANDROID_ID);
        val reqPhrase = "03jEj8yvaWN9ONG0bkIZuo(#"
        val accessCode = "Hl9Itul2QJrNGcK3J9X9"
        val merchantIdentifier = "612f33f4"

        val signatureStr = reqPhrase + "access_code=" + accessCode + "device_id=" + deviceID +
                "language=" + lang + "merchant_identifier=" + merchantIdentifier + "service_command=SDK_TOKEN" +
                reqPhrase

        val shaSignature: String = getSha256(signatureStr)

        val request = FortTokenRequest(
            "SDK_TOKEN", accessCode, merchantIdentifier, lang,
            deviceID!!, shaSignature
        )

        return request
    }

    fun getSha256(base: String): String {
        return try {
            val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
            val hash: ByteArray = digest.digest(base.toByteArray(charset("UTF-8")))
            val hexString = StringBuilder()
            for (i in hash.indices) {
                val hex = Integer.toHexString(0xff and hash[i].toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            hexString.toString()
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }
    }

    fun getFortRequest(sdkToken: String, amount: String): FortRequest {

        val fortRequest = FortRequest()
        fortRequest.isShowResponsePage = true
        val map: MutableMap<String, Any> = TreeMap()
        map["language"] = "en"
        map["sdk_token"] = sdkToken
        map["command"] = "PURCHASE"//"AUTHORIZATION"
        map["currency"] = "SAR"
        map["amount"] = "1"
        map["merchant_reference"] = "OrderNo_" + getRandomNumber()
        map["customer_email"] = "mustafa@bluelimits.com"

        fortRequest.requestMap = map

        return fortRequest
    }

    fun getRandomNumber(): Int
    {
        val random = (0..1000).random() // generated random from 0 to 10 included
        return  random;
    }
}