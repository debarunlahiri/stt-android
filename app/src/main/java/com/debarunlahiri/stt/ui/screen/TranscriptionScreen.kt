package com.debarunlahiri.stt.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.debarunlahiri.stt.ui.navigation.Screen
import com.debarunlahiri.stt.ui.viewmodel.TranscriptionViewModel
import com.debarunlahiri.stt.util.AudioRecorder
import com.debarunlahiri.stt.util.Constants
import com.debarunlahiri.stt.util.UiState
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun TranscriptionScreen(
    navController: NavController,
    viewModel: TranscriptionViewModel
) {
    val context = LocalContext.current
    val transcriptionState by viewModel.transcriptionState.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    
    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    
    val audioRecorder = remember {
        AudioRecorder(context).apply {
            setAudioQualityCallback { quality, rmsLevel ->
                viewModel.updateAudioQuality(quality, rmsLevel)
            }
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    
    // Auto-transcribe when recording stops
    LaunchedEffect(recordedFile) {
        recordedFile?.let { file ->
            viewModel.transcribeAudioFile(file)
        }
    }
    
    // Auto-navigate to messenger after successful transcription
    LaunchedEffect(transcriptionState) {
        if (transcriptionState is UiState.Success) {
            val data = (transcriptionState as UiState.Success).data
            val transcribedText = data.text
            val englishText = data.englishText
            val hindiText = data.hindiText
            val koreanText = data.koreanText
            val audioUrl = data.audioFileUrl
            
            val encodedMessage = java.net.URLEncoder.encode(transcribedText, "UTF-8")
            val encodedEnglish = java.net.URLEncoder.encode(englishText, "UTF-8")
            val encodedHindi = java.net.URLEncoder.encode(hindiText, "UTF-8")
            val encodedKorean = java.net.URLEncoder.encode(koreanText, "UTF-8")
            val encodedAudioUrl = audioUrl?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: "null"
            
            navController.navigate(
                "${Screen.Messenger.route}?message=$encodedMessage&englishText=$encodedEnglish&hindiText=$encodedHindi&koreanText=$encodedKorean&audioUrl=$encodedAudioUrl"
            )
            
            viewModel.resetState()
        }
    }
    
    // Timer for recording with 30-second limit
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(100)
                val duration = audioRecorder.getRecordingDuration()
                viewModel.updateRecordingDuration(duration)
                
                if (duration >= Constants.MAX_RECORDING_DURATION_MS) {
                    val file = audioRecorder.stopRecording()
                    isRecording = false
                    recordedFile = file
                    break
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (transcriptionState) {
            is UiState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Transcribing...")
            }
            is UiState.Error -> {
                Text(
                    text = (transcriptionState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { recordedFile?.let { viewModel.transcribeAudioFile(it) } }) {
                    Text("Retry")
                }
            }
            else -> {
                Text(
                    text = if (isRecording) formatDuration(recordingDuration) else "Tap to Record",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                FloatingActionButton(
                    onClick = {
                        if (isRecording) {
                            val file = audioRecorder.stopRecording()
                            isRecording = false
                            recordedFile = file
                        } else {
                            if (hasPermission) {
                                val file = audioRecorder.startRecording()
                                if (file != null) {
                                    isRecording = true
                                    recordedFile = null
                                    viewModel.resetState()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop" else "Record"
                    )
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}
