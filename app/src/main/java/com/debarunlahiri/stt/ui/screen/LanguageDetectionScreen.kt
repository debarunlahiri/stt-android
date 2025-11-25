package com.debarunlahiri.stt.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.debarunlahiri.stt.ui.components.ErrorDisplay
import com.debarunlahiri.stt.ui.components.LoadingIndicator
import com.debarunlahiri.stt.ui.components.ResultCard
import com.debarunlahiri.stt.ui.viewmodel.LanguageDetectionViewModel
import com.debarunlahiri.stt.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDetectionScreen(
    navController: NavController,
    viewModel: LanguageDetectionViewModel
) {
    val detectionState by viewModel.detectionState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Language Detection") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Input Text",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.setInputText(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        placeholder = { Text("Enter text to detect language...") },
                        maxLines = 8
                    )
                    
                    Text(
                        text = "Supports: English, Hindi, Korean",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Detect Button
            Button(
                onClick = { viewModel.detectLanguage() },
                modifier = Modifier.fillMaxWidth(),
                enabled = inputText.isNotBlank() && detectionState !is UiState.Loading
            ) {
                Text("Detect Language")
            }
            
            // Result Display
            when (val state = detectionState) {
                is UiState.Loading -> {
                    LoadingIndicator("Detecting language...")
                }
                is UiState.Success -> {
                    ResultCard(title = "Detection Result") {
                        // Main Detection
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = state.data.languageName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Language Code: ${state.data.detectedLanguage.uppercase()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Confidence: ${"%.2f%%".format(state.data.confidence * 100)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // All Detections (if available)
                        state.data.allDetections?.let { detections ->
                            if (detections.isNotEmpty()) {
                                Divider()
                                Text(
                                    text = "All Detections",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                
                                detections.forEach { detection ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = detection.language.uppercase(),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "${"%.2f%%".format(detection.confidence * 100)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorDisplay(
                        message = state.message,
                        onRetry = { viewModel.resetState() }
                    )
                }
                is UiState.Idle -> {
                    // Show nothing or placeholder
                }
            }
        }
    }
}
