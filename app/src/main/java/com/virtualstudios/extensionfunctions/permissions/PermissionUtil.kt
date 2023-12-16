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
import com.virtualstudios.extensionfunctions.R
import toast
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
            getString(R.string.ok)
        ) { dialog, _ ->
            dialog.dismiss()
            onAction(true)
        }
        alertBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
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
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                onAction(true)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
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

   /* fun AppCompatActivity.navigateToAppSettings(onResult: () -> Unit) {
        val launcher = activityResultRegistry.register(
            "Result",
            ActivityResultContracts.StartActivityForResult()
        ) {
            onResult()
        }
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        launcher.launch(intent)
    }*/

    fun AppCompatActivity.navigateToAppSettings(onResult: () -> Unit) {
        val launcher = activityResultRegistry.register(
            "Result",
            ActivityResultContracts.StartActivityForResult()
        ) {
            onResult()
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        launcher.launch(intent)
    }

    fun AppCompatActivity.checkForPostNotificationsPermissions(proceed: () -> Unit){
        val launcher = activityResultRegistry.register(
            "Result",
            ActivityResultContracts.RequestPermission()
        ) {
            proceed()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    proceed()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showGoToNotificationSettingsDialog{
                        if (it){
                            navigateToAppSettings {
                                proceed()
                            }
                        }else{
                            proceed()
                        }
                    }
                }

                else -> {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            proceed()
        }
    }

    private fun Context.showGoToNotificationSettingsDialog(onAction: (Boolean) -> Unit) {
        val materialAlertDialogBuilder = AlertDialog.Builder(this).apply {
            setCancelable(false)
            setTitle(getString(R.string.enable_notifications))
            setMessage(getString(R.string.grant_notifications_permissions_from_settings))
            setPositiveButton(
                getString(R.string.settings_camelcase)
            ) { dialog, _ ->
                dialog.dismiss()
                onAction(true)
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                onAction(false)
            }
        }
        materialAlertDialogBuilder.create().show()
    }

}



/* pop delivery driver
object PermissionUtil {

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @JvmInline
    value class Permission(val result: ActivityResultLauncher<Array<String>>)

    sealed class PermissionState {
        data object Granted : PermissionState()
        data object Denied : PermissionState()
        data object PermanentlyDenied : PermissionState()

        data object ShowRationale : PermissionState()

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

    fun AppCompatActivity.launchLocationPermissions(onPermissionResult: (PermissionState) -> Unit) {
        val launcher = activityResultRegistry.register(
            "Result",
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            onPermissionResult(getPermissionState(this, it))
        }
        launcher.launch(locationPermissions)
    }

    fun AppCompatActivity.navigateToAppSettings(onResult: () -> Unit) {
        val launcher = activityResultRegistry.register(
            "Result",
            ActivityResultContracts.StartActivityForResult()
        ) {
            onResult()
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        launcher.launch(intent)
    }

    fun AppCompatActivity.checkForPostNotificationsPermissions(proceed: () -> Unit){
        val launcher = activityResultRegistry.register(
            "Result",
            ActivityResultContracts.RequestPermission()
        ) {
            proceed()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    proceed()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showGoToNotificationSettingsDialog{
                        if (it){
                            navigateToAppSettings {
                                proceed()
                            }
                        }else{
                            proceed()
                        }
                    }
                }

                else -> {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            proceed()
        }
    }

    private fun Context.showGoToNotificationSettingsDialog(onAction: (Boolean) -> Unit) {
        val materialAlertDialogBuilder = AlertDialog.Builder(this).apply {
            setCancelable(false)
            setTitle(getString(R.string.enable_notifications))
            setMessage(getString(R.string.grant_notifications_permissions_from_settings))
            setPositiveButton(
                getString(R.string.settings_camelcase)
            ) { dialog, _ ->
                dialog.dismiss()
                onAction(true)
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                onAction(false)
            }
        }
        materialAlertDialogBuilder.create().show()
    }

}*/

//pop delivery
/*object PermissionUtil {

    @JvmInline
    value class Permission(val result: ActivityResultLauncher<Array<String>>)

    sealed class PermissionState {
        data object Granted : PermissionState()
        data object Denied : PermissionState()
        data object PermanentlyDenied : PermissionState()

        data object ShowRationale : PermissionState()

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

    fun Context.isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    fun Context.toastPermissionDenied() {
        toast(getString(R.string.permission_denied))
    }

    fun AppCompatActivity.navigateToAppSettings(onResult: () -> Unit) {
        val launcher = activityResultRegistry.register(
            "Result",
            ActivityResultContracts.StartActivityForResult()
        ) {
            onResult()
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            *//*addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)*//*
        }
        launcher.launch(intent)
    }

    fun AppCompatActivity.checkForPostNotificationsPermissions(proceed: () -> Unit){
        val launcher = activityResultRegistry.register(
            "Result",
            ActivityResultContracts.RequestPermission()
        ) {
            proceed()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    proceed()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showGoToNotificationSettingsDialog{
                        if (it){
                            navigateToAppSettings {
                                proceed()
                            }
                        }else{
                            proceed()
                        }
                    }
                }

                else -> {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            proceed()
        }
    }

    private fun Context.showGoToNotificationSettingsDialog(onAction: (Boolean) -> Unit) {
        val materialAlertDialogBuilder = AlertDialog.Builder(this).apply {
            setCancelable(false)
            setTitle(getString(R.string.enable_notifications))
            setMessage(getString(R.string.grant_notifications_permissions_from_settings))
            setPositiveButton(
                getString(R.string.settings_camelcase)
            ) { dialog, _ ->
                dialog.dismiss()
                onAction(true)
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                onAction(false)
            }
        }
        materialAlertDialogBuilder.create().show()
    }

    fun AppCompatActivity.checkForCameraPermissions(onPermissionResult: (Boolean) -> Unit){
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
                    showCameraPermissionDialog{
                        if (it){
                            launcher.launch(Manifest.permission.CAMERA)
                        }else{
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

    private fun AppCompatActivity.showCameraPermissionDialog(onAction: (Boolean) -> Unit) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(false)
        alertBuilder.setTitle(getString(R.string.camera_permission_needed))
        alertBuilder.setMessage(getString(R.string.camera_permission_info))
        alertBuilder.setPositiveButton(
            android.R.string.ok
        ) { dialog, _ ->
            dialog.dismiss()
            onAction(true)
        }
        alertBuilder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
            toast(
                getString(R.string.permission_denied)
            )
            onAction(false)
        }
        val alert = alertBuilder.create()
        alert.show()
    }


}*/
