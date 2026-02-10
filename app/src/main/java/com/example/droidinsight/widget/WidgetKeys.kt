package com.example.droidinsight.presentation.widget

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

/**
 * Glance 위젯의 상태를 저장하기 위한 DataStore Key 모음
 * 위젯의 표시 모드와 데이터 갱신 트리거를 관리
 */
object WidgetKeys {

    // 현재 위젯이 표시하고 있는 정보 모드
    val CURRENT_MODE = intPreferencesKey("current_mode")

    /**
     * 마지막으로 위젯이 갱신된 타임스탬프
     * Glance는 데이터가 변하지 않으면(예: 배터리 100% -> 100%) UI를 다시 그리지 않을 수 있기에
     * 사용자가 '새로고침'을 눌렀을 때, 강제로 UI 업데이트를 발생시키기 위해 이 값을 변경
     */
    val LAST_UPDATED = longPreferencesKey("last_updated")
}