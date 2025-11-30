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
    val messengerState by viewModel.messengerState.collectAsState()
    val translationState by viewModel.translationState.collectAsState()
    
    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    
    val audioRecorder = remember { AudioRecorder(context) }
    
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
                    PushToTalkButton(
                        isRecording = isRecording,
                        hasPermission = hasPermission,
                        onStartRecording = {
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
                        },
                        onStopRecording = {
                            val file = audioRecorder.stopRecording()
                            isRecording = false
                            recordedFile = file
                        }
                    )
                    
                    if (!hasPermission) {
                        Text(
                            text = "Microphone permission required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            

            
            
            // Transcription Results
            when (val state = transcriptionState) {
                is UiState.Loading -> {
                    LoadingIndicator("Transcribing audio...")
                }
                is UiState.Success -> {
                    ResultCard(title = "Transcription Result") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = state.data.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Divider()
                            
                            Text(
                                text = "Language: ${state.data.detectedLanguage.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text("Word Count: ${state.data.wordCount}")
                            Text("Confidence: ${state.data.confidence?.let { "%.2f%%".format(it * 100) } ?: "N/A"}")
                            Text("Processing Time: ${"%.2f".format(state.data.processingTimeSec)}s")
                            Text("Audio Duration: ${"%.2f".format(state.data.audioDurationSec)}s")
                            Text("Real-Time Factor: ${"%.2f".format(state.data.realTimeFactor)}")
                            
                            // Audio Player
                            state.data.audioFileUrl?.let { audioUrl ->
                                Divider()
                                AudioPlayer(audioUrl = audioUrl)
                            }
                            
                            state.data.segments?.let { segments ->
                                if (segments.isNotEmpty()) {
                                    Divider()
                                    Text(
                                        text = "Segments (${segments.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    segments.forEach { segment ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "${"%.2f".format(segment.start)}s - ${"%.2f".format(segment.end)}s",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(text = segment.text)
                                                
                                                segment.words?.let { words ->
                                                    if (words.isNotEmpty()) {
                                                        Text(
                                                            text = "Words: ${words.size}",
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Translation Options - Show based on detected language
                    if (translationState is UiState.Idle) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Translate to:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Show translation options based on detected language
                                    val detectedLang = state.data.detectedLanguage.lowercase()
                                    val translationOptions = when (detectedLang) {
                                        "hi", "hindi" -> listOf("en" to "English", "ko" to "Korean")
                                        "en", "english" -> listOf("hi" to "Hindi", "ko" to "Korean")
                                        "ko", "korean" -> listOf("en" to "English", "hi" to "Hindi")
                                        else -> listOf("en" to "English", "hi" to "Hindi")
                                    }
                                    
                                    translationOptions.forEach { (code, name) ->
                                        Button(
                                            onClick = {
                                                viewModel.translateText(
                                                    text = state.data.text,
                                                    sourceLanguage = detectedLang,
                                                    targetLanguage = code
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(name)
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
                        onRetry = { 
                            recordedFile?.let { file ->
                                viewModel.transcribeAudioFile(file)
                            }
                        }
                    )
                }
                is UiState.Idle -> {
                    // Show nothing
                }
            }
            
            // Translation Results
            when (val state = translationState) {
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
                            Text("Translating...")
                        }
                    }
                }
                is UiState.Success -> {
                    ResultCard(title = "Translation Results") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Show all three language translations
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
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = state.data.englishText)
                                }
                            }
                            
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
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = state.data.hindiText)
                                }
                            }
                            
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
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = state.data.koreanText)
                                }
                            }
                            
                            Divider()
                            
                            Text(
                                text = "Source: ${state.data.sourceLanguage.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            TextButton(onClick = { viewModel.resetTranslationState() }) {
                                Text("Hide Translations")
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorDisplay(
                        message = "Translation: ${state.message}",
                        onRetry = { viewModel.resetTranslationState() }
                    )
                }
                is UiState.Idle -> {
                    // Show nothing
                }
            }
        }
    }
}

@Composable
fun PushToTalkButton(
    isRecording: Boolean,
    hasPermission: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // Click to toggle recording instead of hold
    Box(
        modifier = Modifier
            .size(180.dp)
            .clickable(
                onClick = {
                    if (isRecording) {
                        onStopRecording()
                    } else {
                        onStartRecording()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Visual button
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}

