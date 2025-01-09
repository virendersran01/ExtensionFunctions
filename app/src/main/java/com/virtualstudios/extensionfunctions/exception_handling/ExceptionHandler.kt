package com.virtualstudios.extensionfunctions.exception_handling

import android.content.Context
import android.content.Intent
import android.os.Build
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/***
 * MenuActivity is used for to get all related permissions ,all backgraound services will start
 *
 * uses -> Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
 *         add this in oncreate method of Activity
 */
class ExceptionHandler(val context: Context) : Thread.UncaughtExceptionHandler {

    private val LINE_SEPARATOR: String = "\n"

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        val stackTrace = StringWriter().apply {
            exception.printStackTrace(PrintWriter(this))
        }

        val errorReport = buildString {
            append("************ CAUSE OF ERROR ************\n\n")
            append(stackTrace)

            append("\n************ DEVICE INFORMATION ***********\n")
            append("Brand: ${Build.BRAND}$LINE_SEPARATOR")
            append("Model: ${Build.MODEL}$LINE_SEPARATOR")
            append("SDK: ${Build.VERSION.SDK}$LINE_SEPARATOR")
            append("Release: ${Build.VERSION.RELEASE}$LINE_SEPARATOR")
        }

        Intent(context, ExceptionLogActivity::class.java).apply {
            putExtra("exceptionlog", errorReport)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }

        exitProcess(1)
    }
}
