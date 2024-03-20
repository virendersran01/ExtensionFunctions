package com.virtualstudios.extensionfunctions.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.virtualstudios.extensionfunctions.R
import toast
import java.util.concurrent.TimeUnit

val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
const val READ_MEDIA_VISUAL_USER_SELECTED = Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED

fun Context.showPermissionDeniedToast() {
    toast(getString(R.string.permission_denied))
}

fun AppCompatActivity.navigateToAppSettings(onResult: () -> Unit) {
    val launcher = activityResultRegistry.register(
        "Result",
        ActivityResultContracts.StartActivityForResult()
    ) {
        onResult()
    }
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
//            addCategory(Intent.CATEGORY_DEFAULT)
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }.also {
        launcher.launch(it)
    }
}

fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED

fun Context.hasPermissions(vararg permissions: String) = permissions.all { permission ->
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.isLocationPermissionsGranted() =
    isPermissionGranted(locationPermissions.first()) && isPermissionGranted(
        locationPermissions.last()
    )

fun Context.showRationaleDialog(
    title: String,
    message: String,
    positiveButtonText: String,
    negativeButtonText: String?,
    onAction: (Boolean) -> Unit
) {
    AlertDialog.Builder(this).apply {
        setCancelable(false)
        setTitle(title)
        setMessage(message)
        setPositiveButton(positiveButtonText) { dialog, which ->
            dialog.dismiss()
            onAction(true)
        }
        negativeButtonText?.let {
            setNegativeButton(negativeButtonText) { dialog, which ->
                dialog.dismiss()
                onAction(false)
            }
        }
    }.also {
        it.create().show()
    }
}


fun createLocationRequest(updateInterval: Long = 1) =
    LocationRequest.Builder(TimeUnit.SECONDS.toMillis(updateInterval)).apply {
        setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        setIntervalMillis(TimeUnit.SECONDS.toMillis(updateInterval))
    }.build()


@RequiresApi(Build.VERSION_CODES.M)
fun AppCompatActivity.checkForLocationPermissions(onProceed: () -> Unit) {
    var requestLauncher: ActivityResultLauncher<Array<String>>? = null
    val launcher = activityResultRegistry.register(
        "Result",
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.isNotEmpty()) {
            val deniedList: List<String> = permissions.filter {
                it.value.not()
            }.map {
                it.key
            }
            if (deniedList.isEmpty()) {
                onProceed()
            } else {
                val permanentlyMappedList = deniedList.map {
                    shouldShowRequestPermissionRationale(it)
                }
                if (permanentlyMappedList.contains(false)) {
                    showRationaleDialog(
                        title = getString(R.string.loc_permission_needed),
                        message = getString(R.string.app_need_loc_permission),
                        positiveButtonText = getString(R.string.settings_camelcase),
                        negativeButtonText = getString(android.R.string.cancel)
                    ) {
                        if (it) {
                            navigateToAppSettings {
                                if (isPermissionGranted(locationPermissions.first()) && isPermissionGranted(
                                        locationPermissions.last()
                                    )
                                ) {
                                    onProceed()
                                } else {
                                    showPermissionDeniedToast()
                                    finish()
                                }
                            }

                        } else {
                            showPermissionDeniedToast()
                            finish()
                        }
                    }

                } else {
                    showRationaleDialog(
                        title = getString(R.string.loc_permission_needed),
                        message = getString(R.string.app_need_loc_permission),
                        positiveButtonText = getString(R.string.ok),
                        negativeButtonText = getString(android.R.string.cancel)
                    ) {
                        if (it) {
                            requestLauncher?.launch(locationPermissions) ?: run {
                                showPermissionDeniedToast()
                                finish()
                            }
                        } else {
                            showPermissionDeniedToast()
                            finish()
                        }
                    }

                }
            }
        }
    }
    requestLauncher = launcher

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (isLocationPermissionsGranted()) {
            onProceed()
        } else {
            val showRationale1 =
                shouldShowRequestPermissionRationale(locationPermissions.first())
            val showRationale2 =
                shouldShowRequestPermissionRationale(locationPermissions.last())
            if (showRationale1 && showRationale2) {
                showRationaleDialog(
                    title = getString(R.string.loc_permission_needed),
                    message = getString(R.string.app_need_loc_permission),
                    positiveButtonText = getString(R.string.ok),
                    negativeButtonText = getString(android.R.string.cancel)
                ) {
                    if (it) {
                        launcher.launch(locationPermissions)
                    } else {
                        showPermissionDeniedToast()
                        finish()
                    }
                }
            } else {
                launcher.launch(locationPermissions)
            }
        }
    } else {
        onProceed()
    }
}


fun AppCompatActivity.requestDeviceLocation(onLocationStateChanged: (Boolean) -> Unit) {
    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(createLocationRequest())
    val client: SettingsClient = LocationServices.getSettingsClient(this)
    val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

    task.addOnSuccessListener { locationSettingsResponse ->
        val state = locationSettingsResponse.locationSettingsStates
        state?.let { locationSettingsStates ->
            if (locationSettingsStates.isGpsPresent && locationSettingsStates.isLocationUsable) {
                onLocationStateChanged(true)
            } else {
                onLocationStateChanged(false)
            }
        } ?: onLocationStateChanged(false)
    }

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                registerLocationSetting(exception) {
                    if (it) {
                        onLocationStateChanged(true)
                    } else {
                        onLocationStateChanged(false)
                    }
                }
            } catch (sendEx: IntentSender.SendIntentException) {
                onLocationStateChanged(false)
            }
        }
    }
}

fun AppCompatActivity.registerLocationSetting(
    exception: ResolvableApiException,
    onSettingResult: (Boolean) -> Unit
) {
    val launcher = activityResultRegistry.register(
        "Result",
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onSettingResult(true)
        } else {
            onSettingResult(false)
        }

    }
    launcher.launch(IntentSenderRequest.Builder(exception.resolution).build())
}


fun Context.showLocationNotEnabledToast() {
    toast("getString(R.string.location_not_enabled_please_turn_on_location)")
}

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


fun AppCompatActivity.checkForPostNotificationsPermissions(block: () -> Unit) {
    val launcher = activityResultRegistry.register(
        "Result",
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            block()
        } else {
            showRationaleDialog(
                title = getString(R.string.unable_to_get_notified),
                message = getString(R.string.app_need_post_notification_permission),
                positiveButtonText = getString(android.R.string.ok),
                negativeButtonText = null
            ) {
                block()
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                block()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showRationaleDialog(
                    title = getString(R.string.enable_notifications),
                    message = getString(R.string.grant_notifications_permissions_from_settings),
                    positiveButtonText = getString(R.string.settings_camelcase),
                    negativeButtonText = getString(android.R.string.cancel)
                ) {
                    if (it) {
                        navigateToAppSettings {
                            block()
                        }
                    } else {
                        block()
                    }
                }
            }

            else -> {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    } else {
        block()
    }
}


fun AppCompatActivity.checkForCameraPermissions(onPermissionResult: (Boolean) -> Unit) {
    val launcher = activityResultRegistry.register(
        "Result",
        ActivityResultContracts.RequestPermission()
    ) {
        onPermissionResult(it)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                onPermissionResult(true)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRationaleDialog(
                    title = getString(R.string.camera_permission_needed),
                    message = getString(R.string.camera_permission_info),
                    positiveButtonText = getString(android.R.string.ok),
                    negativeButtonText = getString(android.R.string.cancel)
                ) {
                    if (it) {
                        launcher.launch(Manifest.permission.CAMERA)
                    } else {
                        showPermissionDeniedToast()
                        onPermissionResult(false)
                    }
                }
            }

            else -> {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }
    } else {
        onPermissionResult(true)
    }
}

fun AppCompatActivity.checkForStoragePermissions(onPermissionResult: (Boolean) -> Unit) {
    val launcher = activityResultRegistry.register(
        "Result",
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            (
                    ContextCompat.checkSelfPermission(
                        this,
                        READ_MEDIA_IMAGES
                    ) == PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_VIDEO
                            ) == PERMISSION_GRANTED
                    )
        ) {
            onPermissionResult(true)
            // Full access on Android 13 (API level 33) or higher
        } else if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(
                this,
                READ_MEDIA_VISUAL_USER_SELECTED
            ) == PERMISSION_GRANTED
        ) {
            onPermissionResult(true)
            // Partial access on Android 14 (API level 34) or higher
        } else if (ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED
        ) {
            onPermissionResult(true)
            // Full access up to Android 12 (API level 32)
        } else {
            onPermissionResult(false)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        launcher.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED))
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher.launch(arrayOf(READ_MEDIA_IMAGES))
    } else {
        launcher.launch(arrayOf(READ_EXTERNAL_STORAGE))
    }
}






