package com.debarunlahiri.stt.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.debarunlahiri.stt.ui.viewmodel.LanguageDetectionViewModel
import com.debarunlahiri.stt.util.UiState

@Composable
fun LanguageDetectionScreen(
    navController: NavController,
    viewModel: LanguageDetectionViewModel
) {
    val detectionState by viewModel.detectionState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Language Detection",
            style = MaterialTheme.typography.headlineMedium
        )
        
        OutlinedTextField(
            value = inputText,
            onValueChange = { viewModel.setInputText(it) },
            label = { Text("Enter text to detect language") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = { viewModel.detectLanguage() },
            enabled = inputText.isNotBlank()
        ) {
            Text("Detect Language")
        }
        
        when (detectionState) {
            is UiState.Loading -> {
                CircularProgressIndicator()
            }
            is UiState.Success -> {
                val result = (detectionState as UiState.Success).data
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Detected Language:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = result.detectedLanguage,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confidence: ${(result.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            is UiState.Error -> {
                Text(
                    text = (detectionState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UiState.Idle -> {
                // No state to show
            }
        }
    }
}
