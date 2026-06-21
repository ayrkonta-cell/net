package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    currentSpeed: Double,
    maxSpeedValue: Double = 100.0,
    modifier: Modifier = Modifier,
    gaugeSize: androidx.compose.ui.unit.Dp = 200.dp,
    testTag: String = "speedometer_gauge"
) {
    // Smoothly animate the gauge angle
    val currentRatio = (currentSpeed / maxSpeedValue).coerceIn(0.0, 1.0).toFloat()
    val animatedRatio by animateFloatAsState(
        targetValue = currentRatio,
        animationSpec = tween(durationMillis = 150),
        label = "SpeedRatio"
    )

    val startAngle = 135f
    val sweepAngle = 270f
    val activeSweepAngle = sweepAngle * animatedRatio

    val trackColor = Color(0xFF2D3033)
    val sleekAccent = Color(0xFFD1E1FF)
    val sleekTextSecondary = Color(0xFFC2C7CF)

    Box(
        modifier = modifier
            .size(gaugeSize)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width / 2) - 16.dp.toPx()
            val strokeWidth = 14.dp.toPx()

            // 1. Draw Background Track Arc
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Draw Active Heat Progress Arc
            if (activeSweepAngle > 0.5f) {
                val activeGradient = Brush.linearGradient(
                    colors = listOf(sleekAccent.copy(alpha = 0.7f), sleekAccent, sleekAccent),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f)
                )

                drawArc(
                    brush = activeGradient,
                    startAngle = startAngle,
                    sweepAngle = activeSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 3. Draw subtle scale tick lines along the dial edge
            val tickCount = 21
            val tickRadiusStart = radius - 24.dp.toPx()
            val tickRadiusEnd = radius - 16.dp.toPx()
            for (i in 0 until tickCount) {
                val ratio = i / (tickCount - 1).toFloat()
                val angleDeg = startAngle + (sweepAngle * ratio)
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val startX = center.x + tickRadiusStart * cos(angleRad).toFloat()
                val startY = center.y + tickRadiusStart * sin(angleRad).toFloat()
                val endX = center.x + tickRadiusEnd * cos(angleRad).toFloat()
                val endY = center.y + tickRadiusEnd * sin(angleRad).toFloat()

                val isHighTicked = ratio <= animatedRatio
                val tickColor = if (isHighTicked) {
                    sleekAccent.copy(alpha = 0.9f)
                } else {
                    Color.White.copy(alpha = 0.15f)
                }
                
                val tickThickness = if (i % 5 == 0) 3.dp.toPx() else 1.5.dp.toPx()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickThickness,
                    cap = StrokeCap.Round
                )
            }
        }

        // Inner Gauge Speed Text readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.0f", currentSpeed),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp,
                    fontFamily = FontFamily.SansSerif
                ),
                color = sleekAccent,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Mbps",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = sleekTextSecondary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "הורדה / DOWN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = sleekTextSecondary.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
