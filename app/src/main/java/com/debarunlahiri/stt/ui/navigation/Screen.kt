package com.debarunlahiri.stt.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Transcription : Screen("transcription")
    object Translation : Screen("translation")
    object LanguageDetection : Screen("language_detection")
    object Messenger : Screen("messenger")
}
