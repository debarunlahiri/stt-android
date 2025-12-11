package com.debarunlahiri.stt.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.debarunlahiri.stt.ui.viewmodel.TranslationViewModel
import com.debarunlahiri.stt.util.UiState

@Composable
fun TranslationScreen(
    navController: NavController,
    viewModel: TranslationViewModel
) {
    val translationState by viewModel.translationState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val sourceLanguage by viewModel.sourceLanguage.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Translation",
            style = MaterialTheme.typography.headlineMedium
        )
        
        OutlinedTextField(
            value = inputText,
            onValueChange = { viewModel.setInputText(it) },
            label = { Text("Text to translate") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = { viewModel.translateText() },
            enabled = inputText.isNotBlank()
        ) {
            Text("Translate")
        }
        
        when (translationState) {
            is UiState.Loading -> {
                CircularProgressIndicator()
            }
            is UiState.Success -> {
                val result = (translationState as UiState.Success).data
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "English:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = result.englishText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Hindi:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = result.hindiText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Korean:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = result.koreanText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            is UiState.Error -> {
                Text(
                    text = (translationState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UiState.Idle -> {
                // No state to show
            }
        }
    }
}
