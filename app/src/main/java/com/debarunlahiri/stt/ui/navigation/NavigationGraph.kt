package com.debarunlahiri.stt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.debarunlahiri.stt.ui.screen.*
import com.debarunlahiri.stt.ui.viewmodel.HealthCheckViewModel
import com.debarunlahiri.stt.ui.viewmodel.LanguageDetectionViewModel
import com.debarunlahiri.stt.ui.viewmodel.TranscriptionViewModel
import com.debarunlahiri.stt.ui.viewmodel.TranslationViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel: HealthCheckViewModel = hiltViewModel()
            HomeScreen(navController, viewModel)
        }
        
        composable(Screen.Transcription.route) {
            val viewModel: TranscriptionViewModel = hiltViewModel()
            TranscriptionScreen(navController, viewModel)
        }
        
        composable(Screen.Translation.route) {
            val viewModel: TranslationViewModel = hiltViewModel()
            TranslationScreen(navController, viewModel)
        }
        
        composable(Screen.LanguageDetection.route) {
            val viewModel: LanguageDetectionViewModel = hiltViewModel()
            LanguageDetectionScreen(navController, viewModel)
        }
        
        composable(
            route = "${Screen.Messenger.route}?message={message}&englishText={englishText}&hindiText={hindiText}&koreanText={koreanText}&audioUrl={audioUrl}",
            arguments = listOf(
                navArgument("message") { type = NavType.StringType },
                navArgument("englishText") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("hindiText") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("koreanText") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("audioUrl") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val viewModel: TranscriptionViewModel = hiltViewModel()
            val message = backStackEntry.arguments?.getString("message")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val englishText = backStackEntry.arguments?.getString("englishText")?.let {
                if (it != "null") URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) else null
            }
            val hindiText = backStackEntry.arguments?.getString("hindiText")?.let {
                if (it != "null") URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) else null
            }
            val koreanText = backStackEntry.arguments?.getString("koreanText")?.let {
                if (it != "null") URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) else null
            }
            val audioUrl = backStackEntry.arguments?.getString("audioUrl")?.let {
                if (it != "null") URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) else null
            }
            MessengerScreen(
                navController = navController,
                viewModel = viewModel,
                transcribedText = message,
                englishText = englishText,
                hindiText = hindiText,
                koreanText = koreanText,
                audioFileUrl = audioUrl
            )
        }
    }
}
