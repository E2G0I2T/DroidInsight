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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.droidinsight.domain.model.UsageModel

@Composable
fun UsageBarChart(
    usageList: List<UsageModel>,
    modifier: Modifier = Modifier
) {
    // 상위 5개만 추려서 그리기
    val top5Apps = remember(usageList) { usageList.take(5) }

    // 테마 컬러 가져오기 (다크모드 대응)
    val barColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb() // Native Paint용

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp) // 차트 높이 고정
            .padding(16.dp)
    ) {
        if (top5Apps.isEmpty()) return@Canvas

        // 1. 그리기 영역 계산
        val barWidth = 40.dp.toPx()
        val spacing = 20.dp.toPx()
        val graphHeight = size.height - 40.dp.toPx() // 텍스트 공간 확보
        val maxTime = top5Apps.maxOf { it.usageTime }.toFloat()

        // 2. X축, Y축 그리기 (좌표 0,0은 왼쪽 상단임에 주의!)
        // Y축 (세로선)
        drawLine(
            color = axisColor,
            start = Offset(0f, 0f),
            end = Offset(0f, graphHeight),
            strokeWidth = 2.dp.toPx()
        )
        // X축 (가로선)
        drawLine(
            color = axisColor,
            start = Offset(0f, graphHeight),
            end = Offset(size.width, graphHeight),
            strokeWidth = 2.dp.toPx()
        )

        // 3. 막대(Bar) 그리기
        top5Apps.forEachIndexed { index, app ->
            val ratio = if (maxTime > 0) app.usageTime / maxTime else 0f
            val barHeight = graphHeight * ratio

            val x = (index * (barWidth + spacing)) + 20.dp.toPx() // 여백 줌
            val y = graphHeight - barHeight

            // 막대 그리기
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )

            // 4. 앱 이름 그리기 (Native Paint 사용)
            // Compose Canvas는 drawText가 실험적 기능이라 Native Canvas를 쓰는 게 안정적입니다.
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    app.appName.take(4), // 이름이 길면 4글자만
                    x + (barWidth / 2), // 텍스트 x 위치 (막대 중앙)
                    graphHeight + 30.dp.toPx(), // 텍스트 y 위치 (X축 아래)
                    Paint().apply {
                        color = textColor
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}