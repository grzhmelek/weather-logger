package com.grzhmelek.weatherlogger.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private const val BASE_URL = "https://api.openweathermap.org/"

// Client for retrofit request/response logging
var httpClient: OkHttpClient =
    OkHttpClient.Builder() //here we can add Interceptor for dynamical adding headers
        .addInterceptor { chain ->
            val request: Request = chain.request().newBuilder().addHeader("test", "test").build()
            chain.proceed(request)
        } // Interceptor for full level logging
        .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(httpClient)
    .build()

object WeatherApi {
    val retrofitService: Api by lazy {
        retrofit.create(Api::class.java)
    }
}