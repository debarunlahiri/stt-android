package com.debarunlahiri.stt.ui.screen.wear

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import com.debarunlahiri.stt.ui.viewmodel.TranscriptionViewModel
import com.debarunlahiri.stt.util.AudioRecorder
import com.debarunlahiri.stt.util.Constants
import com.debarunlahiri.stt.util.UiState
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun WearTranscriptionScreen(
    navController: NavController,
    viewModel: TranscriptionViewModel
) {
    val context = LocalContext.current
    val transcriptionState by viewModel.transcriptionState.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    
    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    
    val audioRecorder = remember { AudioRecorder(context) }
    
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
                "${com.debarunlahiri.stt.ui.navigation.Screen.Messenger.route}?message=$encodedMessage&englishText=$encodedEnglish&hindiText=$encodedHindi&koreanText=$encodedKorean&audioUrl=$encodedAudioUrl"
            )
            
            // Reset state to prevent loop when navigating back
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
                
                // Auto-stop recording after 30 seconds
                if (duration >= Constants.MAX_RECORDING_DURATION_MS) {
                    val file = audioRecorder.stopRecording()
                    isRecording = false
                    recordedFile = file
                    break
                }
            }
        }
    }
    
    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    transcriptionState is UiState.Loading -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Transcribing...", style = MaterialTheme.typography.body2)
                    }
                    transcriptionState is UiState.Error -> {
                        Text(
                            text = "Error: ${(transcriptionState as UiState.Error).message}",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                recordedFile?.let { file ->
                                    viewModel.transcribeAudioFile(file)
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.resetState()
                                recordedFile = null
                            }
                        ) {
                            Text("Record Again")
                        }
                    }
                    !hasPermission -> {
                        Text(
                            text = "Microphone permission required",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                    else -> {
                        // Status Text
                        val isNearLimit = isRecording && recordingDuration >= (Constants.MAX_RECORDING_DURATION_MS * 0.8)
                        Text(
                            text = if (isRecording) {
                                formatDuration(recordingDuration)
                            } else {
                                "Tap to Record"
                            },
                            style = if (isRecording) MaterialTheme.typography.title1 else MaterialTheme.typography.body1,
                            color = when {
                                isNearLimit -> MaterialTheme.colors.error
                                isRecording -> MaterialTheme.colors.error
                                else -> MaterialTheme.colors.onBackground
                            },
                            textAlign = TextAlign.Center
                        )
                        
                        if (isRecording && isNearLimit) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Max: ${Constants.MAX_RECORDING_DURATION_SEC}s",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.error,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Big Record Button
                        WearRecordButton(
                            isRecording = isRecording,
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
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WearRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isRecording) MaterialTheme.colors.error else MaterialTheme.colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = if (isRecording) "Stop" else "Record",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}
