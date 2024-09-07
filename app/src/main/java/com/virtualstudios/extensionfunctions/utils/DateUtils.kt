package com.virtualstudios.extensionfunctions.utils

import io.opencensus.internal.StringUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun getDate(pattern: String, year: Int, month: Int, day: Int): String {
    val calendar = Calendar.getInstance()
    calendar[year, month, day, 0] = 0
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return StringUtils.nonNull(sdf.format(calendar.time))
}

fun getDate(pattern: String, year: Int, month: Int, day: Int, hour: Int, min: Int): String {
    val calendar = Calendar.getInstance()
    calendar[year, month, day, 0] = 0
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return StringUtils.nonNull(sdf.format(calendar.time))
}

fun getDate(pattern: String, date: Long): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return StringUtils.nonNull(sdf.format(Date(date)))
}

fun getDate(pattern: String, date: Date): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return StringUtils.nonNull(sdf.format(date))
}

fun getDate(pattern: String, ymd: TripleObject<Int?, Int?, Int?>): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return StringUtils.nonNull(sdf.format(getTimeInMillis(ymd)))
}

fun getToday(pattern: String?): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return StringUtils.nonNull(sdf.format(Date(System.currentTimeMillis())))
}

fun getYesterday(pattern: String?): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    return StringUtils.nonNull(sdf.format(calendar.timeInMillis))
}

fun getLastMonth(pattern: String?): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
    return StringUtils.nonNull(sdf.format(calendar.timeInMillis))
}


fun getThisMonth(pattern: String?): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
    return StringUtils.nonNull(sdf.format(calendar.timeInMillis))
}

fun getThisMonthStartAndEnd(pattern: String?): PairObject<String, String> {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
    val start: String = StringUtils.nonNull(sdf.format(calendar.timeInMillis))
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val end: String = StringUtils.nonNull(sdf.format(calendar.timeInMillis))
    return PairObject(start, end)
}

fun getThisMonthStartAndEnd(): PairObject<Long, Long> {
    val calendar = Calendar.getInstance()
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
    val start = calendar.timeInMillis
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val end = calendar.timeInMillis
    return PairObject(start, end)
}


fun getLastMonthStartAndEnd(pattern: String?): PairObject<String, String> {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar[Calendar.MONTH] = -1
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
    val start: String = StringUtils.nonNull(sdf.format(calendar.timeInMillis))
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val end: String = StringUtils.nonNull(sdf.format(calendar.timeInMillis))
    return PairObject(start, end)
}

fun getLastMonthStartAndEnd(): PairObject<Long, Long> {
    val calendar = Calendar.getInstance()
    calendar[Calendar.MONTH] = -1
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
    val start = calendar.timeInMillis
    calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val end = calendar.timeInMillis
    return PairObject(start, end)
}

fun getYesterday(): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    return calendar.timeInMillis
}

fun getTodayYMD(): TripleObject<Int, Int, Int> {
    val calendar = Calendar.getInstance()
    val y = calendar[Calendar.YEAR]
    val m = calendar[Calendar.MONTH]
    val d = calendar[Calendar.DAY_OF_MONTH]
    return TripleObject(y, m, d)
}

fun isToday(sdf: SimpleDateFormat, date: String): Boolean {
    try {
        val c1 = Calendar.getInstance()
        c1.time = sdf.parse(date)
        val now = Calendar.getInstance()
        return c1[Calendar.DAY_OF_MONTH] == now[Calendar.DAY_OF_MONTH] && c1[Calendar.MONTH] == now[Calendar.MONTH] && c1[Calendar.YEAR] == now[Calendar.YEAR]
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

fun isToday(timestamp: Long): Boolean {
    try {
        val c1 = Calendar.getInstance()
        c1.timeInMillis = timestamp
        val now = Calendar.getInstance()
        return c1[Calendar.DAY_OF_MONTH] == now[Calendar.DAY_OF_MONTH] && c1[Calendar.MONTH] == now[Calendar.MONTH] && c1[Calendar.YEAR] == now[Calendar.YEAR]
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

fun isToday(ymd: TripleObject<Int?, Int?, Int?>): Boolean {
    try {
        val c1 = Calendar.getInstance()
        c1[Calendar.YEAR] = ymd.o1
        c1[Calendar.MONDAY] = ymd.o2
        c1[Calendar.DAY_OF_MONTH] = ymd.o3
        val now = Calendar.getInstance()
        return c1[Calendar.DAY_OF_MONTH] == now[Calendar.DAY_OF_MONTH] && c1[Calendar.MONTH] == now[Calendar.MONTH] && c1[Calendar.YEAR] == now[Calendar.YEAR]
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}


fun getDays(timeStamp: Long): Long {
    return TimeUnit.MILLISECONDS.toDays(timeStamp)
}

fun getDays(pattern: String, date: String): Long? {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    try {
        return getDays(sdf.parse(date).time)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}


fun getHours(timeStamp: Long): Long {
    return TimeUnit.MILLISECONDS.toHours(timeStamp)
}

fun getHoursTillDate(): Long {
    return TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis())
}

fun getHours(pattern: String, date: String): Long? {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    try {
        return getHours(sdf.parse(date).time)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun getTimeInMillis(ymd: TripleObject<Int?, Int?, Int?>): Long {
    val calendar = Calendar.getInstance()
    calendar[Calendar.YEAR] = ymd.o1
    calendar[Calendar.MONDAY] = ymd.o2
    calendar[Calendar.DAY_OF_MONTH] = ymd.o3
    return calendar.timeInMillis
}

interface PATTERN {
    companion object {
        const val FULL_DISPLAY_FORMAT: String = "yyyy/MM/dd hh:mm a"
        const val DISPLAY_FORMAT: String = "yyyy/MM/dd"
        const val MONTH_YEAR_DISPLAY_FORMAT: String = "MMM, yyyy"
        const val AM_PM_HOURS: String = "hh"
        const val MINUTES: String = "mm"
        const val AM_PM: String = "a"
        const val TIME_AM_PM: String = "hh:mm a"
        const val DAY_NAME: String = "EEE"
    }
}