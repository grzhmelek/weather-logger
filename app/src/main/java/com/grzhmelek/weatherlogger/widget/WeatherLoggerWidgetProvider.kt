package com.grzhmelek.weatherlogger.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.grzhmelek.weatherlogger.MainActivity
import com.grzhmelek.weatherlogger.R


class WeatherLoggerWidgetProvider : AppWidgetProvider() {

//    override fun onUpdate(
//        context: Context,
//        appWidgetManager: AppWidgetManager,
//        appWidgetIds: IntArray
//    ) {
//        // Perform this loop procedure for each App Widget that belongs to this provider
//        appWidgetIds.forEach { appWidgetId ->
//            // Create an Intent to launch ExampleActivity
//            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
//                .let { intent ->
//                    PendingIntent.getActivity(context, 0, intent, 0)
//                }
//
//            // Get the layout for the App Widget and attach an on-click listener
//            // to the button
//            val views: RemoteViews = RemoteViews(
//                context.packageName,
//                R.layout.weather_logger_widget
//            ).apply {
//                setOnClickPendingIntent(R.id.tvWidget, pendingIntent)
//            }
//
//            // Tell the AppWidgetManager to perform an update on the current app widget
//            appWidgetManager.updateAppWidget(appWidgetId, views)
//        }
//    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context, appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.weather_logger_widget)
        // Construct an Intent object includes web adresss.
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://erenutku.com"))

        val intent = Intent(context, MainActivity::class.java)

        // In widget we are not allowing to use intents as usually. We have to use PendingIntent instead of 'startActivity'
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        // Here the basic operations the remote view can do.
        views.setOnClickPendingIntent(R.id.widget, pendingIntent)
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}