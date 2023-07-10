package com.grzhmelek.weatherlogger.ui.details

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grzhmelek.weatherlogger.database.WeatherResult

class WeatherDetailsViewModelFactory(
    private val application: Application,
    private val weatherResult: WeatherResult?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherDetailsViewModel::class.java)) {
            return WeatherDetailsViewModel(application, weatherResult) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}