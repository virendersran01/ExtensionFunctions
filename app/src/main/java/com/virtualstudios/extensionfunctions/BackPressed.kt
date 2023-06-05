package com.virtualstudios.extensionfunctions

import android.app.Activity
import android.os.Build
import android.window.OnBackInvokedCallback
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

private typealias BackPressCallback = () -> Boolean

@Suppress("DEPRECATION")
open class BackPressCompatActivity : AppCompatActivity() {
    private var listener: BackPressCallback? = null

    /**
     * A custom back press listener to support predictive back navigation
     * introduced in Android 13.
     *
     * If return true, activity will finish otherwise no action taken.
     */
    fun setOnBackPressListener(block: BackPressCallback) {
        listener = block

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(1000, object: OnBackInvokedCallback {
                override fun onBackInvoked() {
                    if (block()) {
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            })
        }
    }

    @Deprecated("Deprecated in Java")
    final override fun onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (listener?.invoke() == false) {
                return
            }
        }
        super.onBackPressed()
    }
}

/*
class Activity : BackPressCompatActivity() {

    override fun onCreate() {
        setOnBackPressListener { handleBackPress() }
    }

    private fun handleBackPress(): Boolean {
        if (shouldClose()) {
            return true
        }
        return false
    }

}*/


/*
onBackInvokedDispatcher.registerOnBackInvokedCallback(1000, object: OnBackInvokedCallback {
    override fun onBackInvoked() {
        // perform any action
        ...

        // finally call the remaining callbacks or just `onBackPressed()` both are same.
        // if no callback is registered it will fallback to app.Activity -> onBackPressed()
        onBackPressedDispatcher.onBackPressed()
    }
})*/
