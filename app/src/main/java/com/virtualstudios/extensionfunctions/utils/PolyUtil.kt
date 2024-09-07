package com.virtualstudios.extensionfunctions.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan


object PolyUtil {
    private const val DEFAULT_TOLERANCE = 0.1 // meters.

    /**
     * Returns tan(latitude-at-lng3) on the great circle (lat1, lng1) to (lat2,
     * lng2). lng1==0. See http://williams.best.vwh.net/avform.htm .
     */
    private fun tanLatGC(
        lat1: Double, lat2: Double, lng2: Double,
        lng3: Double
    ): Double {
        return ((tan(lat1) * sin(lng2 - lng3) + tan(lat2) * sin(lng3))
                / sin(lng2))
    }

    /**
     * Returns mercator(latitude-at-lng3) on the Rhumb line (lat1, lng1) to
     * (lat2, lng2). lng1==0.
     */
    private fun mercatorLatRhumb(
        lat1: Double, lat2: Double,
        lng2: Double, lng3: Double
    ): Double {
        return (MathUtil.mercator(lat1) * (lng2 - lng3) + MathUtil.mercator(lat2) * lng3) / lng2
    }

    /**
     * Computes whether the vertical segment (lat3, lng3) to South Pole
     * intersects the segment (lat1, lng1) to (lat2, lng2). Longitudes are
     * offset by -lng1; the implicit lng1 becomes 0.
     */
    private fun intersects(
        lat1: Double, lat2: Double, lng2: Double,
        lat3: Double, lng3: Double, geodesic: Boolean
    ): Boolean {
        // Both ends on the same side of lng3.
        if ((lng3 >= 0 && lng3 >= lng2) || (lng3 < 0 && lng3 < lng2)) {
            return false
        }
        // Point is South Pole.
        if (lat3 <= -Math.PI / 2) {
            return false
        }
        // Any segment end is a pole.
        if (lat1 <= -Math.PI / 2 || lat2 <= -Math.PI / 2 || lat1 >= Math.PI / 2 || lat2 >= Math.PI / 2) {
            return false
        }
        if (lng2 <= -Math.PI) {
            return false
        }
        val linearLat = (lat1 * (lng2 - lng3) + lat2 * lng3) / lng2
        // Northern hemisphere and point under lat-lng line.
        if (lat1 >= 0 && lat2 >= 0 && lat3 < linearLat) {
            return false
        }
        // Southern hemisphere and point above lat-lng line.
        if (lat1 <= 0 && lat2 <= 0 && lat3 >= linearLat) {
            return true
        }
        // North Pole.
        if (lat3 >= Math.PI / 2) {
            return true
        }
        // Compare lat3 with latitude on the GC/Rhumb segment corresponding to
        // lng3.
        // Compare through a strictly-increasing function (tan() or mercator())
        // as convenient.
        return if (geodesic) tan(lat3) >= tanLatGC(lat1, lat2, lng2, lng3)
        else MathUtil.mercator(lat3) >= mercatorLatRhumb(lat1, lat2, lng2, lng3)
    }

    /**
     * Computes whether the given point lies inside the specified polygon. The
     * polygon is always cosidered closed, regardless of whether the last point
     * equals the first or not. Inside is defined as not containing the South
     * Pole -- the South Pole is always outside. The polygon is formed of great
     * circle segments if geodesic is true, and of rhumb (loxodromic) segments
     * otherwise.
     */
    fun containsLocation(
        point: LatLng, polygon: List<LatLng>,
        geodesic: Boolean
    ): Boolean {
        val size = polygon.size
        if (size == 0) {
            return false
        }
        val lat3 = Math.toRadians(point.latitude)
        val lng3 = Math.toRadians(point.longitude)
        val prev = polygon[size - 1]
        var lat1 = Math.toRadians(prev.latitude)
        var lng1 = Math.toRadians(prev.longitude)
        var nIntersect = 0
        for (point2 in polygon) {
            val dLng3 = MathUtil.wrap(lng3 - lng1, -Math.PI, Math.PI)
            // Special case: point equal to vertex is inside.
            if (lat3 == lat1 && dLng3 == 0.0) {
                return true
            }
            val lat2 = Math.toRadians(point2.latitude)
            val lng2 = Math.toRadians(point2.longitude)
            // Offset longitudes by -lng1.
            if (intersects(
                    lat1, lat2, MathUtil.wrap(lng2 - lng1, -Math.PI, Math.PI), lat3, dLng3,
                    geodesic
                )
            ) {
                ++nIntersect
            }
            lat1 = lat2
            lng1 = lng2
        }
        return (nIntersect and 1) != 0
    }

    /**
     * Computes whether the given point lies on or near the edge of a polygon,
     * within a specified tolerance in meters. The polygon edge is composed of
     * great circle segments if geodesic is true, and of Rhumb segments
     * otherwise. The polygon edge is implicitly closed -- the closing segment
     * between the first point and the last point is included.
     */
    fun isLocationOnEdge(
        point: LatLng, polygon: List<LatLng>,
        geodesic: Boolean, tolerance: Double
    ): Boolean {
        return isLocationOnEdgeOrPath(point, polygon, true, geodesic, tolerance)
    }

    /**
     * Same as [.isLocationOnEdge] with a
     * default tolerance of 0.1 meters.
     */
    fun isLocationOnEdge(
        point: LatLng, polygon: List<LatLng>,
        geodesic: Boolean
    ): Boolean {
        return isLocationOnEdge(point, polygon, geodesic, DEFAULT_TOLERANCE)
    }

    /**
     * Computes whether the given point lies on or near a polyline, within a
     * specified tolerance in meters. The polyline is composed of great circle
     * segments if geodesic is true, and of Rhumb segments otherwise. The
     * polyline is not closed -- the closing segment between the first point and
     * the last point is not included.
     */
    fun isLocationOnPath(
        point: LatLng, polyline: List<LatLng>,
        geodesic: Boolean, tolerance: Double
    ): Boolean {
        return isLocationOnEdgeOrPath(
            point, polyline, false, geodesic,
            tolerance
        )
    }

    /**
     * Same as [.isLocationOnPath]
     *
     *
     * with a default tolerance of 0.1 meters.
     */
    fun isLocationOnPath(
        point: LatLng, polyline: List<LatLng>,
        geodesic: Boolean
    ): Boolean {
        return isLocationOnPath(point, polyline, geodesic, DEFAULT_TOLERANCE)
    }

    private fun isLocationOnEdgeOrPath(
        point: LatLng,
        poly: List<LatLng>, closed: Boolean, geodesic: Boolean,
        toleranceEarth: Double
    ): Boolean {
        val size = poly.size
        if (size == 0) {
            return false
        }
        val tolerance = toleranceEarth / MathUtil.EARTH_RADIUS
        val havTolerance = MathUtil.hav(tolerance)
        val lat3 = Math.toRadians(point.latitude)
        val lng3 = Math.toRadians(point.longitude)
        val prev = poly[if (closed) size - 1 else 0]
        var lat1 = Math.toRadians(prev.latitude)
        var lng1 = Math.toRadians(prev.longitude)
        if (geodesic) {
            for (point2 in poly) {
                val lat2 = Math.toRadians(point2.latitude)
                val lng2 = Math.toRadians(point2.longitude)
                if (isOnSegmentGC(
                        lat1, lng1, lat2, lng2, lat3, lng3,
                        havTolerance
                    )
                ) {
                    return true
                }
                lat1 = lat2
                lng1 = lng2
            }
        } else {
            // We project the points to mercator space, where the Rhumb segment
            // is a straight line,
            // and compute the geodesic distance between point3 and the closest
            // point on the
            // segment. This method is an approximation, because it uses
            // "closest" in mercator
            // space which is not "closest" on the sphere -- but the error is
            // small because
            // "tolerance" is small.
            val minAcceptable = lat3 - tolerance
            val maxAcceptable = lat3 + tolerance
            var y1 = MathUtil.mercator(lat1)
            val y3 = MathUtil.mercator(lat3)
            val xTry = DoubleArray(3)
            for (point2 in poly) {
                val lat2 = Math.toRadians(point2.latitude)
                val y2 = MathUtil.mercator(lat2)
                val lng2 = Math.toRadians(point2.longitude)
                if (max(lat1, lat2) >= minAcceptable
                    && min(lat1, lat2) <= maxAcceptable
                ) {
                    // We offset longitudes by -lng1; the implicit x1 is 0.
                    val x2 = MathUtil.wrap(lng2 - lng1, -Math.PI, Math.PI)
                    val x3Base = MathUtil.wrap(lng3 - lng1, -Math.PI, Math.PI)
                    xTry[0] = x3Base
                    // Also explore wrapping of x3Base around the world in both
                    // directions.
                    xTry[1] = x3Base + 2 * Math.PI
                    xTry[2] = x3Base - 2 * Math.PI
                    for (x3 in xTry) {
                        val dy = y2 - y1
                        val len2 = x2 * x2 + dy * dy
                        val t: Double = if (len2 <= 0) 0 else MathUtil.clamp(
                            (x3 * x2 + (y3 - y1)
                                    * dy)
                                    / len2, 0, 1
                        )
                        val xClosest = t * x2
                        val yClosest = y1 + t * dy
                        val latClosest = MathUtil.inverseMercator(yClosest)
                        val havDist = MathUtil.havDistance(
                            lat3, latClosest, x3
                                    - xClosest
                        )
                        if (havDist < havTolerance) {
                            return true
                        }
                    }
                }
                lat1 = lat2
                lng1 = lng2
                y1 = y2
            }
        }
        return false
    }

    /**
     * Returns sin(initial bearing from (lat1,lng1) to (lat3,lng3) minus initial
     * bearing from (lat1, lng1) to (lat2,lng2)).
     */
    private fun sinDeltaBearing(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double, lat3: Double, lng3: Double
    ): Double {
        val sinLat1 = sin(lat1)
        val cosLat2 = cos(lat2)
        val cosLat3 = cos(lat3)
        val lat31 = lat3 - lat1
        val lng31 = lng3 - lng1
        val lat21 = lat2 - lat1
        val lng21 = lng2 - lng1
        val a = sin(lng31) * cosLat3
        val c = sin(lng21) * cosLat2
        val b = sin(lat31) + 2 * sinLat1 * cosLat3 * MathUtil.hav(lng31)
        val d = sin(lat21) + 2 * sinLat1 * cosLat2 * MathUtil.hav(lng21)
        val denom = (a * a + b * b) * (c * c + d * d)
        return if (denom <= 0) 1.0 else (a * d - b * c) / sqrt(denom)
    }

    private fun isOnSegmentGC(
        lat1: Double, lng1: Double, lat2: Double,
        lng2: Double, lat3: Double, lng3: Double, havTolerance: Double
    ): Boolean {
        val havDist13 = MathUtil.havDistance(lat1, lat3, lng1 - lng3)
        if (havDist13 <= havTolerance) {
            return true
        }
        val havDist23 = MathUtil.havDistance(lat2, lat3, lng2 - lng3)
        if (havDist23 <= havTolerance) {
            return true
        }
        val sinBearing = sinDeltaBearing(lat1, lng1, lat2, lng2, lat3, lng3)
        val sinDist13 = MathUtil.sinFromHav(havDist13)
        val havCrossTrack = MathUtil.havFromSin(sinDist13 * sinBearing)
        if (havCrossTrack > havTolerance) {
            return false
        }
        val havDist12 = MathUtil.havDistance(lat1, lat2, lng1 - lng2)
        val term = havDist12 + havCrossTrack * (1 - 2 * havDist12)
        if (havDist13 > term || havDist23 > term) {
            return false
        }
        if (havDist12 < 0.74) {
            return true
        }
        val cosCrossTrack = 1 - 2 * havCrossTrack
        val havAlongTrack13 = (havDist13 - havCrossTrack) / cosCrossTrack
        val havAlongTrack23 = (havDist23 - havCrossTrack) / cosCrossTrack
        val sinSumAlongTrack = MathUtil.sinSumFromHav(
            havAlongTrack13,
            havAlongTrack23
        )
        return sinSumAlongTrack > 0 // Compare with half-circle == PI using
        // sign of sin().
    }

    /**
     * Decodes an encoded path string into a sequence of LatLngs.
     */
    fun decode(encodedPath: String): List<LatLng> {
        val len = encodedPath.length

        // For speed we preallocate to an upper bound on the final length, then
        // truncate the array before returning.
        val path: MutableList<LatLng> = ArrayList()
        var index = 0
        var lat = 0
        var lng = 0

        var pointIndex = 0
        while (index < len) {
            var result = 1
            var shift = 0
            var b: Int
            do {
                b = encodedPath[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lat += if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)

            result = 1
            shift = 0
            do {
                b = encodedPath[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)

            path.add(LatLng(lat * 1e-5, lng * 1e-5))
            ++pointIndex
        }

        return path
    }

    /**
     * Encodes a sequence of LatLngs into an encoded path string.
     */
    fun encode(path: List<LatLng>): String {
        var lastLat: Long = 0
        var lastLng: Long = 0

        val result = StringBuffer()

        for (point in path) {
            val lat = Math.round(point.latitude * 1e5)
            val lng = Math.round(point.longitude * 1e5)

            val dLat = lat - lastLat
            val dLng = lng - lastLng

            encode(dLat, result)
            encode(dLng, result)

            lastLat = lat
            lastLng = lng
        }
        return result.toString()
    }

    private fun encode(v: Long, result: StringBuffer) {
        var v = v
        v = if (v < 0) (v shl 1).inv() else v shl 1
        while (v >= 0x20) {
            result.append(Character.toChars(((0x20L or (v and 0x1fL)) + 63).toInt()))
            v = v shr 5
        }
        result.append(Character.toChars((v + 63).toInt()))
    }
}
