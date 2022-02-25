package com.app.bluelimits.model

import retrofit2.Call
import retrofit2.http.*


interface PaymentAPI {

    @Headers("Content-Type: application/json")
    @POST("paymentApi")
    fun postRequest(
        @Body request: FortTokenRequest,
        ): Call<PayFortData>
}