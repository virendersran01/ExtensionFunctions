package com.virtualstudios.extensionfunctions.location

import com.google.android.gms.common.api.ResolvableApiException

private fun checkLocationPermissions() {
    checkLocationPermissions {
        when (it) {
            PermissionUtil.PermissionState.Denied -> requestLocationPermissions()
            PermissionUtil.PermissionState.Granted -> requestDeviceLocation()
            PermissionUtil.PermissionState.PermanentlyDenied -> showLocationPermissionDenyDialog()
            PermissionUtil.PermissionState.ShowRationale -> showLocationPermissionRationale()
        }
    }
}

private fun requestDeviceLocation() {
    this.requestDeviceLocation { locationState ->
        when (locationState) {
            is LocationUtil.LocationState.GoToLocationSetting -> {
                requestDeviceLocationEnable(locationState.exception)
            }

            LocationUtil.LocationState.LocationDisabled -> {
                showLocationNotEnabledToast()
                finish()
            }

            LocationUtil.LocationState.LocationEnabled -> navigate()
        }
    }
}

private val locationPermissionResult = registerPermission {
    when (it) {
        PermissionUtil.PermissionState.Denied -> requestLocationPermissions()
        PermissionUtil.PermissionState.Granted -> requestDeviceLocation()
        PermissionUtil.PermissionState.PermanentlyDenied -> showLocationPermissionDenyDialog()
        PermissionUtil.PermissionState.ShowRationale -> showLocationPermissionRationale()
    }
}

private fun showLocationPermissionDenyDialog() {
    logDebug("showLocationPermissionDenyDialog")
    isAnyDialogDisplaying = true
    showLocationPermissionDenyDialog { state ->
        if (state) {
            navigateToAppSettings()
            isAnyDialogDisplaying = false
        } else {
            toastPermissionDenied()
            finish()
        }
    }
}

private fun showLocationPermissionRationale() {
    logDebug("showLocationPermissionRationale")
    isAnyDialogDisplaying = true
    showLocationPermissionRationaleDialog {
        if (it) {
            requestLocationPermissions()
        } else {
            toastPermissionDenied()
            finish()
        }
    }
}

private fun requestLocationPermissions() {
    locationPermissionResult.launchMultiplePermission(locationPermissions)
}

private val locationSettingResult = registerLocationSetting { isLocationEnabled ->
    if (isLocationEnabled) {
        navigate()
    } else {
        showLocationNotEnabledToast()
        finish()
    }
}

private fun requestDeviceLocationEnable(exception: ResolvableApiException) {
    isAnyDialogDisplaying = true
    locationSettingResult.launchSettingRequest(exception)
}