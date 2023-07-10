package com.grzhmelek.weatherlogger.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("data/2.5/weather")
    suspend fun getWeatherByCityCodeAsync(
        @Query("id") cityId: String,
        @Query("units") unit: String,
        @Query("lang") lang: String,
        @Query("appid") appId: String,
    ): Response<WeatherResponse>

    @GET("data/2.5/weather")
    suspend fun getWeatherByLocationAsync(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") unit: String,
        @Query("lang") lang: String,
        @Query("appid") appId: String,
    ): Response<WeatherResponse>
}