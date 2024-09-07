package com.virtualstudios.extensionfunctions.utils

import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker

fun GoogleMap.toggleStyle(mapStyle: Int = GoogleMap.MAP_TYPE_NORMAL) {
    mapType = if (mapStyle == GoogleMap.MAP_TYPE_NORMAL) {
        GoogleMap.MAP_TYPE_SATELLITE
    } else {
        GoogleMap.MAP_TYPE_NORMAL
    }
}

fun LatLng.convertLatLngToLocation(provider: String = "GPS"): Location {
    return Location(provider).apply {
        latitude = this@convertLatLngToLocation.latitude
        longitude = this@convertLatLngToLocation.longitude
    }
}

fun LatLng.bearingBetweenLatLngs(toLatLng: LatLng): Float {
    val fromLocation: Location =
        this.convertLatLngToLocation(provider = "GPS1")
    val toLocation: Location =
        toLatLng.convertLatLngToLocation(provider = "GPS2")
    return fromLocation.bearingTo(toLocation)
}

fun Marker.encodeMarkerForDirection() = this.position.latitude.toString() + "," + this.position.longitude

fun GoogleMap.fixZoomForLatLngs(
    latLngs: List<LatLng?>?,
    padding: Int = 50,
    durationInMs: Int = 4000,
    callback: GoogleMap.CancelableCallback? = null,
) {
     LatLngBounds.Builder().apply {
         latLngs?.forEach { nullableLatLng ->
             nullableLatLng?.let { latLng ->
                 include(latLng)
             }
         }
     }.also { latLngBounds ->
         animateCamera(
             CameraUpdateFactory.newLatLngBounds(
                 latLngBounds.build(),
                 padding
             ),
             durationInMs,
             callback
         )
     }
}

fun GoogleMap.fixZoomForMarkers(
    markers: List<Marker>,
    padding: Int = 50,
    durationInMs: Int = 4000,
    callback: GoogleMap.CancelableCallback? = null,
) {
    LatLngBounds.builder().apply {
        markers.forEach { marker ->
            include(marker.position)
        }
    }.also { bounds ->
        animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                padding
            ),
            durationInMs,
            callback
        )
    }
}

fun getSampleLatLngs() = ArrayList<LatLng>().apply {
    add(LatLng(50.961813797827055, 3.5168474167585373))
    add(LatLng(50.96085423274633, 3.517405651509762))
    add(LatLng(50.96020550146382, 3.5177918896079063))
    add(LatLng(50.95936754348453, 3.518972061574459))
    add(LatLng(50.95877285446026, 3.5199161991477013))
    add(LatLng(50.958179213755905, 3.520646095275879))
    add(LatLng(50.95901719316589, 3.5222768783569336))
    add(LatLng(50.95954430150347, 3.523542881011963))
    add(LatLng(50.95873336312275, 3.5244011878967285))
    add(LatLng(50.95955781702322, 3.525688648223877))
    add(LatLng(50.958855004782116, 3.5269761085510254))
}