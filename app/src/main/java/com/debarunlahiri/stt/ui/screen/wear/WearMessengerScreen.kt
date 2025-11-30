package com.debarunlahiri.stt.ui.screen.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import com.debarunlahiri.stt.ui.viewmodel.TranscriptionViewModel
import com.debarunlahiri.stt.util.UiState

@Composable
fun WearMessengerScreen(
    navController: NavController,
    viewModel: TranscriptionViewModel,
    transcribedText: String,
    englishText: String?,
    hindiText: String?,
    koreanText: String?,
    audioFileUrl: String?
) {
    val messengerState by viewModel.messengerState.collectAsState()
    
    var senderName by remember { mutableStateOf("John Doe") }
    var senderId by remember { mutableStateOf("user_12345") }
    var message by remember { mutableStateOf(transcribedText) }
    var englishMessage by remember { mutableStateOf(englishText ?: "") }
    var hindiMessage by remember { mutableStateOf(hindiText ?: "") }
    var koreanMessage by remember { mutableStateOf(koreanText ?: "") }
    var audioLink by remember { mutableStateOf(audioFileUrl ?: "") }
    
    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            when (val state = messengerState) {
                is UiState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sending...", style = MaterialTheme.typography.body2)
                    }
                }
                is UiState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Message sent!",
                            style = MaterialTheme.typography.title3,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back")
                        }
                    }
                }
                is UiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back")
                        }
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Send to Messenger",
                            style = MaterialTheme.typography.title3,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (message.isNotEmpty()) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        
                        if (englishMessage.isNotEmpty() || hindiMessage.isNotEmpty() || koreanMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "With translations",
                                style = MaterialTheme.typography.body1,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
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
                            enabled = senderName.isNotEmpty() && message.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

