package com.grzhmelek.weatherlogger.ui.details

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.grzhmelek.weatherlogger.databinding.FragmentWeatherDetailsBinding

class WeatherDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding: FragmentWeatherDetailsBinding = FragmentWeatherDetailsBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application

        val arguments: WeatherDetailsFragmentArgs by navArgs()

        val weatherDetailsViewModelFactory =
            WeatherDetailsViewModelFactory(application, arguments.weatherResult)

        val weatherDetailsViewModel: WeatherDetailsViewModel =
            ViewModelProvider(
                this,
                weatherDetailsViewModelFactory
            )[WeatherDetailsViewModel::class.java]

        binding.weatherDetailsViewModel = weatherDetailsViewModel
        binding.lifecycleOwner = this

        // Transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.root.transitionName = arguments.weatherResult.weatherResultId.toString()
        }

        weatherDetailsViewModel.weatherData.observe(viewLifecycleOwner) {
            it?.main?.temp?.let { temp ->
                weatherDetailsViewModel.setTemperatureColor(
                    binding.temperature.context,
                    temp
                )
            }
        }

        return binding.root
    }

}