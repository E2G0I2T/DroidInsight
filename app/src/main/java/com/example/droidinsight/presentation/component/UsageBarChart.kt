package com.example.droidinsight.presentation.component

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.droidinsight.domain.model.UsageModel

/**
 * 앱 사용 시간을 막대 그래프(Bar Chart)로 시각화하는 컴포넌트
 * Canvas API와 Android Native Paint를 혼합하여 그래픽과 텍스트를 모두 처리
 */
@Composable
fun UsageBarChart(
    usageList: List<UsageModel>,
    modifier: Modifier = Modifier
) {
    // 1. 데이터 가공: 상위 5개만 추출
    val top5Apps = remember(usageList) {
        usageList.sortedByDescending { it.usageTime }.take(5)
    }

    if (top5Apps.isEmpty()) return

    // 2. 색상 및 사이즈 준비
    val barColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    // 텍스트 크기를 sp -> px로 변환
    val density = LocalDensity.current
    val textPaintSize = with(density) { 12.sp.toPx() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(16.dp)
    ) {
        // 그리기 영역 계산
        val width = size.width
        val height = size.height
        val graphBottom = height - 40.dp.toPx() // 텍스트 들어갈 공간

        // 최대값 계산
        val maxTime = top5Apps.maxOfOrNull { it.usageTime }?.toFloat() ?: 1f

        // 막대 하나의 너비와 간격 계산
        val barWidth = (width / top5Apps.size) * 0.5f // 너비는 공간의 50%
        val spacing = (width / top5Apps.size) * 0.5f  // 간격은 나머지 50%

        // 3. 축 그리기 (X축 바닥 선)
        drawLine(
            color = axisColor,
            start = Offset(0f, graphBottom),
            end = Offset(width, graphBottom),
            strokeWidth = 2.dp.toPx()
        )

        // 4. 데이터 그리기
        top5Apps.forEachIndexed { index, app ->
            val x = (index * (barWidth + spacing)) + (spacing / 2)
            val ratio = if (maxTime > 0) app.usageTime / maxTime else 0f
            val barHeight = (graphBottom * ratio)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, graphBottom - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    app.appName.take(5), // 5글자까지만 표시
                    x + (barWidth / 2),  // 텍스트 중앙 정렬
                    height,              // 그래프 바닥 아래에 위치
                    Paint().apply {
                        color = textColor
                        textSize = textPaintSize
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true // 텍스트 깨짐 방지
                    }
                )
            }
        }
    }
}