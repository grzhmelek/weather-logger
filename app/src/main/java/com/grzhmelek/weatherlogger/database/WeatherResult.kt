package com.grzhmelek.weatherlogger.database

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "weather_history_table")
data class WeatherResult(
    @PrimaryKey(autoGenerate = true)
    val weatherResultId: Long? = System.currentTimeMillis(),
    @ColumnInfo(name = "date")
    var date: Long? = System.currentTimeMillis(),
    @ColumnInfo(name = "name")
    val name: String? = "",
    @ColumnInfo(name = "weather")
    val weather: List<Weather>? = emptyList(),
    @ColumnInfo(name = "main")
    val main: Main? = Main(),
    @ColumnInfo(name = "wind")
    val wind: Wind? = Wind(),
) : Parcelable {
    constructor() : this(null, null, null, null, null, null)
}

@Parcelize
class Weather(
    val id: Long?,
    val main: String?,
    val description: String?,
    val icon: String?,
) : Parcelable {
    constructor() : this(null, null, null, null)
}

@Parcelize
class Main(
    val temp: Double?,
    val feels_like: Double?,
    val temp_min: Double?,
    val temp_max: Double?,
    val pressure: Int?,
    val humidity: Int?,
) : Parcelable {
    constructor() : this(null, null, null, null, null, null)
}

@Parcelize
class Wind(
    val speed: Double?,
) : Parcelable {
    constructor() : this(null)
}