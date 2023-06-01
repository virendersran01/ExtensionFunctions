package com.virtualstudios.extensionfunctions

import android.os.Handler
import android.os.HandlerThread

/**
 * @author : Manish Sharma - 20/02/2023
 * Worker thread based on Looper mechanism.
 * Add Runnable to executed on background in a FIFO fashion.
 */
class SilentWorker : HandlerThread("SilentWorker") {

    private var handler: Handler


    init {
        start()
        handler = Handler(looper)
    }

    /**
     * To add Runnable in the MessageQueue of the Looper.
     * @param task : Runnable to be added in the queue.
     * @return - SilentWorker instance to call execute easily with chaining.
     */

    fun execute(task: Runnable): SilentWorker {
        handler.post(task)
        return this
    }

}