package com.debarunlahiri.stt.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.debarunlahiri.stt.R
import com.debarunlahiri.stt.databinding.ActivityHomeBinding
import com.debarunlahiri.stt.ui.components.AudioWaveformView
import com.debarunlahiri.stt.util.AudioQuality
import com.debarunlahiri.stt.util.AudioRecorder
import com.debarunlahiri.stt.util.Constants
import com.debarunlahiri.stt.util.UiState
import com.debarunlahiri.stt.ui.viewmodel.HealthCheckViewModel
import com.debarunlahiri.stt.ui.viewmodel.TranscriptionViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import java.io.File

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private lateinit var healthViewModel: HealthCheckViewModel
    private lateinit var transcriptionViewModel: TranscriptionViewModel
    
    private var isRecording = false
    private var recordedFile: File? = null
    private var hasPermission = false
    private lateinit var audioRecorder: AudioRecorder
    private var recordingJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                updateRecordingUI()
                handler.postDelayed(this, 100)
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        updatePermissionUI()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        healthViewModel = ViewModelProvider(this)[HealthCheckViewModel::class.java]
        transcriptionViewModel = ViewModelProvider(this)[TranscriptionViewModel::class.java]
        
        audioRecorder = AudioRecorder(this).apply {
            setAudioQualityCallback { quality, rmsLevel ->
                runOnUiThread {
                    transcriptionViewModel.updateAudioQuality(quality, rmsLevel)
                    updateAudioQualityUI(quality)
                }
            }
        }
        
        setupObservers()
        setupClickListeners()
        requestPermission()
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    healthViewModel.healthState.collect { state ->
                        updateStatusPill(state)
                    }
                }
                
                launch {
                    transcriptionViewModel.transcriptionState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                showLottieAnimation(true)
                            }
                            is UiState.Success -> {
                                showLottieAnimation(false)
                                showTranscriptionResult(state.data.text)
                            }
                            is UiState.Error -> {
                                showLottieAnimation(false)
                            }
                            is UiState.Idle -> {
                                showLottieAnimation(false)
                                hideTranscriptionResult()
                            }
                        }
                    }
                }
                
                launch {
                    transcriptionViewModel.recordingDuration.collect { duration ->
                        updateTimer(duration)
                    }
                }
                
                launch {
                    transcriptionViewModel.audioAmplitude.collect { amplitude ->
                        binding.audioWaveform.updateAmplitude(amplitude, isRecording)
                    }
                }
                
                launch {
                    transcriptionViewModel.audioQuality.collect { quality ->
                        updateAudioQualityUI(quality)
                    }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.refreshButton.setOnClickListener {
            healthViewModel.checkHealth()
        }
        
        binding.microphoneButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }
    
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
            updatePermissionUI()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    private fun startRecording() {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        
        val isTranscribing = transcriptionViewModel.transcriptionState.value is UiState.Loading
        if (isTranscribing) {
            return
        }
        
        val file = audioRecorder.startRecording()
        if (file != null) {
            isRecording = true
            recordedFile = null
            transcriptionViewModel.resetState()
            updateRecordingUI()
            handler.post(updateRunnable)
        }
    }
    
    private fun stopRecording() {
        val isTranscribing = transcriptionViewModel.transcriptionState.value is UiState.Loading
        if (isTranscribing) {
            return
        }
        
        val file = audioRecorder.stopRecording()
        isRecording = false
        recordedFile = file
        handler.removeCallbacks(updateRunnable)
        updateRecordingUI()
        
        file?.let {
            transcriptionViewModel.transcribeAudioFile(it)
        }
    }
    
    private fun updateRecordingUI() {
        binding.recordingStatusText.text = if (isRecording) "Recording..." else if (recordedFile != null) "Ready to Transcribe" else "Hold to Record"
        
        val duration = audioRecorder.getRecordingDuration()
        transcriptionViewModel.updateRecordingDuration(duration)
        transcriptionViewModel.updateAmplitude(audioRecorder.getMaxAmplitude())
        
        if (duration >= Constants.MAX_RECORDING_DURATION_MS) {
            stopRecording()
        }
        
        updateTimer(duration)
        updateMicrophoneButton()
    }
    
    private fun updateTimer(duration: Long) {
        if (isRecording || duration > 0) {
            binding.timerText.visibility = View.VISIBLE
            binding.timerText.text = formatDuration(duration)
            
            val isNearLimit = duration >= (Constants.MAX_RECORDING_DURATION_MS * 0.8)
            val isAtLimit = duration >= Constants.MAX_RECORDING_DURATION_MS
            
            binding.timerText.setTextColor(
                ContextCompat.getColor(this, when {
                    isAtLimit -> android.R.color.holo_red_dark
                    isNearLimit && isRecording -> android.R.color.holo_orange_dark
                    isRecording -> android.R.color.holo_red_dark
                    else -> android.R.color.holo_blue_dark
                })
            )
            
            if (isRecording) {
                binding.maxTimeText.visibility = View.VISIBLE
                binding.maxTimeText.text = "Max: ${Constants.MAX_RECORDING_DURATION_SEC}s"
                binding.maxTimeText.setTextColor(
                    ContextCompat.getColor(this, if (isNearLimit) android.R.color.holo_red_dark else android.R.color.darker_gray)
                )
            } else {
                binding.maxTimeText.visibility = View.GONE
            }
        } else {
            binding.timerText.visibility = View.GONE
            binding.maxTimeText.visibility = View.GONE
        }
    }
    
    private fun updateAudioQualityUI(quality: AudioQuality) {
        if (isRecording && quality != AudioQuality.GOOD) {
            binding.audioQualityText.visibility = View.VISIBLE
            binding.audioQualityText.text = when (quality) {
                AudioQuality.LOW_VOLUME -> "Please speak louder"
                AudioQuality.HIGH_NOISE -> "Too much background noise"
                AudioQuality.SILENT -> "No audio detected"
                AudioQuality.GOOD -> ""
            }
        } else {
            binding.audioQualityText.visibility = View.GONE
        }
    }
    
    private fun updateMicrophoneButton() {
        val isTranscribing = transcriptionViewModel.transcriptionState.value is UiState.Loading
        binding.microphoneButton.isEnabled = !isTranscribing
        
        binding.microphoneButton.setColorFilter(
            ContextCompat.getColor(this, when {
                isTranscribing -> android.R.color.darker_gray
                isRecording -> android.R.color.holo_red_dark
                else -> android.R.color.holo_blue_dark
            })
        )
        
        val scale = if (isRecording) 1.2f else 1.0f
        binding.microphoneButton.scaleX = scale
        binding.microphoneButton.scaleY = scale
    }
    
    private fun showLottieAnimation(show: Boolean) {
        binding.lottieAnimation.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.lottieAnimation.setAnimation("ai_logo.json")
            binding.lottieAnimation.repeatCount = -1
            binding.lottieAnimation.playAnimation()
        } else {
            binding.lottieAnimation.cancelAnimation()
        }
    }
    
    private fun showTranscriptionResult(text: String) {
        binding.transcriptionResultCard.visibility = View.VISIBLE
        binding.transcriptionText.text = text
    }
    
    private fun hideTranscriptionResult() {
        binding.transcriptionResultCard.visibility = View.GONE
    }
    
    private fun updateStatusPill(state: UiState<*>) {
        val statusText = when (state) {
            is UiState.Success -> "Connected"
            is UiState.Loading -> "Connecting..."
            is UiState.Error -> {
                if (isWifiConnected(this)) {
                    "Disconnected"
                } else {
                    "No WiFi"
                }
            }
            else -> "Checking..."
        }
        
        val backgroundDrawable = when (state) {
            is UiState.Success -> R.drawable.pill_background
            is UiState.Loading -> R.drawable.pill_background_warning
            is UiState.Error -> R.drawable.pill_background_error
            else -> R.drawable.pill_background_warning
        }
        
        binding.statusPill.text = statusText
        binding.statusPill.setBackgroundResource(backgroundDrawable)
        binding.statusPill.visibility = View.VISIBLE
    }
    
    private fun updatePermissionUI() {
        binding.permissionText.visibility = if (!hasPermission) View.VISIBLE else View.GONE
    }
    
    override fun onDestroy() {
        super.onDestroy()
        recordingJob?.cancel()
        handler.removeCallbacks(updateRunnable)
        if (isRecording) {
            audioRecorder.stopRecording()
        }
    }
    
    companion object {
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
    }
}

