package com.virtualstudios.extensionfunctions.utils

import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Collections


/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each
 * collection's individually managed listeners.
 *
 *
 * All marker operations (adds and removes) should occur via its collection class. That is, don't
 * add a marker via a collection, then remove it via Marker.remove()
 */
class MarkerManager(private val mMap: GoogleMap) : OnInfoWindowClickListener, OnMarkerClickListener,
    OnMarkerDragListener, InfoWindowAdapter {
    private val mNamedCollections: MutableMap<String, Collection?> = HashMap()
    private val mAllMarkers: MutableMap<Marker?, Collection> = HashMap()

    fun newCollection(): Collection {
        return Collection()
    }

    /**
     * Create a new named collection, which can later be looked up by [.getCollection]
     *
     * @param id a unique id for this collection.
     */
    fun newCollection(id: String): Collection {
        require(mNamedCollections[id] == null) { "collection id is not unique: $id" }
        val collection: Collection = Collection()
        mNamedCollections[id] = collection
        return collection
    }

    /**
     * Gets a named collection that was created by [.newCollection]
     *
     * @param id the unique id for this collection.
     */
    fun getCollection(id: String): Collection? {
        return mNamedCollections[id]
    }

    override fun getInfoWindow(marker: Marker): View? {
        val collection = mAllMarkers[marker]
        if (collection?.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter!!.getInfoWindow(marker)
        }
        return null
    }

    override fun getInfoContents(marker: Marker): View? {
        val collection = mAllMarkers[marker]
        if (collection?.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter!!.getInfoContents(marker)
        }
        return null
    }

    override fun onInfoWindowClick(marker: Marker) {
        val collection = mAllMarkers[marker]
        if (collection?.mInfoWindowClickListener != null) {
            collection.mInfoWindowClickListener!!.onInfoWindowClick(marker)
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val collection = mAllMarkers[marker]
        if (collection?.mMarkerClickListener != null) {
            return collection.mMarkerClickListener!!.onMarkerClick(marker)
        }
        return false
    }

    override fun onMarkerDragStart(marker: Marker) {
        val collection = mAllMarkers[marker]
        if (collection?.mMarkerDragListener != null) {
            collection.mMarkerDragListener!!.onMarkerDragStart(marker)
        }
    }

    override fun onMarkerDrag(marker: Marker) {
        val collection = mAllMarkers[marker]
        if (collection?.mMarkerDragListener != null) {
            collection.mMarkerDragListener!!.onMarkerDrag(marker)
        }
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val collection = mAllMarkers[marker]
        if (collection?.mMarkerDragListener != null) {
            collection.mMarkerDragListener!!.onMarkerDragEnd(marker)
        }
    }

    /**
     * Removes a marker from its collection.
     *
     * @param marker the marker to remove.
     * @return true if the marker was removed.
     */
    fun remove(marker: Marker): Boolean {
        val collection = mAllMarkers[marker]
        return collection != null && collection.remove(marker)
    }

    inner class Collection {
        private val mMarkers: MutableSet<Marker?> = HashSet()
        var mInfoWindowClickListener: OnInfoWindowClickListener? = null
        var mMarkerClickListener: OnMarkerClickListener? = null
        var mMarkerDragListener: OnMarkerDragListener? = null
        var mInfoWindowAdapter: InfoWindowAdapter? = null

        fun addMarker(opts: MarkerOptions?): Marker? {
            val marker = mMap.addMarker(opts!!)
            mMarkers.add(marker)
            mAllMarkers[marker] = this@Collection
            return marker
        }

        fun remove(marker: Marker): Boolean {
            if (mMarkers.remove(marker)) {
                mAllMarkers.remove(marker)
                marker.remove()
                return true
            }
            return false
        }

        fun clear() {
            for (marker in mMarkers) {
                marker!!.remove()
                mAllMarkers.remove(marker)
            }
            mMarkers.clear()
        }

        val markers: kotlin.collections.Collection<Marker?>
            get() = Collections.unmodifiableCollection(mMarkers)

        fun setOnInfoWindowClickListener(infoWindowClickListener: OnInfoWindowClickListener?) {
            mInfoWindowClickListener = infoWindowClickListener
        }

        fun setOnMarkerClickListener(markerClickListener: OnMarkerClickListener?) {
            mMarkerClickListener = markerClickListener
        }

        fun setOnMarkerDragListener(markerDragListener: OnMarkerDragListener?) {
            mMarkerDragListener = markerDragListener
        }

        fun setOnInfoWindowAdapter(infoWindowAdapter: InfoWindowAdapter?) {
            mInfoWindowAdapter = infoWindowAdapter
        }
    }
}
