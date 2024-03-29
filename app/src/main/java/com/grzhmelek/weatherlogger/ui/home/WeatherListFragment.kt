package com.grzhmelek.weatherlogger.ui.home

import android.Manifest
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.R.integer
import com.grzhmelek.weatherlogger.database.WeatherDatabase
import com.grzhmelek.weatherlogger.database.WeatherDatabaseDao
import com.grzhmelek.weatherlogger.databinding.FragmentWeatherListBinding
import com.grzhmelek.weatherlogger.utils.storeImage
import kotlinx.coroutines.launch
import timber.log.Timber


class WeatherListFragment : Fragment() {

    private lateinit var binding: FragmentWeatherListBinding

    private lateinit var application: Application

    private lateinit var dataSource: WeatherDatabaseDao

    private lateinit var viewModelFactory: WeatherListViewModelFactory

    private lateinit var adapter: WeatherRecyclerViewAdapter

    private val weatherListViewModel: WeatherListViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[WeatherListViewModel::class.java]
    }

    private var isTablet = false

    private val requestLocationPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            if (it.value.not()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.message_permission_location_denied),
                    Snackbar.LENGTH_LONG
                ).show()
                getWeatherByCityCode()
                return@registerForActivityResult
            }
        }
        weatherListViewModel.getLocation(this.requireActivity())
    }

    private val requestSharingPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            getBitmapFromViewAndStoreImage()
        } else {
            Toast.makeText(
                activity,
                getString(R.string.message_permission_denied),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWeatherListBinding.inflate(inflater)

        application = requireNotNull(this.activity).application

        dataSource = WeatherDatabase.getInstance(application).weatherDatabaseDao

        viewModelFactory = WeatherListViewModelFactory(application, dataSource)

        binding.weatherListViewModel = weatherListViewModel
        binding.lifecycleOwner = this
        weatherListViewModel.observeChanges()

        isTablet = resources.getBoolean(R.bool.isTablet)

        adapter = WeatherRecyclerViewAdapter(
            WeatherClickListener { weather, position ->
                // Transitions
                exitTransition = MaterialElevationScale(true).apply {
                    duration =
                        resources.getInteger(integer.transition_motion_duration_short).toLong()
                }

                reenterTransition = MaterialElevationScale(true).apply {
                    duration =
                        resources.getInteger(integer.transition_motion_duration_long).toLong()
                }
                weatherListViewModel.onWeatherClicked(weather, position)
            },
            requireActivity().theme
        )
        binding.weatherList.adapter = adapter

        binding.weatherList.addItemDecoration(
            DividerItemDecoration(
                application,
                DividerItemDecoration.VERTICAL
            )
        )

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun WeatherListViewModel.observeChanges() {
        // Observe if need to retrieve weather from CurrentWeatherData API
        getWeatherEvent.observe(viewLifecycleOwner) {
            requestLocationPermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        // Observe getting current location
        getCurrentLocationEvent.observe(viewLifecycleOwner) {
            Timber.d("Current location=$it")
            getWeatherByLocation(it)
        }

        // Observe weather data retrieved from CurrentWeatherData API
        weatherResultData.observe(viewLifecycleOwner) {
            if (it?.main != null) {
                insertCurrentWeather(it)
            }
        }

        // Observe weather log red from database
        weatherHistoryData.observe(viewLifecycleOwner) {
            adapter.submitList(weatherHistoryData.value)
        }

        selectedPosition.observe(viewLifecycleOwner) {
            //TODO: when orientation changed from portrait to landscape,
            // selected position needed to be changed to -1
            // or perform to pass selectedPosition item data(WeatherResult) to details
            Timber.d("isTablet=$isTablet, selectedPosition=$it")
            if (isTablet) {
                adapter.notifyItemChanged(it)
                previousPosition.value?.let { previousPosition ->
                    adapter.notifyItemChanged(
                        previousPosition
                    )
                }
            }
        }

        // Observe if some message needed to be shown in weather log fragment
        showMessageEvent.observe(viewLifecycleOwner) {
            Toast.makeText(application, it, Toast.LENGTH_SHORT).show()
        }

        // Observe if navigation to details fragment needed
        /** To support multipane screens:
         * 1. Additional navigation graph created (navigation_list here)
         * 2. bools.xml added with isTable value (probably replace with child nav host fragment
         * existence check)
         * 3. Multipane views added (layout-land/fragment_weather_list here)*/
        navigateToDetailsEvent.observe(viewLifecycleOwner) {
            when (isTablet) {
                true -> {
                    val navHostFragment =
                        childFragmentManager.findFragmentById(R.id.details_nav_host_fragment) as NavHostFragment

                    navHostFragment.navController.navigate(
                        WeatherListFragmentDirections.actionWeatherListFragmentToWeatherDetailsFragment(
                            it
                        )
                    )
                }

                false -> {
                    findNavController().navigate(
                        WeatherListFragmentDirections.actionWeatherListFragmentToWeatherDetailsFragment(
                            it
                        )
                    )
                }
            }
        }

        // Observe saved image uri for weather log sharing
        setImageUriToShareEvent.observe(viewLifecycleOwner) {
            shareWeatherImage(it)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.weather_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_clear -> weatherListViewModel.clearHistory()
            R.id.menu_share -> requestSharingPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Transitions
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun getWeatherByLocation(currentLocation: Pair<Double, Double>) {
        weatherListViewModel.getWeatherResultData(
            currentLocation.first,
            currentLocation.second,
            application.getString(R.string.temperature_unit),
            application.getString(R.string.lang),
            application.getString(R.string.weather_api_key)
        )
    }

    private fun getWeatherByCityCode() {
        weatherListViewModel.getWeatherResultData(
            getString(R.string.city_id),
            getString(R.string.temperature_unit),
            getString(R.string.lang),
            getString(R.string.weather_api_key)
        )
    }

    private fun shareWeatherImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
    }

    private fun getBitmapFromViewAndStoreImage() {
        with(binding.scrollableWrapper) {
            isDrawingCacheEnabled = true
            val bitmap = loadBitmapFromView(this)
            isDrawingCacheEnabled = false

            weatherListViewModel.store(bitmap)
        }
    }

    private fun loadBitmapFromView(scrollView: NestedScrollView): Bitmap {
        val bitmap = Bitmap.createBitmap(
            scrollView.getChildAt(0).width * 2,
            scrollView.getChildAt(0).height * 2,
            Bitmap.Config.ARGB_8888
        )

        Canvas(bitmap).apply {
            scale(2.0f, 2.0f)
            drawColor(Color.WHITE)
            scrollView.getChildAt(0).draw(this)
        }
        return bitmap
    }

    private fun WeatherListViewModel.store(imageData: Bitmap) {
        showProgress(true)

        val fileName = "${getString(R.string.app_name)}_${System.currentTimeMillis()}.jpg"
        lifecycleScope.launch {
            setImageUriToShareEvent.postValue(
                storeImage(requireContext(), imageData, fileName)
            )

            showProgress(false)
        }
    }

}