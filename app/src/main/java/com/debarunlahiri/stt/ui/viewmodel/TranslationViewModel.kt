package com.debarunlahiri.stt.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debarunlahiri.stt.data.model.TranslationResponse
import com.debarunlahiri.stt.data.repository.Result
import com.debarunlahiri.stt.data.repository.SttRepository
import com.debarunlahiri.stt.util.Constants
import com.debarunlahiri.stt.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslationViewModel @Inject constructor(
    private val repository: SttRepository
) : ViewModel() {
    
    private val _translationState = MutableStateFlow<UiState<TranslationResponse>>(UiState.Idle)
    val translationState: StateFlow<UiState<TranslationResponse>> = _translationState.asStateFlow()
    
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    private val _sourceLanguage = MutableStateFlow(Constants.LANG_AUTO)
    val sourceLanguage: StateFlow<String> = _sourceLanguage.asStateFlow()
    
    private val _targetLanguage = MutableStateFlow(Constants.LANG_ENGLISH)
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()
    
    fun setInputText(text: String) {
        _inputText.value = text
    }
    
    fun setSourceLanguage(language: String) {
        _sourceLanguage.value = language
    }
    
    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
    }
    
    fun translateText() {
        val inputText = _inputText.value
        if (inputText.isBlank()) {
            _translationState.value = UiState.Error("Please enter text to translate")
            return
        }
        
        viewModelScope.launch {
            _translationState.value = UiState.Loading
            
            when (val result = repository.translateText(
                text = inputText,
                sourceLanguage = _sourceLanguage.value,
                targetLanguage = _targetLanguage.value
            )) {
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
    
    fun resetState() {
        _translationState.value = UiState.Idle
    }
}
