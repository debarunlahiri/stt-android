package com.debarunlahiri.stt.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AudioWaveform(
    amplitude: Int,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    waveCount: Int = 5
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    // Normalize amplitude (MediaRecorder amplitude is 0-32767)
    val normalizedAmplitude = (amplitude / 32767f).coerceIn(0f, 1f)
    
    // Animated amplitude with smooth transitions
    val animatedAmplitude by animateFloatAsState(
        targetValue = if (isRecording) normalizedAmplitude else 0f,
        animationSpec = tween(100, easing = FastOutSlowInEasing),
        label = "amplitude"
    )
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        if (!isRecording) {
            // Draw flat line when not recording
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            return@Canvas
        }
        
        // Draw multiple waves with different properties
        for (i in 0 until waveCount) {
            val waveAlpha = 1f - (i * 0.15f).coerceAtMost(0.7f)
            val waveAmplitude = animatedAmplitude * (1f - i * 0.1f) * height * 0.4f
            val phaseShift = i * 0.5f
            
            val points = mutableListOf<Offset>()
            val segments = 100
            
            for (j in 0..segments) {
                val x = (j / segments.toFloat()) * width
                val normalizedX = j / segments.toFloat()
                
                // Create wave using sine function
                val wave = sin(normalizedX * 4 * Math.PI + phase + phaseShift).toFloat()
                val y = centerY + wave * waveAmplitude
                
                points.add(Offset(x, y))
            }
            
            // Draw the wave
            for (k in 0 until points.size - 1) {
                drawLine(
                    color = color.copy(alpha = waveAlpha),
                    start = points[k],
                    end = points[k + 1],
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
