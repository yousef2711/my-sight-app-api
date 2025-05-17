package com.yousef.mysight00

import com.yousef.mysight00.model.RegisterRequest
import com.yousef.mysight00.model.RegisterResponse
import com.yousef.mysight00.model.forgotPasswordRequest
import com.yousef.mysight00.model.forgotPasswordResponse
import com.yousef.mysight00.model.loginRequest
import com.yousef.mysight00.model.loginResponse
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit


interface ApiService {
    @POST("auth/register/")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

  @POST("auth/login/")
    suspend fun loginUser(
        @Body request: loginRequest
    ): Response<loginResponse>

    @POST("auth/password-reset/")
    suspend fun forgotPassword(
        @Body request: forgotPasswordRequest
    ): Response<forgotPasswordResponse>
}

object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://88cb-196-133-28-1.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
