package com.virtualstudios.extensionfunctions.utils

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.virtualstudios.extensionfunctions.R
import io.opencensus.internal.StringUtils
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.Objects

fun <T> tryCatch(tag: String, method: HelperMethods.VoidMethod) {
    try {
        method.execute()
    } catch (e: Exception) {
        Debug.LogE(tag, e)
    }
}

fun doubleToLong(debugTag: String, value: Double): Long {
    try {
        return java.lang.Double.doubleToLongBits(value)
    } catch (e: Exception) {
        Debug.LogE(debugTag, e)
        return 0
    }
}

fun longToDouble(debugTag: String, value: Long): Double {
    try {
        return java.lang.Double.longBitsToDouble(value)
    } catch (e: Exception) {
        Debug.LogE(debugTag, e)
        return 0
    }
}

//    public static double mileToMeter(double value) {
//        return value * 1609.34;
//    }
//
//    public static double meterToMile(double value) {
//        return value * 0.000621371;
//    }
//
fun shareApplication(context: Context) {
    val appPackageName = context.packageName
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appPackageName")
            )
        )
    } catch (anfe: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
        )
    }
}

@RequiresPermission(permission.CALL_PHONE)
fun makePhoneCall(context: Context?, phoneNumber: String): Boolean {
    var phoneNumber = phoneNumber
    if (context == null || phoneNumber.isEmpty()) return false
    val callIntent = Intent(Intent.ACTION_CALL)
    if (!phoneNumber.trim { it <= ' ' }.startsWith("0") && !phoneNumber.trim { it <= ' ' }
            .contains("+")) {
        phoneNumber = "0$phoneNumber"
    }
    callIntent.setData(Uri.parse("tel:$phoneNumber")) //change the number
    if (PermissionChecker.checkSelfPermission(
            context,
            permission.CALL_PHONE
        ) == PermissionChecker.PERMISSION_GRANTED
    ) {
        context.startActivity(callIntent)
        return true
    }
    return false
}

fun shareText(context: Context, text: String) {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.setType("text/plain")
    sharingIntent.putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(sharingIntent, "Share using"))
}

fun sendTextMsg(context: Context, mobileNumber: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:$mobileNumber")))
}


/**
 * @param context Context of activity or application
 * @param src     LatLng of source location
 * @param dest    LatLng of destination location
 */
fun viewInGoogleMaps(
    context: Context, src: LatLng,
    dest: LatLng
) {
    val url = ("http://maps.google.com/maps?saddr="
            + src.latitude + "," + src.longitude
            + "&daddr=" + dest.latitude + "," + dest.longitude)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

fun <T : Activity?> handleGPS(activity: T, code: Int) {
    val locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval((10 * 1000).toLong())
        .setFastestInterval(1000)
    val settingsBuilder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
    settingsBuilder.setAlwaysShow(true)
    val result = LocationServices.getSettingsClient(activity)
        .checkLocationSettings(settingsBuilder.build())
    result.addOnCompleteListener { task: Task<LocationSettingsResponse?> ->
        try {
            val response =
                task.getResult(
                    ApiException::class.java
                )
            try {
                if (response != null) {
                    val states = response.locationSettingsStates
                    Debug.LogV(
                        activity.javaClass.getSimpleName(), "GPS : " + states!!.isGpsPresent
                                + ", GPS USABLE : " + states.isGpsUsable
                                + ", LOCATION: " + states.isLocationPresent
                                + ", LOCATION USABLE : " + states.isLocationUsable
                                + ", NETWORK LOCATION: " + states.isNetworkLocationPresent
                                + ", NETWORK LOCATION USABLE: " + states.isNetworkLocationUsable
                    )
                } else Debug.LogE(
                    activity.javaClass.getSimpleName(),
                    "LocalionSettingsStates null!"
                )
            } catch (ignored: Exception) {
            }
        } catch (ex: ApiException) {
            when (ex.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    val resolvableApiException =
                        ex as ResolvableApiException
                    resolvableApiException
                        .startResolutionForResult(activity, code)
                } catch (e: SendIntentException) {
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
            }
        }
    }
    result.addOnFailureListener { e: Exception? ->
        try {
            Debug.LogE(activity.javaClass.getSimpleName(), e)
        } catch (ex: Exception) {
            Debug.LogE("handleGPS", ex)
        }
    }
}


fun isGPSEnabled(context: Context): Boolean {
    val locationManager = ContextCompat.getSystemService(
        context,
        LocationManager::class.java
    )
    return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

@SuppressLint("PackageManagerGetSignatures")
fun getCertificateSHA1Fingerprint(context: Context): String {
    try {
        val manager = context.packageManager
        val packageName = context.packageName
        val signatures: Array<Signature>
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            signatures =
                manager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
        } else {
            val packageInfo =
                manager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            signatures = packageInfo.signingInfo.apkContentsSigners
        }
        val cert = signatures[0].toByteArray()
        val input: InputStream = ByteArrayInputStream(cert)
        var cf: CertificateFactory? = null
        try {
            cf = CertificateFactory.getInstance("X509")
        } catch (e: CertificateException) {
            e.printStackTrace()
        }
        var c: X509Certificate? = null
        try {
            c = Objects.requireNonNull(cf).generateCertificate(input) as X509Certificate
        } catch (e: CertificateException) {
            e.printStackTrace()
        }
        var hexString: String? = null
        try {
            val md = MessageDigest.getInstance("SHA1")
            val publicKey = md.digest(Objects.requireNonNull(c).encoded)
            hexString = byte2HexFormatted(publicKey)
        } catch (e1: NoSuchAlgorithmException) {
            e1.printStackTrace()
        } catch (e: CertificateEncodingException) {
            e.printStackTrace()
        }
        return StringUtils.nonNull(hexString)
    } catch (e: Exception) {
        return ""
    }
}

private fun byte2HexFormatted(arr: ByteArray): String {
    val str = StringBuilder(arr.size * 2)
    for (i in arr.indices) {
        var h = Integer.toHexString(arr[i].toInt())
        val l = h.length
        if (l == 1) h = "0$h"
        if (l > 2) h = h.substring(l - 2, l)
        str.append(h.uppercase(Locale.getDefault()))
        if (i < (arr.size - 1)) str.append(':')
    }
    return str.toString()
}


private fun md5(s: String): String {
    val MD5 = "MD5"
    try {
        // Create MD5 Hash
        val digest = MessageDigest
            .getInstance(MD5)
        digest.update(s.toByteArray())
        val messageDigest = digest.digest()

        // Create Hex String
        val hexString = StringBuilder()
        for (aMessageDigest in messageDigest) {
            val h = StringBuilder(Integer.toHexString(0xFF and aMessageDigest.toInt()))
            while (h.length < 2) {
                h.insert(0, "0")
            }
            hexString.append(h)
        }
        return hexString.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}

fun copyToClipBoard(context: Context, text: String) {
    val clipboardManager = ContextCompat.getSystemService(
        context,
        ClipboardManager::class.java
    )
    val clip = ClipData.newPlainText(context.getString(R.string.app_name), text)
    if (clipboardManager != null) {
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, R.string.msg_copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }
}


fun openWebPage(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (ignored: ActivityNotFoundException) {
    }
}