package com.virtualstudios.extensionfunctions.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class LocationService {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location {

        if (!context.hasPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            throw throw LocationServiceException.MissingPermissionException()
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled) {
            throw LocationServiceException.LocationDisabledException()
        }
        if (!isNetworkEnabled) {
            throw LocationServiceException.NoInternetException()
        }

        val locationProvider = LocationServices.getFusedLocationProviderClient(context)
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        try {
            val location = locationProvider.getCurrentLocation(request, null).await()

            return location

        } catch (e: Exception) {
            throw LocationServiceException.UnknownException(e)
        }

    }

    fun Context.hasPermissions(vararg permissions: String) =
        permissions.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }


    sealed class LocationServiceException : Exception() {
        class MissingPermissionException : LocationServiceException()
        class LocationDisabledException : LocationServiceException()
        class NoInternetException : LocationServiceException()
        class UnknownException(val exception: Exception) : LocationServiceException()
    }

}
// uses
/*scope.launch {
    try {
        val location = LocationService().getCurrentLocation(context)
        currentLocation =
            "Latitude: ${location.latitude}, Longitude: ${location.longitude}"

    } catch (e: LocationService.LocationServiceException) {
        when (e) {
            is LocationService.LocationServiceException.LocationDisabledException -> {
                //handle location disabled, show dialog or a snack-bar to enable location
            }

            is LocationService.LocationServiceException.MissingPermissionException -> {
                permissionRequest.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

            is LocationService.LocationServiceException.NoNetworkEnabledException -> {
                //handle no network enabled, show dialog or a snack-bar to enable network
            }

            is LocationService.LocationServiceException.UnknownException -> {
                //handle unknown exception
            }
        }
    }
}*/
