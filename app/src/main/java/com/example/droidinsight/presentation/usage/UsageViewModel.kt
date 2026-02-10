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

    // 1. UI 상태 (앱 목록)
    private val _uiState = MutableStateFlow<List<UsageModel>>(emptyList())
    val uiState = _uiState.asStateFlow()

    // 2. 권한 상태
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission = _hasPermission.asStateFlow()

    /**
     * 화면이 포커스를 받을 때마다 호출
     * 권한이 있는지 확인하고, 있다면 최신 데이터를 로드
     */
    fun checkPermissionAndLoadData() {
        val isAllowed = repository.hasPermission()
        _hasPermission.value = isAllowed

        if (isAllowed) {
            loadUsageStats()
        }
    }

    private fun loadUsageStats() {
        viewModelScope.launch {
            _uiState.value = repository.getTodayUsageStats()
        }
    }

    /**
     * 밀리초(ms) 단위를 "1시간 30분" 같은 문자열로 변환
     * View에서 로직을 처리하지 않도록 ViewModel이 변환을 담당
     */
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