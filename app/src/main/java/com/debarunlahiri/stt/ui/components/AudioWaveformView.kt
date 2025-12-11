package com.debarunlahiri.stt.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class AudioWaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
        color = 0xFF7C3AED.toInt() // Primary purple
    }
    
    private var amplitude = 0
    private var isRecording = false
    private var phase = 0f
    private val waveCount = 5
    
    private val phaseAnimator = object : Runnable {
        override fun run() {
            phase += 0.1f
            if (phase > 2 * Math.PI) {
                phase = 0f
            }
            if (isRecording) {
                invalidate()
                postDelayed(this, 16)
            }
        }
    }
    
    fun updateAmplitude(amplitude: Int, isRecording: Boolean) {
        this.amplitude = amplitude
        this.isRecording = isRecording
        if (isRecording) {
            // Remove any pending callbacks and post a new one
            handler?.removeCallbacks(phaseAnimator)
            post(phaseAnimator)
        }
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2
        
        if (!isRecording) {
            paint.alpha = (255 * 0.3f).toInt()
            canvas.drawLine(0f, centerY, width, centerY, paint)
            return
        }
        
        val normalizedAmplitude = (amplitude / 32767f).coerceIn(0f, 1f)
        
        for (i in 0 until waveCount) {
            val waveAlpha = (255 * (1f - (i * 0.15f).coerceAtMost(0.7f))).toInt()
            val waveAmplitude = normalizedAmplitude * (1f - i * 0.1f) * height * 0.4f
            val phaseShift = i * 0.5f
            
            paint.alpha = waveAlpha
            
            val path = Path()
            val segments = 100
            
            for (j in 0..segments) {
                val x = (j / segments.toFloat()) * width
                val normalizedX = j / segments.toFloat()
                
                val wave = sin(normalizedX * 4 * Math.PI + phase + phaseShift).toFloat()
                val y = centerY + wave * waveAmplitude
                
                if (j == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            canvas.drawPath(path, paint)
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler?.removeCallbacks(phaseAnimator)
    }
}

