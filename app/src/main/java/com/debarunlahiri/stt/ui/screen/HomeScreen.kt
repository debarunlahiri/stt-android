package com.debarunlahiri.stt.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.debarunlahiri.stt.ui.viewmodel.HealthCheckViewModel
import com.debarunlahiri.stt.ui.viewmodel.TranscriptionViewModel
import com.debarunlahiri.stt.util.UiState

@Composable
fun HomeScreen(
    navController: NavController,
    healthViewModel: HealthCheckViewModel,
    transcriptionViewModel: TranscriptionViewModel
) {
    val healthState by healthViewModel.healthState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "STT App",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (healthState) {
            is UiState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Connecting...")
            }
            is UiState.Success -> {
                Text(
                    text = "Connected",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is UiState.Error -> {
                Text(
                    text = "Disconnected",
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UiState.Idle -> {
                Text("Checking connection...")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { navController.navigate("transcription") }
        ) {
            Text("Start Transcription")
        }
    }
}
