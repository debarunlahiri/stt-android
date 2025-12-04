package com.debarunlahiri.stt.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debarunlahiri.stt.data.model.TranscriptionResponse
import com.debarunlahiri.stt.data.model.TranslationResponse
import com.debarunlahiri.stt.data.repository.Result
import com.debarunlahiri.stt.data.repository.SttRepository
import com.debarunlahiri.stt.util.AudioQuality
import com.debarunlahiri.stt.util.Constants
import com.debarunlahiri.stt.util.FileUtils
import com.debarunlahiri.stt.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TranscriptionViewModel @Inject constructor(
    private val repository: SttRepository
) : ViewModel() {
    
    private val _transcriptionState = MutableStateFlow<UiState<TranscriptionResponse>>(UiState.Idle)
    val transcriptionState: StateFlow<UiState<TranscriptionResponse>> = _transcriptionState.asStateFlow()
    
    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()
    
    private val _audioAmplitude = MutableStateFlow(0)
    val audioAmplitude: StateFlow<Int> = _audioAmplitude.asStateFlow()
    
    private val _audioQuality = MutableStateFlow<AudioQuality>(AudioQuality.GOOD)
    val audioQuality: StateFlow<AudioQuality> = _audioQuality.asStateFlow()
    
    private val _audioRmsLevel = MutableStateFlow(0.0)
    val audioRmsLevel: StateFlow<Double> = _audioRmsLevel.asStateFlow()
    
    fun updateRecordingDuration(duration: Long) {
        _recordingDuration.value = duration
    }
    
    fun updateAmplitude(amplitude: Int) {
        _audioAmplitude.value = amplitude
    }
    
    fun updateAudioQuality(quality: AudioQuality, rmsLevel: Double) {
        _audioQuality.value = quality
        _audioRmsLevel.value = rmsLevel
    }
    
    private val _messengerState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val messengerState: StateFlow<UiState<String>> = _messengerState.asStateFlow()
    
    private val _translationState = MutableStateFlow<UiState<TranslationResponse>>(UiState.Idle)
    val translationState: StateFlow<UiState<TranslationResponse>> = _translationState.asStateFlow()
    
    fun sendToMessenger(message: String) {
        viewModelScope.launch {
            _messengerState.value = UiState.Loading
            when (val result = repository.sendToMessenger(message)) {
                is Result.Success -> {
                    _messengerState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _messengerState.value = UiState.Error(result.message, result.exception)
                }
                is Result.Loading -> {
                    _messengerState.value = UiState.Loading
                }
            }
        }
    }
    
    fun resetMessengerState() {
        _messengerState.value = UiState.Idle
    }
    
    fun translateText(text: String, sourceLanguage: String, targetLanguage: String) {
        viewModelScope.launch {
            _translationState.value = UiState.Loading
            when (val result = repository.translateText(text, sourceLanguage, targetLanguage)) {
                is Result.Success -> {
                    _translationState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _translationState.value = UiState.Error(result.message, result.exception)
                }
                is Result.Loading -> {
                    _translationState.value = UiState.Loading
                }
            }
        }
    }
    
    fun resetTranslationState() {
        _translationState.value = UiState.Idle
    }
    
    private var transcriptionJob: kotlinx.coroutines.Job? = null

    fun transcribeAudioFile(audioFile: File) {
        // Cancel previous job if active
        transcriptionJob?.cancel()
        
        transcriptionJob = viewModelScope.launch {
            _transcriptionState.value = UiState.Loading
            
            try {
                // Check file size
                val fileSizeMB = FileUtils.getFileSizeInMB(audioFile)
                
                if (fileSizeMB > Constants.MAX_FILE_SIZE_MB) {
                    _transcriptionState.value = UiState.Error(
                        "File size (${"%.2f".format(fileSizeMB)} MB) exceeds maximum allowed size of ${Constants.MAX_FILE_SIZE_MB} MB"
                    )
                    audioFile.delete()
                    return@launch
                }
                
                val audioPart = FileUtils.createMultipartBody(audioFile)
                
                when (val result = repository.transcribeAudio(
                    audioPart = audioPart,
                    language = Constants.LANG_AUTO,
                    enableWordTimestamps = true
                )) {
                    is Result.Success -> {
                        _transcriptionState.value = UiState.Success(result.data)
                    }
                    is Result.Error -> {
                        _transcriptionState.value = UiState.Error(result.message, result.exception)
                    }
                    is Result.Loading -> {
                        _transcriptionState.value = UiState.Loading
                    }
                }
                
                // Clean up temporary file
                audioFile.delete()
                
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    throw e
                }
                _transcriptionState.value = UiState.Error(
                    "Error: ${e.message ?: "Unknown error"}",
                    e
                )
            }
        }
    }
    
    fun resetState() {
        _transcriptionState.value = UiState.Idle
        _recordingDuration.value = 0L
        _audioAmplitude.value = 0
        _audioQuality.value = AudioQuality.GOOD
        _audioRmsLevel.value = 0.0
        _translationState.value = UiState.Idle
        _messengerState.value = UiState.Idle
    }
}
