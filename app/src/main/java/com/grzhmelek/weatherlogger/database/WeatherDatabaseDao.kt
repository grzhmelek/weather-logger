package com.grzhmelek.weatherlogger.database

import androidx.room.*
import com.grzhmelek.weatherlogger.list.WeatherResult

@Dao
interface WeatherDatabaseDao {

    // CRUD operations
    // Insert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(weatherResult: WeatherResult) : Long

    // Read
    @Query("SELECT * FROM weather_history_table ORDER BY weatherResultId DESC")
    fun getWeatherHistory(): List<WeatherResult>

    // Delete
    @Query("DELETE FROM weather_history_table")
    fun clear()

    // Operations below is used for testing only
    // Read by Id
    @Query("SELECT * FROM weather_history_table WHERE weatherResultId = :key")
    fun getWeatherItem(key: Long): WeatherResult

    // Update
    @Update
    fun update(weatherResult: WeatherResult)

    // Delete an item
    @Delete
    fun deleteWeatherItem(weatherResult: WeatherResult)
}