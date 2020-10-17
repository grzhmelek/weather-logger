package com.grzhmelek.weatherlogger.database.typeConverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grzhmelek.weatherlogger.list.Wind
import java.lang.reflect.Type

class WindConverters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromWind(wind: Wind?): String? {
            val gson = Gson()
            return gson.toJson(wind)
        }

        @TypeConverter
        @JvmStatic
        fun toWind(value: String?): Wind? {
            val windType: Type = object :
                TypeToken<Wind?>() {}.type
            return Gson().fromJson<Wind>(
                value,
                windType
            )
        }
    }
}