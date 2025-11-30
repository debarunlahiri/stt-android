package com.debarunlahiri.stt.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.debarunlahiri.stt.ui.components.ErrorDisplay
import com.debarunlahiri.stt.ui.viewmodel.TranscriptionViewModel
import com.debarunlahiri.stt.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(
    navController: NavController,
    viewModel: TranscriptionViewModel,
    transcribedText: String,
    englishText: String?,
    hindiText: String?,
    koreanText: String?,
    audioFileUrl: String?
) {
    val messengerState by viewModel.messengerState.collectAsState()
    
    // Dummy data for now
    var senderName by remember { mutableStateOf("John Doe") }
    var senderId by remember { mutableStateOf("user_12345") }
    var message by remember { mutableStateOf(transcribedText) }
    var englishMessage by remember { mutableStateOf(englishText ?: "") }
    var hindiMessage by remember { mutableStateOf(hindiText ?: "") }
    var koreanMessage by remember { mutableStateOf(koreanText ?: "") }
    var audioLink by remember { mutableStateOf(audioFileUrl ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send to Messenger") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Message Details",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    // Sender Name
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { },
                        label = { Text("Sender Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true
                    )
                    
                    // Sender ID
                    OutlinedTextField(
                        value = senderId,
                        onValueChange = { },
                        label = { Text("Sender ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true
                    )
                    
                    // Original Message
                    OutlinedTextField(
                        value = message,
                        onValueChange = { },
                        label = { Text("Original Message (Transcribed)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        readOnly = true
                    )
                    
                    // English Translation
                    OutlinedTextField(
                        value = englishMessage,
                        onValueChange = { },
                        label = { Text("English Translation") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        readOnly = true
                    )
                    
                    // Hindi Translation
                    OutlinedTextField(
                        value = hindiMessage,
                        onValueChange = { },
                        label = { Text("Hindi Translation") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        readOnly = true
                    )
                    
                    // Korean Translation
                    OutlinedTextField(
                        value = koreanMessage,
                        onValueChange = { },
                        label = { Text("Korean Translation") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        readOnly = true
                    )
                    
                    // Audio Link
                    if (audioFileUrl != null) {
                        OutlinedTextField(
                            value = audioLink,
                            onValueChange = { },
                            label = { Text("Audio Link") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            readOnly = true
                        )
                    }
                }
            }
            
            // Send Button
            Button(
                onClick = {
                    val messagePayload = buildString {
                        append("Sender Name: $senderName")
                        if (senderId.isNotEmpty()) {
                            append(" ($senderId)")
                        }
                        append("\n\n")
                        append("Original Message: $message")
                        if (englishMessage.isNotEmpty()) {
                            append("\n\nEnglish: $englishMessage")
                        }
                        if (hindiMessage.isNotEmpty()) {
                            append("\n\nHindi: $hindiMessage")
                        }
                        if (koreanMessage.isNotEmpty()) {
                            append("\n\nKorean: $koreanMessage")
                        }
                        if (audioLink.isNotEmpty()) {
                            append("\n\nAudio Link: $audioLink")
                        }
                    }
                    viewModel.sendToMessenger(messagePayload)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = messengerState !is UiState.Loading && senderName.isNotEmpty() && message.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send to Messenger")
            }
            
            // Messenger Status
            when (val state = messengerState) {
                is UiState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Sending to messenger...")
                        }
                    }
                }
                is UiState.Success -> {
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
                                text = "✓ ${state.data}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = { navController.navigateUp() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Back to Transcription")
                                }
                                TextButton(
                                    onClick = { viewModel.resetMessengerState() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Send Another")
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorDisplay(
                        message = "Messenger: ${state.message}",
                        onRetry = { viewModel.resetMessengerState() }
                    )
                }
                is UiState.Idle -> {
                    // Show nothing
                }
            }
        }
    }
}
