package com.example.kotlintest.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("email.php")
    fun sendText(@Body request: ApiModel): Call<ApiModel>
}
