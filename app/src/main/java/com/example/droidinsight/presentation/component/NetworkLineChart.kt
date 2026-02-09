package com.example.droidinsight.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun NetworkLineChart(
    dataPoints: List<Long>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF00E676) // 기본값: 네온 그린
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (dataPoints.isEmpty()) return@Canvas

        // 1. 데이터 정규화 (Y축 스케일 계산)
        // 그래프가 천장을 뚫지 않도록, 현재 데이터 중 가장 큰 값을 찾아서 높이를 맞춤
        val maxVal = dataPoints.maxOrNull()?.toFloat() ?: 1f
        val height = size.height
        val width = size.width

        // 데이터 간의 가로 간격 (X축 간격)
        val xStep = width / (dataPoints.size - 1)

        val path = Path()

        // 2. 경로(Path) 그리기 시작
        dataPoints.forEachIndexed { index, value ->
            val x = index * xStep
            // Y좌표 계산: 값이 클수록 위로 가야 하므로 (height - 계산값)
            // 최소값이 0이면 바닥에 붙도록 처리
            val yRatio = if (maxVal > 0) value.toFloat() / maxVal else 0f
            val y = height - (yRatio * height)

            if (index == 0) {
                path.moveTo(x, y) // 시작점
            } else {
                path.lineTo(x, y) // 선 긋기
            }
        }

        // 3. 선 그리기 (Stroke)
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // 4. [선택] 선 아래쪽 그라데이션 채우기 (고급 효과)
        // 기존 선 경로를 닫힌 도형으로 만듦 (오른쪽 아래 -> 왼쪽 아래 -> 시작점)
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.5f), // 위쪽은 반투명
                    Color.Transparent             // 아래쪽은 투명
                ),
                startY = 0f,
                endY = height
            )
        )
    }
}