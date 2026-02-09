package com.example.droidinsight.presentation.widget

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey // 추가

object WidgetKeys {
    val CURRENT_MODE = intPreferencesKey("current_mode")

    // [추가] 새로고침을 강제하기 위한 "마지막 업데이트 시간" 키
    val LAST_UPDATED = longPreferencesKey("last_updated")
}