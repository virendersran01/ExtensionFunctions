package com.virtualstudios.extensionfunctions.exception_handling


//uses -> LogReport.writeLog("tests: $tests")

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogReport {

    private lateinit var filepath: File

    /**
     * Initializes the filepath using the app-specific external files directory.
     * This function must be called with a valid context before using the logging functions.
     */
    fun init(context: Context) {
        val appSpecificDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Logs")
        if (!appSpecificDir.exists()) {
            appSpecificDir.mkdirs()
        }
        filepath = File(appSpecificDir, "OMRLogFile.txt")
    }

    fun createFile() {
        try {
            logDebug("LogReport, create file: path = ${filepath.absolutePath}")
            if (!this::filepath.isInitialized) {
                throw IllegalStateException("LogReport not initialized. Call init(context) first.")
            }
            if (filepath.createNewFile()) {
                FileWriter(filepath).use { writer ->
                    writer.append("Report\n")
                    writer.flush()
                }
            } else {
                writeLog("File exists")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            logDebug("createFile Error ===> ${e.message}")
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            logDebug("Initialization Error ===> ${e.message}")
        }
    }

    fun writeLog(string: String) {
        try {
            if (!this::filepath.isInitialized) {
                throw IllegalStateException("LogReport not initialized. Call init(context) first.")
            }
            if (filepath.exists()) {
                FileWriter(filepath, true).use { fstream ->
                    BufferedWriter(fstream).use { out ->
                        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        out.write("\nLine Added on: $timestamp\t$string\n")
                    }
                }
            } else {
                logDebug("Log file does not exist. Cannot write log.")
            }
        } catch (e: IOException) {
            logDebug("writeLog Error ===> ${e.message}")
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            logDebug("Initialization Error ===> ${e.message}")
        }
    }

    private fun logDebug(tag: String, message: String) {
        println("$tag: $message")
    }

    private fun logDebug(message: String) {
        println("LogReport Debug: $message")
    }
}

object LogReportOld {
    private const val LOG_FILE_NAME = "OMRLogFile.txt"

    //File(Environment.getExternalStorageDirectory().toString() + "/OMRLogFile.txt")
    private val filepath: File by lazy {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        File(directory, LOG_FILE_NAME)
    }
//    private val filepath: File =
//        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/LogFile.txt")
    //File(Environment.getExternalStorageDirectory().toString() + "/OMRLogFile.txt")


    fun createfile() {
        try {
            Log.d("pathh==>", filepath.absolutePath)

            if (filepath.createNewFile()) {
                FileWriter(filepath).use { writer ->
                    writer.append("Report\n")
                    writer.flush()
                }
            } else {
                writeLog("file exist")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("Error===>", e.message + "_")
        }
    }


    fun writeLog(string: String) {
        try {
            if (!filepath.exists()) createfile()

            FileWriter(filepath, true).use { fstream ->
                BufferedWriter(fstream).use { out ->
                    out.write("\n Line Added on: ${Date()}\t$string\n")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
