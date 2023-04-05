package com.virtualstudios.extensionfunctions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

object EventBus {
    private val _events = MutableSharedFlow<Any>()
    val events = _events.asSharedFlow()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun post(event: Any) {
        scope.launch {
            _events.emit(event)
        }
    }
    //need customScope ex) service..
    inline fun<reified T> receive(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher? = null,
        crossinline action: (t: T) -> Unit) {
        events.filterIsInstance<T>().onEach {
            if (dispatcher == null) {
                action.invoke(it)
            } else {
                withContext(dispatcher) {
                    action.invoke(it)
                }
            }
        }.launchIn(scope)
    }
    inline fun<reified T> Fragment.eventReceive(
        dispatcher: CoroutineDispatcher? = null,
        crossinline action: (t: T) -> Unit) {
        events.filterIsInstance<T>().onEach {
            if (dispatcher == null) {
                action.invoke(it)
            } else {
                withContext(dispatcher) {
                    action.invoke(it)
                }
            }
        }.launchIn(lifecycleScope)
    }
    inline fun<reified T> FragmentActivity.eventReceive(
        dispatcher: CoroutineDispatcher? = null,
        crossinline action: (t: T) -> Unit) {
        events.filterIsInstance<T>().onEach {
            if (dispatcher == null) {
                action.invoke(it)
            } else {
                withContext(dispatcher) {
                    action.invoke(it)
                }
            }
        }.launchIn(lifecycleScope)
    }
}

/*
//Usage examples
EventBus.post(xxEvent())

//Usage examples
EventBus<xxxEvent>.receive(CoroutineScope(), DisPatchers.Main) {
    //do action
}

//Usage examples
eventReceive<xxEvent> {
    //action..
}*/
