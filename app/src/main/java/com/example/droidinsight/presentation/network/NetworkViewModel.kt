package com.example.droidinsight.presentation.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.droidinsight.domain.model.NetworkModel
import com.example.droidinsight.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repository: NetworkRepository
) : ViewModel() {

    // ... (기존 변수들: currentDownloadSpeed, uploadSpeed, history 등 유지) ...
    private val _currentDownloadSpeed = MutableStateFlow(0L)
    val currentDownloadSpeed = _currentDownloadSpeed.asStateFlow()

    private val _currentUploadSpeed = MutableStateFlow(0L)
    val currentUploadSpeed = _currentUploadSpeed.asStateFlow()

    private val _downloadHistory = MutableStateFlow<List<Long>>(List(60) { 0L })
    val downloadHistory = _downloadHistory.asStateFlow()

    // 측정 관련 상태
    private val _isTesting = MutableStateFlow(false)
    val isTesting = _isTesting.asStateFlow()

    private val _maxSpeed = MutableStateFlow(0L)
    val maxSpeed = _maxSpeed.asStateFlow()

    private val _avgSpeed = MutableStateFlow(0L)
    val avgSpeed = _avgSpeed.asStateFlow()

    private var totalSpeedSum = 0L
    private var sampleCount = 0
    private var testJob: Job? = null // 테스트 취소용 Job

    init {
        viewModelScope.launch {
            repository.observeNetworkUsage().collect { networkModel ->
                _currentDownloadSpeed.value = networkModel.downloadSpeed
                _currentUploadSpeed.value = networkModel.uploadSpeed

                val oldList = _downloadHistory.value.toMutableList()
                if (oldList.isNotEmpty()) {
                    oldList.removeAt(0)
                    oldList.add(networkModel.downloadSpeed)
                }
                _downloadHistory.value = oldList

                // 측정 중일 때 통계 계산
                if (_isTesting.value) {
                    val currentTotal = networkModel.downloadSpeed

                    if (currentTotal > _maxSpeed.value) {
                        _maxSpeed.value = currentTotal
                    }

                    // 0이 아닌 유효한 속도만 평균에 반영
                    if (currentTotal > 0) {
                        totalSpeedSum += currentTotal
                        sampleCount++
                        _avgSpeed.value = totalSpeedSum / sampleCount
                    }
                }
            }
        }
    }

    // [핵심 수정] 실제 다운로드를 걸어서 속도를 측정함
    fun toggleTest() {
        if (_isTesting.value) {
            stopTest()
        } else {
            startTest()
        }
    }

    private fun startTest() {
        _isTesting.value = true
        _maxSpeed.value = 0L
        _avgSpeed.value = 0L
        totalSpeedSum = 0L
        sampleCount = 0

        testJob = viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            // [수정] 가장 안정적인 테스트 파일 (HTTPS)
            val request = Request.Builder()
                .url("https://proof.ovh.net/files/100Mb.dat")
                .build()

            try {
                Log.d("SpeedTest", "🚀 다운로드 시작...") // 로그 확인용

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("SpeedTest", "❌ 서버 응답 실패: ${response.code}")
                        throw IOException("Unexpected code $response")
                    }

                    val inputStream = response.body?.byteStream()
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    Log.d("SpeedTest", "✅ 연결 성공! 데이터 읽는 중...")

                    // 데이터를 읽으면서 루프 (TrafficStats가 감지함)
                    while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1 && _isTesting.value) {
                        // 여기서 아무것도 안 해도 read() 하는 행위 자체가 트래픽을 유발함
                    }
                }
                Log.d("SpeedTest", "🏁 다운로드 완료")

            } catch (e: Exception) {
                // [중요] 에러가 나면 여기에 뜹니다.
                Log.e("SpeedTest", "❌ 에러 발생: ${e.message}", e)
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    stopTest()
                }
            }
        }
    }

    fun stopTest() {
        _isTesting.value = false
        testJob?.cancel() // 다운로드 중단
    }

    // ... (formatSpeed 함수 유지) ...
    fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format("%.1f MB/s", bytesPerSec / (1024f * 1024f))
            bytesPerSec >= 1024 -> String.format("%.1f KB/s", bytesPerSec / 1024f)
            else -> "$bytesPerSec B/s"
        }
    }
}