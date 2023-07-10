package com.grzhmelek.weatherlogger.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.grzhmelek.weatherlogger.database.typeconverters.MainConverters
import com.grzhmelek.weatherlogger.database.typeconverters.WeatherConverters
import com.grzhmelek.weatherlogger.database.typeconverters.WindConverters

@Database(entities = [WeatherResult::class], version = 1, exportSchema = false)
@TypeConverters(
    WeatherConverters::class,
    MainConverters::class,
    WindConverters::class
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract val weatherDatabaseDao: WeatherDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WeatherDatabase::class.java,
                        "weather_history_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
