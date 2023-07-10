package com.grzhmelek.weatherlogger.ui.home

import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.database.WeatherResult
import com.grzhmelek.weatherlogger.databinding.WeatherListItemBinding
import com.grzhmelek.weatherlogger.ui.home.WeatherRecyclerViewAdapter.ViewHolder
import com.grzhmelek.weatherlogger.ui.home.WeatherRecyclerViewAdapter.ViewHolder.Companion.selectedPosition

class WeatherRecyclerViewAdapter(
    private val clickListener: WeatherClickListener,
    private val theme: Resources.Theme
) : ListAdapter<WeatherResult,
        ViewHolder>(WeatherDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, theme, item, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: WeatherListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            clickListener: WeatherClickListener,
            theme: Resources.Theme,
            item: WeatherResult,
            position: Int
        ) {
            binding.weatherResult = item
            binding.clickListener = clickListener
            binding.position = position

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

            // Item highlighting for multiple pane screens
            if (binding.root.context.resources.getBoolean(R.bool.isTablet)) {
                if (selectedPosition == position) {
                    theme.resolveAttribute(R.attr.colorSecondaryLight, typedValue, true)
                    val color = typedValue.data
                    binding.root.setBackgroundColor(color)
                } else {
                    binding.root.setBackgroundColor(Color.TRANSPARENT)
                }
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

            var selectedPosition: Int = -1
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

class WeatherClickListener(val clickListener: (weatherResult: WeatherResult, position: Int) -> Unit) {
    fun onClick(weatherResult: WeatherResult, position: Int) {
        selectedPosition = position
        clickListener(weatherResult, position)
    }
}