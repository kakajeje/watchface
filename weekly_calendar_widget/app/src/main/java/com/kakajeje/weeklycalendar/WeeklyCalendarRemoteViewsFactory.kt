package com.kakajeje.weeklycalendar

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class WeeklyCalendarRemoteViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private sealed class ListItem {
        data class DayHeader(
            val date: LocalDate,
            val isToday: Boolean,
            val isWeekend: Boolean
        ) : ListItem()

        data class EventItem(val event: CalendarEvent) : ListItem()

        object EmptyDay : ListItem()
    }

    private var items: List<ListItem> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val weekEvents = CalendarRepository.getWeekEvents(context)
        val today = LocalDate.now()
        val newItems = mutableListOf<ListItem>()

        for ((date, events) in weekEvents) {
            val dow = date.dayOfWeek.value  // 1=Mon .. 7=Sun
            val isWeekend = dow == 6 || dow == 7
            newItems.add(ListItem.DayHeader(date, date == today, isWeekend))

            if (events.isEmpty()) {
                newItems.add(ListItem.EmptyDay)
            } else {
                events.forEach { newItems.add(ListItem.EventItem(it)) }
            }
        }

        items = newItems
    }

    override fun onDestroy() {}

    override fun getCount(): Int = items.size

    override fun getViewTypeCount(): Int = 3

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ListItem.DayHeader -> 0
        is ListItem.EventItem -> 1
        is ListItem.EmptyDay -> 2
    }

    override fun getViewAt(position: Int): RemoteViews = when (val item = items[position]) {
        is ListItem.DayHeader -> buildDayHeader(item)
        is ListItem.EventItem -> buildEventItem(item)
        is ListItem.EmptyDay -> buildEmptyItem()
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false

    private fun buildDayHeader(item: ListItem.DayHeader): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_list_item_day)

        val koreanDayNames = arrayOf("월", "화", "수", "목", "금", "토", "일")
        val dow = item.date.dayOfWeek.value  // 1=Mon
        val dayName = "${koreanDayNames[dow - 1]}요일"

        rv.setTextViewText(R.id.tvDayName, dayName)
        rv.setTextViewText(R.id.tvDate, "${item.date.monthValue}/${item.date.dayOfMonth}")

        val dayColor = when {
            item.isToday -> Color.parseColor("#64B5F6")
            item.isWeekend -> Color.parseColor("#EF9A9A")
            else -> Color.parseColor("#B0BEC5")
        }
        rv.setTextColor(R.id.tvDayName, dayColor)

        val bg = if (item.isToday) R.drawable.today_header_background else R.drawable.day_header_background
        rv.setInt(R.id.dayHeaderRoot, "setBackgroundResource", bg)

        rv.setViewVisibility(R.id.tvToday, if (item.isToday) View.VISIBLE else View.GONE)

        // Tap day header → open calendar to that date
        val fillIn = Intent().apply {
            putExtra("selectedTime",
                item.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }
        rv.setOnClickFillInIntent(R.id.dayHeaderRoot, fillIn)

        return rv
    }

    private fun buildEventItem(item: ListItem.EventItem): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_list_item_event)

        val timeText = if (item.event.allDay) {
            context.getString(R.string.all_day)
        } else {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val s = sdf.format(Date(item.event.startTime))
            val e = sdf.format(Date(item.event.endTime))
            "$s-$e"
        }

        rv.setTextViewText(R.id.tvEventTime, timeText)
        rv.setTextViewText(R.id.tvEventTitle, item.event.title)

        val color = if (item.event.calendarColor != 0) item.event.calendarColor
                    else Color.parseColor("#4FC3F7")
        rv.setInt(R.id.viewColorIndicator, "setBackgroundColor", color)

        val fillIn = Intent().apply {
            putExtra("eventId", item.event.id)
            putExtra("eventStartTime", item.event.startTime)
        }
        rv.setOnClickFillInIntent(R.id.tvEventTitle, fillIn)

        return rv
    }

    private fun buildEmptyItem(): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_list_item_empty)
}
