package com.example.droidinsight.presentation.widget

import android.content.Context
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.Image // 추가
import androidx.glance.ImageProvider // 추가
import androidx.glance.layout.Row // 추가
import androidx.glance.layout.size // 추가
import com.example.droidinsight.R // 리소스 임포트
import androidx.glance.layout.width // [필수] 이거 한 줄이면 해결됩니다!
import androidx.glance.layout.height // 혹시 없으면 이것도
import androidx.glance.layout.size // 아이콘 사이즈 조절용

class BatteryWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val mode = prefs[WidgetKeys.CURRENT_MODE] ?: 0
            val lastUpdated = prefs[WidgetKeys.LAST_UPDATED] ?: 0L

            val info = when(mode) {
                0 -> getBatteryInfo(context) // 여기서는 lastUpdated를 안 써도, 위에서 읽었으니 재실행됨
                1 -> getRamInfo(context)
                else -> "Err"
            }

            val title = when(mode) {
                0 -> "Battery ↻"
                1 -> "RAM Usage ↻"
                else -> "Info"
            }

            WidgetContent(title, info)
        }
    }

    @Composable
    fun WidgetContent(title: String, info: String) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.DarkGray))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // [1. 상단] 모드 변경 버튼 (제목 + 아이콘)
                // "Battery" 글자나 옆의 아이콘을 누르면 모드가 바뀜
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.clickable(actionRunCallback<ToggleAction>()) // 전체 클릭 가능
                ) {
                    Text(
                        text = title.replace(" ↻", ""),
                        style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))

                    // 작은 전환 아이콘
                    Image(
                        provider = ImageProvider(android.R.drawable.ic_menu_sort_by_size),
                        contentDescription = "Change Mode",
                        modifier = GlanceModifier.size(16.dp),
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Color.White))
                    )
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                // [2. 하단] 새로고침 버튼 (값 + 아이콘)
                // 값을 누르면 새로고침됨
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>()) // 전체 클릭 가능
                ) {
                    Text(
                        text = info,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF00E676)),
                            fontSize = 28.sp, // 글자 더 크게
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // 큰 새로고침 아이콘 (회전 화살표)
                    Image(
                        provider = ImageProvider(android.R.drawable.ic_popup_sync), // 동기화 아이콘
                        contentDescription = "Refresh",
                        modifier = GlanceModifier.size(24.dp),
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Color(0xFF00E676))) // 색깔 맞춤
                    )
                }
            }
        }
    }

    private fun getBatteryInfo(context: Context): String {
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return "$level%"
    }

    private fun getRamInfo(context: Context): String {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val percent = ((memInfo.totalMem - memInfo.availMem) * 100 / memInfo.totalMem).toInt()
        return "$percent%"
    }
}