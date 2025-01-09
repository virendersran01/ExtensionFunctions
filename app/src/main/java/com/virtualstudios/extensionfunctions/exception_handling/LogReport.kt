package com.virtualstudios.extensionfunctions.exception_handling

import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Date

object LogReport {
    private val filepath: File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/LogFile.txt")
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

//uses -> LogReport.writeLog("tests: $tests")
