package com.grzhmelek.weatherlogger.network

import retrofit2.Response

open class ApiImpl : Api {

    override suspend fun getWeatherByCityCodeAsync(
        cityId: String,
        unit: String,
        lang: String,
        appId: String,
    ): Response<WeatherResponse> =
        WeatherApi.retrofitService.getWeatherByCityCodeAsync(cityId, unit, lang, appId)

    override suspend fun getWeatherByLocationAsync(
        latitude: Double,
        longitude: Double,
        unit: String,
        lang: String,
        appId: String,
    ): Response<WeatherResponse> =
        WeatherApi.retrofitService.getWeatherByLocationAsync(latitude, longitude, unit, lang, appId)
}