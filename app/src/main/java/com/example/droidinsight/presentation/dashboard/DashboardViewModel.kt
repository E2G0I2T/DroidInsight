package com.example.droidinsight.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.droidinsight.data.repository.SystemRepository
import com.example.droidinsight.domain.model.BatteryModel
import com.example.droidinsight.domain.model.SystemInfo
import com.example.droidinsight.domain.repository.BatteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val systemRepository: SystemRepository
) : ViewModel() {

    // 배터리 상태 (기존 코드 유지)
    val batteryState: StateFlow<BatteryModel> = batteryRepository.getBatteryData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BatteryModel()
        )

    // [추가] 시스템 정보 상태
    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo = _systemInfo.asStateFlow()

    init {
        startSystemMonitor()
    }

    // 3초마다 RAM/저장소 정보 갱신
    private fun startSystemMonitor() {
        viewModelScope.launch {
            while (isActive) {
                _systemInfo.value = systemRepository.getSystemInfo()
                delay(3000) // 3초 주기
            }
        }
    }

    // 용량 변환 헬퍼 함수 (Byte -> GB)
    fun formatSize(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return String.format("%.1f GB", gb)
    }
}