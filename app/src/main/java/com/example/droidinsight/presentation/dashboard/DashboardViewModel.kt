package com.example.droidinsight.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.droidinsight.data.repository.SystemRepository
import com.example.droidinsight.domain.model.BatteryModel
import com.example.droidinsight.domain.model.SystemInfo
import com.example.droidinsight.domain.repository.BatteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    batteryRepository: BatteryRepository,
    private val systemRepository: SystemRepository
) : ViewModel() {

    // 1. 배터리 상태 (Reactive Stream)
    val batteryState: StateFlow<BatteryModel> = batteryRepository.getBatteryData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BatteryModel()
        )

    // 2. 시스템 정보 (Polling Stream)
    val systemInfo: StateFlow<SystemInfo> = flow {
        while (true) {
            emit(systemRepository.getSystemInfo()) // 데이터 방출
            delay(3000) // 3초 대기
        }
    }
        .flowOn(Dispatchers.IO) // 시스템 정보 조회는 I/O 작업이므로 IO 스레드에서 실행
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SystemInfo()
        )

    /**
     * 바이트(Byte) 단위를 사람이 읽기 쉬운 GB 단위 문자열로 변환
     * UI 로직(View)을 단순하게 유지하기 위해 ViewModel에서 포맷팅
     */
    fun formatSize(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        // Locale.getDefault()를 사용하여 국가별 소수점 표기(.,)를 준수
        return String.format(Locale.getDefault(), "%.1f GB", gb)
    }
}