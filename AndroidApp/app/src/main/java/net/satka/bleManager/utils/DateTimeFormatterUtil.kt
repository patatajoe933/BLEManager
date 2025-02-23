package net.satka.bleManager.utils

import android.content.Context
import android.text.format.DateFormat
import java.util.Calendar
import java.util.Date

object DateTimeFormatterUtil {

    fun formatTime(context: Context, hour: Int?, minute: Int?): String? {
        if (hour == null || minute == null) {
            return null
        }
        return DateFormat.getTimeFormat(context).format(createTime(hour, minute))
    }

    fun formatDate(context: Context, year: Int?, month: Int?, day: Int?): String? {
        if (year == null || month == null || day == null) {
            return null
        }
        return DateFormat.getMediumDateFormat(context).format(createDate(year, month, day))
    }

    fun formatDateTime(context: Context, year: Int?, month: Int?, day: Int?, hour: Int?, minute: Int?): String? {
        if (year == null || month == null || day == null || hour == null || minute == null) {
            return null
        }
        val dateTime = createDateTime(year, month, day, hour, minute)
        val formattedDate = DateFormat.getMediumDateFormat(context).format(dateTime)
        val formattedTime = DateFormat.getTimeFormat(context).format(dateTime)
        return "$formattedDate $formattedTime"
    }

    fun formatDateTime(context: Context, dateMillis: Long?, hour: Int?, minute: Int?): String? {
        if (dateMillis == null || hour == null || minute == null) {
            return null
        }
        val dateTime = createDateTime(dateMillis, hour, minute)
        val formattedDate = DateFormat.getMediumDateFormat(context).format(dateTime)
        val formattedTime = DateFormat.getTimeFormat(context).format(dateTime)
        return "$formattedDate $formattedTime"
    }

    private fun createTime(hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return calendar.time
    }

    private fun createDate(year: Int, month: Int, day: Int): Date {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // Měsíce jsou indexované od 0 (leden = 0)
            set(Calendar.DAY_OF_MONTH, day)
        }
        return calendar.time
    }

    private fun createDateTime(millis: Long): Date {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
        }
        return calendar.time
    }

    private fun createDateTime(dateMillis: Long, hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return calendar.time
    }

    private fun createDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // Měsíce jsou indexované od 0 (leden = 0)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return calendar.time
    }
}
