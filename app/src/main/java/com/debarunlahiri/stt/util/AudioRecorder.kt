package com.debarunlahiri.stt.util

import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import kotlin.math.sqrt

enum class AudioQuality {
    GOOD,
    LOW_VOLUME,
    HIGH_NOISE,
    SILENT
}

class AudioRecorder(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var recordingStartTime: Long = 0L
    
    private var audioRecord: AudioRecord? = null
    private var monitoringJob: Job? = null
    private val monitoringScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentAudioQuality = AudioQuality.GOOD
    private var currentRmsLevel = 0.0
    private var audioQualityCallback: ((AudioQuality, Double) -> Unit)? = null
    
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_MULTIPLIER = 2
        
        private const val LOW_VOLUME_THRESHOLD = 0.01
        private const val HIGH_NOISE_THRESHOLD = 0.15
        private const val SILENT_THRESHOLD = 0.005
    }
    
    fun setAudioQualityCallback(callback: (AudioQuality, Double) -> Unit) {
        audioQualityCallback = callback
    }
    
    fun startRecording(): File? {
        if (isRecording) {
            return null
        }
        
        try {
            // Create output file
            val audioDir = File(context.cacheDir, "audio_recordings")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }
            
            outputFile = File(audioDir, "recording_${System.currentTimeMillis()}.m4a")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile?.absolutePath)
                
                try {
                    prepare()
                    start()
                    isRecording = true
                    recordingStartTime = System.currentTimeMillis()
                } catch (e: IOException) {
                    e.printStackTrace()
                    release()
                    return null
                }
            }
            
            startAudioMonitoring()
            
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            release()
            return null
        }
    }
    
    private fun startAudioMonitoring() {
        stopAudioMonitoring()
        
        try {
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
                return
            }
            
            val actualBufferSize = bufferSize * BUFFER_SIZE_MULTIPLIER
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                actualBufferSize
            )
            
            audioRecord?.let { recorder ->
                if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                    recorder.startRecording()
                    
                    monitoringJob = monitoringScope.launch {
                        val buffer = ShortArray(bufferSize)
                        val samples = mutableListOf<Double>()
                        
                        while (isRecording && isActive) {
                            val readResult = recorder.read(buffer, 0, buffer.size)
                            
                            if (readResult > 0) {
                                val rms = calculateRMS(buffer, readResult)
                                samples.add(rms)
                                
                                if (samples.size > 10) {
                                    samples.removeAt(0)
                                }
                                
                                val avgRms = samples.average()
                                currentRmsLevel = avgRms
                                
                                val quality = detectAudioQuality(avgRms)
                                currentAudioQuality = quality
                                
                                audioQualityCallback?.invoke(quality, avgRms)
                                
                                delay(100)
                            } else {
                                delay(100)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun calculateRMS(buffer: ShortArray, length: Int): Double {
        var sum = 0.0
        for (i in 0 until length) {
            val sample = buffer[i].toDouble() / Short.MAX_VALUE
            sum += sample * sample
        }
        return sqrt(sum / length)
    }
    
    private fun detectAudioQuality(rms: Double): AudioQuality {
        return when {
            rms < SILENT_THRESHOLD -> AudioQuality.SILENT
            rms < LOW_VOLUME_THRESHOLD -> AudioQuality.LOW_VOLUME
            rms > HIGH_NOISE_THRESHOLD -> AudioQuality.HIGH_NOISE
            else -> AudioQuality.GOOD
        }
    }
    
    private fun stopAudioMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        
        try {
            audioRecord?.let { recorder ->
                if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                    recorder.stop()
                }
                recorder.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
        currentAudioQuality = AudioQuality.GOOD
        currentRmsLevel = 0.0
    }
    
    fun releaseAll() {
        stopAudioMonitoring()
        monitoringScope.cancel()
        release()
    }
    
    fun getCurrentAudioQuality(): AudioQuality = currentAudioQuality
    fun getCurrentRmsLevel(): Double = currentRmsLevel
    
    fun stopRecording(): File? {
        if (!isRecording) {
            return null
        }
        
        stopAudioMonitoring()
        
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            recordingStartTime = 0L
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            release()
            null
        }
    }
    
    fun cancelRecording() {
        if (isRecording) {
            stopAudioMonitoring()
            
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaRecorder = null
            isRecording = false
            recordingStartTime = 0L
            
            // Delete the file
            outputFile?.delete()
            outputFile = null
        }
    }
    
    fun isRecording(): Boolean = isRecording
    
    fun getRecordingDuration(): Long {
        return if (isRecording && recordingStartTime > 0) {
            System.currentTimeMillis() - recordingStartTime
        } else {
            0
        }
    }
    
    fun getMaxAmplitude(): Int {
        return try {
            if (isRecording && mediaRecorder != null) {
                mediaRecorder?.maxAmplitude ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    private fun release() {
        stopAudioMonitoring()
        
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        isRecording = false
        recordingStartTime = 0L
        outputFile = null
    }
}

