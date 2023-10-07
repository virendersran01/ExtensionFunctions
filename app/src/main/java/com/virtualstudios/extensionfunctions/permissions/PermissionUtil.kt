package com.virtualstudios.extensionfunctions.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.it.ozzierides.rider.R
import java.util.concurrent.TimeUnit

object PermissionUtil {

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @JvmInline
    value class Permission(val result: ActivityResultLauncher<Array<String>>)

    sealed class PermissionState {
        object Granted : PermissionState()
        object Denied : PermissionState()
        object PermanentlyDenied : PermissionState()

        object ShowRationale : PermissionState()

    }

    private fun getPermissionState(
        activity: Activity?,
        result: Map<String, Boolean>
    ): PermissionState {
        val deniedList: List<String> = result.filter {
            it.value.not()
        }.map {
            it.key
        }

        var state = when (deniedList.isEmpty()) {
            true -> PermissionState.Granted
            false -> PermissionState.Denied
        }

        if (state == PermissionState.Denied) {
            val permanentlyMappedList = deniedList.map {
                activity?.let { activity ->
                    shouldShowRequestPermissionRationale(activity, it)
                }
            }

            state = if (permanentlyMappedList.contains(false)) {
                PermissionState.PermanentlyDenied
            } else {
                PermissionState.ShowRationale
            }
        }
        return state
    }

    fun Fragment.registerPermission(onPermissionResult: (PermissionState) -> Unit): Permission {
        return Permission(
            this.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                onPermissionResult(getPermissionState(activity, it))
            }
        )
    }

    fun AppCompatActivity.registerPermission(onPermissionResult: (PermissionState) -> Unit): Permission {
        return Permission(
            this.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                onPermissionResult(getPermissionState(this, it))
            }
        )
    }

    fun Permission.launchSinglePermission(permission: String) {
        this.result.launch(arrayOf(permission))
    }

    fun Permission.launchMultiplePermission(permissionList: Array<String>) {
        this.result.launch(permissionList)
    }

    fun Context.showLocationPermissionRationaleDialog(onAction: (Boolean) -> Unit) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(false)
        alertBuilder.setTitle(getString(R.string.loc_permission_needed))
        alertBuilder.setMessage(getString(R.string.app_need_loc_permission))
        alertBuilder.setPositiveButton(
            getString(R.string.ok_uppercase)
        ) { dialog, _ ->
            dialog.dismiss()
            onAction(true)
        }
        alertBuilder.setNegativeButton(getString(R.string.cancel_uppercase)) { dialog, _ ->
            dialog.dismiss()
            onAction(false)
        }

        val alert = alertBuilder.create()
        alert.show()
    }

    fun Context.showLocationPermissionDenyDialog(onAction: (Boolean) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.loc_permission_needed))
            .setMessage(getString(R.string.app_need_loc_permission))
            .setPositiveButton(getString(R.string.ok_uppercase)) { dialog, _ ->
                dialog.dismiss()
                onAction(true)
            }
            .setNegativeButton(getString(R.string.no_uppercase)) { dialog, _ ->
                dialog.dismiss()
                onAction(false)
            }
            .create()
            .show()
    }


    fun Context.navigateToAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    fun createLocationRequest(updateInterval: Long = 1) =
        LocationRequest.Builder(TimeUnit.SECONDS.toMillis(updateInterval)).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            setIntervalMillis(TimeUnit.SECONDS.toMillis(updateInterval))
        }.build()

    fun Context.isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    fun Activity.checkLocationPermissions(onPermissionResult: (PermissionState) -> Unit) {
        if (this.isPermissionGranted(
                locationPermissions.first()
            ) && this.isPermissionGranted(
                locationPermissions.last()
            )
        ) {
            onPermissionResult(PermissionState.Granted)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val showRationale1 =
                    shouldShowRequestPermissionRationale(locationPermissions.first())
                val showRationale2 =
                    shouldShowRequestPermissionRationale(locationPermissions.last())
                if (showRationale1 && showRationale2) {
                    onPermissionResult(PermissionState.ShowRationale)
                } else {
                    onPermissionResult(PermissionState.Denied)
                }
            } else {
                onPermissionResult(PermissionState.Denied)
            }
        }
    }

    fun AppCompatActivity.requestLocationPermissions(onPermissionResult: (PermissionState) -> Unit) {
        val permissions = registerPermission {
            onPermissionResult(it)
        }
        permissions.launchMultiplePermission(locationPermissions)
    }

    fun Context.toastPermissionDenied() {
        toast(getString(R.string.permission_denied))
    }

    fun AppCompatActivity.launchLocationPermissions(onPermissionResult: (PermissionState) -> Unit){
        val launcher = activityResultRegistry.register("Result", ActivityResultContracts.RequestMultiplePermissions()){
            onPermissionResult(getPermissionState(this, it))
        }
        launcher.launch(locationPermissions)
    }

}
