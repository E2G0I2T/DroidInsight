package com.example.droidinsight.presentation.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * 홈 화면 위젯의 Broadcast Receiver
 * AndroidManifest.xml에 <receiver> 태그로 등록되어야 작동*
 */
class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryWidget()
}