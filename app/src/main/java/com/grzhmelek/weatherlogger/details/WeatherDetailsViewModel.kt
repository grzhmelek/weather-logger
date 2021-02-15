package com.grzhmelek.weatherlogger.details

import android.app.Application
import android.content.Context
import android.util.TypedValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.list.WeatherResult

class WeatherDetailsViewModel(
    @get:JvmName("getApplication_") val application: Application,
    val weatherResult: WeatherResult?
) :
    AndroidViewModel(application) {

    private val _weatherData = MutableLiveData<WeatherResult>()
    val weatherData: LiveData<WeatherResult>
        get() = _weatherData

    private val _temperatureTextColor = MutableLiveData<Int>()
    val temperatureTextColor: LiveData<Int>
        get() = _temperatureTextColor

    val isEmptyDetailsTextVisible = Transformations.map(_weatherData) {
        it == null
    }

    init {
        _weatherData.value = weatherResult
    }

    fun setTemperatureColor(context: Context, temp: Double) {
        val typedValue = TypedValue()
        val theme = context.theme
        if (temp > 0) {
            theme.resolveAttribute(R.attr.colorSecondaryVariant, typedValue, true)
        } else {
            theme.resolveAttribute(R.attr.colorColdVariant, typedValue, true)
        }
        _temperatureTextColor.value = typedValue.data
    }
}