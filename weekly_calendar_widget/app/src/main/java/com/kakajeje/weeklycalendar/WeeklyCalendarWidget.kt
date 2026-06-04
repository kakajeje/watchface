package com.kakajeje.weeklycalendar

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            for (id in ids) {
                updateAppWidget(context, manager, id)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.kakajeje.weeklycalendar.ACTION_REFRESH"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weekly_calendar)

            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                views.setViewVisibility(R.id.widget_list, View.GONE)
                views.setViewVisibility(R.id.layoutNoPermission, View.VISIBLE)

                val permIntent = Intent(context, PermissionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val permPi = PendingIntent.getActivity(
                    context, appWidgetId, permIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.btnGrantPermission, permPi)
            } else {
                views.setViewVisibility(R.id.widget_list, View.VISIBLE)
                views.setViewVisibility(R.id.layoutNoPermission, View.GONE)

                // Week range label in header
                val today = LocalDate.now()
                val endDay = today.plusDays(6)
                val fmt = DateTimeFormatter.ofPattern("M/d")
                views.setTextViewText(
                    R.id.tvWeekRange,
                    "${today.format(fmt)} ~ ${endDay.format(fmt)}"
                )

                // Connect ListView to RemoteViewsService
                val serviceIntent = Intent(context, WeeklyCalendarWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
                views.setRemoteAdapter(R.id.widget_list, serviceIntent)

                // Tapping an event/day opens Samsung Calendar
                val calIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val calPi = PendingIntent.getActivity(
                    context, 0, calIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setPendingIntentTemplate(R.id.widget_list, calPi)

                // Tap title → manual refresh
                val refreshIntent = Intent(context, WeeklyCalendarWidget::class.java).apply {
                    action = ACTION_REFRESH
                }
                val refreshPi = PendingIntent.getBroadcast(
                    context, appWidgetId, refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.tvWidgetTitle, refreshPi)

                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
