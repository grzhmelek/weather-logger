package com.grzhmelek.weatherlogger.ui.home

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.database.WeatherDatabaseDao
import com.grzhmelek.weatherlogger.database.WeatherResult
import com.grzhmelek.weatherlogger.network.api.NetworkState
import com.grzhmelek.weatherlogger.network.usecases.WeatherByCityCodeUseCase
import com.grzhmelek.weatherlogger.network.usecases.WeatherByLocationUseCase
import com.grzhmelek.weatherlogger.utils.GpsTracker
import com.grzhmelek.weatherlogger.utils.SingleLiveEvent
import com.grzhmelek.weatherlogger.utils.storeImage
import kotlinx.coroutines.*
import timber.log.Timber

class WeatherListViewModel(
    @get:JvmName("getApplication_") val application: Application,
    val database: WeatherDatabaseDao,
) :
    AndroidViewModel(application) {

    private val _weatherResultData = MutableLiveData<WeatherResult>()
    val weatherResultData: LiveData<WeatherResult> = _weatherResultData

    private val _weatherHistoryData = MutableLiveData<List<WeatherResult>>()
    val weatherHistoryData: LiveData<List<WeatherResult>> = _weatherHistoryData

    private val _selectedPosition = MutableLiveData<Int>()
    val selectedPosition: LiveData<Int> = _selectedPosition

    private val _previousPosition = MutableLiveData<Int>()
    val previousPosition: LiveData<Int> = _previousPosition

    private val _isProgressVisible = MutableLiveData<Boolean>()
    val isProgressVisible = _isProgressVisible.map { true == it }

    val isEmptyTextVisible = _weatherHistoryData.map { it.isEmpty() }

    val setImageUriToShareEvent = SingleLiveEvent<Uri>()

    val getCurrentLocationEvent = SingleLiveEvent<Pair<Double, Double>>()

    val getWeatherEvent = SingleLiveEvent<Unit>()

    val showMessageEvent = SingleLiveEvent<String>()

    val navigateToDetailsEvent = SingleLiveEvent<WeatherResult>()

    init {
        initializeWeatherHistory()
    }

    fun needToGetWeather() {
        getWeatherEvent.postValue(Unit)
    }

    fun getLocation(activity: FragmentActivity) {
        val gpsTracker = GpsTracker(activity)
        if (gpsTracker.canGetLocation()) {
            getCurrentLocationEvent.postValue(
                Pair(
                    gpsTracker.getLatitude(),
                    gpsTracker.getLongitude()
                )
            )
        } else {
            gpsTracker.showSettingsAlert()
        }
    }

    fun getWeatherResultData(cityId: String, unit: String, lang: String, appId: String) {
        viewModelScope.launch {
            when (val response =
                WeatherByCityCodeUseCase().getWeatherByCityCode(cityId, unit, lang, appId)) {
                is NetworkState.Loading -> {
                    _isProgressVisible.postValue(true)
                }

                is NetworkState.Success -> {
                    _isProgressVisible.postValue(false)
                    handleResult(response.data.toWeatherResult())
                }

                is NetworkState.Error -> {
                    _isProgressVisible.postValue(false)
                    //TODO: handle error
                }
            }
        }
    }

    fun getWeatherResultData(
        latitude: Double,
        longitude: Double,
        unit: String,
        lang: String,
        appId: String,
    ) {
        viewModelScope.launch {
            Timber.d("getWeatherResultData current location, lat=$latitude, lon=$longitude")
            when (val response = WeatherByLocationUseCase().getWeatherByLocation(
                latitude,
                longitude,
                unit,
                lang,
                appId
            )) {
                is NetworkState.Loading -> {
                    _isProgressVisible.postValue(true)
                }

                is NetworkState.Success -> {
                    _isProgressVisible.postValue(false)
                    handleResult(response.data.toWeatherResult())
                }

                is NetworkState.Error -> {
                    _isProgressVisible.postValue(false)
                    //TODO: handle error
                }
            }
        }
    }

    private fun handleResult(weatherResult: WeatherResult) {
        _weatherResultData.postValue(weatherResult)
    }

    fun onWeatherClicked(weatherResult: WeatherResult, position: Int) {
        navigateToDetailsEvent.postValue(weatherResult)
        _previousPosition.postValue(selectedPosition.value)
        _selectedPosition.postValue(position)

    }

    private fun initializeWeatherHistory() {
        viewModelScope.launch {
            _weatherHistoryData.postValue(getWeatherHistory())
        }
    }

    fun insertCurrentWeather(weatherResult: WeatherResult) {
        _isProgressVisible.value = true
        viewModelScope.launch {
            insetWeatherResult(weatherResult)
            _weatherResultData.postValue(null)
            _weatherHistoryData.postValue(getWeatherHistory())
            _isProgressVisible.postValue(false)
        }
    }

    private suspend fun insetWeatherResult(weatherResult: WeatherResult) {
        withContext(Dispatchers.IO) {
            database.insert(weatherResult)
        }
    }

    private suspend fun getWeatherHistory(): List<WeatherResult> {
        return withContext(Dispatchers.IO) {
            database.getWeatherHistory()
        }
    }

    private suspend fun clearDatabase() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    fun clearHistory() {
        _isProgressVisible.value = true
        viewModelScope.launch {
            clearDatabase()
            _weatherHistoryData.postValue(getWeatherHistory())
            _isProgressVisible.postValue(false)
        }
    }

   fun showProgress(show: Boolean) {
       _isProgressVisible.postValue(show)
   }
}