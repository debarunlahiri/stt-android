package com.debarunlahiri.stt.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.debarunlahiri.stt.ui.components.AudioPlayer
import com.debarunlahiri.stt.ui.components.AudioWaveform
import com.debarunlahiri.stt.ui.components.ErrorDisplay

import com.debarunlahiri.stt.ui.components.LoadingIndicator
import com.debarunlahiri.stt.ui.components.ResultCard
import com.debarunlahiri.stt.ui.viewmodel.TranscriptionViewModel
import com.debarunlahiri.stt.util.AudioQuality
import com.debarunlahiri.stt.util.AudioRecorder
import com.debarunlahiri.stt.util.Constants
import com.debarunlahiri.stt.util.UiState
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptionScreen(
    navController: NavController,
    viewModel: TranscriptionViewModel
) {
    val context = LocalContext.current
    val transcriptionState by viewModel.transcriptionState.collectAsState()

    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    
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
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
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
    
    
    // Timer and amplitude monitor for recording with 30-second limit
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(100)
                val duration = audioRecorder.getRecordingDuration()
                viewModel.updateRecordingDuration(duration)
                viewModel.updateAmplitude(audioRecorder.getMaxAmplitude())
                
                // Auto-stop recording after 30 seconds
                if (duration >= Constants.MAX_RECORDING_DURATION_MS) {
                    val file = audioRecorder.stopRecording()
                    isRecording = false
                    recordedFile = file
                    break
                }
            }
        } else {
            viewModel.updateAmplitude(0)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Transcription") },
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
            // Recording Card - NOW AT TOP!
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isRecording) "Recording..." else if (recordedFile != null) "Ready to Transcribe" else "Hold to Record",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    // Timer section - ALWAYS same height
                    Box(
                        modifier = Modifier.height(100.dp), // Fixed height for timer area
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRecording || recordingDuration > 0) {
                            val isNearLimit = recordingDuration >= (Constants.MAX_RECORDING_DURATION_MS * 0.8)
                            val isAtLimit = recordingDuration >= Constants.MAX_RECORDING_DURATION_MS
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = formatDuration(recordingDuration),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isAtLimit -> MaterialTheme.colorScheme.error
                                        isNearLimit && isRecording -> MaterialTheme.colorScheme.tertiary
                                        isRecording -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                                
                                // Always show max label during recording to maintain height
                                Text(
                                    text = if (isRecording) "Max: ${Constants.MAX_RECORDING_DURATION_SEC}s" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isNearLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Audio Quality Feedback
                    if (isRecording && audioQuality != AudioQuality.GOOD) {
                        val feedbackMessage = when (audioQuality) {
                            AudioQuality.LOW_VOLUME -> "Please speak louder"
                            AudioQuality.HIGH_NOISE -> "Too much background noise"
                            AudioQuality.SILENT -> "No audio detected"
                            AudioQuality.GOOD -> ""
                        }
                        
                        if (feedbackMessage.isNotEmpty()) {
                            Text(
                                text = feedbackMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    
                    // Waveform visualization
                    AudioWaveform(
                        amplitude = audioAmplitude,
                        isRecording = isRecording,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    
                    // Push to Talk Button
                    val isTranscribing = transcriptionState is UiState.Loading
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTranscribing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(120.dp),
                                strokeWidth = 4.dp
                            )
                        }
                        
                        PushToTalkButton(
                            isRecording = isRecording,
                            hasPermission = hasPermission,
                            isTranscribing = isTranscribing,
                            onStartRecording = {
                                if (hasPermission && !isTranscribing) {
                                    val file = audioRecorder.startRecording()
                                    if (file != null) {
                                        isRecording = true
                                        recordedFile = null
                                        viewModel.resetState()
                                    }
                                } else if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onStopRecording = {
                                if (!isTranscribing) {
                                    val file = audioRecorder.stopRecording()
                                    isRecording = false
                                    recordedFile = file
                                }
                            }
                        )
                    }
                    
                    if (!hasPermission) {
                        Text(
                            text = "Microphone permission required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            

            
            
            // Transcription Results - removed cards, only show error if needed
            when (val state = transcriptionState) {
                is UiState.Error -> {
                    ErrorDisplay(
                        message = state.message,
                        onRetry = { 
                            recordedFile?.let { file ->
                                viewModel.transcribeAudioFile(file)
                            }
                        }
                    )
                }
                else -> {
                    // Show nothing for other states
                }
            }
        }
    }
}



