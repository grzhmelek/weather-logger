package com.grzhmelek.weatherlogger.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.list.WeatherListViewModel
import com.grzhmelek.weatherlogger.list.WeatherResult
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("convert_millis")
fun bindMillisToString(textView: TextView, millis: Long) {
    val sdf =
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale(textView.context.getString(R.string.lang)))
    val resultDate = Date(millis)
    textView.text = sdf.format(resultDate)
}

@BindingAdapter("weather_icon")
fun bindIcon(imageView: ImageView, weatherResult: WeatherResult) {
    val context = imageView.context
    Glide.with(context)
        .load(weatherResult.weather?.get(0)?.icon?.let { getDrawable(context, it) })
        .into(imageView)
}

@BindingAdapter("temperature_text_color")
fun bindTextColor(textView: TextView, color: Int) {
    if (color != 0) textView.setTextColor(color)
}

@BindingAdapter("progress_visibility")
fun bindProgress(imageView: ImageView, isProgressVisible: Boolean) {
    imageView.visibility = if (isProgressVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("empty_text_visibility")
fun bindEmptyText(textView: TextView, isEmptyTextVisible: Boolean) {
    textView.visibility = if (isEmptyTextVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("format_temperature")
fun bindTemperature(textView: TextView, temperature: Double) {
    val df = DecimalFormat("#,###.#")
    df.maximumFractionDigits = 2
    textView.text =
        textView.context.getString(R.string.formatted_temperature, df.format(temperature))
}

@BindingAdapter("humidity")
fun bindHumidity(textView: TextView, humidity: Int) {
    val strHumidityFormat: String = textView.context.getString(R.string.humidity)
    textView.text = String.format(strHumidityFormat, humidity)
}

@BindingAdapter("speed")
fun bindSpeed(textView: TextView, speed: Double) {
    val df = DecimalFormat("#,###.#")
    df.maximumFractionDigits = 2
    textView.text =
        textView.context.getString(R.string.speed, df.format(speed))
}

fun getDrawable(context: Context, iconCode: String): Drawable? {
    return when (iconCode) {
        "01d" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_01d, null)
        "01n" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_01n, null)
        "02d" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_02d, null)
        "02n" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_02n, null)
        "03d" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_03d, null)
        "03n" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_03n, null)
        "04d" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_04d, null)
        "04n" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_04n, null)
        "09d" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_09d, null)
        "09n" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_09n, null)
        "10d" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_10d, null)
        "10n" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_10n, null)
        "11d" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_11d, null)
        "11n" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_11n, null)
        "13d" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_13d, null)
        "13n" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_13n, null)
        "50d" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_50d, null)
        "50n" -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_50n, null)
        else -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_empty_icon, null)
    }
}