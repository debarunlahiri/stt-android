package com.debarunlahiri.stt.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.debarunlahiri.stt.ui.components.ErrorDisplay
import com.debarunlahiri.stt.ui.components.LoadingIndicator
import com.debarunlahiri.stt.ui.navigation.Screen
import com.debarunlahiri.stt.ui.viewmodel.HealthCheckViewModel
import com.debarunlahiri.stt.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    healthViewModel: HealthCheckViewModel
) {
    val healthState by healthViewModel.healthState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("STT Service") },
                actions = {
                    IconButton(onClick = { healthViewModel.checkHealth() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (healthState) {
                        is UiState.Success -> MaterialTheme.colorScheme.primaryContainer
                        is UiState.Error -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (healthState) {
                                is UiState.Success -> Icons.Default.CheckCircle
                                is UiState.Error -> Icons.Default.Error
                                is UiState.Loading -> Icons.Default.HourglassEmpty
                                else -> Icons.Default.Info
                            },
                            contentDescription = null
                        )
                        Text(
                            text = "Service Status",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    when (val state = healthState) {
                        is UiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                        is UiState.Success -> {
                            Text("Status: ${state.data.status}")
                            Text("Model: ${state.data.modelSize}")
                            Text("Device: ${state.data.device}")
                            if (state.data.gpuAvailable && state.data.gpuName != null) {
                                Text("GPU: ${state.data.gpuName}")
                            }
                            Text("Supported Languages: ${state.data.supportedLanguages.joinToString(", ")}")
                        }
                        is UiState.Error -> {
                            Text("Error: ${state.message}")
                        }
                        is UiState.Idle -> {
                            Text("Checking service status...")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Features",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Feature Cards
            FeatureCard(
                title = "Audio Transcription",
                description = "Convert audio to text with timestamps",
                icon = Icons.Default.Mic,
                onClick = { navController.navigate(Screen.Transcription.route) }
            )
            
            FeatureCard(
                title = "Text Translation",
                description = "Translate between English, Hindi, and Korean",
                icon = Icons.Default.Translate,
                onClick = { navController.navigate(Screen.Translation.route) }
            )
            
            FeatureCard(
                title = "Language Detection",
                description = "Detect language of input text",
                icon = Icons.Default.Language,
                onClick = { navController.navigate(Screen.LanguageDetection.route) }
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go"
            )
        }
    }
}
