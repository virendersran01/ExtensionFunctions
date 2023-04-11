package com.virtualstudios.extensionfunctions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

interface IActivityLogger{
    fun registerActivity(activity: LifecycleOwner)
}

class ActivityLogger : IActivityLogger, LifecycleEventObserver{
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event){
            Lifecycle.Event.ON_CREATE -> "OnCreate"
            Lifecycle.Event.ON_START -> "ON_START"
            Lifecycle.Event.ON_PAUSE -> "ON_PAUSE"
            Lifecycle.Event.ON_RESUME -> "ON_RESUME"
            Lifecycle.Event.ON_STOP -> "ON_STOP"
            Lifecycle.Event.ON_DESTROY -> "ON_DESTROY"
            else -> "Other Event"
        }
    }

    override fun registerActivity(activity: LifecycleOwner) {
       activity.lifecycle.addObserver(this)
    }

}


