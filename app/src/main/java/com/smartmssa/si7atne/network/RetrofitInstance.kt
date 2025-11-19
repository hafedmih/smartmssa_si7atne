package com.smartmssa.si7atne.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // The base URL of your API
    private const val BASE_URL = "https://smartmssa-si7atne-staging-dev-25498255.dev.odoo.com/"

    // Create a logging interceptor to see request and response logs in Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Use BODY to see all details
    }

    // Create an OkHttpClient and add the interceptor
    // This is useful for debugging but should be configured for release builds
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Create the Retrofit instance
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson to parse JSON
            .build()
    }

    // Create a publicly accessible instance of the ApiService
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}