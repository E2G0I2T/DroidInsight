package com.example.droidinsight.presentation.network

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.droidinsight.domain.model.NetworkModel
import com.example.droidinsight.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repository: NetworkRepository
) : ViewModel() {

    companion object {
        private const val TAG = "NetworkViewModel"
        // 100MB 더미 파일
        private const val TEST_FILE_URL = "https://proof.ovh.net/files/100Mb.dat"
        private const val BUFFER_SIZE = 8192 // 8KB
        private const val HISTORY_SIZE = 60
    }

    private val _currentDownloadSpeed = MutableStateFlow(0L)
    val currentDownloadSpeed = _currentDownloadSpeed.asStateFlow()

    private val _currentUploadSpeed = MutableStateFlow(0L)
    val currentUploadSpeed = _currentUploadSpeed.asStateFlow()

    private val _downloadHistory = MutableStateFlow<List<Long>>(List(HISTORY_SIZE) { 0L })
    val downloadHistory = _downloadHistory.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting = _isTesting.asStateFlow()

    private val _maxSpeed = MutableStateFlow(0L)
    val maxSpeed = _maxSpeed.asStateFlow()

    private val _avgSpeed = MutableStateFlow(0L)
    val avgSpeed = _avgSpeed.asStateFlow()

    private var totalSpeedSum = 0L
    private var sampleCount = 0
    private var testJob: Job? = null

    private val client by lazy { OkHttpClient() }

    init {
        observeNetworkData()
    }

    private fun observeNetworkData() {
        viewModelScope.launch {
            repository.observeNetworkUsage().collect { networkModel ->
                updateRealtimeStats(networkModel)

                if (_isTesting.value) {
                    calculateBenchmarkStats(networkModel.downloadSpeed)
                }
            }
        }
    }

    private fun updateRealtimeStats(model: NetworkModel) {
        _currentDownloadSpeed.value = model.downloadSpeed
        _currentUploadSpeed.value = model.uploadSpeed

        // 히스토리 업데이트
        val currentList = _downloadHistory.value.toMutableList()
        if (currentList.isNotEmpty()) {
            currentList.removeAt(0)
            currentList.add(model.downloadSpeed)
        }
        _downloadHistory.value = currentList
    }

    private fun calculateBenchmarkStats(currentSpeed: Long) {
        // 최대 속도 갱신
        if (currentSpeed > _maxSpeed.value) {
            _maxSpeed.value = currentSpeed
        }

        // 평균 속도 갱신 (0인 구간은 제외)
        if (currentSpeed > 0) {
            totalSpeedSum += currentSpeed
            sampleCount++
            _avgSpeed.value = totalSpeedSum / sampleCount
        }
    }

    fun toggleTest() {
        if (_isTesting.value) stopTest() else startTest()
    }

    /**
     * 원칙적으로 네트워크 요청 로직은 Repository나 UseCase에 위치해야 함
     * 하지만 이 프로젝트에서는 'TrafficStats'의 변화를 유발하기 위한 트리거 역할이므로 편의상 ViewModel에 구현
     */
    private fun startTest() {
        resetBenchmarkStats()
        _isTesting.value = true

        testJob = viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder().url(TEST_FILE_URL).build()

            try {
                Log.d(TAG, "🚀 Start Download Benchmark: $TEST_FILE_URL")

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val inputStream = response.body?.byteStream() ?: return@use
                    val buffer = ByteArray(BUFFER_SIZE)

                    // 데이터를 읽어들이며 트래픽 발생 (TrafficStats가 감지함)
                    // _isTesting이 false가 되면(중지 버튼) 루프 탈출
                    while (isActive && _isTesting.value && inputStream.read(buffer) != -1) {
                        // Just consume the stream
                    }
                }
                Log.d(TAG, "✅ Download Benchmark Finished")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Benchmark Error: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    stopTest()
                }
            }
        }
    }

    fun stopTest() {
        _isTesting.value = false
        testJob?.cancel()
    }

    private fun resetBenchmarkStats() {
        _maxSpeed.value = 0L
        _avgSpeed.value = 0L
        totalSpeedSum = 0L
        sampleCount = 0
    }

    fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB/s", bytesPerSec / (1024f * 1024f))
            bytesPerSec >= 1024 -> String.format(Locale.getDefault(), "%.1f KB/s", bytesPerSec / 1024f)
            else -> "$bytesPerSec B/s"
        }
    }
}