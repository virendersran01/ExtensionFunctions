package com.virtualstudios.extensionfunctions.utils

import android.annotation.SuppressLint
import android.app.Activity
import com.google.android.gms.maps.model.LatLng


/**
 * @author Hardik A. Bhalodi
 */
@SuppressLint("JavascriptInterface")
class PolyLineUtils {
    private var activity: Activity? = null

    //	private OnPolyLineBufferPathSuccessListner onPolySuccessListner;
    constructor()

    constructor(activity: Activity?) {
        this.activity = activity
        //		this.onPolySuccessListner = (OnPolyLineBufferPathSuccessListner) activity;
    }

    fun decodePoly(encoded: String): List<LatLng> {
        val poly: MutableList<LatLng> = ArrayList()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
            lng += dlng

            val p = LatLng(
                ((lat.toDouble() / 1E5)),
                ((lng.toDouble() / 1E5))
            )
            poly.add(p)
        }

        return poly
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
    } // Function build url from points and call the buffer function
    //	public void getPolyGoneBufferPath(ArrayList<LatLng> points) {
    //		String url = "";
    //		for (int i = 0; i < points.size(); i++) {
    //
    //			LatLng loc = points.get(i);
    //
    //			if (i == 0) {
    //				url = loc.latitude + "," + loc.longitude;
    //
    //			} else {
    //				url = url + "|" + loc.latitude + "," + loc.longitude;
    //
    //			}
    //		}
    //		getPolyGoneBufferPathUsingJSI(url);
    //	}
    // This function Convert polygone to buffer polygone using javascript
    //	private void getPolyGoneBufferPathUsingJSI(final String polyUrl) {
    //		if (activity != null) {
    //
    //			WebView wv = new WebView(activity);
    //			wv.getSettings().setJavaScriptEnabled(true);
    //			wv.setWebViewClient(new WebViewClient() {
    //				@Override
    //				public void onPageFinished(WebView view, String url) {
    //
    //					StringBuilder buf = new StringBuilder(
    //							"javascript:getPolyGone(");
    //					buf.append("\"");
    //					buf.append(polyUrl);
    //					buf.append("\"");
    //					buf.append(")");
    //					System.out.println("polyurl========>" + buf.toString());
    //					view.loadUrl(buf.toString());
    //				}
    //			});
    //			wv.addJavascriptInterface(new MyJavaScriptInterface() {
    //
    //				@Override
    //				public void receiveValueFromJs(final String str) {
    //					if (onPolySuccessListner != null) {
    //						onPolySuccessListner.polyLineBufferPathSuccess(str);
    //					} else {
    //						System.out.println("Listner not register...");
    //					}
    //
    //				}
    //			}, "MyAndroid");
    //			wv.loadUrl("file:///android_asset/polygonebufferpath.html");
    //		}
    //	}
}
