package com.grzhmelek.weatherlogger.database.typeConverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grzhmelek.weatherlogger.list.Main
import java.lang.reflect.Type

class MainConverters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromMain(main: Main?): String? {
            val gson = Gson()
            return gson.toJson(main)
        }

        @TypeConverter
        @JvmStatic
        fun toMain(value: String?): Main? {
            val mainType: Type = object :
                TypeToken<Main?>() {}.type
            return Gson().fromJson<Main>(
                value,
                mainType
            )
        }
    }
}