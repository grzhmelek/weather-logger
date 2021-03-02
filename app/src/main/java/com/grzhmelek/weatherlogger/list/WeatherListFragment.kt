package com.grzhmelek.weatherlogger.list

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.transition.MaterialElevationScale
import com.grzhmelek.weatherlogger.R
import com.grzhmelek.weatherlogger.database.WeatherDatabase
import com.grzhmelek.weatherlogger.database.WeatherDatabaseDao
import com.grzhmelek.weatherlogger.databinding.FragmentWeatherListBinding


class WeatherListFragment : Fragment() {

    companion object {
        private val TAG = WeatherListFragment::class.simpleName

        private const val PERMISSIONS_FOR_LOCATION_CODE = 1001
        private const val PERMISSIONS_FOR_SHARING_CODE = 1002
    }

    private lateinit var binding: FragmentWeatherListBinding

    private lateinit var application: Application

    private lateinit var dataSource: WeatherDatabaseDao

    private lateinit var viewModelFactory: WeatherListViewModelFactory

    private val weatherListViewModel: WeatherListViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(WeatherListViewModel::class.java)
    }

    private val permissionsForLocation = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val permissionsForSharing = listOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherListBinding.inflate(inflater)

        application = requireNotNull(this.activity).application

        dataSource = WeatherDatabase.getInstance(application).weatherDatabaseDao

        viewModelFactory = WeatherListViewModelFactory(application, dataSource)

        binding.weatherListViewModel = weatherListViewModel
        binding.lifecycleOwner = this

        val adapter = WeatherRecyclerViewAdapter(
            WeatherClickListener { weather, position ->
                // Transitions
                exitTransition = MaterialElevationScale(true).apply {
                    duration =
                        resources.getInteger(R.integer.transition_motion_duration_short).toLong()
                }

                reenterTransition = MaterialElevationScale(true).apply {
                    duration =
                        resources.getInteger(R.integer.transition_motion_duration_long).toLong()
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

        Log.d(TAG, "weatherResultData is ${weatherListViewModel.weatherResultData.value}")
        val isTablet = requireContext().resources.getBoolean(R.bool.isTablet)

        // Observe if need to retrieve weather from CurrentWeatherData API
        weatherListViewModel.weatherNeeded.observe(viewLifecycleOwner, {
            if (it == true) {
                checkForPermission(permissionsForLocation)
                weatherListViewModel.weatherGettingComplete()
            }
        })

        // Observe getting current location
        weatherListViewModel.currentLocation.observe(viewLifecycleOwner, {
            if (it != null) {
                Log.d(TAG, "Current location=$it")
                getWeatherByLocation(it)
                weatherListViewModel.noCurrentLocationNeeded()
            }
        })

        // Observe weather data retrieved from CurrentWeatherData API
        weatherListViewModel.weatherResultData.observe(viewLifecycleOwner, {
            if (it?.main != null) {
                weatherListViewModel.insertCurrentWeather(it)
            }
        })

        // Observe weather log red from database
        weatherListViewModel.weatherHistory.observe(viewLifecycleOwner, {
            adapter.submitList(weatherListViewModel.weatherHistory.value)
        })

        weatherListViewModel.selectedPosition.observe(viewLifecycleOwner, {
            //TODO: when orientation changed from portrait to landscape,
            // selected position needed to be changed to -1
            // or perform to pass selectedPosition item data(WeatherResult) to details
            Log.d(TAG, "isTablet=$isTablet, selectedPosition=$it")
            if (isTablet) {
                adapter.notifyItemChanged(it)
                weatherListViewModel.previousPosition.value?.let { previousPosition ->
                    adapter.notifyItemChanged(
                        previousPosition
                    )
                }
            }
        })

        // Observe if some message needed to be shown in weather log fragment
        weatherListViewModel.showMessage.observe(viewLifecycleOwner, {
            if (it != null) {
                Toast.makeText(application, it, Toast.LENGTH_SHORT).show()
                weatherListViewModel.showMessageComplete()
            }
        })

        // Observe if navigation to details fragment needed
        /** To support multipane screens:
         * 1. Additional navigation graph created (navigation_list here)
         * 2. bools.xml added with isTable value (probably replace with child nav host fragment
         * existence check)
         * 3. Multipane views added (layout-land/fragment_weather_list here)*/
        weatherListViewModel.navigateToDetails.observe(viewLifecycleOwner, {
            if (it != null) {
                when (isTablet) {
                    true -> {
                        val navHostFragment =
                            childFragmentManager.findFragmentById(R.id.details_nav_host_fragment)
                                    as NavHostFragment

//                        navHostFragment.navController.navigate(WeatherListFragmentDirections.actionWeatherListFragmentToWeatherDetailsFragment(
//                                it
//                        ))
                        val bundle = bundleOf("weatherResult" to it)
                        navHostFragment.navController.navigate(R.id.weatherDetailsFragment, bundle)
                    }
                    false -> {
                        this.findNavController().navigate(
                            WeatherListFragmentDirections.actionWeatherListFragmentToWeatherDetailsFragment(
                                it
                            )
                        )
                    }
                }
                weatherListViewModel.navigateToDetailsComplete()
            }
        })

        // Observe saved image uri for weather log sharing
        weatherListViewModel.imageUriToShare.observe(viewLifecycleOwner, {
            if (it != null) {
                shareWeatherImage(it)
            }
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.weather_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_clear -> weatherListViewModel.clearHistory()
            R.id.menu_share -> checkForPermission(permissionsForSharing)
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
            application.getString(R.string.city_id),
            application.getString(R.string.temperature_unit),
            application.getString(R.string.lang),
            application.getString(R.string.weather_api_key)
        )
    }

    private fun shareWeatherImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
        weatherListViewModel.shareImageComplete()
    }

    private fun checkForPermission(permissions: List<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsDenied = arrayListOf<String>()
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        application.applicationContext,
                        permission
                    ) ==
                    PackageManager.PERMISSION_DENIED
                ) permissionsDenied.add(permission)
            }
            //there are permissions denied
            if (permissionsDenied.isNotEmpty()) {

                //show popup to request runtime permission
                requestPermissions(
                    permissionsDenied.toTypedArray(),
                    getPermissionCode(permissions)
                )
            } else {
                //permission already granted
                performAction(permissions)
            }
        } else {
            //system OS is < Marshmallow
            performAction(permissions)
        }
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Check if all permissions from passed permission list are granted
        var grantedPermissionCount = 0
        if (grantResults.isNotEmpty()) {
            if (permissions.toList() == permissionsForLocation
                && ContextCompat.checkSelfPermission(
                    application.applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                grantedPermissionCount = permissionsForLocation.size
            } else {
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) grantedPermissionCount++
                }
            }
        }
        if (grantedPermissionCount == permissions.size) {
            //permission from popup granted
            performActionByRequestCode(requestCode)
        } else {
            //permission from popup denied
            // If location permissions denied show weather statically for Riga only
            when (permissions.toList()) {
                permissionsForLocation -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.message_permission_location_denied),
                        Toast.LENGTH_LONG
                    ).show()

                    getWeatherByCityCode()
                }
                else -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.message_permission_denied),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getPermissionCode(permissions: List<String>): Int {
        return when (permissions) {
            permissionsForLocation -> PERMISSIONS_FOR_LOCATION_CODE
            permissionsForSharing -> PERMISSIONS_FOR_SHARING_CODE
            else -> -1
        }
    }

    private fun performAction(permissions: List<String>) {
        if (permissions == permissionsForLocation) {
            weatherListViewModel.getLocation(this.requireActivity())
        } else if (permissions == permissionsForSharing)
            weatherListViewModel.getBitmapFromViewAndStoreImage(binding.scrollableWrapper)
    }

    private fun performActionByRequestCode(requestCode: Int) {
        when (requestCode) {
            PERMISSIONS_FOR_LOCATION_CODE ->
                weatherListViewModel.getLocation(this.requireActivity())
            PERMISSIONS_FOR_SHARING_CODE ->
                weatherListViewModel.getBitmapFromViewAndStoreImage(binding.scrollableWrapper)
        }
    }
}