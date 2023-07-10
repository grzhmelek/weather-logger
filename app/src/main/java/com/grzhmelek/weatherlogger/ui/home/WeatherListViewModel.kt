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
import com.grzhmelek.weatherlogger.database.WeatherResult
import com.grzhmelek.weatherlogger.database.WeatherDatabaseDao
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

    private val _weatherHistory = MutableLiveData<List<WeatherResult>>()
    val weatherHistory: LiveData<List<WeatherResult>> = _weatherHistory

    private val _navigateToDetails = MutableLiveData<WeatherResult>()
    val navigateToDetails: LiveData<WeatherResult> = _navigateToDetails

    private val _selectedPosition = MutableLiveData<Int>()
    val selectedPosition: LiveData<Int> = _selectedPosition

    private val _previousPosition = MutableLiveData<Int>()
    val previousPosition: LiveData<Int> = _previousPosition

    private val _showMessage = MutableLiveData<String>()
    val showMessage: LiveData<String> = _showMessage

    private val _isProgressVisible = MutableLiveData<Boolean>()
    val isProgressVisible = _isProgressVisible.map { true == it }

    val isEmptyTextVisible = _weatherHistory.map { it.isEmpty() }

    private val _imageUriToShare = MutableLiveData<Uri>()
    val imageUriToShare: LiveData<Uri> = _imageUriToShare

    val getCurrentLocationEvent = SingleLiveEvent<Pair<Double, Double>>()

    val getWeatherEvent = SingleLiveEvent<Unit>()

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
        Timber.d("onWeatherClicked")
        _navigateToDetails.value = weatherResult
        _previousPosition.value = _selectedPosition.value
        _selectedPosition.value = position

    }

    private fun initializeWeatherHistory() {
        viewModelScope.launch {
            _weatherHistory.postValue(getWeatherHistory())
        }
    }

    fun insertCurrentWeather(weatherResult: WeatherResult) {
        _isProgressVisible.value = true
        viewModelScope.launch {
            insetWeatherResult(weatherResult)
            _weatherResultData.postValue(null)
            _weatherHistory.postValue(getWeatherHistory())
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

    fun navigateToDetailsComplete() {
        _navigateToDetails.value = null
    }

    fun showMessageComplete() {
        _showMessage.value = null
    }

    fun clearHistory() {
        _isProgressVisible.value = true
        viewModelScope.launch {
            database.clear()
            _weatherHistory.postValue(getWeatherHistory())
            _isProgressVisible.postValue(false)
        }
    }

    fun getBitmapFromViewAndStoreImage(scrollView: NestedScrollView) {
        scrollView.isDrawingCacheEnabled = true
        val bitmap = loadBitmapFromView(scrollView)
        scrollView.isDrawingCacheEnabled = false

        storeImage(
            scrollView.context,
            bitmap
        )
    }

    private fun loadBitmapFromView(scrollView: NestedScrollView): Bitmap {
        val bitmap = Bitmap.createBitmap(
            scrollView.getChildAt(0).width * 2,
            scrollView.getChildAt(0).height * 2,
            Bitmap.Config.ARGB_8888
        )

        val c = Canvas(bitmap)
        c.scale(2.0f, 2.0f)
        c.drawColor(Color.WHITE)
        scrollView.getChildAt(0).draw(c)
        return bitmap
    }

    private fun storeImage(context: Context, imageData: Bitmap) {
        _isProgressVisible.value = true
        val fileName = "${context.getString(R.string.app_name)}_${System.currentTimeMillis()}.jpg"
        viewModelScope.launch {
            _imageUriToShare.postValue(storeImage(context, imageData, fileName))
            _isProgressVisible.postValue(false)
        }
    }

    fun shareImageComplete() {
        _imageUriToShare.value = null
    }
}