package com.grzhmelek.weatherlogger.network.usecases

import com.grzhmelek.weatherlogger.network.ApiImpl
import com.grzhmelek.weatherlogger.network.NetworkState
import com.grzhmelek.weatherlogger.network.WeatherResponse

class WeatherByLocationUseCase {

    private val api = ApiImpl()

    suspend fun getWeatherByLocation(
        latitude: Double,
        longitude: Double,
        unit: String,
        lang: String,
        appId: String,
    ): NetworkState<WeatherResponse> {
        val response = api.getWeatherByLocationAsync(latitude, longitude, unit, lang, appId)
        return if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                NetworkState.Success(responseBody)
            } else {
                NetworkState.Error(response)
            }
        } else {
            NetworkState.Error(response)
        }
    }
}