package com.debarunlahiri.stt.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var recordingStartTime: Long = 0L
    
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
            
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            release()
            return null
        }
    }
    
    fun stopRecording(): File? {
        if (!isRecording) {
            return null
        }
        
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

