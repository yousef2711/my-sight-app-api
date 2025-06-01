package com.yousef.mysight00

import com.yousef.mysight00.model.EditProfileRequest
import com.yousef.mysight00.model.EditProfileResponse
import com.yousef.mysight00.model.RegisterRequest
import com.yousef.mysight00.model.RegisterResponse
import com.yousef.mysight00.model.TokenResponse
import com.yousef.mysight00.model.forgotPasswordRequest
import com.yousef.mysight00.model.forgotPasswordResponse
import com.yousef.mysight00.model.loginRequest
import com.yousef.mysight00.model.loginResponse
import com.yousef.mysight00.utils.UserPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import okhttp3.Response as OkHttpResponse

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

    @GET("profile/")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<EditProfileResponse>

    @POST("profile/")
    suspend fun editProfile(
        @Header("Authorization") token: String,
        @Body request: EditProfileRequest
    ): Response<EditProfileResponse>

    @POST("auth/token/refresh/")
    suspend fun refreshToken(
        @Body refresh: String
    ): Response<TokenResponse>
}

object RetrofitInstance {
    private const val BASE_URL = "https://dacb-154-177-159-225.ngrok-free.app/" // Replace with your actual API base URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApi(context: android.content.Context): ApiService {
        authInterceptor.setContext(context)
        return retrofit.create(ApiService::class.java)
    }
}

class AuthInterceptor : Interceptor {
    private var context: android.content.Context? = null

    fun setContext(context: android.content.Context) {
        this.context = context
    }

    override fun intercept(chain: Interceptor.Chain): OkHttpResponse {
        val originalRequest = chain.request()
        val accessToken = context?.let { UserPreferences(it).getAccessToken() }

        return if (accessToken != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
