package com.grzhmelek.weatherlogger.network

import retrofit2.Response

sealed class NetworkState<out T> {
    object Loading : NetworkState<Nothing>()
    data class Success<out T>(val data: T) : NetworkState<T>()
    data class Error<T>(val response: Response<T>) : NetworkState<T>()
}