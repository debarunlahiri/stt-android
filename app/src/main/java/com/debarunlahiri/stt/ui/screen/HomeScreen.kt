package com.debarunlahiri.stt.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.debarunlahiri.stt.ui.components.AudioWaveform
import com.debarunlahiri.stt.ui.components.LoadingIndicator
import com.debarunlahiri.stt.ui.navigation.Screen
import com.debarunlahiri.stt.ui.viewmodel.HealthCheckViewModel
import com.debarunlahiri.stt.ui.viewmodel.TranscriptionViewModel
import com.debarunlahiri.stt.util.AudioQuality
import com.debarunlahiri.stt.util.AudioRecorder
import com.debarunlahiri.stt.util.Constants
import com.debarunlahiri.stt.util.UiState
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    healthViewModel: HealthCheckViewModel,
    transcriptionViewModel: TranscriptionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val healthState by healthViewModel.healthState.collectAsState()
    val transcriptionState by transcriptionViewModel.transcriptionState.collectAsState()
    val recordingDuration by transcriptionViewModel.recordingDuration.collectAsState()
    val audioAmplitude by transcriptionViewModel.audioAmplitude.collectAsState()
    val audioQuality by transcriptionViewModel.audioQuality.collectAsState()
    
    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    
    val audioRecorder = remember { 
        AudioRecorder(context).apply {
            setAudioQualityCallback { quality, rmsLevel ->
                transcriptionViewModel.updateAudioQuality(quality, rmsLevel)
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
    
    LaunchedEffect(recordedFile) {
        recordedFile?.let { file ->
            transcriptionViewModel.transcribeAudioFile(file)
        }
    }
    
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(100)
                val duration = audioRecorder.getRecordingDuration()
                transcriptionViewModel.updateRecordingDuration(duration)
                transcriptionViewModel.updateAmplitude(audioRecorder.getMaxAmplitude())
                
                if (duration >= Constants.MAX_RECORDING_DURATION_MS) {
                    val file = audioRecorder.stopRecording()
                    isRecording = false
                    recordedFile = file
                    break
                }
            }
        } else {
            transcriptionViewModel.updateAmplitude(0)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("STT Service")
                        ServiceStatusPill(
                            healthState = healthState,
                            context = context
                        )
                    }
                },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transcription Recording Card
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
                    
                    Box(
                        modifier = Modifier.height(100.dp),
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
                                
                                Text(
                                    text = if (isRecording) "Max: ${Constants.MAX_RECORDING_DURATION_SEC}s" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isNearLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
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
                    
                    AudioWaveform(
                        amplitude = audioAmplitude,
                        isRecording = isRecording,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    
                    val isTranscribing = transcriptionState is UiState.Loading
                    
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTranscribing) {
                            AiLoadingAnimation()
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
                                        transcriptionViewModel.resetState()
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
            
            // Transcription Result
            when (val state = transcriptionState) {
                is UiState.Loading -> {
                    // Loading is shown around the button, so we don't need a separate indicator
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
                                text = "Transcription:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.data.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    // Error handling removed - errors are not displayed
                }
                is UiState.Idle -> {
                    // Show nothing
                }
            }
            
        }
    }
}

@Composable
fun ServiceStatusPill(
    healthState: UiState<*>,
    context: Context
) {
    val statusColor = when (healthState) {
        is UiState.Success -> Color(0xFF4CAF50) // Green
        is UiState.Loading -> Color(0xFFFF9800) // Orange
        is UiState.Error -> {
            if (isWifiConnected(context)) {
                Color(0xFFF44336) // Red - server error
            } else {
                Color(0xFFF44336) // Red - WiFi not connected
            }
        }
        else -> Color(0xFFFF9800) // Orange - connecting/checking
    }
    
    Box(
        modifier = Modifier
            .background(
                color = statusColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = when (healthState) {
                is UiState.Success -> "Connected"
                is UiState.Loading -> "Connecting"
                is UiState.Error -> {
                    if (isWifiConnected(context)) {
                        "Not Connected"
                    } else {
                        "WiFi Not Connected"
                    }
                }
                else -> "Checking"
            },
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
fun AiLoadingAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("ai_logo.json")
    )
    
    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = Int.MAX_VALUE,
            modifier = Modifier.size(140.dp)
        )
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun isWifiConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
    }
}

@Composable
fun PushToTalkButton(
    isRecording: Boolean,
    hasPermission: Boolean,
    isTranscribing: Boolean = false,
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
    
    Box(
        modifier = Modifier
            .size(180.dp)
            .clickable(
                enabled = !isTranscribing,
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
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(
                    color = when {
                        isTranscribing -> MaterialTheme.colorScheme.surfaceVariant
                        isRecording -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
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

