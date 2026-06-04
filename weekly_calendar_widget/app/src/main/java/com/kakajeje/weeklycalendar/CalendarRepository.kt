package com.kakajeje.weeklycalendar

import android.content.Context
import android.provider.CalendarContract
import java.time.LocalDate
import java.time.ZoneId

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val calendarColor: Int,
    val location: String?
)

object CalendarRepository {

    fun getWeekEvents(context: Context): LinkedHashMap<LocalDate, List<CalendarEvent>> {
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()

        val startMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = today.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val rawEvents = queryCalendarEvents(context, startMillis, endMillis)

        val result = LinkedHashMap<LocalDate, MutableList<CalendarEvent>>()
        for (i in 0..6) {
            result[today.plusDays(i.toLong())] = mutableListOf()
        }

        for (event in rawEvents) {
            for (i in 0..6) {
                val day = today.plusDays(i.toLong())
                val dayStart = day.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val dayEnd = day.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                if (event.startTime < dayEnd && event.endTime > dayStart) {
                    result[day]?.add(event)
                }
            }
        }

        result.forEach { (_, list) ->
            list.sortWith(compareBy({ if (it.allDay) 0L else 1L }, { it.startTime }))
        }

        return LinkedHashMap(result.mapValues { it.value.toList() })
    }

    private fun queryCalendarEvents(
        context: Context,
        startMillis: Long,
        endMillis: Long
    ): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(startMillis.toString())
            .appendPath(endMillis.toString())
            .build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.CALENDAR_COLOR,
            CalendarContract.Instances.EVENT_LOCATION
        )

        context.contentResolver.query(
            uri, projection, null, null,
            "${CalendarContract.Instances.BEGIN} ASC"
        )?.use { cursor ->
            val idxId = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val idxTitle = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val idxBegin = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val idxEnd = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val idxAllDay = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val idxColor = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_COLOR)
            val idxLocation = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)

            while (cursor.moveToNext()) {
                events.add(
                    CalendarEvent(
                        id = cursor.getLong(idxId),
                        title = cursor.getString(idxTitle) ?: "제목 없음",
                        startTime = cursor.getLong(idxBegin),
                        endTime = cursor.getLong(idxEnd),
                        allDay = cursor.getInt(idxAllDay) != 0,
                        calendarColor = cursor.getInt(idxColor),
                        location = cursor.getString(idxLocation)
                    )
                )
            }
        }

        return events
    }
}
