package com.example.droidinsight.presentation.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    // 우리가 만들 위젯 UI 클래스를 연결해줍니다.
    override val glanceAppWidget: GlanceAppWidget = BatteryWidget()
}