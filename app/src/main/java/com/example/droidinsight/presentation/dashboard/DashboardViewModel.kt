package com.example.droidinsight.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.droidinsight.domain.model.BatteryModel
import com.example.droidinsight.domain.repository.BatteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    // Repository에서 오는 Flow 데이터를 UI가 구독할 수 있는 StateFlow로 변환
    val batteryState: StateFlow<BatteryModel> = batteryRepository.getBatteryData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 화면 꺼지면 5초 뒤 구독 중단 (배터리 절약)
            initialValue = BatteryModel() // 초기값 (빈 껍데기)
        )
}