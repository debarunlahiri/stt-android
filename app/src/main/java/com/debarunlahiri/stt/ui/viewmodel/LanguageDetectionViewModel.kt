package com.debarunlahiri.stt.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debarunlahiri.stt.data.model.LanguageDetectionResponse
import com.debarunlahiri.stt.data.repository.Result
import com.debarunlahiri.stt.data.repository.SttRepository
import com.debarunlahiri.stt.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageDetectionViewModel @Inject constructor(
    private val repository: SttRepository
) : ViewModel() {
    
    private val _detectionState = MutableStateFlow<UiState<LanguageDetectionResponse>>(UiState.Idle)
    val detectionState: StateFlow<UiState<LanguageDetectionResponse>> = _detectionState.asStateFlow()
    
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    fun setInputText(text: String) {
        _inputText.value = text
    }
    
    fun detectLanguage() {
        if (_inputText.value.isBlank()) {
            _detectionState.value = UiState.Error("Please enter text to detect language")
            return
        }
        
        viewModelScope.launch {
            _detectionState.value = UiState.Loading
            
            when (val result = repository.detectLanguage(_inputText.value)) {
                is Result.Success -> {
                    _detectionState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _detectionState.value = UiState.Error(result.message, result.exception)
                }
                is Result.Loading -> {
                    _detectionState.value = UiState.Loading
                }
            }
        }
    }
    
    fun resetState() {
        _detectionState.value = UiState.Idle
    }
}
