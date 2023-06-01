package com.virtualstudios.extensionfunctions

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

private const val TAG = "LocationListener=>"

/**
 * @author Manish Sharma - 21/01/2023
 *
 * Location listener that is lifecycle aware. Start update when calling class calls enable() method.
 * It automatically disconnects when activity goes to onStop and connects automatically when activity goes to onStart().
 *
 * @param context : Context of the calling activity.
 * @param lifeCycle : Life cycle of the calling activity.
 * @param result :  High order function that will return location to the calling activity.
 */

class LocationListener constructor(
    context: Context, private val lifeCycle: Lifecycle, private val result: (Location) -> Unit

) : DefaultLifecycleObserver, android.location.LocationListener {

    /**
     * Location Manager that will be used to get location updates.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private var locationManager: LocationManager =
        context.getSystemService(LocationManager::class.java)

    /**
     * Minimum time between location updates. Should be in milli-seconds.
     */
    private val timeForLocationUpdate: Long = 2000L

    /**
     * Minimum distance between location updates. Should be in meters.
     */
    private val distanceForLocationUpdate: Float = 2F

    /**
     * Flag to enable/disable location updates.
     */
    private var isEnable = false

    /**
     * Flag to know if location manager is connected/disconnected.
     */
    private var isConnected = false

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (isEnable) {
            connect()
        }

    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        disconnect()

    }

    /**
     *  To enable the location updates.
     *  Call it from activity to start location updates.
     */
    fun enable() {

        logDebug(message = "$TAG Enabling location manager...")
        isEnable = true
        if (lifeCycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            // Connect to the location if not connected.
            if (!isConnected) {
                connect()
            }
            logDebug(message = "$TAG Enabling location manager success.")

        } else {
            logDebug(message = "$TAG Enabling location manager failed because activity has not been started.")
        }

    }

    /**
     * To disable location updates manually.
     * Call this from activity to stop location updates manually.
     */
    fun disable() {
        logDebug(message = "$TAG Disabling location manager...")

        isEnable = false
        disconnect()

        logDebug(message = "$TAG Location manager disabled.")

    }

    /**
     * Call this method to start location updates.
     */
    @SuppressLint("MissingPermission")
    private fun connect() {

        logDebug(message = "$TAG Connecting to location manager...")

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, timeForLocationUpdate, distanceForLocationUpdate, this
        )
        isConnected = true

        logDebug(message = "$TAG Location manager is connected!")

    }

    /**
     * Call this method to stop location updates.
     */
    private fun disconnect() {
        logDebug(message = "$TAG Disconnecting location manager...")

        locationManager.removeUpdates(this)
        isConnected = false

        logDebug(message = "$TAG Location manager is disconnected.")


    }

    /**
     * To know if GPS provider is enabled or not.
     */
    fun isGPSProviderEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onLocationChanged(location: Location) {
        logDebug(message = "$TAG Location received : ${location.latitude},${location.longitude}")
        result(location)
    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onProviderEnabled(provider: String) {

    }

}