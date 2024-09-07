package com.virtualstudios.extensionfunctions.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

fun Context.writeToFile(fileName: String = "file", text: String) {
    var fileContent = text
    try {
        fileContent += "\n"

        val fos = openFileOutput(
            fileName,
            Context.MODE_APPEND or Context.MODE_WORLD_READABLE
        )
        fos.write(fileContent.toByteArray())
        fos.close()

        val storageState = Environment.getExternalStorageState()

        if (storageState == Environment.MEDIA_MOUNTED) {
            val file = File(
                getExternalFilesDir(null),
                fileName
            )
            val fos2 = FileOutputStream(file, true)
            fos2.write(fileContent.toByteArray())
            fos2.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}