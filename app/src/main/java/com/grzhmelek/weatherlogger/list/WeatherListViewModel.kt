package com.grzhmelek.weatherlogger.list

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.WeatherLoggerApplication
import com.grzhmelek.weatherlogger.database.WeatherDatabaseDao
import com.grzhmelek.weatherlogger.network.WeatherApi
import com.grzhmelek.weatherlogger.utils.GpsTracker
import com.grzhmelek.weatherlogger.utils.storeImage
import kotlinx.coroutines.*

class WeatherListViewModel(
    @get:JvmName("getApplication_") val application: Application,
    val database: WeatherDatabaseDao
) :
    AndroidViewModel(application) {

    companion object {
        val TAG = WeatherListViewModel::class.simpleName
    }

    private var weatherViewModelJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + weatherViewModelJob)

    private val _weatherResultData = MutableLiveData<WeatherResult>()
    val weatherResultData: LiveData<WeatherResult>
        get() = _weatherResultData

    private val _weatherHistory = MutableLiveData<List<WeatherResult>>()
    val weatherHistory: LiveData<List<WeatherResult>>
        get() = _weatherHistory

    private val _navigateToDetails = MutableLiveData<WeatherResult>()
    val navigateToDetails: LiveData<WeatherResult>
        get() = _navigateToDetails

    private val _showMessage = MutableLiveData<String>()
    val showMessage: LiveData<String>
        get() = _showMessage

    private val _isProgressVisible = MutableLiveData<Boolean>()
    val isProgressVisible = Transformations.map(_isProgressVisible) {
        true == it
    }

    val isEmptyTextVisible = Transformations.map(_weatherHistory) {
        it.isEmpty()
    }

    private val _imageUriToShare = MutableLiveData<Uri>()
    val imageUriToShare: LiveData<Uri>
        get() = _imageUriToShare

    private val _currentLocation = MutableLiveData<Pair<Double, Double>>()
    val currentLocation: LiveData<Pair<Double, Double>>
        get() = _currentLocation

    private val _weatherNeeded = MutableLiveData<Boolean>()
    val weatherNeeded: LiveData<Boolean>
        get() = _weatherNeeded

    init {
        initializeWeatherHistory()
    }

    override fun onCleared() {
        super.onCleared()
        weatherViewModelJob.cancel()
    }

    fun needToGetWeather() {
        _weatherNeeded.value = true
    }

    fun weatherGettingComplete() {
        _weatherNeeded.value = false
    }

    fun getLocation(activity: FragmentActivity) {
        val gpsTracker = GpsTracker(activity)
        if (gpsTracker.canGetLocation()) {
            _currentLocation.value = Pair(gpsTracker.getLatitude(), gpsTracker.getLongitude())
        } else {
            gpsTracker.showSettingsAlert()
        }
    }

    fun noCurrentLocationNeeded() {
        _currentLocation.value = null
    }

    fun getWeatherResultData(cityId: String, unit: String, lang: String, appId: String) {
        _isProgressVisible.value = true
        coroutineScope.launch {
            val getWeatherDeferred =
                WeatherApi.retrofitService.getWeatherByCityCodeAsync(cityId, unit, lang, appId)
            handleResult(getWeatherDeferred)
        }
    }

    fun getWeatherResultData(
        latitude: Double,
        longitude: Double,
        unit: String,
        lang: String,
        appId: String
    ) {
        _isProgressVisible.value = true
        coroutineScope.launch {
            Log.d(TAG, "getWeatherResultData current location, lat=$latitude, lon=$longitude")
            val getWeatherDeferred =
                WeatherApi.retrofitService.getWeatherByLocationAsync(
                    latitude,
                    longitude,
                    unit,
                    lang,
                    appId
                )
            handleResult(getWeatherDeferred)
        }
    }

    private suspend fun handleResult(weatherDeferred: Deferred<WeatherResult>) {
        try {
            val weatherResult = weatherDeferred.await()
            _weatherResultData.postValue(weatherResult)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            _weatherResultData.postValue(WeatherResult())
            _showMessage.postValue(application.getString(R.string.message_connection_error))
        }
        _isProgressVisible.postValue(false)
    }

    fun onWeatherClicked(weatherResult: WeatherResult) {
        _navigateToDetails.value = weatherResult
    }

    private fun initializeWeatherHistory() {
        coroutineScope.launch {
            _weatherHistory.postValue(getWeatherHistory())
        }
    }

    fun insertCurrentWeather(weatherResult: WeatherResult) {
        _isProgressVisible.value = true
        coroutineScope.launch {
            database.insert(weatherResult)
            _weatherResultData.postValue(null)
            _weatherHistory.postValue(getWeatherHistory())
            _isProgressVisible.postValue(false)
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
        coroutineScope.launch {
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
        coroutineScope.launch {
            _imageUriToShare.postValue(storeImage(context, imageData, fileName))
            _isProgressVisible.postValue(false)
        }
    }

    fun shareImageComplete() {
        _imageUriToShare.value = null
    }
}