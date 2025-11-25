package com.debarunlahiri.stt.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debarunlahiri.stt.data.model.HealthCheckResponse
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
class HealthCheckViewModel @Inject constructor(
    private val repository: SttRepository
) : ViewModel() {
    
    private val _healthState = MutableStateFlow<UiState<HealthCheckResponse>>(UiState.Idle)
    val healthState: StateFlow<UiState<HealthCheckResponse>> = _healthState.asStateFlow()
    
    init {
        checkHealth()
    }
    
    fun checkHealth() {
        viewModelScope.launch {
            _healthState.value = UiState.Loading
            when (val result = repository.healthCheck()) {
                is Result.Success -> {
                    _healthState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _healthState.value = UiState.Error(result.message, result.exception)
                }
                is Result.Loading -> {
                    _healthState.value = UiState.Loading
                }
            }
        }
    }
}
