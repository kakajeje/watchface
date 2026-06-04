package com.kakajeje.weeklycalendar

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class WeeklyCalendarWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_PROVIDER_CHANGED ||
            intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, WeeklyCalendarWidget::class.java)
            )
            for (id in ids) updateAppWidget(context, manager, id)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.kakajeje.weeklycalendar.ACTION_REFRESH"
        private const val MAX_EVENTS_PER_DAY = 5

        private val KOREAN_DAYS = arrayOf("월", "화", "수", "목", "금", "토", "일")

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weekly_calendar)

            // Tap on title → manual refresh
            val refreshIntent = Intent(context, WeeklyCalendarWidget::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPi = PendingIntent.getBroadcast(
                context, appWidgetId, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tvWidgetTitle, refreshPi)

            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                views.setViewVisibility(R.id.weekContainer, View.GONE)
                views.setViewVisibility(R.id.layoutNoPermission, View.VISIBLE)

                val permPi = PendingIntent.getActivity(
                    context, appWidgetId,
                    Intent(context, PermissionActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.btnGrantPermission, permPi)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            views.setViewVisibility(R.id.weekContainer, View.VISIBLE)
            views.setViewVisibility(R.id.layoutNoPermission, View.GONE)

            val today = LocalDate.now()
            val endDay = today.plusDays(6)
            views.setTextViewText(
                R.id.tvWeekRange,
                "${today.format(DateTimeFormatter.ofPattern("M/d"))} ~ " +
                        endDay.format(DateTimeFormatter.ofPattern("M/d"))
            )

            // Build 7 day columns
            val weekEvents = CalendarRepository.getWeekEvents(context)
            views.removeAllViews(R.id.weekContainer)

            for (i in 0..6) {
                val date = today.plusDays(i.toLong())
                val events = weekEvents[date] ?: emptyList()
                val col = buildDayColumn(context, date, events, today)
                views.addView(R.id.weekContainer, col)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun buildDayColumn(
            context: Context,
            date: LocalDate,
            events: List<CalendarEvent>,
            today: LocalDate
        ): RemoteViews {
            val rv = RemoteViews(context.packageName, R.layout.widget_day_column)

            val dow = date.dayOfWeek.value  // 1=Mon … 7=Sun
            val isToday = date == today
            val isWeekend = dow == 6 || dow == 7

            // Day name
            rv.setTextViewText(R.id.tvDayOfWeek, KOREAN_DAYS[dow - 1])
            rv.setTextColor(
                R.id.tvDayOfWeek,
                when {
                    isToday -> Color.parseColor("#64B5F6")
                    isWeekend -> Color.parseColor("#EF9A9A")
                    else -> Color.parseColor("#B0BEC5")
                }
            )

            // Date number
            rv.setTextViewText(R.id.tvDayNum, date.dayOfMonth.toString())

            // Header background: brighter for today
            rv.setInt(
                R.id.dayHeaderArea,
                "setBackgroundResource",
                if (isToday) R.drawable.today_header_background else R.drawable.day_header_background
            )

            // "오늘" badge
            rv.setViewVisibility(R.id.tvTodayBadge, if (isToday) View.VISIBLE else View.GONE)

            // Events
            rv.removeAllViews(R.id.eventsContainer)
            if (events.isEmpty()) {
                rv.addView(
                    R.id.eventsContainer,
                    RemoteViews(context.packageName, R.layout.widget_day_no_events)
                )
            } else {
                events.take(MAX_EVENTS_PER_DAY).forEach { event ->
                    rv.addView(R.id.eventsContainer, buildEventItem(context, event))
                }
            }

            return rv
        }

        private fun buildEventItem(context: Context, event: CalendarEvent): RemoteViews {
            val rv = RemoteViews(context.packageName, R.layout.widget_day_event)

            rv.setTextViewText(R.id.tvEventTitle, event.title)

            val timeText = if (event.allDay) {
                context.getString(R.string.all_day)
            } else {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(Date(event.startTime))
            }
            rv.setTextViewText(R.id.tvEventTime, timeText)

            val dotColor = if (event.calendarColor != 0) event.calendarColor
                           else Color.parseColor("#4FC3F7")
            rv.setInt(R.id.viewDot, "setBackgroundColor", dotColor)

            return rv
        }

        /** Open Samsung Calendar (or default calendar app) to current date. */
        fun buildCalendarPendingIntent(context: Context): PendingIntent {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALENDAR)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
