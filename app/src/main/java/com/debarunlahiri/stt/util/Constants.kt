package com.debarunlahiri.stt.util

object Constants {
    // API Configuration
    const val BASE_URL = "http://192.168.0.65:3000/" // Use 10.0.2.2 for emulator, or your actual server IP for physical device
    
    // API Endpoints
    const val ENDPOINT_HEALTH = "health"
    const val ENDPOINT_TRANSCRIBE = "v1/transcribe"
    const val ENDPOINT_TRANSLATE = "v1/translate"
    const val ENDPOINT_DETECT_LANGUAGE = "v1/detect-language"
    
    // Supported Languages
    const val LANG_AUTO = "auto"
    const val LANG_ENGLISH = "en"
    const val LANG_HINDI = "hi"
    const val LANG_KOREAN = "ko"
    
    val SUPPORTED_LANGUAGES = listOf(
        LANG_AUTO to "Auto Detect",
        LANG_ENGLISH to "English",
        LANG_HINDI to "Hindi",
        LANG_KOREAN to "Korean"
    )
    
    val TRANSLATION_LANGUAGES = listOf(
        LANG_ENGLISH to "English",
        LANG_HINDI to "Hindi",
        LANG_KOREAN to "Korean"
    )
    
    // File Limitations (as per API documentation)
    const val MAX_FILE_SIZE_MB = 500
    const val MAX_AUDIO_DURATION_SEC = 60
    const val MAX_RECORDING_DURATION_MS = 30000L // 30 seconds limit for recording
    const val MAX_RECORDING_DURATION_SEC = 30
    
    // Timeouts
    const val CONNECT_TIMEOUT_SEC = 30L
    const val READ_TIMEOUT_SEC = 60L
    const val WRITE_TIMEOUT_SEC = 60L
}
