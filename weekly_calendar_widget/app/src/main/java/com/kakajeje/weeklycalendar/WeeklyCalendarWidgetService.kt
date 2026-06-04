package com.kakajeje.weeklycalendar

import android.content.Intent
import android.widget.RemoteViewsService

class WeeklyCalendarWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        WeeklyCalendarRemoteViewsFactory(applicationContext, intent)
}
