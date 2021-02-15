package com.grzhmelek.weatherlogger.details

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.grzhmelek.weatherlogger.databinding.FragmentWeatherDetailsBinding

class WeatherDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentWeatherDetailsBinding = FragmentWeatherDetailsBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application

        val arguments = WeatherDetailsFragmentArgs.fromBundle(requireArguments())

        val weatherDetailsViewModelFactory =
            WeatherDetailsViewModelFactory(application, arguments.weatherResult)

        val weatherDetailsViewModel: WeatherDetailsViewModel =
            ViewModelProvider(this, weatherDetailsViewModelFactory)
                .get(WeatherDetailsViewModel::class.java)

        binding.weatherDetailsViewModel = weatherDetailsViewModel
        binding.lifecycleOwner = this

        // Transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.root.transitionName = arguments.weatherResult?.weatherResultId.toString()
        }

        weatherDetailsViewModel.weatherData.observe(viewLifecycleOwner, {
            if (it?.main?.temp != null) weatherDetailsViewModel.setTemperatureColor(
                binding.temperature.context,
                it.main.temp
            )
        })

        return binding.root
    }

    //    override fun onCreate(savedInstanceState: Bundle?) {
//        // Transitions
//        sharedElementEnterTransition = MaterialContainerTransform().apply {
//            duration = resources.getInteger(R.integer.transition_motion_duration_long).toLong()
//            isElevationShadowEnabled = true
//        }
//        super.onCreate(savedInstanceState)
//    }

}