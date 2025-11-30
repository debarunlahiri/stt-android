package com.debarunlahiri.stt.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.debarunlahiri.stt.ui.components.ErrorDisplay
import com.debarunlahiri.stt.ui.components.LanguageDropdown
import com.debarunlahiri.stt.ui.components.LoadingIndicator
import com.debarunlahiri.stt.ui.components.ResultCard
import com.debarunlahiri.stt.ui.viewmodel.TranslationViewModel
import com.debarunlahiri.stt.util.Constants
import com.debarunlahiri.stt.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationScreen(
    navController: NavController,
    viewModel: TranslationViewModel
) {
    val translationState by viewModel.translationState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val sourceLanguage by viewModel.sourceLanguage.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text Translation") },
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
                        placeholder = { Text("Enter text to translate...") },
                        maxLines = 8
                    )
                }
            }
            
            // Language Selection Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Languages",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LanguageDropdown(
                            selectedLanguage = sourceLanguage,
                            languages = Constants.SUPPORTED_LANGUAGES,
                            onLanguageSelected = { viewModel.setSourceLanguage(it) },
                            label = "From",
                            modifier = Modifier.weight(1f)
                        )
                        
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Translate",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        LanguageDropdown(
                            selectedLanguage = targetLanguage,
                            languages = Constants.TRANSLATION_LANGUAGES,
                            onLanguageSelected = { viewModel.setTargetLanguage(it) },
                            label = "To",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text(
                        text = "Tip: Select 'Auto Detect' for source language to automatically detect the input language",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Translate Button
            Button(
                onClick = { viewModel.translateText() },
                modifier = Modifier.fillMaxWidth(),
                enabled = inputText.isNotBlank() && translationState !is UiState.Loading
            ) {
                Text("Translate")
            }
            
            // Result Display
            when (val state = translationState) {
                is UiState.Loading -> {
                    LoadingIndicator("Translating...")
                }
                is UiState.Success -> {
                    ResultCard(title = "Translation Results (All Languages)") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // English Translation
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "English",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = state.data.englishText,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            
                            // Hindi Translation
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Hindi",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = state.data.hindiText,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            
                            // Korean Translation
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Korean",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = state.data.koreanText,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        
                        Divider()
                        
                        Text("Detected Language: ${state.data.detectedLanguage.uppercase()}")
                        Text("Source Language: ${state.data.sourceLanguage.uppercase()}")
                        Text("Detection Confidence: ${"%.2f%%".format(state.data.detectionConfidence * 100)}")
                        Text("Processing Time: ${"%.3f".format(state.data.processingTimeSec)}s")
                        Text("Translation Applied: ${if (state.data.translationApplied) "Yes" else "No (same language)"}")
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
