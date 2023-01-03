package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

class SelectLocationViewModel(val app: Application) : BaseViewModel(app) {

    private val _saveLocationClicked = MutableLiveData<Boolean>()
    val saveLocationClicked: LiveData<Boolean>
        get() = _saveLocationClicked

    private val _selectLocation = MutableLiveData<ReminderDTO>()
    val selectLocation: LiveData<ReminderDTO>
        get() = _selectLocation

    private val _selectPoint = MutableLiveData<PointOfInterest>()
    val selectPoint: LiveData<PointOfInterest>
        get() = _selectPoint


    init {
        _selectLocation.value = null
        _saveLocationClicked.value = false
        _selectPoint.value = null
    }

    fun setLocation(location: String, latitude: Double, longitude: Double, poi: PointOfInterest?) {
        _selectLocation.value = ReminderDTO(null, null, location, latitude, longitude)
        _selectPoint.value = poi
    }

    fun onSaveLocationClicked() {
        _saveLocationClicked.value = true
    }

    fun onSaveLocationDone() {
        _saveLocationClicked.value = false
    }

    fun onClear() {
        _selectLocation.value = null
        _saveLocationClicked.value = false
        _selectPoint.value = null
    }
}