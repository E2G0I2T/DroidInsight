package com.example.droidinsight.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.droidinsight.presentation.widget.BatteryWidget.WidgetMode

/**
 * 위젯 상단(타이틀)을 클릭했을 때 실행되는 액션
 * Battery -> RAM -> ... 순서로 모드를 순환 변경
 */
class ToggleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            // 현재 모드 인덱스 가져오기 (기본값: Battery)
            val currentIndex = prefs[WidgetKeys.CURRENT_MODE] ?: WidgetMode.Battery.index
            val nextIndex = (currentIndex + 1) % WidgetMode.entries.size

            prefs[WidgetKeys.CURRENT_MODE] = nextIndex
            prefs[WidgetKeys.LAST_UPDATED] = System.currentTimeMillis() // 갱신 시간 기록
        }
        // 위젯 UI 업데이트 요청
        BatteryWidget().update(context, glanceId)
    }
}

/**
 * 위젯 하단(값/아이콘)을 클릭했을 때 실행되는 액션
 * 데이터를 최신 상태로 새로고침
 */
class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            // 데이터가 변하지 않았더라도, 강제로 리컴포지션을 유발하기 위해 타임스탬프 갱신
            prefs[WidgetKeys.LAST_UPDATED] = System.currentTimeMillis()
        }
        BatteryWidget().update(context, glanceId)
    }
}