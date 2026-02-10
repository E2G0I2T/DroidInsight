package com.example.droidinsight.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * 실시간 네트워크 트래픽을 시각화하는 선 그래프 컴포넌트
 * @param dataPoints 트래픽 데이터 리스트 (Long 타입)
 * @param lineColor 그래프 선 색상 (기본값: 네온 그린)
 */
@Composable
fun NetworkLineChart(
    dataPoints: List<Long>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF00E676)
) {
    // 데이터가 없으면 그리지 않음
    if (dataPoints.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // 1. 데이터 스케일링 (Y축 정규화)
        // 그래프가 천장을 뚫지 않도록 최대값을 기준으로 비율 계산
        val maxVal = dataPoints.maxOrNull() ?: 1L
        val realMax = if (maxVal == 0L) 1f else maxVal.toFloat()

        val width = size.width
        val height = size.height

        // X축 간격
        val xStep = width / (dataPoints.size - 1).coerceAtLeast(1)

        val strokePath = Path()

        // 2. 선 경로 생성
        dataPoints.forEachIndexed { index, value ->
            val x = index * xStep
            val yRatio = value.toFloat() / realMax
            val y = height - (yRatio * height)

            if (index == 0) {
                strokePath.moveTo(x, y)
            } else {
                strokePath.lineTo(x, y)
            }
        }

        // 3. 그라데이션 채우기 경로 생성
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.5f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
        )

        // 4. 선 그리기 (Stroke)
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}