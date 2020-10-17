package com.grzhmelek.weatherlogger.list

import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.databinding.WeatherListItemBinding

class WeatherRecyclerViewAdapter(
    private val clickListener: WeatherClickListener,
    private val theme: Resources.Theme
) : ListAdapter<WeatherResult,
        WeatherRecyclerViewAdapter.ViewHolder>(WeatherDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, theme, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: WeatherListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            clickListener: WeatherClickListener,
            theme: Resources.Theme,
            item: WeatherResult
        ) {
            binding.weatherResult = item
            binding.clickListener = clickListener

            // Text color depends on temperature
            val typedValue = TypedValue()
            val temp = item.main?.temp
            if (temp != null) {
                if (temp > 0) {
                    theme.resolveAttribute(R.attr.colorSecondaryVariant, typedValue, true)
                } else {
                    theme.resolveAttribute(R.attr.colorColdVariant, typedValue, true)
                }
                val color = typedValue.data
                binding.temperature.setTextColor(color)
            }

            // Transitions
            ViewCompat.setTransitionName(binding.root, item.weatherResultId.toString())

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = WeatherListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class WeatherDiffCallback : DiffUtil.ItemCallback<WeatherResult>() {
    override fun areItemsTheSame(oldItem: WeatherResult, newItem: WeatherResult): Boolean {
        return oldItem.weatherResultId == newItem.weatherResultId
    }

    override fun areContentsTheSame(oldItem: WeatherResult, newItem: WeatherResult): Boolean {
        return oldItem == newItem
    }
}

class WeatherClickListener(val clickListener: (weatherResult: WeatherResult) -> Unit) {
    fun onClick(weatherResult: WeatherResult) {
        clickListener(weatherResult)
    }
}