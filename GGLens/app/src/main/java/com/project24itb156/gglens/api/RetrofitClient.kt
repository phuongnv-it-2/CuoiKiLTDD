package com.project24itb156.gglens.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ── Thay bằng URL backend thật (Render hoặc IP local) ───────────────────
    // Base URL must end with / for Retrofit. 
    // Adding ngrok header to bypass the browser warning page.
    private const val BACKEND_BASE_URL = "https://enlisted-coke-guide.ngrok-free.dev/"

    private const val VISION_BASE_URL = "https://vision.googleapis.com/"
    private const val GOOGLE_BASE_URL = "https://www.googleapis.com/"

    // Token hiện tại, được TokenManager set vào lúc app khởi động / sau khi login
    @Volatile
    var authToken: String? = null

    // Interceptor tự động gắn "Authorization: Bearer <token>" vào mọi request tới backend
    // Thêm "ngrok-skip-browser-warning" để bypass trang cảnh báo của ngrok
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = authToken

        val requestBuilder = original.newBuilder()
            .addHeader("ngrok-skip-browser-warning", "true")
            
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val backendApi: GGLensBackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GGLensBackendApi::class.java)
    }

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    val visionApi: VisionApi by lazy {
        Retrofit.Builder()
            .baseUrl(VISION_BASE_URL)
            .client(OkHttpClient.Builder() // Vision API không cần gắn token backend
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VisionApi::class.java)
    }

    val googleSearchApi: GoogleSearchApi by lazy {
        Retrofit.Builder()
            .baseUrl(GOOGLE_BASE_URL)
            .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleSearchApi::class.java)
    }
}
