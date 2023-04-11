package com.virtualstudios.extensionfunctions

import android.os.SystemClock
import android.view.View
import java.util.*

abstract class DebouncedOnClickListener(private val minInterval: Long = 500L) : View.OnClickListener {

    private val lastClickMap = WeakHashMap<View, Long>()

    abstract fun onDebouncedClick(v: View)

    override fun onClick(v: View) {
        val previousClickTimestamp = lastClickMap[v]
        val currentTimestamp = SystemClock.uptimeMillis()

        lastClickMap[v] = currentTimestamp
        if (previousClickTimestamp == null || currentTimestamp - previousClickTimestamp > minInterval) {
            onDebouncedClick(v)
        }
    }
}

fun View.setDebouncedOnClickListener(action: (() -> Unit)? = null) {
    setOnClickListener(object : DebouncedOnClickListener() {
        override fun onDebouncedClick(v: View) {
            action?.invoke()
        }
    })
}

class OnSingleClickListener(private val delay: Long, private val block: () -> Unit) : View.OnClickListener {

    private var lastClickTime = 0L

    override fun onClick(view: View) {
        if (System.currentTimeMillis() - lastClickTime < delay) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        block()
    }
}

fun View.setOnSingleClickListener(delay: Long = 500L, block: () -> Unit) {
    setOnClickListener(OnSingleClickListener(delay,block))
}