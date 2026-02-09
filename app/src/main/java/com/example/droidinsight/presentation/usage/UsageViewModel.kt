package com.example.droidinsight.presentation.usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.droidinsight.domain.model.UsageModel
import com.example.droidinsight.domain.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsageViewModel @Inject constructor(
    private val repository: UsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<UsageModel>>(emptyList())
    val uiState = _uiState.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission = _hasPermission.asStateFlow()

    // 화면이 뜰 때마다 권한 체크 & 데이터 로드
    fun checkPermissionAndLoadData() {
        _hasPermission.value = repository.hasPermission()
        if (_hasPermission.value) {
            loadUsageStats()
        }
    }

    private fun loadUsageStats() {
        viewModelScope.launch {
            val rawList = repository.getTodayUsageStats()

            // 데이터가 하나도 없으면 빈 리스트 처리
            if (rawList.isEmpty()) {
                _uiState.value = emptyList()
                return@launch
            }

            // 1. 가장 오래 쓴 앱의 시간 찾기 (이게 기준값 1.0이 됨)
            val maxUsageTime = rawList.first().usageTime.toFloat()

            // 2. 각 앱의 비율(progress) 계산해서 업데이트
            val processedList = rawList.map { model ->
                model.copy(
                    progress = if (maxUsageTime > 0) (model.usageTime / maxUsageTime) else 0f
                )
            }

            _uiState.value = processedList
        }
    }

    // 시간 포맷팅 헬퍼 함수 (예: 1시간 30분 10초)
    fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}시간 ${minutes % 60}분"
            minutes > 0 -> "${minutes}분 ${seconds % 60}초"
            else -> "${seconds}초"
        }
    }
}