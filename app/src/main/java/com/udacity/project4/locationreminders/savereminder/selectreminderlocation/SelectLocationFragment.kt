package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.google.android.gms.common.api.ApiException
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
    val _selectLocationViewModel: SelectLocationViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    lateinit var mMap: GoogleMap
    private lateinit var fragmentContext: Context
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _selectLocationViewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fragmentContext = binding.saveLocBtton.context

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(fragmentContext)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupObserver()

////        TODO: call this function after the user confirms on the selected location
//        onLocationSelected()

        return binding.root
    }

//    private fun onLocationSelected() {
//        //        TODO: When the user confirms on the selected location,
//        //         send back the selected location details to the view model
//        //         and navigate back to the previous fragment to save the reminder and add the geofence
//    }
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
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // add a new marker to the map
        addMarkerOnMap("Marker in Sydney", LatLng(-34.0, 151.0), moveCamera = true)

        //add a map overview
        addOverView(R.drawable.map, LatLng(-34.0, 151.0))

        //Change the map style
        setMapStyle()

        //enable the user current location
        enableMyLocation()

    }

    private fun addMarkerOnMap(
        title: String,
        latLng: LatLng,
        moveCamera: Boolean = true,
        zoomLevel: Float = 12.0f
    ) {

        // Add a marker and move the camera
        val options = MarkerOptions()
        options.position(latLng)
        options.title(title)

        mMap.addMarker(options)

        if (moveCamera) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
            mMap.moveCamera(cameraUpdate)
        }
    }


    private fun addOverView(drawableRes: Int, homeLatLng: LatLng) {
        val overlaySize = 100f
        val androidOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(drawableRes))
            .position(homeLatLng, overlaySize)

        mMap.addGroundOverlay(androidOverlay)
    }

    private fun setMapStyle() {
        try {

            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("TAG", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("TAG", "Can't find style. Error: ", e)
        }
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

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
        const val REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 2
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

    override fun onDestroy() {
        super.onDestroy()
        _selectLocationViewModel.onClear()
    }
}
