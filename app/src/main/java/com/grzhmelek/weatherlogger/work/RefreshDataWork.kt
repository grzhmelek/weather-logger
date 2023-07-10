package com.grzhmelek.weatherlogger.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.database.WeatherDatabase
import com.grzhmelek.weatherlogger.network.WeatherApi
import com.grzhmelek.weatherlogger.utils.GpsTracker
import retrofit2.HttpException
import timber.log.Timber


class RefreshDataWorker(private val appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }

    override suspend fun doWork(): Result {
        Timber.d("doWork")
        return try {
            val location = getLocation(appContext)
            Timber.d("result success")
            Result.success(refresh(location))
        } catch (e: HttpException) {
            Timber.d("result retry")
            Result.retry()
        }
    }

    private fun getLocation(context: Context): Pair<Double, Double> {
        var location: Pair<Double, Double> = Pair((-1F).toDouble(), (-1F).toDouble())
        val gpsTracker = GpsTracker(context)
        if (gpsTracker.canGetLocation()) {
            location = Pair(gpsTracker.getLatitude(), gpsTracker.getLongitude())
        }
        Timber.d("location=$location")
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
                Timber.e(e.message ?: "Getting weather error")
            }
        }
        return Data.Builder()
            .putDouble("Latitude", location.first)
            .putDouble("Longitude", location.second)
            .build()
    }
}