package com.virtualstudios.extensionfunctions.location

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.it.ozzierides.rider.R
import com.virtualstudios.extensionfunctions.R
import com.virtualstudios.extensionfunctions.permissions.PermissionUtil
import toast

object LocationUtil {

    sealed class LocationState {
        object LocationEnabled : LocationState()
        object LocationDisabled : LocationState()

        data class GoToLocationSetting(val exception: ResolvableApiException) : LocationState()

    }

    @JvmInline
    value class LocationSettings(val result: ActivityResultLauncher<IntentSenderRequest>)

    private fun Context.checkLocationEnabled() {
        val manager =
            getSystemService(FragmentActivity.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice()) {
            //requestLocationUpdates()
        } else {
            //enableLocation()
        }

    }


    private fun Context.hasGPSDevice(): Boolean {
        val mgr = getSystemService(FragmentActivity.LOCATION_SERVICE) as LocationManager
        val providers = mgr.allProviders
        return providers.contains(LocationManager.GPS_PROVIDER)
    }

    private fun Activity.checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(
                    this,
                    resultCode,
                    11
                )!!.show()
            } else {
                toast(getString(R.string.error))
            }
            return false
        }
        return true
    }

    fun AppCompatActivity.requestDeviceLocation(onLocationStateChanged: (LocationState) -> Unit) {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(PermissionUtil.createLocationRequest())
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            val state = locationSettingsResponse.locationSettingsStates
            state?.let { locationSettingsStates ->
                if (locationSettingsStates.isGpsPresent && locationSettingsStates.isLocationUsable) {
                    onLocationStateChanged(LocationState.LocationEnabled)
                } else {
                    onLocationStateChanged(LocationState.LocationDisabled)
                }
            } ?: onLocationStateChanged(LocationState.LocationDisabled)
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    onLocationStateChanged(LocationState.GoToLocationSetting(exception))
                } catch (sendEx: IntentSender.SendIntentException) {
                    onLocationStateChanged(LocationState.LocationDisabled)
                }
            }
        }
    }

    fun AppCompatActivity.registerLocationSetting(onSettingResult: (Boolean) -> Unit): LocationSettings {
        return LocationSettings(
            this.registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    onSettingResult(true)
                } else {
                    onSettingResult(false)
                }

            }
        )
    }

    fun LocationSettings.launchSettingRequest(exception: ResolvableApiException) {
        this.result.launch(IntentSenderRequest.Builder(exception.resolution).build())
    }

    fun Context.showLocationNotEnabledToast() {
        toast("Location not enabled. please turn on location")
    }

}

/*
val resolutionForLocationSettingsResult =
    registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onLocationEnableCheck(true)
        } else {
            onLocationEnableCheck(false)
        }

    }

val intentSenderRequest =
    IntentSenderRequest.Builder(exception.resolution).build()
resolutionForLocationSettingsResult.launch(intentSenderRequest)*/
