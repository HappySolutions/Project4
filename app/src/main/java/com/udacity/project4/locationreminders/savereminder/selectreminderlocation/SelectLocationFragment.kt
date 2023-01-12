package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback  {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private val _selectLocationViewModel: SelectLocationViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fragmentContext: Context
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _selectLocationViewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fragmentContext = binding.saveLocBtton.context

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(fragmentContext)

        val mapFragment = childFragmentManager.findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupObserver()

        return binding.root
    }

    private fun setupObserver() {
    _selectLocationViewModel.saveLocationClicked.observe(
        viewLifecycleOwner
    ) { isLocationSaved ->
        if (isLocationSaved) {
            onLocationSelected()
            _selectLocationViewModel.onSaveLocationDone()
        }
    }
}

    private fun onLocationSelected() {
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
        val reminderDTO = _selectLocationViewModel.selectLocation.value
        if (reminderDTO != null) {
            _viewModel.latitude.value = reminderDTO.latitude
            _viewModel.longitude.value = reminderDTO.longitude
            _viewModel.reminderSelectedLocationStr.value = reminderDTO.location
        }
        val selectedPOI = _selectLocationViewModel.selectPoint.value
        if (selectedPOI != null) {
            _viewModel.selectedPOI.value = selectedPOI
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        //Change the map type based on the user's selection.
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
        enableMyLocation()

    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isForegroundLocationGrantedFromContext(fragmentContext)) {
            if (areLocationServicesEnabled(fragmentContext)) {
                mMap.isMyLocationEnabled = true
                zoomToUserLocation()
                setMapClickListener()
                setPointClick()
            } else {
                promptUserToEnableLocationServices()
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun zoomToUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                val zoomLevel = 15f
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel))
            }
        }
    }

    private fun promptUserToEnableLocationServices() {
        _viewModel.showErrorMessage.postValue(fragmentContext.getString(R.string.location_required_error))
    }

    private fun setMapClickListener() {
        mMap.setOnMapClickListener { location ->
            if (_selectLocationViewModel.selectLocation.value == null) {
                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(location.latitude, location.longitude))
                        .title(fragmentContext.getString(R.string.unknown_location))
                )
                _selectLocationViewModel.setLocation(
                    fragmentContext.getString(R.string.unknown_location),
                    location.latitude,
                    location.longitude,
                    null
                )
            } else {
                _viewModel.showErrorMessage.postValue(fragmentContext.getString(R.string.only_one_location_allowed))
            }
        }
    }

    private fun setPointClick() {
        mMap.setOnPoiClickListener { point ->
            if (_selectLocationViewModel.selectLocation.value == null) {
                val poiMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(point.latLng)
                        .title(point.name)
                )
                poiMarker.showInfoWindow()
                _selectLocationViewModel.setLocation(
                    point.name,
                    point.latLng.latitude,
                    point.latLng.longitude,
                    point
                )
            } else {
                _viewModel.showErrorMessage.postValue(fragmentContext.getString(R.string.only_one_location_allowed))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isRequestCodeEqualLocationPermissionCode(requestCode)) {
            if (isForegroundLocationPermissionGrantedFromResult(grantResults)) {
                enableMyLocation()
            } else {
                if ((areLocationServicesEnabled(fragmentContext))) {
                    promptUserToGrantLocationPermission()
                } else {
                    promptUserToEnableLocationServices()
                }
            }
        }
    }

    private fun promptUserToGrantLocationPermission() {
        _viewModel.showErrorMessage.postValue(fragmentContext.getString(R.string.permission_denied_explanation))
    }
    override fun onStart() {
        super.onStart()
        checkPermissionsthenEnableGeofencing()
    }

    private fun checkPermissionsthenEnableGeofencing() {
        if (areforegroundAndBackgroundLocationPermissionApproved(fragmentContext)) {
            checkDeviceLocSettingsthenStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }
    private fun checkDeviceLocSettingsthenStartGeofence(resolve: Boolean = true) {
        val locationSettingsResponseTask = getLocationSettingsResponseTask(fragmentContext)

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocSettingsthenStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                println(fragmentContext.getString(R.string.location_enabled))
            }
        }
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (areforegroundAndBackgroundLocationPermissionApproved(fragmentContext))
            return

        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = getForegroundAndBackgroundResultCode(permissionsArray)

        requestPermissions(
            permissionsArray,
            resultCode
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        _selectLocationViewModel.onClear()
    }
}
