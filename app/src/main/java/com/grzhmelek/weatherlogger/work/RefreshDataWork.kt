package com.grzhmelek.weatherlogger.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.bumptech.glide.load.HttpException
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.database.WeatherDatabase
import com.grzhmelek.weatherlogger.network.WeatherApi
import com.grzhmelek.weatherlogger.utils.GpsTracker


class RefreshDataWorker(private val appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        private val TAG = RefreshDataWorker::class.simpleName
        const val WORK_NAME = "RefreshDataWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork")
        return try {
            val location = getLocation(appContext)
            Log.d(TAG, "result success")
            Result.success(refresh(location))
        } catch (e: HttpException) {
            Log.d(TAG, "result retry")
            Result.retry()
        }
    }

    private fun getLocation(context: Context): Pair<Double, Double> {
        var location: Pair<Double, Double> = Pair((-1F).toDouble(), (-1F).toDouble())
        val gpsTracker = GpsTracker(context)
        if (gpsTracker.canGetLocation()) {
            location = Pair(gpsTracker.getLatitude(), gpsTracker.getLongitude())
        }
        Log.d(TAG, "location=$location")
        return location
    }

    private suspend fun refresh(location: Pair<Double, Double>): Data {
        val dataSource = WeatherDatabase.getInstance(appContext).weatherDatabaseDao
        if (location.first > -1) {
            val getWeatherDeferred =
                WeatherApi.retrofitService.getWeatherByLocationAsync(
                    location.first,
                    location.second,
                    appContext.getString(R.string.temperature_unit),
                    appContext.getString(R.string.lang),
                    appContext.getString(R.string.weather_api_key)
                )
            try {
                val weatherResult = getWeatherDeferred.await()
                dataSource.insert(weatherResult)
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
        }
        return Data.Builder()
            .putDouble("Latitude", location.first)
            .putDouble("Longitude", location.second)
            .build()
    }
}