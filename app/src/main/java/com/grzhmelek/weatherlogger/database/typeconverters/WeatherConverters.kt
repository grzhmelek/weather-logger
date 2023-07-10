package com.grzhmelek.weatherlogger.database.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grzhmelek.weatherlogger.data.Weather
import java.lang.reflect.Type

class WeatherConverters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromWeather(weather: List<Weather>?): String? {
            val gson = Gson()
            return gson.toJson(weather)
        }

        @TypeConverter
        @JvmStatic
        fun toWeather(value: String?): List<Weather>? {
            val weatherType: Type = object :
                TypeToken<List<Weather>?>() {}.type
            return Gson().fromJson<List<Weather>>(
                value,
                weatherType
            )
        }
    }
}