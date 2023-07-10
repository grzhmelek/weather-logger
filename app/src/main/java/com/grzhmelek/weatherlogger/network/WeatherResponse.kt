package com.grzhmelek.weatherlogger.network

import com.google.gson.annotations.SerializedName
import com.grzhmelek.weatherlogger.data.Main
import com.grzhmelek.weatherlogger.data.Weather
import com.grzhmelek.weatherlogger.data.WeatherResult
import com.grzhmelek.weatherlogger.data.Wind


data class WeatherResponse(
    @SerializedName("weatherResultId") val weatherResultId: Long?,
    @SerializedName("date") var date: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("weather") val weather: List<WeatherItem>?,
    @SerializedName("main") val main: MainItem?,
    @SerializedName("wind") val wind: WindItem?,
) {

    fun toWeatherResult(): WeatherResult =
        WeatherResult(
            weatherResultId,
            date,
            name,
            weather?.map { it.toWeather() },
            main?.toMain(),
            wind?.toWind()
        )
}


class WeatherItem(
    @SerializedName("id") val id: Long?,
    @SerializedName("main") val main: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String?,
) {

    fun toWeather(): Weather =
        Weather(id, main, description, icon)
}


class MainItem(
    @SerializedName("temp") val temp: Double?,
    @SerializedName("feels_like") val feels_like: Double?,
    @SerializedName("temp_min") val temp_min: Double?,
    @SerializedName("temp_max") val temp_max: Double?,
    @SerializedName("pressure") val pressure: Int?,
    @SerializedName("humidity") val humidity: Int?,
) {

    fun toMain(): Main =
        Main(temp, feels_like, temp_min, temp_max, pressure, humidity)
}

class WindItem(
    @SerializedName("speed") val speed: Double?,
) {

    fun toWind(): Wind = Wind(speed)
}