package com.example.droidinsight.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.droidinsight.presentation.widget.WidgetKeys.CURRENT_MODE
import com.example.droidinsight.presentation.widget.WidgetKeys.LAST_UPDATED // 추가

class ToggleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[CURRENT_MODE] ?: 0
            prefs[CURRENT_MODE] = (current + 1) % 3

            prefs[LAST_UPDATED] = System.currentTimeMillis()
        }
        BatteryWidget().update(context, glanceId)
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        // [핵심 수정] 상태(State)를 강제로 변경합니다.
        updateAppWidgetState(context, glanceId) { prefs ->
            // 현재 시간을 저장해서 무조건 데이터가 변하게 만듦
            prefs[LAST_UPDATED] = System.currentTimeMillis()
        }

        // 데이터가 변했으니 update 호출 시 확실하게 다시 그립니다.
        BatteryWidget().update(context, glanceId)
    }
}