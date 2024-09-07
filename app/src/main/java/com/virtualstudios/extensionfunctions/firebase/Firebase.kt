package com.virtualstudios.extensionfunctions.firebase

import android.content.Intent


const val NOTIFICATION_RECEIVED_ACTION = "notification.received.action"

private fun getNotificationId(appointmentId: String): Int {
    try {
        val value = appointmentId.toInt()
        if (value > 0) return value
    } catch (e: Exception) {
        /*logDebug(
            "Invalid or Non-Integer type appointment id"
        )*/
    }
    return System.currentTimeMillis().toInt()
}

//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)