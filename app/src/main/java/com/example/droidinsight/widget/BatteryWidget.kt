package com.example.droidinsight.presentation.widget

import android.content.Context
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class BatteryWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val modeIndex = prefs[WidgetKeys.CURRENT_MODE] ?: 0

            // Enum 변환
            val currentMode = if (modeIndex == 0) WidgetMode.Battery else WidgetMode.Ram

            val (title, infoValue) = when (currentMode) {
                WidgetMode.Battery -> "Battery" to getBatteryLevel(context)
                WidgetMode.Ram -> "RAM Usage" to getRamUsage(context)
            }

            WidgetContent(
                title = title,
                info = "$infoValue%",
                isBatteryMode = currentMode == WidgetMode.Battery
            )
        }
    }

    @Composable
    private fun WidgetContent(
        title: String,
        info: String,
        isBatteryMode: Boolean
    ) {
        val backgroundColor = Color.DarkGray
        val accentColor = if (isBatteryMode) Color(0xFF00E676) else Color(0xFF2979FF)
        val bgProvider = ColorProvider(day = backgroundColor, night = backgroundColor)
        val textProvider = ColorProvider(day = Color.White, night = Color.White)
        val accentProvider = ColorProvider(day = accentColor, night = accentColor)
        val iconTintProvider = ColorProvider(day = Color.LightGray, night = Color.LightGray)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgProvider)
                .cornerRadius(16.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // [Header] 타이틀 + 모드 변경 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.clickable(actionRunCallback<ToggleAction>())
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            color = textProvider,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Image(
                        provider = ImageProvider(android.R.drawable.ic_menu_sort_by_size),
                        contentDescription = "Switch Mode",
                        modifier = GlanceModifier.size(16.dp),
                        colorFilter = ColorFilter.tint(iconTintProvider)
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // [Body] 값 표시 + 새로고침 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>())
                ) {
                    Text(
                        text = info,
                        style = TextStyle(
                            color = accentProvider,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Image(
                        provider = ImageProvider(android.R.drawable.ic_popup_sync),
                        contentDescription = "Refresh",
                        modifier = GlanceModifier.size(20.dp),
                        // [수정] ColorFilter 참조 에러 해결
                        colorFilter = ColorFilter.tint(accentProvider)
                    )
                }
            }
        }
    }

    private fun getBatteryLevel(context: Context): Int {
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getRamUsage(context: Context): Int {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        if (memInfo.totalMem == 0L) return 0
        return ((memInfo.totalMem - memInfo.availMem) * 100 / memInfo.totalMem).toInt()
    }

    enum class WidgetMode(val index: Int) {
        Battery(0), Ram(1)
    }
}