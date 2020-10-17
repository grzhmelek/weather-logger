package com.grzhmelek.weatherlogger.list

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grzhmelek.weatherlogger.WeatherLoggerApplication
import com.grzhmelek.weatherlogger.database.WeatherDatabaseDao

class WeatherListViewModelFactory(
    private val application: Application,
    private val dataSource: WeatherDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherListViewModel::class.java)) {
            return WeatherListViewModel(application, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}