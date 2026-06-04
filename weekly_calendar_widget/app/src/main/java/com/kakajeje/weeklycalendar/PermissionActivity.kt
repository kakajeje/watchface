package com.kakajeje.weeklycalendar

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle

class PermissionActivity : Activity() {

    companion object {
        private const val REQ_CALENDAR = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            triggerWidgetUpdate()
            finish()
            return
        }

        requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), REQ_CALENDAR)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CALENDAR) {
            triggerWidgetUpdate()
        }
        finish()
    }

    private fun triggerWidgetUpdate() {
        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(this, WeeklyCalendarWidget::class.java))
        val intent = Intent(this, WeeklyCalendarWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        sendBroadcast(intent)
    }
}
