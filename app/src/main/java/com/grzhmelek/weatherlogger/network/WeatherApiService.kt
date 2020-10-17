package com.grzhmelek.weatherlogger.network

import com.grzhmelek.weatherlogger.list.WeatherResult
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val BASE_URL = "https://api.openweathermap.org/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

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
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(httpClient)
    .build()

interface WeatherApiService {
    @GET("data/2.5/weather")
    fun getWeatherByCityCodeAsync(
        @Query("id") cityId: String,
        @Query("units") unit: String,
        @Query("lang") lang: String,
        @Query("appid") appId: String
    ):
            Deferred<WeatherResult>

    @GET("data/2.5/weather")
    fun getWeatherByLocationAsync(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") unit: String,
        @Query("lang") lang: String,
        @Query("appid") appId: String
    ):
            Deferred<WeatherResult>
}

object WeatherApi {
    val retrofitService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}