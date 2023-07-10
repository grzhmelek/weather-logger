package com.grzhmelek.weatherlogger.ui.details

import android.app.Application
import android.content.Context
import android.util.TypedValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.database.WeatherResult

class WeatherDetailsViewModel(
    @get:JvmName("getApplication_") val application: Application,
    val weatherResult: WeatherResult?,
) :
    AndroidViewModel(application) {

    private val _weatherData = MutableLiveData<WeatherResult>()
    val weatherData: LiveData<WeatherResult> = _weatherData

    private val _temperatureTextColor = MutableLiveData<Int>()
    val temperatureTextColor: LiveData<Int> = _temperatureTextColor

    val isEmptyDetailsTextVisible = _weatherData.map { it == null }

    init {
        _weatherData.value = weatherResult
    }

    fun setTemperatureColor(context: Context, temp: Double) {
        val typedValue = TypedValue()

        context.theme.resolveAttribute(
            if (temp > 0) R.attr.colorSecondaryVariant else R.attr.colorColdVariant,
            typedValue,
            true
        )

        _temperatureTextColor.value = typedValue.data
    }
}