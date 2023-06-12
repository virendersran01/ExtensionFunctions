package com.virtualstudios.extensionfunctions

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity(), IActivityLogger by ActivityLogger() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            // adding loading condition
            setKeepOnScreenCondition {
                return@setKeepOnScreenCondition true
            }
            setOnExitAnimationListener { splashScreen ->
                // access to the splash screen and moving it down
                ObjectAnimator.ofFloat(
                    splashScreen.view,
                    View.TRANSLATION_Y,
                    // from top to down
                    0f, splashScreen.view.height.toFloat()
                ).apply {
                    // deceleration interpolaror, duration
                    interpolator = DecelerateInterpolator()
                    duration = 500L
                    // do not forget to remove the splash screen
                    doOnEnd { splashScreen.remove() }
                    start()
                }
            }
        }
        setContentView(R.layout.activity_main)
        registerActivity(this)
    }
}