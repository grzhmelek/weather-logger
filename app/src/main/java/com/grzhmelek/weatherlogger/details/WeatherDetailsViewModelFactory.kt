package com.grzhmelek.weatherlogger.details

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grzhmelek.weatherlogger.list.WeatherResult

class WeatherDetailsViewModelFactory(
    private val application: Application,
    private val weatherResult: WeatherResult
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherDetailsViewModel::class.java)) {
            return WeatherDetailsViewModel(application, weatherResult) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}