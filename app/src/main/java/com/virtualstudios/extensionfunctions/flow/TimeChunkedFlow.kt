package com.virtualstudios.extensionfunctions.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

//https://gist.github.com/PatilShreyas/c501182a92aa338e10e6ca9fdfa43da7

fun main() = runBlocking<Unit> {
    flow {
        repeat(100) {
            emit(it)
            delay(50)
        }
    }.chunked(1.seconds).collect {
        println(it)
    }
}

private class TimeChunkedFlow<T>(
    private val upstream: Flow<T>,
    private val duration: Duration
) : Flow<List<T>> {
    override suspend fun collect(collector: FlowCollector<List<T>>) = coroutineScope<Unit> {
        val mutex = Mutex()

        // Holds the un-emitted items
        val values = mutableListOf<T>()

        // Flag to know the status of upstream flow whether it has been completed or not
        var isFlowCompleted = false

        launch {
            while (true) {
                delay(duration)
                mutex.withLock {
                    // If the upstream flow has been completed and there are no values
                    // pending to emit in the collector, just break this loop.
                    if (isFlowCompleted && values.isEmpty()) {
                        return@launch
                    }
                    collector.emit(values.toList())
                    values.clear()
                }
            }
        }

        // Collect the upstream flow and add the items to the above `values` list
        upstream.collect {
            mutex.withLock {
                values.add(it)
            }
        }

        // If we reach here it means the upstream flow has been completed and won't
        // produce any values anymore. So set the flag as flow is completed so that
        // child coroutine will break its loop
        isFlowCompleted = true
    }
}

fun <T> Flow<T>.chunked(duration: Duration): Flow<List<T>> = TimeChunkedFlow(this, duration)