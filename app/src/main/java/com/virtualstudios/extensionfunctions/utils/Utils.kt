package com.virtualstudios.extensionfunctions.utils

import android.os.Handler
import android.os.SystemClock
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


object Utils {
    fun bounceMarker(googleMap: GoogleMap, marker: Marker) {
        //Make the marker bounce
        val handler = Handler()

        val startTime = SystemClock.uptimeMillis()
        val duration: Long = 2000

        val proj = googleMap.projection
        val markerLatLng = marker.position
        val startPoint = proj.toScreenLocation(markerLatLng)
        startPoint.offset(0, -100)
        val startLatLng = proj.fromScreenLocation(startPoint)

        val interpolator: Interpolator = BounceInterpolator()

        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                val lng = t * markerLatLng.longitude + (1 - t) * startLatLng.longitude
                val lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude
                marker.position = LatLng(lat, lng)

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }
}
